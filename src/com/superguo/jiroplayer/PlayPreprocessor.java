package com.superguo.jiroplayer;

import java.util.LinkedList;

import com.superguo.jiroplayer.PlayModel.Bar;
import com.superguo.jiroplayer.PlayModel.PreprocessedNote;
import com.superguo.jiroplayer.TJAFormat.TJACommand;

/** class used by PlayModel
 * 
 * @author superguo
 *
 */
final class PlayPreprocessor
{
	/** The pre-processing BPM */
	public float iBPM;
	
	/** The pre-processing X in MEASURE X/Y, available until changed */
	public int iMeasureX;
	
	/** The pre-processing Y in MEASURE X/Y, available until changed */
	public int iMeasureY;
	
	/** The pre-processing  microseconds per beat = 60 000 000 / BPM, available until changed */
	public long iMicSecPerBeat;
	
	/** The pre-processing speed of one note in pixels per 1000 seconds, available until changed */
	public int iSpeed;
	
	/** The pre-processing beat distance = BEAT_DIST * scroll, available until changed */
	public double iBeatDist;
	
	/** The pre-processing iBarLine, with value of on/off, available until changed */
	public boolean iBarLine;
	
	/** The pre-processing flag indicating if the last pre-processed note is rolling */
	public boolean iLastNoteRolling;

	/** Reset all pre-processing state variables */
	public final void reset(float aBMP)
	{
		setBPM(aBMP);
		setScroll(1.0f);
		iMeasureX = 4;
		iMeasureY = 4;
		iLastNoteRolling = false;
		iBarLine = true;
	}
	
	public final void calcSpeed()
	{
		iSpeed = (int) (iBeatDist * 2 * iBPM / 60 * 1000 );
	}
	
	public final void setBPM(float aBPM)
	{
		iBPM = aBPM;
		iMicSecPerBeat = (long) (60000000 / aBPM);
		calcSpeed();
	}
	
	public final void setScroll(float scroll)
	{
		iBeatDist = PlayModel.BEAT_DIST * scroll;
		calcSpeed();
	}

	private final void processCmdNote(Bar bar, int[] barNotes, float delay)
	{
		// The number of beats in a bar is measureX / measureY
		double numBeats = (double)iMeasureX / iMeasureY;
		
		// The duration in minutes is numBeats / BPM
		// To convert the minutes to microseconds, just make it times 60 000 000
		bar.iDurationMicros = (long) (iMicSecPerBeat * numBeats);
	
		// When scroll is 1.0, one beat is two notes' length in pixels
		bar.iLength = (int) (iBeatDist * 2 * numBeats);
	
		bar.iSpeed = iSpeed; // = bar.iLength / bar.iDuration * 1e9
	
		bar.iNumPreprocessedNotes = 0;
		
		int numNotes = barNotes.length;
		if (iBarLine)
		{
			PreprocessedNote noteOffset = bar.iNotes[bar.iNumPreprocessedNotes++];
			noteOffset.iNoteType = PlayModel.NOTE_BARLINE;
			noteOffset.iOffsetTimeMillis = 0;
			noteOffset.iOffsetPos = 0;
		}
		
		// transfer field variable to local variable
		boolean isLastNoteRolling = iLastNoteRolling;
		
		for (int i=0; i<numNotes; ++i)
		{
			int note = barNotes[i];
			if (isLastNoteRolling)
			{	// rolling is not complete last time
				if (note==8)	// 8 means finished
				{
					isLastNoteRolling = false;
					bar.addPreprocessedNote(PlayModel.NOTE_STOP_ROLLING, i, numNotes);
				}
			}
			else
			{
				switch (note)
				{
				case 5:	// len-da (combo)
				case 6:	// Big len-da
				case 7:	// Balloon
				case 9:	// Potato
					isLastNoteRolling = true;
					// DO NOT break here
				case 1:
				case 2:
				case 3:
				case 4:
					bar.addPreprocessedNote(note, i, numNotes);
					break;
	
				case 0:
				case 8:	// Bad note here
				default:
					break;
				}
			}
		}
		
		// The iDelay will change iDuration & iLength
		// And cannot be handled before bar.addPreprocessedNote(), which uses
		// iDuration & iLength
		if (delay>0.001f)
		{
			bar.iDurationMicros += delay * 1000000;
			bar.iLength += delay * bar.iSpeed / 1000;
			
			// Reset after being pre-processed
			delay = 0.0f;
		}
		// transfer local variable back to field variable
		iLastNoteRolling = isLastNoteRolling;
	}

