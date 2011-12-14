package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.*;

public final class PlayModel {
	public final static int FULL_SCORES[][] =	{
		{	// easy
			0,
			150000,		// 1
			200000,		// 2
			250000,		// 3
			300000,		// 4
			350000,		// 5
		},
		{	// normal
			0,
			350000,		// 1
			400000,		// 2
			450000,		// 3
			500000,		// 4
			550000,		// 5
			600000,		// 6
			650000,		// 7
		},
		{	// hard
			0,
			500000,		// 1
			600000,		// 2
			650000,		// 3
			700000,		// 4
			750000,		// 5
			800000,		// 6
			850000,		// 7
			900000,		// 8
		},
		{	// oni
			0,
			6500000,	// 1
			7000000,	// 2
			8000000,	// 3
			8500000,	// 4
			9000000,	// 5
			9500000,	// 6
			1000000,	// 7
			1050000,	// 8
			1100000,	// 9
			1200000		// 10
		}
	};
	public final static int MAX_LEVEL_OF[] = { 5, 7, 8, 10 };
	public final static int MAX_DIFFICULITES = 4;
	public final static long FIXED_TIME_OFFSET = -2000;	// start after 2 seconds
	public final static int BEAT_DIST = 64;	// pixel distance between two beats
	
	public final static int HIT_NONE = 0;
	public final static int HIT_FACE = 1;
	public final static int HIT_SIDE = 2;
	
	public final static int MAX_COMPILED_PARA = 3;


	
	/*
	private final class RuntimePara
	{
		public int 		iIndexOfTJAPara;
		public long 	iTimeOffset;	// time offset since first para
		public float 	iSpeed;
		public NoteOffset[] iNoteOffset = new NoteOffset[PlayDisplayInfo.MAX_NOTE_POS];
		public int 		iNumNoteOffset;	//
		public int 		iNoteIndexToHit;	// 0 ~ iNumNoteOffset - 1,
									// the next note to hit, only for face notes/side notes, 
		public boolean iIsGGT;
		
		public void clear()
		{
			iIndexOfTJAPara = -1;
			iTimeOffset = 0;
			iSpeed = 0.0f;
			iNumNoteOffset = 0;
			iNoteIndexToHit = -1;
			iIsGGT = false;
		}
	}
	
	private final class SectionStat
	{
		public int iNumGood;
		public int iNumNormal;
		public int iNumTotal;
		public int iNumRolled;
		
		public final void reset()
		{
			iNumGood = iNumNormal = iNumTotal = iNumRolled = 0;
		}
		
		// 0.0f ~ 1.0f
		public final float precision()
		{
			if (iNumTotal==0) return 0.0f;
			return (iNumGood * 2.0f + iNumNormal) / (iNumTotal * 2.0f);  
		}
	}
	*/
	private PlayDisplayInfo iDisplayInfo = new PlayDisplayInfo();

	private TJAFormat iTJA;
	private TJACourse iCourse;
	private TJACommand[] iNotation;
	private int iScoreInit;
	private int iScoreDiff;
	
	private long iOffsetTime;		// offset since the first note paragraph
	private int iNumMaxCombo;
	private int iNumMaxNotes;
	private int iNumHitNotes;
	private int iNumTotalRolling;

	/*
	private RuntimePara[] iRuntimeParas = 
		new RuntimePara[MAX_COMPILED_PARA];
	private int iNumAvailableRuntimeParas;
	private int iLastCompiledRuntimeParaSlot;	// 0 ~ iNumAvailableRuntimeParas - 1
	private int iIndexOfLastCompiledTJAPara;	// 0 ~ iParas.length - 1 
	*/
	private int iJustPlayingParaSlot;	// 0 ~ iNumAvailableRuntimeParas - 1
	private int iInPlayingParaSlot;
	
	// private SectionStat iSectionStat = new SectionStat();

	private final static class CompiledNote
	{
		public int 		iNoteType;		// see PlayDisplayInfo
		public short 	iTimeOffset;	// time offset in milliseconds since its begin of bar
		public int 		iPosOffset;
	}
	
	private final static class CompiledBar
	{
		public TJACommand[] iRunTimeCommand;	// All commands that cannot be executed in compile time will be here
		
		/** The duration in microseconds */
		public long iDuration;	
		
