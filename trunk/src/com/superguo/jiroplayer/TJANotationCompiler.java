package com.superguo.jiroplayer;

import java.util.ArrayList;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;
import com.superguo.jiroplayer.TJANotation.Note;
import com.superguo.jiroplayer.TJANotation.NoteBar;
import com.superguo.jiroplayer.TJANotation.StartBranchCommand;

public class TJANotationCompiler {

	public static final int NOTATION_INDEX_SINGLE = 0;
	public static final int NOTATION_INDEX_P1     = 1;
	public static final int NOTATION_INDEX_P2     = 2;
	private static final int BRANCH_INDEX_NORMAL = 0;
	private static final int BRANCH_INDEX_EASY   = 1;
	private static final int BRANCH_INDEX_MASTER   = 2;
	private TJANotation mNotation;
	private TJAFormat mTja;
	private TJACommand[] mNotationCommands;
	private long mPrepTimeMillis;
	private TJACourse mCourse;
	private int mBeatDist;
	private int mScrollBandLeft;
	private int mScrollBandRight;
	private int mTargetNoteCenter;
	
	public static final class TJANotationCompilerException extends Exception {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -6481065923819947596L;

		public TJANotationCompilerException(int lineNo, String line, String msg) {
			super("Line " + lineNo + " " + msg + "\n"
					+ (line == null ? "" : line));
		}
	
		public TJANotationCompilerException(int lineNo, String line, Throwable r) {
			super("Line " + lineNo + "\n" + (line == null ? "" : line), r);
		}
	}

	/**
	 * Compile the specified notation of a TJA format into a compiled TJANotaion
	 * 
	 * @param tja
	 *            The TJA format data.
	 * @param courseIndex
	 *            The index of the course stored in TJA format data.
	 * @param notationIndex
	 *            The index of the notation to compile. The index is one of
	 *            {@link #NOTATION_INDEX_SINGLE}, {@link #NOTATION_INDEX_P1} or
	 *            {@link #NOTATION_INDEX_P2}
	 * @param prepTimeMillis
	 *            The time to wait before the notation or music (choose the
	 *            earlier) starts to scroll.
	 * @param beatDist
	 *            The distance between two 16th tja notes in standard scrolling
	 *            speed, which also equals the diameter of one tja note
	 * @param scrollBandLeft
	 *            The leftmost x position of the scroll band
	 * @param scrollBandRight
	 *            The rightmost x position of the scroll band
	 * @param targetNoteCenter
	 *            The x position of the target beat note's center point
	 * @return
	 */
	public TJANotation compile(TJAFormat tja, int courseIndex,
			int notationIndex, long prepTimeMillis, int beatDist,
			int scrollBandLeft, int scrollBandRight, int targetNoteCenter) {
		mCourse = tja.courses[courseIndex];
		if (mCourse == null) {
			return null;
		}
		TJACommand[][] uncompiledNotations = { 
				mCourse.notationSingle, mCourse.notationP1, mCourse.notationP2 };
		TJACommand[] notationCommands = uncompiledNotations[notationIndex];
		if (notationCommands == null) {
			return null;
		}
		mNotation = new TJANotation();
		mTja = tja;
		mNotationCommands = notationCommands;
		mPrepTimeMillis = prepTimeMillis;
		mBeatDist = beatDist;
		mScrollBandLeft = scrollBandLeft;
		mScrollBandRight = scrollBandRight;
		mTargetNoteCenter = targetNoteCenter;
		doCompile();
		return mNotation;
	}

	private void doCompile() {
		if (mTja.offset<0) {
			// If the music starts earlier than the notation
			mNotation.musicStartTimeMillis = mPrepTimeMillis;
		} else {
			// If the music starts later than the notation - we should avoid this case
			mNotation.musicStartTimeMillis = mPrepTimeMillis + Math.round(mTja.offset * 1000);
		}
		boolean hasBranch = mCourse.hasBranch;

		ArrayList<TJANotation.Bar> normalBranch = compileBranch(BRANCH_INDEX_NORMAL);

		if (hasBranch) {
			ArrayList<TJANotation.Bar> easyBranch = compileBranch(BRANCH_INDEX_EASY);
			ArrayList<TJANotation.Bar> masterBranch = compileBranch(BRANCH_INDEX_MASTER);
			
			int numBars = normalBranch.size();
			for (int i=0; i<numBars; ++i) {
				// TODO Check all sub-branches' duration consistency 
			}
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
			mNotation.easyBranch   = (Bar[]) easyBranch.toArray();
			mNotation.masterBranch = (Bar[]) masterBranch.toArray();
		} else {
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
		}
	}