	/**
	 * 
	 * @param aPlayingBarIndex
	 * @param aBars
	 * @param notation
	 * @param [in/out] aProcessedCommandIndexRef
	 * @param [out] aProcessedBarIndexRef
	 * @return
	 */
	public boolean processNextBar(
			Bar[] aBars,
			TJACommand[] notation,
			IntegerRef aProcessedCommandIndexRef,
			IntegerRef aProcessedBarIndexRef)
	{
		int processedCommandIndex = aProcessedCommandIndexRef.get();
		if (processedCommandIndex>=notation.length-1) return false;
		int lastProcessedBarIndex = aProcessedBarIndexRef.get();
		
		// Get the next unprocessed bar
		int barIndex = PlayModel.nextIndexOfBar(lastProcessedBarIndex);
		
		if (barIndex==lastProcessedBarIndex) return false;
		if ( ! aBars[barIndex].iPreprocessed ) return false;
		
		// Initialize the bar value
		Bar bar = aBars[barIndex];
		bar.iPreprocessed = false;
		bar.iHasBranchStartNextBar = false;
		bar.iNumPreprocessedNotes = 0;

		LinkedList<TJACommand> unprocCmd = null;
		// Do not allocate memory until next command is not COMMAND_TYPE_NOTE
		if (notation[processedCommandIndex+1].iCommandType != TJAFormat.COMMAND_TYPE_NOTE)
		{	unprocCmd = new LinkedList<TJACommand>();	}
		
		float delay = 0.0f;
		
		for(;	!bar.iPreprocessed &&
				++processedCommandIndex < notation.length;)
		{
			TJACommand cmd = notation[processedCommandIndex];
			switch (cmd.iCommandType)
			{
			case TJAFormat.COMMAND_TYPE_NOTE:
				// Emit notes
				processCmdNote(bar, cmd.iArgs, delay);

				// Set runtime offset
				if (lastProcessedBarIndex==-1)
					bar.iOffsetTimeMicros = 0;
				else
					bar.iOffsetTimeMicros = 
						aBars[lastProcessedBarIndex].iOffsetTimeMicros + 
						aBars[lastProcessedBarIndex].iDurationMicros;
				// Check #BRACHSTART
				for (int i=processedCommandIndex+1; i<notation.length; ++i)
				{
					TJACommand cmd2 = notation[i];
					if (cmd2.iCommandType == TJAFormat.COMMAND_TYPE_NOTE)
						break;
					else if (cmd2.iCommandType == TJAFormat.COMMAND_TYPE_BRANCHSTART)
					{
						bar.iHasBranchStartNextBar = true;
						break;
					}
				}

				// Emit unprocessed commands
				if (unprocCmd != null && unprocCmd.size()>0)
					bar.iUnprocessedCommand = 
						unprocCmd.toArray(new TJACommand[unprocCmd.size()]);
				else
					bar.iUnprocessedCommand = null;
				
				// Set processed
				bar.iPreprocessed = true;
				
				break;
				
			case TJAFormat.COMMAND_TYPE_BPMCHANGE:
			{
				float BPM = Float.intBitsToFloat(cmd.iArgs[0]);
				setBPM(BPM);
				break;
			}
	
			case TJAFormat.COMMAND_TYPE_MEASURE:
				iMeasureX = cmd.iArgs[0];
				iMeasureY = cmd.iArgs[1];
				break;
			
			case TJAFormat.COMMAND_TYPE_SCROLL:
			{
				float scroll = Float.intBitsToFloat(cmd.iArgs[0]);
				setScroll(scroll);
				break;
			}
			
			case TJAFormat.COMMAND_TYPE_GOGOSTART:
			case TJAFormat.COMMAND_TYPE_GOGOEND:
			case TJAFormat.COMMAND_TYPE_SECTION:
				// Will be executed in running bar
				unprocCmd.addLast(cmd.clone());
				break;
			
			case TJAFormat.COMMAND_TYPE_DELAY:
				delay = Float.intBitsToFloat(cmd.iArgs[0]);
				break;
			
			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				// Emit a special bar containing no notes

				// No length at all
				bar.iDurationMicros = 0;
				bar.iLength = 0;
				
				// Will be executed in running bar before this!
				unprocCmd.addLast(cmd.clone());
				
				// Emit unprocessed commands.
				// The last command is always #BRANCHSTART
				bar.iUnprocessedCommand = 
					unprocCmd.toArray(new TJACommand[unprocCmd.size()]);
				
				// Set processed
				bar.iPreprocessed = true;

				break;

			case TJAFormat.COMMAND_TYPE_BRANCHEND:
			case TJAFormat.COMMAND_TYPE_N:
			case TJAFormat.COMMAND_TYPE_E:
			case TJAFormat.COMMAND_TYPE_M:
				// Ignored
				break;
				
			case TJAFormat.COMMAND_TYPE_LEVELHOLD:
				//TODO
				// Not supported
				break;
				
			case TJAFormat.COMMAND_TYPE_BARLINEOFF:
				iBarLine = false;
				break;
				
			case TJAFormat.COMMAND_TYPE_BARLINEON:
				iBarLine = true;
				break;
				
			default:;
			}
		}
		aProcessedCommandIndexRef.set(processedCommandIndex);
		aProcessedBarIndexRef.set(barIndex);
		return true;
	}
}
