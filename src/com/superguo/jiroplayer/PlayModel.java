/** PlayModel the playing model for TJAFormat
 * @author superguo
 */
package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;

public final class PlayModel {
	public static final int NOTE_BARLINE		= -1;
	public static final int NOTE_NONE 			= 0;
	public static final int NOTE_FACE 			= 1;
	public static final int NOTE_SIDE 			= 2;
	public static final int NOTE_BIG_FACE 		= 3;
	public static final int NOTE_BIG_SIDE 		= 4;
	public static final int NOTE_START_ROLLING_LENDA 		= 5;
	public static final int NOTE_START_ROLLING_BIG_LENDA 	= 6;
	public static final int NOTE_START_ROLLING_BALOON		= 7;
	public static final int NOTE_START_ROLLING_POTATO		= 9;
	public static final int NOTE_STOP_ROLLING	= 8;

	public static final int BRANCH_NONE			= 0;
	public static final int BRANCH_NORMAL		= 1;
	public static final int BRANCH_EASY			= 2;
	public static final int BRANCH_MASTER		= 3;
	
	public static final int ROLLING_NONE_LENDA		= -1;	// Just in convenience to display 
	public static final int ROLLING_NONE_BIG_LENDA	= -2;	// Just in convenience to display
	public static final int ROLLING_NONE			= 0;
	public static final int ROLLING_LENDA_BAR		= 1;
	public static final int ROLLING_BIG_LENDA_BAR	= 2;
	public static final int ROLLING_BALLOON			= 3;
	public static final int ROLLING_POTATO			= 4;

	/** The maximum level of 4 course */
	public static final int MAX_LEVEL_OF[] = { 5, 7, 8, 10 };
	public static final int MAX_COURSE = 4;
	