		/** The length in pixels */
		public int iLength;		
		
		/** The speed of one note in pixels per 1000 seconds */
		public int iSpeed;
		
		public CompiledNote[] iCompiledNotes = new CompiledNote[PlayDisplayInfo.MAX_NOTE_POS];
		
		/** The number of compiled notes. The note 0 is omitted if not rolling */
		public int iNumCompiledNotes;
		
		public void addCompiledNote(int noteType, int origIndex, int origTotal)
		{
			CompiledNote noteOffset = iCompiledNotes[iNumCompiledNotes++];
			noteOffset.iNoteType 	= noteType;
			noteOffset.iTimeOffset = (short) (iDuration * origIndex / origTotal / 1000);
			noteOffset.iPosOffset 	=  iLength * origIndex / origTotal;
		}
	}
	
	private float iCompilingBPM;
	private int iCompilingMeasureX;
	private int iCompilingMeasureY;
	
	/** The microseconds per beat = 60 000 000 / BPM */
	private long iCompilingMicSecPerBeat;
	
	/** The speed of one note in pixels per 1000 seconds */
	private int iCompilingSpeed;
	
	/** = BEAT_DIST * scroll */
	private double iCompilingBeatDist;
	
	private boolean iCompilingLastNoteRolling;
	
	private final void calcCompilingSpeed()
	{
		iCompilingSpeed = (int) (iCompilingBeatDist * 2 * iCompilingBPM / 60 * 1000 );
	}
	
	private final void setComplingBPM(float BPM)
	{
		iCompilingBPM = BPM;
		iCompilingMicSecPerBeat = (long) (60000000 / BPM);
		calcCompilingSpeed();
	}
	
	private final void setCompilingScroll(float scroll)
	{
		iCompilingBeatDist = BEAT_DIST * scroll;
		calcCompilingSpeed();
	}

	// Compiles a bar of notes
	// iCompilingMicSecPerBeat = 60 000 000 / BPM 
	private void compCmdNote(CompiledBar bar, int barNotes[])
	{
		// The number of beats in a bar is measureX / measureY
		double numBeats = (double)iCompilingMeasureX / iCompilingMeasureY;
		
		// The duration in minutes is numBeats / BPM
		// To convert the minutes to microseconds, just make it times 60 000 000
		bar.iDuration = (long) (iCompilingMicSecPerBeat * numBeats);

		// When scroll is 1.0, one beat is two notes' length in pixels
		bar.iLength = (int) (iCompilingBeatDist * 2 * numBeats);

		bar.iSpeed = iCompilingSpeed; // = bar.iLength / bar.iDuration * 1e9

		bar.iNumCompiledNotes = 0;
		
		int numNotes = barNotes.length;
		CompiledNote noteOffset = bar.iCompiledNotes[bar.iNumCompiledNotes++];
		noteOffset.iNoteType = PlayDisplayInfo.NOTE_SEPARATOR;
		noteOffset.iTimeOffset = 0;
		noteOffset.iPosOffset = 0;
		
		// transfer field variable to local variable
		boolean isLastNoteRolling = iCompilingLastNoteRolling;
		
		for (int i=0; i<numNotes; ++i)
		{
			int note = barNotes[i];
			if (isLastNoteRolling)
			{	// rolling is not complete last time
				if (note==8)	// 8 means finished
				{
					isLastNoteRolling = false;
					bar.addCompiledNote(PlayDisplayInfo.NOTE_STOP_ROLLING, i, numNotes);
				}
			}
			else
			{
				switch (note)
				{
				case 0:
				case 8:	// Bad note here
					break;

				case 5:	// len-da (combo)
				case 6:	// Big len-da
				case 7:	// Balloon
				case 9:	// Potato
					isLastNoteRolling = true;
				default:
					bar.addCompiledNote(note, i, numNotes);
				}
			}
		}
		
		// transfer local variable back to field variable
		iCompilingLastNoteRolling = isLastNoteRolling;
	}