	private ArrayList<Bar> compileBranch(int brachIndex) {
		int length = mNotationCommands.length;
		
		/** The branch to emit */
		ArrayList<Bar> branch = new ArrayList<Bar>(length);
		
		final int beatDist = mBeatDist;
		
		final int scrollBandWidth = mScrollBandRight - mScrollBandLeft;
		
		/** The compiling bar's BPM */
		double bpm = mCourse.BPM;
		
		/**
		 * The BPM to change to, from #BPMCHANGE. negative means the BPM is not
		 * to be changed.
		 */
		double bpmToChange = -1.0;
		
		/** The compiling bar's X in MEASURE X/Y */
		int measureX = 4;
		
		/** The compiling bar's Y in MEASURE X/Y */
		int measureY = 4;
		
		/** The compiling bar's scroll value  */
		double scroll = 1.0;
		
		/** The scroll to change to, from #SCROLL. negative means the scroll is not
		 * to be changed */
		double scrollToChange = -1.0;
		
		/** The visibility of the bar line of the compiling note bar */
		boolean isBarLineOn = true;
		
		/** The compiling  bar's flag indicating if the last compiled note is rolling */
		boolean isLastNoteRolling = false;
		
		/**
		 * The accumulated delay in millisecond before the next note bar starts.
		 * It will be reseted to 0.0 once after the next note bar is compiled
		 */
		double delay = 0.0;
	
		/**  The first note bar's beat time */
		double firstBarBeatTime;
		
		if (mTja.offset<0) {
			// If the music starts earlier than the notation
			firstBarBeatTime = mPrepTimeMillis - Math.round(mTja.offset*1000);
		} else {
			// If the music starts later than the notation - we should avoid this case
			firstBarBeatTime = mPrepTimeMillis;
		}
		
		/** The time offset of the beginning of the current note bar */
		double barBeatTime = firstBarBeatTime;
		
		/** The bar's speed in pixels per millisecond */
		double barSpeed = computeBarSpeed(bpm, scroll, beatDist);
		
		/** The current #BRANCHSTART. It is not null until #BRANCHEND is reached */
		StartBranchCommand startBranchCmd = null;
		
		for (int i=0; i<length; ++i) {
			TJACommand oCmd = mNotationCommands[i];
			Bar bar = new Bar();
			ArrayList<Note> compiledNotes = new ArrayList<Note>();
			
			switch (oCmd.commandType) {
			case TJAFormat.COMMAND_TYPE_NOTE: {
				bar.beatTimeMillis = Math.round(barBeatTime + delay);
				bar.isNoteBar = true;
				NoteBar noteBar = bar.noteBar = new NoteBar();
				noteBar.isBarLineOn = isBarLineOn;
				double appearTimeMillis;
				short[] xCoords;
				/* One TJA note's width is 16th note (semiquaver) length
				 * so 1 beat = 1 quarter notes(crochets) = 4 16th note = 4 TJA notes
				 * The mBeatDist is actually a TJA note's width
				 */

				boolean isBpmChanging = bpmToChange > 0;
				boolean isScrollChanging = scrollToChange > 0;
				
				if ( ! mTja.bmScroll && ! mTja.hbScroll || bpmToChange<0.0 && Math.abs(delay)<0.001 ) {
					// BPM or scroll changes immediately if neither BMSCROLL nor HBSCROLL is on, 
					// or neither #BPMCHANGE nor #DELAY is present before this note begins
					if (isBpmChanging) {
						bpm = bpmToChange;
						bpmToChange = -1.0;
					}
					if (isScrollChanging) {
						scroll = scrollToChange;
						scrollToChange = -1.0;
					}
					if (isBpmChanging || isScrollChanging) {
						// Re-compute barSpeed if either bpm or scroll has just changed
						barSpeed = computeBarSpeed(bpm, scroll, beatDist);	
					}
					
					appearTimeMillis = barBeatTime
							- (scrollBandWidth - mTargetNoteCenter + beatDist / 2.0) / barSpeed;
					// maxBarWidth is the total width of the bar, i.e. the
					// distance between the beginning of this bar and next bar
					double maxBarWidth = (double)measureX / measureY * 4 * beatDist * scroll;
					// barVisibleDuration is the total time when the bar passes through the scroll band 
					int barVisibleDuration = (int) (Math.round((maxBarWidth + scrollBandWidth) / barSpeed)) + 1;
					xCoords = new short[barVisibleDuration];
					double preciseXCoord = mScrollBandRight + beatDist/2.0;
					xCoords[0] =(short) preciseXCoord;
					for (int t=1; t<barVisibleDuration; ++t) {
						preciseXCoord -= barSpeed; // * 1.0;
						xCoords[t] =(short) preciseXCoord;
					}
					
					noteBar.appearTimeMillis = Math.round(appearTimeMillis); 
					noteBar.preComputedXCoords = xCoords;
					
				} else { // When HBSCROLL or BMSCROLL is on, the bar's speed
						 // does not change until it is passing the target beat note

					if (mTja.hbScroll && isScrollChanging) {
						// When HBSCROLL is on, the post-beat-note bar speed
						// takes the scroll value into account, on the condition
						// that the scroll value has just changed
						barSpeed = computeBarSpeed(bpm, scrollToChange, beatDist);
					}
					// postBarSpeed is the actual speed immediately after the
					// bar enters the target beat note
					double postBarSpeed = computeBarSpeed(
							isBpmChanging ? bpmToChange : bpm,
							isScrollChanging ? scrollToChange : scroll,
							beatDist); 

					// The minimum precision of delay is 0.001 second 
					if (delay >= 0.001) {
						// If delay is positive, the bar stops before target
						// beat note until the delayed time has elapsed.
						// In this case, we leave some space - the radius of the
						// beatDist - before the the bar hit the target beat note
						double spaceBetweenDelayAndBeat = beatDist / 2.0;
						appearTimeMillis = barBeatTime
								- (scrollBandWidth - mTargetNoteCenter + beatDist / 2.0) / barSpeed;
						double maxBarWidth = (double) measureX / measureY * 4 * beatDist
								* (mTja.hbScroll && isScrollChanging ? 
										scrollToChange : scroll);
						double visibleDurationBeforeDelay = (scrollBandWidth 
								- mTargetNoteCenter - spaceBetweenDelayAndBeat) / barSpeed;
						
						int barVisibleDuration = (int) (Math.round(
								delay
								+ scrollBandWidth / barSpeed
								+ maxBarWidth / postBarSpeed)) + 1;
						xCoords = new short[barVisibleDuration];
						double preciseXCoord = mScrollBandRight + beatDist/2.0;
						xCoords[0] =(short) preciseXCoord;
						double xCoordOnStop = mTargetNoteCenter + spaceBetweenDelayAndBeat;
						for (int t=1; t<barVisibleDuration; ++t) {
							if (t<visibleDurationBeforeDelay) {
								preciseXCoord -= barSpeed;
							} else if ( t < visibleDurationBeforeDelay + delay) {
								// preciseXCoord remains unchanged
								preciseXCoord = xCoordOnStop;
							} else {
								preciseXCoord -= postBarSpeed;
							}
							xCoords[t] =(short) preciseXCoord;
						}
						
						noteBar.appearTimeMillis = Math.round(appearTimeMillis); 
						noteBar.preComputedXCoords = xCoords;
					} else {
						// The delay is 0 or negative
						appearTimeMillis = barBeatTime
								- (scrollBandWidth - mTargetNoteCenter + beatDist / 2.0) / barSpeed;
						double maxBarWidth = (double) measureX / measureY * 4 * beatDist
								* (mTja.hbScroll && isScrollChanging ? 
										scrollToChange : scroll);
						double visibleDurationBeforeBeat = (scrollBandWidth - mTargetNoteCenter)
								/ barSpeed;
						
						int barVisibleDuration = (int) (Math.round(
								+ scrollBandWidth / barSpeed
								+ maxBarWidth / postBarSpeed)) + 1;
						xCoords = new short[barVisibleDuration];
						double preciseXCoord = mScrollBandRight + beatDist/2.0;
						xCoords[0] =(short) preciseXCoord;
						for (int t=1; t<barVisibleDuration; ++t) {
							if (t < visibleDurationBeforeBeat) {
								preciseXCoord -= barSpeed;
							} else {
								preciseXCoord -= postBarSpeed;
							}
							xCoords[t] =(short) preciseXCoord;
						}
						
					} // End of delay

					if (isBpmChanging) {
						bpm = bpmToChange;
						bpmToChange = -1.0;
					}
					if (isScrollChanging) {
						scroll = scrollToChange;
						scrollToChange = -1.0;
					}

				} // End of hbscroll and bmscroll
				noteBar.appearTimeMillis = Math.round(appearTimeMillis); 
				noteBar.preComputedXCoords = xCoords;
				
				int[] oNotes = oCmd.args;
				
				compiledNotes.clear();
				double noteBeatSpan = 15000.0 / bpm; // == 0.25 / bpm * 60000;
				double noteBeatTime = barBeatTime;
				for (int j=0; j<oNotes.length; ++j, noteBeatTime += noteBeatSpan) {
					
					if (isLastNoteRolling) {
						if (oNotes[i] == 8) { // 8 means finished
							isLastNoteRolling = false;
							Note note = new Note();
							note.beatTimeMillis = (long) noteBeatTime;
							note.noteValue = PlayModel.NOTE_STOP_ROLLING; // i.e 8
							compiledNotes.add(note);
						}
					} else {
						switch (oNotes[i]) {
						case 5: // len-da (combo)
						case 6: // Big len-da
						case 7: // Balloon
						case 9: // Potato
							isLastNoteRolling = true;
							// DO NOT break here
						case 1:
						case 2:
						case 3:
						case 4:
							Note note = new Note();
							note.beatTimeMillis = (long) noteBeatTime;
							note.noteValue = oNotes[i];
							compiledNotes.add(note);
							break;

						case 0:
						case 8: // Bad note here
						default:
							break;
						}
					}
				}
				// Compute next beat time
				barBeatTime += (double)measureX / measureY / bpm * 60000.0;
				// Emit notes
				noteBar.notes = (Note[]) compiledNotes.toArray();
				// Clear values
				delay = 0.0;
				break;
			}
			
			case TJAFormat.COMMAND_TYPE_BPMCHANGE:
				bpmToChange = (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOSTART:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_GOGOSTART);
				break;
				
			case TJAFormat.COMMAND_TYPE_GOGOEND:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_GOGOEND);
				break;
				
			case TJAFormat.COMMAND_TYPE_MEASURE: 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
				measureX = oCmd.args[0];
				measureY = oCmd.args[1];
				break;

			case TJAFormat.COMMAND_TYPE_SCROLL: 	// float(0.1 - 16.0)
				scrollToChange = (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_DELAY: 	// float(>0.001)
				// NOTE here that the delay value is to be accumulated rather
				// than be set
				delay += (double)Float.intBitsToFloat(oCmd.args[0]);
				break;
				
			case TJAFormat.COMMAND_TYPE_SECTION:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_SECTION);
				break;

			case TJAFormat.COMMAND_TYPE_BRANCHSTART: {
				bar.isNoteBar = false;
				startBranchCmd = new TJANotation.StartBranchCommand();
				startBranchCmd.compileTJAFormatArgs(oCmd.args);
				bar.command = startBranchCmd;
				break;
			}

			case TJAFormat.COMMAND_TYPE_BRANCHEND:
			case TJAFormat.COMMAND_TYPE_N:
			case TJAFormat.COMMAND_TYPE_E:
			case TJAFormat.COMMAND_TYPE_M:
				// TODO
				break;

			case TJAFormat.COMMAND_TYPE_LEVELHOLD:
				bar.isNoteBar = false;
				bar.command = new TJANotation.Command(TJANotation.COMMAND_LEVEL_HOLD);
				break;

				
			case TJAFormat.COMMAND_TYPE_BARLINEOFF:
				isBarLineOn = false;
				break;
				
			case TJAFormat.COMMAND_TYPE_BARLINEON:
				isBarLineOn = true;
				break;
			
			default:;
			}
			
			branch.add(bar);
		}
		
		return branch;
	}

	/**
	 * The bar's speed (pixel per millisecond) is 
	 * bpm * 4 * beatDist / 60000.0 * scroll
	 * @param bpm
	 * @param scroll
	 * @param beatDist
	 * @return
	 */
	private static double computeBarSpeed(double bpm, double scroll, final int beatDist) {
		return bpm * beatDist / 15000.0 * scroll;
	}
}