	public static final int FULL_SCORES[][] =	{
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
	
	/** The max soul gauge grid */
	public static final int MAX_GAUGE_GRID = 50;
	/** The minimum gauge grids for pass of 4 courses */
	public static final int PASSED_GAUGE_GRID_OF[] = {25, 30, 35, 40};
	/** The gauge value per grid */
	public static final int GAUGE_PER_GRID = 100;
	/** The max soul gauge value */
	public static final int MAX_GAUGE = MAX_GAUGE_GRID * GAUGE_PER_GRID;
	/** The minimum GOOD notes played rate to reach max gauge */
	public static final double MAX_GAUGE_RATES[][] =	{
		{	// easy
			0,
			0.4,		// 1
			0.4,		// 2
			0.4,		// 3
			0.4,		// 4
			0.4,		// 5
		},
		{	// normal
			0,
			0.5,		// 1
			0.5,		// 2
			0.5,		// 3
			0.5,		// 4
			0.5,		// 5
			0.5,		// 6
			0.5,		// 7
		},
		{	// hard
			0,
			0.67,		// 1
			0.67,		// 2
			0.67,		// 3
			0.67,		// 4
			0.67,		// 5
			0.67,		// 6
			0.67,		// 7
			0.67,		// 8
		},
		{	// oni
			0,
			0.69,	// 1
			0.69,	// 2
			0.69,	// 3
			0.69,	// 4
			0.69,	// 5
			0.69,	// 6
			0.69,	// 7
			0.69,	// 8
			0.77,	// 9
			0.76		// 10
		}
	};
	
	public static final int FIXED_START_TIME_OFFSET = 2000;	// start after 2 seconds
	public static final int FIXED_END_TIME_OFFSET = 2000;		// 2 seconds after notes end 
	public static final int BEAT_DIST = 64;	// pixel distance between two beats
	
	public static final int HIT_NONE = 0;
	public static final int HIT_FACE = 1;
	public static final int HIT_SIDE = 2;
	
	public static final int MAX_PREPROCESSED_BAR = 3;
	
	public static final int TIME_JUDGE_GOOD 	= 50;
	public static final int TIME_JUDGE_NORMAL 	= 150;
	public static final int TIME_JUDGE_MISSED 	= 217;

	private static final int SCORE_INDEX_NOT_GGT	= 0;
	private static final int SCORE_INDEX_GGT	 	= 1;
	private static final int GAUGE_OR_SCORE_INDEX_HALF 	= 0;
	private static final int GAUGE_OR_SCORE_INDEX_FULL 	= 1;
	private static final int GAUGE_OR_SCORE_INDEX_TWICE = 2;

	// SECTION statistics
	final class SectionStat {
		int numRolled;
		int score;
		int precisionPlayed;
		int precisionTotal;

		void reset() {
			numRolled = score = precisionPlayed = precisionTotal = 0;
		}
	}

	private TJAFormat mTJA;
	private TJACourse mCourse;
	private TJACommand[] mNotation;

	private PlayerMessage mPlayerMessage = new PlayerMessage();
	private Bar[] mBars = new Bar[PlayModel.MAX_PREPROCESSED_BAR];
	private IntegerRef mPreprocessedCommandIndexRef = new IntegerRef();
	private IntegerRef mPreprocessedBarIndexRef = new IntegerRef();
	
	/** The command index of the exit of the playing branch */
	private IntegerRef mBranchExitIndexRef = new IntegerRef();
	
	private int mRollingBaloonIndex;
	private int mScoreInit;
	private int mScoreDiff;
	private int[] mGaugePerNote = new int[3];
	private int[][] mScorePerNote = new int[2][3];
	private SectionStat mSectionStat = new SectionStat();
	

	/** The adjusted offset time before first bar begins.
	 * In milliseconds.
	 * It can be positive or negative.
	 */
	private long mStartOffsetTimeMillis;
	
	/** The end time (when last bar is just passed) in milliseconds
	 */
	private long mEndTimeMillis;
	
	/** The last adjusted event time in milliseconds
	 * The event time is relative to start() is called
	 * It is always 0 after start() is called	 
	 */
	private long mLastEventTimeMillis;
	private int mLastPlayingBarIndex;
	private int mLastPlayingNoteIndex;

	private PlayPreprocessor mPreprocessor = new PlayPreprocessor();
	
	private String mPlayTimeError = "";
	
	// private SectionStat iSectionStat = new SectionStat();

	static final class PreprocessedNote {
		/**
		 * The note type @see contance begins with NOTE_ The NOTE_NONE note is
		 * deleted after being parsed But it will become NOTE_NONE after being
		 * hit/missed/passed
		 */
		public int noteType;

		/**
		 * The time offset of the note in milliseconds since beginning of its
		 * bar
		 */
		public short offsetTimeMillis;

		/**
		 * The distance offset of the note in pixels since beginning of its bar
		 */
		public int offsetPos;
	}
	
	static final class Bar {
		/** The beginning time in microseconds since first bar.	 */
		public long offsetTimeMicros;
		
		/** Indicates whether it is pre-processed
		 * Will changed to false if this Bar has finished
		 * been played 
		 */
		public boolean preprocessed;
		
		/** All commands that cannot be pre-processed 
		 * fall here
		 */
		public TJACommand[] unprocessedCommand;
		
		/** The duration in microseconds */
		public long durationMicros;	
		
		/** The length in pixels */
		public int length;		
		
		/** The speed of one note in pixels per 1000 seconds */
		public int speed;
		
		/** Indicates if there is #BRANCHSTART before next bar */
		public boolean hasBranchStartNextBar;
		
		/** The processed notes */
		public PreprocessedNote[] notes = new PreprocessedNote[PlayerMessage.MAX_NOTE_POS];
		
		/** The number of compiled notes. The note 0 is omitted if not rolling */
		public int numPreprocessedNotes;
		
		public void addPreprocessedNote(int noteType, int origIndex, int origTotal)	{
			PreprocessedNote noteOffset = notes[numPreprocessedNotes++];
			noteOffset.noteType 	= noteType;
			noteOffset.offsetTimeMillis = (short) (durationMicros * origIndex / origTotal / 1000);
			noteOffset.offsetPos 	=  length * origIndex / origTotal;
		}
	}

	// package private
	static final int nextIndexOfBar(int index) {
		++index;
		if (index == MAX_PREPROCESSED_BAR)
			index = 0;
		return index;
	}

	public final PlayerMessage prepare(TJAFormat aTJA, int aCourseIndex) {
		mTJA = aTJA;
		mCourse = mTJA.courses[aCourseIndex];
		mPlayerMessage.reset(mCourse);

		// Reset internal values
		mNotation = mCourse.iNotationSingle;
		mPreprocessedCommandIndexRef.set(-1);
		mPreprocessedBarIndexRef.set(-1);
		mBranchExitIndexRef.set(0);
		mLastPlayingBarIndex = 0;
		mLastPlayingNoteIndex = 0;
		mRollingBaloonIndex = -1;
		if (mNotation == null) // Play as P1 if Single STYLE is not defined
			mNotation = mCourse.iNotationP1;
		resetGauge(); // Reset iGaugePerNote
		resetScores(); // Reset the score info
		mScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_FULL] = mScoreInit;
		mScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_FULL] = (int) (mScoreInit * 1.2f) / 10 * 10;
		;
		mScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = mScoreInit << 1;
		mScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = (int) (mScoreInit * 2.4f) / 10 * 10;
		mScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((mScoreInit / 10) >> 1) * 10;
		mScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((mScoreInit / 10) >> 1) * 10;