	public void prepare(TJAFormat aTJA, int aCourseIndex)
	{
		iTJA = aTJA;
		iCourse = iTJA.iCourses[aCourseIndex];
		iNotation = iCourse.iNotationSingle;
		
		// Play as P1 if Single STYLE is not defined
		if (iNotation==null) iNotation = iCourse.iNotationP1;
		
		// reset the score info
		resetScores();
		
		// reset the display info
		iDisplayInfo.reset();

		// reset some counters
		iNumMaxCombo = iNumMaxNotes = iNumHitNotes = iNumTotalRolling = 0;

		// reset runtime values
		setComplingBPM(iCourse.iBPM);
		setCompilingScroll(1.0f);
		iCompilingMeasureX = 4;
		iCompilingMeasureY = 4;
		iCompilingLastNoteRolling = false;
		
		// reset section
		// iSectionStat.reset();
		
		// reset playing para
		iJustPlayingParaSlot = iInPlayingParaSlot = -1;
	}

	public void start()
	{
		iOffsetTime = FIXED_TIME_OFFSET + (long)(iTJA.iOffset * 1000);
	}

	public PlayDisplayInfo onEvent(long timeSinceStarted, int hit)
	{
		// TODO
		return iDisplayInfo;	
	}
	
	private void resetScores() {
		if (iCourse.iScoreInit > 0 && iCourse.iScoreDiff > 0)
		{
			iScoreInit = iCourse.iScoreInit;
			iScoreDiff = iCourse.iScoreDiff;
		}
		else
		{
			int fullScore;	// approximate value of full score
			if (iCourse.iLevel <= MAX_LEVEL_OF[iCourse.iCourse])
				fullScore = FULL_SCORES[iCourse.iCourse][iCourse.iLevel];
			else
				fullScore = FULL_SCORES[iCourse.iCourse][MAX_LEVEL_OF[iCourse.iCourse]] +
				100000 * (MAX_LEVEL_OF[iCourse.iCourse] - iCourse.iLevel);
			float fullNormalNote = (float)fullScore / getScoreCalcNotes();
			iScoreInit = (int)Math.floor(fullNormalNote * 0.08f) * 10;
			iScoreDiff = (int)Math.floor(fullNormalNote * 0.02f) * 10;
		}
	}
	
	// get the total number of notes 1,2,3,4
	// choose master if encounters branches
	// if in GGT, plus 20%
	// half if note index < 100
	private float getScoreCalcNotes()
	{
		float scoredNotes = 0;
		int numNotes = 0;
		int len = iNotation.length;
		int i;
		
		boolean inGGT = false;
		
		for (i=0; i<len; )
		{
			TJACommand cmd = iNotation[i];

			switch (cmd.iCommandType)
			{
			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				i = cmd.iArgs[5];	// Go to the index of COMMAND_TYPE_M
				continue;

			case TJAFormat.COMMAND_TYPE_N:	// Encounters other difficulty
			case TJAFormat.COMMAND_TYPE_E:	// Encounters other difficulty
			case TJAFormat.COMMAND_TYPE_BRANCHEND:
				i = cmd.iArgs[6];
				continue;

			case TJAFormat.COMMAND_TYPE_GOGOSTART:
				inGGT = true;
				break;

			case TJAFormat.COMMAND_TYPE_GOGOEND:
				inGGT = false;
				break;
				
			// other cases are ignored
			}

			if (TJAFormat.COMMAND_TYPE_NOTE == cmd.iCommandType) {
				for (int note : cmd.iArgs) {
					switch (note) {
					case 1:
					case 2:
						scoredNotes += numNotes < 100 ? 0.5f : 1.0f;
						if (inGGT)
							scoredNotes += numNotes < 100 ? .1f : .2f;
						++numNotes;
						break;

					case 3:
					case 4:
						scoredNotes += numNotes < 100 ? 1.0f : 2.0f;
						if (inGGT)
							scoredNotes += numNotes < 100 ? .2f : .4f;
						++numNotes;
						break;

					default:
						;
					}
				}
			}

			i++;
		}
		return scoredNotes;
	}
	