		mSectionStat.reset(); // Reset the SECTION statistics
		mPreprocessor.reset(mCourse.iBPM); // reset preprocessor values

		return mPlayerMessage;
	}

	public final void start() {
		mStartOffsetTimeMillis = FIXED_START_TIME_OFFSET
				+ (int) (mTJA.offset * 1000);
		mEndTimeMillis = 0;
		mLastEventTimeMillis = 0;
		while (PlayPreprocessor.PROCESS_RESULT_OK == tryPreprocessNextBar())
			;

		mPlayerMessage.notePosCount = translateNotePos(
				mPlayerMessage.notePosArray, -mStartOffsetTimeMillis * 1000,
				mBars, mLastPlayingBarIndex);
	}

	/**
	 * 
	 * @param aTimeMillisSinceStarted
	 *            The time in milliseconds since start() is called
	 * @param aHit
	 *            one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * @return true if and only if the playing is not finished.
	 */
	public final boolean onEvent(long aTimeMillisSinceStarted, int aHit) {
		// There is no more bar to play!
		if (!mBars[mLastPlayingBarIndex].preprocessed) {
			// It's time to mark end time
			if (0 == mEndTimeMillis)
				mEndTimeMillis = aTimeMillisSinceStarted;
			// The time has passed FIXED_END_TIME_OFFSET since the mark of end
			// time
			else if (mEndTimeMillis - aTimeMillisSinceStarted > FIXED_END_TIME_OFFSET)
				return false;
		}

		PlayerMessage playerMessage = mPlayerMessage;

		// The first not reached yet
		// if (aTimeMillisSinceStarted + JUDGE_MISSED < iStartOffsetTimeMillis)
		// return true;

		/**
		 * The time in milliseconds since first bar
		 */
		long currentTimeMillisSinceFirstBar = aTimeMillisSinceStarted
				- mStartOffsetTimeMillis;

		int barIndex = mLastPlayingBarIndex;
		int noteIndex = mLastPlayingNoteIndex;
		for (; mBars[barIndex].preprocessed; barIndex = nextIndexOfBar(barIndex)) {
			Bar playingBar = mBars[barIndex];

			// Process the iUnprocessedCommand here
			processUnprocessedCommands(playingBar, playerMessage, mSectionStat);

			// Process the notes
			noteIndex = handleNotes(playingBar, noteIndex,
					currentTimeMillisSinceFirstBar, aHit);

			if (noteIndex >= playingBar.notes.length) {
				// Set the bar unused
				playingBar.preprocessed = false;
				noteIndex = 0;
			} else {
				break;
			}
		}

		if (mLastPlayingBarIndex != barIndex) {
			mLastPlayingBarIndex = barIndex;
			while (PlayPreprocessor.PROCESS_RESULT_OK == tryPreprocessNextBar())
				;
		}

		mLastPlayingNoteIndex = noteIndex;

		if (mLastEventTimeMillis < currentTimeMillisSinceFirstBar)
			mLastEventTimeMillis = currentTimeMillisSinceFirstBar;

		mPlayerMessage.notePosCount = translateNotePos(
				mPlayerMessage.notePosArray, mLastEventTimeMillis * 1000,
				mBars, mLastPlayingBarIndex);

		return true;
	}

	private final int handleNotes(Bar aPlayingBar, int aNoteIndex,
			long aCurrentTimeMillisSinceFirstBar, int aHit) {
		PlayerMessage playerMessage = mPlayerMessage;
		PreprocessedNote[] notes = aPlayingBar.notes;
		long playingBarStartTimeMillis = aPlayingBar.offsetTimeMicros / 1000;
		long currentTimeMillisSincePlayBar = aCurrentTimeMillisSinceFirstBar
				- playingBarStartTimeMillis;

		// Traverse all note
		// Cannot use the statement 'for(note:playingBar)' since we need the
		// index
		for (; aNoteIndex < notes.length; ++aNoteIndex) {
			PreprocessedNote note = notes[aNoteIndex];
			int noteType = note.noteType;

			// Ignore all notes if the rolling is not going to be stopped
			if (playerMessage.rollingState > ROLLING_NONE
					&& noteType != NOTE_STOP_ROLLING) {
				noteType = NOTE_NONE;
			}

			/**
			 * The event time offset of current event time since current note
			 * time
			 */
			long eventOffset = currentTimeMillisSincePlayBar
					- note.offsetTimeMillis;

			/**
			 * Indicates if the current note has passed Used only for rolling
			 * notes/states
			 */
			boolean noteHasPassed = eventOffset >= 0; /*
													 * + (NOTE_FACE<=noteType &&
													 * noteType<=NOTE_BIG_SIDE ?
													 * JUDGE_MISSED : 0);
													 */

			// In all cases 'handled' will be true if 'noteHasPassed' is true
			// In most cases 'handled' will be false if 'noteHasPassed' is false
			// NOTE_BARLINE or NOTE_NONE is not taken into account
			// The following code will handle the exceptions
			boolean handled = false;

			switch (noteType) {
			case NOTE_BARLINE:
			case NOTE_NONE:
				break;

			case NOTE_STOP_ROLLING:
				handled = noteHasPassed
						|| handleNoteTypeStopRolling(playerMessage, note,
								noteHasPassed);
				break;

			case NOTE_START_ROLLING_LENDA:
				if (noteHasPassed) {
					playerMessage.rollingCount = 0;
					playerMessage.rollingState = ROLLING_LENDA_BAR;
					note.noteType = NOTE_NONE;
					handled = true;
				}
				break;

			case NOTE_START_ROLLING_BIG_LENDA:
				if (noteHasPassed) {
					playerMessage.rollingCount = 0;
					playerMessage.rollingState = ROLLING_BIG_LENDA_BAR;
					note.noteType = NOTE_NONE;
					handled = true;
				}
				break;

			case NOTE_START_ROLLING_BALOON:
				if (noteHasPassed) {
					playerMessage.rollingCount = mCourse.iBalloon[++mRollingBaloonIndex];
					playerMessage.rollingState = ROLLING_BALLOON;
					note.noteType = NOTE_NONE;
					handled = true;
				}
				break;

			case NOTE_START_ROLLING_POTATO:
				if (noteHasPassed) {
					playerMessage.rollingCount = mCourse.iBalloon[++mRollingBaloonIndex];
					playerMessage.rollingState = ROLLING_POTATO;
					note.noteType = NOTE_NONE;
					handled = true;
				}
				break;

			case NOTE_FACE:
			case NOTE_SIDE:
			case NOTE_BIG_FACE:
			case NOTE_BIG_SIDE:
				handled = handleNoteTypeFaceOrSide(playerMessage, note,
						eventOffset, mSectionStat, aHit, mGaugePerNote,
						mScorePerNote, mScoreInit, mScoreDiff);
				break;
			}

			if (!handled) {
				if (playerMessage.rollingState > ROLLING_NONE) {
					switch (playerMessage.rollingState) {
					case ROLLING_LENDA_BAR:
						if (HIT_NONE != aHit) {
							playerMessage.addedScore = 300;
							playerMessage.score += 300;
							playerMessage.numTotalRolled++;
							playerMessage.rollingCount++;
						}
						break;

					case ROLLING_BIG_LENDA_BAR:
						if (HIT_NONE != aHit) {
							playerMessage.addedScore = 600;
							playerMessage.score += 600;
							playerMessage.numTotalRolled++;
							playerMessage.rollingCount++;
						}
						break;

					case ROLLING_BALLOON:
						if (HIT_FACE == aHit) {
							playerMessage.rollingCount--;

							playerMessage.numTotalRolled++;
							if (0 == playerMessage.rollingCount) {
								playerMessage.addedScore = 3300;
								playerMessage.score += 3300;
								playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_BALLOON_FINISHED;
							} else {
								playerMessage.addedScore = 300;
								playerMessage.score += 300;
							}
						}
						break;

					case ROLLING_POTATO:
						if (HIT_FACE == aHit) {
							playerMessage.rollingCount--;

							playerMessage.numTotalRolled++;
							if (0 == playerMessage.rollingCount) {
								playerMessage.addedScore = 3300;
								playerMessage.score += 3300;
								playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_POTATO_FINISHED;
							} else {
								playerMessage.addedScore = 300;
								playerMessage.score += 300;
							}
						}
						break;

					}
				}

				// Exit here, for note if no actual note handled
				break;
			}
		}
		return aNoteIndex;
	}

	/**
	 * Handles case when note.iNoteType == NOTE_STOP_ROLLING
	 * 
	 * @param playerMessage
	 * @param note
	 * @param noteHasPassed
	 * @return true if handled, false otherwise.
	 */
	private static final boolean handleNoteTypeStopRolling(
			PlayerMessage playerMessage, PreprocessedNote note,
			boolean noteHasPassed) {
		switch (playerMessage.rollingState) {
		case ROLLING_NONE: // Finished rolling (balloon/potato)
			// This case happens when
			// The balloon/potato finished rolling before NOTE_STOP_ROLLING
			note.noteType = NOTE_NONE;
			return true;

		case ROLLING_NONE_LENDA: // Finished rolling (len-da)
		case ROLLING_NONE_BIG_LENDA: // Finished rolling (big len-da)
			// This case happens when the len-da bar has passed
			// Cannot set note to NOTE_NONE until it exit the screen
			if (note.offsetPos < -BEAT_DIST) {
				playerMessage.rollingState = ROLLING_NONE;
				note.noteType = NOTE_NONE;
				return true;
			}
			break;

		default: // Still rolling
			if (noteHasPassed)
			// This case means the rolling time passed
			{
				switch (playerMessage.rollingState) {
				case ROLLING_LENDA_BAR:
					playerMessage.rollingState = ROLLING_NONE_LENDA;
					break;

				case ROLLING_BIG_LENDA_BAR:
					playerMessage.rollingState = ROLLING_NONE_BIG_LENDA;
					break;

				case ROLLING_BALLOON:
					playerMessage.rollingState = ROLLING_NONE;
					if (playerMessage.rollingCount > 0)
						playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_BALLOON_FAILED;
					break;

				case ROLLING_POTATO:
					playerMessage.rollingState = ROLLING_NONE;
					if (playerMessage.rollingCount > 0)
						playerMessage.rollingCount = PlayerMessage.SPECIAL_ROLLING_COUNT_POTATO_FAILED;
					break;
				}

				note.noteType = NOTE_NONE;
				return true;
			}
		}
		return false;
	}

	/**
	 * Handles case when aNote.iNoteType ==
	 * NOTE_FACE/NOTE_SIDE/NOTE_BIG_FACE/NOTE_BIG_SIDE
	 * 
	 * @param aPlayerMessage
	 * @param aNote
	 * @param anEventOffset
	 * @param aSectionStat
	 * @param aHit
	 * @param aGaugePerNote
	 * @param aScorePerNote
	 * @param aScoreInit
	 * @param aScoreDiff
	 * @return
	 */
	private static final boolean handleNoteTypeFaceOrSide(
			PlayerMessage aPlayerMessage, PreprocessedNote aNote,
			long anEventOffset, SectionStat aSectionStat, int aHit,
			int[] aGaugePerNote, int[][] aScorePerNote, int aScoreInit,
			int aScoreDiff) {
		if (anEventOffset > TIME_JUDGE_NORMAL) {
			// Handling cases MISSED and BREAK

			// Is it a BREAK?
			boolean isBreak = anEventOffset > TIME_JUDGE_MISSED;

			// I try to make it work compatible with lag case
			// So we don't think it is a MISSED even
			// if there is no hit within TIME_JUDGE_NORMAL.
			// However if event(hit or no hit) > TIME_JUDGE_MISSED
			// or there is a hit within (TIME_JUDGE_NORMAL, TIME_JUDGE_MISSED]
			// we can conclude that it is a BREAK or MISSED
			if (isBreak || HIT_NONE != aHit) {
				aPlayerMessage.noteJudged = isBreak ? PlayerMessage.JUDGED_BREAK
						: PlayerMessage.JUDGED_MISSED;
				aSectionStat.precisionTotal += 2;

				aPlayerMessage.gauge -= aGaugePerNote[GAUGE_OR_SCORE_INDEX_TWICE]; // Gauge
																					// reduce
																					// twice
				if (aPlayerMessage.gauge < 0)
					aPlayerMessage.gauge = 0;

				aPlayerMessage.numCombos = 0;
				aPlayerMessage.numTotalNotes++;

				aNote.noteType = NOTE_NONE;

				return true;
			}
		} else {
			int noteType = aNote.noteType;

			if (HIT_FACE == aHit
					&& (NOTE_FACE == noteType || NOTE_BIG_FACE == noteType)
					|| HIT_SIDE == aHit
					&& (NOTE_SIDE == noteType || NOTE_BIG_SIDE == noteType)) {
				// Handling cases GOOD and NORMAL

				// It is good only hit correctly within TIME_JUDGE_GOOD
				boolean isGood = anEventOffset <= TIME_JUDGE_GOOD;

				// Judge
				aPlayerMessage.noteJudged = PlayerMessage.JUDGED_NORMAL;

				// SECTION statistics
				aSectionStat.precisionTotal += 2;
				aSectionStat.precisionPlayed += isGood ? 2 : 1;

				// For both gauge and score
				int addedShifts = 0;
				if (isGood)
					addedShifts++;

				if (NOTE_BIG_FACE == noteType || NOTE_BIG_SIDE == noteType)
					addedShifts++;

				// Gauge
				int gauge = aPlayerMessage.gauge;
				if (gauge < MAX_GAUGE) {
					gauge += aGaugePerNote[addedShifts];
					if (gauge > MAX_GAUGE)
						gauge = MAX_GAUGE;
				}
				aPlayerMessage.gauge = gauge;

				// Combo counter
				if (aPlayerMessage.numCombos++ == aPlayerMessage.numMaxCombos)
					aPlayerMessage.numMaxCombos++;

				// Scores
				if (aPlayerMessage.numCombos <= 100
						&& aPlayerMessage.numCombos % 10 == 0) {
					int scorePerNote = aScoreInit + aScoreDiff
							* (aPlayerMessage.numCombos / 10);
					int scorePerNoteGGT = (int) (scorePerNote * 1.2f) / 10 * 10;

					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_FULL] = scorePerNote;
					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_FULL] = scorePerNoteGGT;

					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = scorePerNote << 1;
					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_TWICE] = scorePerNoteGGT << 1;

					aScorePerNote[SCORE_INDEX_NOT_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((scorePerNote / 10) >> 1) * 10;
					aScorePerNote[SCORE_INDEX_GGT][GAUGE_OR_SCORE_INDEX_HALF] = ((scorePerNoteGGT / 10) >> 1) * 10;
				}

				int ggtIndex = aPlayerMessage.isGGT ? SCORE_INDEX_GGT
						: SCORE_INDEX_NOT_GGT;
				aPlayerMessage.addedScore = aScorePerNote[ggtIndex][addedShifts];
				aPlayerMessage.score += aPlayerMessage.addedScore;

				// Note counter
				aPlayerMessage.numTotalNotes++;
				if (isGood)
					aPlayerMessage.numGoodNotes++;
				else
					aPlayerMessage.numNormalNotes++;

				aNote.noteType = NOTE_NONE;

				return true;
			}
		}

		return false;
	}

	public final String playTimeError() {
		return mPlayTimeError;
	}

	private final int tryPreprocessNextBar() {
		int preprocessedBarIndex = mPreprocessedBarIndexRef.get();
		if (-1 == preprocessedBarIndex
				|| !mBars[preprocessedBarIndex].hasBranchStartNextBar) {
			// No #BRANCHSTART in next bar

			return mPreprocessor.processNextBar(mBars, mNotation,
					mPreprocessedCommandIndexRef, mPreprocessedBarIndexRef,
					mBranchExitIndexRef);
		} else {
			Bar playingBar = mBars[preprocessedBarIndex];
			PlayerMessage playerMessage = mPlayerMessage;
			// The last unprocessed branch is always
			// #BRANCHSTART
			@SuppressWarnings("unused")
			TJACommand cmdBranchStart = playingBar.unprocessedCommand[playingBar.unprocessedCommand.length - 1];
			// TODO Handle case: #BRANCHSTART in next bar
			// TODO May change iPreprocessedCommandIndex
			// TODO May change iPreprocessedBarIndex
			// TODO May add a virtual pre-processed bar

			processUnprocessedCommands(playingBar, playerMessage, mSectionStat);

			return PlayPreprocessor.PROCESS_RESULT_OK;
		}
	}

	private static final void processUnprocessedCommands(Bar aPlayingBar,
			PlayerMessage aPlayerMessage, SectionStat aSectionStat) {
		for (TJACommand cmd : aPlayingBar.unprocessedCommand) {
			switch (cmd.iCommandType) {
			case TJAFormat.COMMAND_TYPE_GOGOSTART:
				aPlayerMessage.isGGT = true;
				break;

			case TJAFormat.COMMAND_TYPE_GOGOEND:
				aPlayerMessage.isGGT = false;
				break;

			case TJAFormat.COMMAND_TYPE_SECTION:
				aSectionStat.reset();
				break;

			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				// Ignored
			}
		}

	}

	private final void resetGauge() {
		int course = mCourse.iCourse;
		int level = Math.min(mCourse.iLevel, MAX_LEVEL_OF[course]);

		int gauge = (int) (MAX_GAUGE / (MAX_GAUGE_RATES[course][level] * getGaugeNotes()));

		if ((gauge & 1) == 1)
			--gauge;

		mGaugePerNote[GAUGE_OR_SCORE_INDEX_FULL] = gauge;
		mGaugePerNote[GAUGE_OR_SCORE_INDEX_HALF] = gauge >> 1;
		mGaugePerNote[GAUGE_OR_SCORE_INDEX_TWICE] = gauge << 1;
	}

	private final void resetScores() {
		TJACourse course = mCourse;
		if (course.iScoreInit > 0 && course.iScoreDiff > 0) {
			mScoreInit = course.iScoreInit;
			mScoreDiff = course.iScoreDiff;
		} else {
			int fullScore; // approximate value of full score
			if (course.iLevel <= MAX_LEVEL_OF[course.iCourse])
				fullScore = FULL_SCORES[course.iCourse][course.iLevel];
			else
				fullScore = FULL_SCORES[course.iCourse][MAX_LEVEL_OF[course.iCourse]]
						+ 100000
						* (MAX_LEVEL_OF[course.iCourse] - course.iLevel);
			float fullNormalNote = (float) fullScore / getScoreCalcNotes();
			mScoreInit = (int) Math.floor(fullNormalNote * 0.08f) * 10;
			mScoreDiff = (int) Math.floor(fullNormalNote * 0.02f) * 10;
		}
	}

	/**
	 * Get the total number of notes 1,2,3,4 for score calculation Choose master
	 * if encounters branches Score 20% more if in GGT, Score half if note index
	 * < 100 Score doubled if the note is "big"
	 * 
	 * @return
	 */
	private final float getScoreCalcNotes() {
		float scoredNotes = 0;
		int numNotes = 0;
		TJACommand[] notation = mNotation;
		int len = notation.length;
		int i;

		boolean inGGT = false;

		for (i = 0; i < len;) {
			TJACommand cmd = notation[i];

			switch (cmd.iCommandType) {
			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				i = cmd.iArgs[5]; // Go to the index of COMMAND_TYPE_M
				continue;

			case TJAFormat.COMMAND_TYPE_N: // Encounters other branch
			case TJAFormat.COMMAND_TYPE_E: // Encounters other branch
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
					case NOTE_FACE:
					case NOTE_SIDE:
						scoredNotes += numNotes < 100 ? 0.5f : 1.0f;
						if (inGGT)
							scoredNotes += numNotes < 100 ? .1f : .2f;
						++numNotes;
						break;

					case NOTE_BIG_FACE:
					case NOTE_BIG_SIDE:
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

	/**
	 * Get the total number of notes 1,2,3,4 for gauge Choose master if
	 * encounters branches
	 * 
	 * @return
	 */
	private final int getGaugeNotes() {
		int gaugeNotes = 0;
		TJACommand[] notation = mNotation;
		int len = notation.length;
		int i;

		for (i = 0; i < len;) {
			TJACommand cmd = notation[i];

			switch (cmd.iCommandType) {
			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				i = cmd.iArgs[5]; // Go to the index of COMMAND_TYPE_M
				continue;

			case TJAFormat.COMMAND_TYPE_N: // Encounters other difficulty
			case TJAFormat.COMMAND_TYPE_E: // Encounters other difficulty
			case TJAFormat.COMMAND_TYPE_BRANCHEND:
				i = cmd.iArgs[6];
				continue;

				// other cases are ignored
			}

			if (TJAFormat.COMMAND_TYPE_NOTE == cmd.iCommandType) {
				for (int note : cmd.iArgs) {
					switch (note) {
					case NOTE_FACE:
					case NOTE_SIDE:
					case NOTE_BIG_FACE:
					case NOTE_BIG_SIDE:
						++gaugeNotes;
						break;

					default:
						;
					}
				}
			}

			i++;
		}
		return gaugeNotes;
	}

	/**
	 * Translate the playing bars into the display purpose NotePos
	 * 
	 * @param aNotePos
	 *            [out]
	 * @param aCurrentTimeMicros
	 * @param aBars
	 * @param aPlayingBarIndex
	 * @return The number of translated note positions
	 */
	private static final int translateNotePos(PlayerMessage.NotePos[] aNotePos,
			long aCurrentTimeMicros, Bar[] aBars, int aPlayingBarIndex) {
		int notePosCount = 0;
		for (int barIndex = aPlayingBarIndex; aBars[barIndex].preprocessed; barIndex = nextIndexOfBar(barIndex)) {
			Bar bar = aBars[barIndex];
			// bar.iNotes may be null
			if (null == bar.notes)
				continue;

			// Cache some value to speed up
			long barRuntimeOffset = aCurrentTimeMicros - bar.offsetTimeMicros;
			long barSpeed = bar.speed * 1000;

			// FIXME Please help to improve the performance
			// of computing notePos.iNotePos
			for (PreprocessedNote pnote : bar.notes) {
				if (NOTE_NONE == pnote.noteType)
					continue;
				PlayerMessage.NotePos notePos = aNotePos[notePosCount++];
				notePos.noteType = pnote.noteType;
				notePos.notePos = (int) (barRuntimeOffset / barSpeed);
			}
		}

		return notePosCount;
	}

	@SuppressWarnings("unused")
	private static final int selectBranch(TJACommand startBranchCommand,
			SectionStat aSectionStat) {
		// N < E < M
		// Normal < Easy < Master !

		int args[] = startBranchCommand.iArgs;
		int limitE, limitM, played;
		switch (args[0]) {
		case TJAFormat.BRANCH_JUDGE_ROLL:
			limitE = (int) Float.intBitsToFloat(args[1]);
			limitM = (int) Float.intBitsToFloat(args[2]);
			played = aSectionStat.numRolled;
			break;

		case TJAFormat.BRANCH_JUDGE_SCORE:
			limitE = (int) Float.intBitsToFloat(args[1]);
			limitM = (int) Float.intBitsToFloat(args[2]);
			played = aSectionStat.score;
			break;

		case TJAFormat.BRANCH_JUDGE_PRECISION:
			limitE = (int) Math.ceil((aSectionStat.precisionTotal * Float
					.intBitsToFloat(args[1])));
			limitM = (int) Math.ceil((aSectionStat.precisionTotal * Float
					.intBitsToFloat(args[2])));
			played = aSectionStat.precisionPlayed;
			break;

		default:
			return BRANCH_NONE;
		}

		// Normal, Easy, Master!
		if (played < limitE) {
			return BRANCH_NORMAL;
		} else if (played < limitM) {
			return BRANCH_EASY;
		} else {
			return BRANCH_MASTER;
		}
	}
}