	/*
	private boolean compileNext()
	{
		int i;
		
		// restriction check
		if (iNumAvailableRuntimeParas >= MAX_COMPILED_PARA-1) return false;
		if (iIndexOfLastCompiledTJAPara >= iParas.length-1) return false;
		
		// get the last compiled
		RuntimePara lastRuntimePara = 	iLastCompiledRuntimeParaSlot == -1 ? 
										null : 
										iRuntimeParas[iLastCompiledRuntimeParaSlot];

		// get the free slot for compilation
		int nextRuntimeParaSlot = nextFreeRuntimeParaSlot();
		RuntimePara newRuntimePara = iRuntimeParas[nextRuntimeParaSlot];
		newRuntimePara.clear();
		
		// get the TJA para to compile
		TJAPara tjaPara = iParas[iIndexOfLastCompiledTJAPara + 1];
		
		// execute the commands
		boolean hasChangedBPM = false;
		boolean hasSection = false;
		float delay = 0.0f;
		int prevMeasureX = iMeasureX;
		int prevMeasureY = iMeasureY;
		for (i=0; i<tjaPara.iCommands.length; ++i)
		{
			TJACommand cmd = tjaPara.iCommands[i];
			switch (cmd.iCommandType)
			{
			case TJAFormat.COMMAND_TYPE_BPMCHANGE:
				iCurrentBPM = Float.intBitsToFloat(cmd.iArgs[0]);
				hasChangedBPM = true;
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOSTART:
				newRuntimePara.iIsGGT = true;
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOEND:
				newRuntimePara.iIsGGT = false;
				break;

			case TJAFormat.COMMAND_TYPE_MEASURE: 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
				iMeasureX = cmd.iArgs[0];
				iMeasureY = cmd.iArgs[1];
				break;
				
			case TJAFormat.COMMAND_TYPE_SCROLL: 	// float(0.1 - 16.0)
				iScroll = Float.intBitsToFloat(cmd.iArgs[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_DELAY: 	// float(>0.001)
				delay = Float.intBitsToFloat(cmd.iArgs[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_SECTION:
				hasSection = true;
				break;
				
			case TJAFormat.COMMAND_TYPE_BRANCHSTART: 	// BRANCH_JUDGE_*(r/p/s, int), X(float), Y(float), (index of #N TJAPara, index of #N command), #E, #M
				switch(cmd.iArgs[0])
				{
				case TJAFormat.BRANCH_JUDGE_ROLL:
				case TJAFormat.BRANCH_JUDGE_SCORE:
				case TJAFormat.BRANCH_JUDGE_PRECISION:
				}
				//TODO
				break;
				
			case TJAFormat.COMMAND_TYPE_BRANCHEND:
				//TODO
				break;
				
			case TJAFormat.COMMAND_TYPE_LEVELHOLD:
				// TODO: to be supported
				break;
				
			case TJAFormat.COMMAND_TYPE_BARLINEOFF:
				// TODO: to be supported
				break;

			case TJAFormat.COMMAND_TYPE_BARLINEON:
				// TODO: to be supported
				break;
			}
		}
		
		// fill iIndexOfTJAPara
		newRuntimePara.iIndexOfTJAPara = iIndexOfLastCompiledTJAPara + 1;
		
		// fill iTimeOffset
		if (null==lastRuntimePara)
			newRuntimePara.iTimeOffset = 0;
		else
			newRuntimePara.iTimeOffset = lastRuntimePara.iTimeOffset + 
			(long)(lastRuntimePara.iSpeed * prevMeasureX * 4000 / prevMeasureY);

		// adjust iTimeOffset if delayed
		if (delay!=0.0f) newRuntimePara.iTimeOffset += (long)(delay*1000);

		if (null==lastRuntimePara || hasChangedBPM)
			newRuntimePara.iSpeed = calcSpeed();
		else
			newRuntimePara.iSpeed = lastRuntimePara.iSpeed;
		
		// TODO deals with hasSection
		
		// TODO: fill the notes

		iLastCompiledRuntimeParaSlot = nextRuntimeParaSlot;
		++iIndexOfLastCompiledTJAPara;
		return true;
	}
	
	private final int nextFreeRuntimeParaSlot()
	{
		int indexOfNextRuntimePara = iLastCompiledRuntimeParaSlot + 1;
		if (indexOfNextRuntimePara == MAX_COMPILED_PARA) indexOfNextRuntimePara = 0;
		return 	iLastCompiledRuntimeParaSlot < MAX_COMPILED_PARA - 1 ?
				iLastCompiledRuntimeParaSlot + 1:
				0;
	}

	private final float calcSpeed()
	{
		return doCalcSpeed(iCurrentBPM, BEAT_DIST);
	}

	private final static float doCalcSpeed(float BPM, int beatDist)
	{
		return BPM * beatDist / 60;
	}
	*/
}
