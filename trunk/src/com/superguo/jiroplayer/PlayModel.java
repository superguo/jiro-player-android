/** PlayModel the playing model for TJAFormat
 * @author superguo
 */
package com.superguo.jiroplayer;

import com.superguo.jiroplayer.PlayerMessage.NotePos;
import com.superguo.jiroplayer.TJAFormat.*;

public final class PlayModel {
	public final static int NOTE_BARLINE		= -1;
	public final static int NOTE_NONE 			= 0;
	public final static int NOTE_FACE 			= 1;
	public final static int NOTE_SIDE 			= 2;
	public final static int NOTE_BIG_FACE 		= 3;
	public final static int NOTE_BIG_SIDE 		= 4;
	public final static int NOTE_START_ROLLING_LENDA_ 		= 5;
	public final static int NOTE_START_ROLLING_BIG_LENDA 	= 6;
	public final static int NOTE_START_ROLLING_BALOON		= 7;
	public final static int NOTE_START_ROLLING_POTATO		= 9;
	public final static int NOTE_STOP_ROLLING	= 8;

	public final static int BRANCH_NONE			= 0;
	public final static int BRANCH_NORMAL		= 1;
	public final static int BRANCH_EASY			= 2;
	public final static int BRANCH_MASTER		= 3;
	
	public final static int ROLLING_NONE		= 0;
	public final static int ROLLING_BAR			= 1;
	public final static int ROLLING_BALLOON		= 2;
	public final static int ROLLING_POTATO		= 3;

	/** The maximum level of 4 course */
	public final static int MAX_LEVEL_OF[] = { 5, 7, 8, 10 };
	public final static int MAX_COURSE = 4;
	
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
	
	/** The max soul gauge grid */
	public final static int MAX_GAUGE_GRID = 50;
	/** The minimum gauge grids for pass of 4 courses */
	public final static int PASSED_GAUGE_GRID_OF[] = {25, 30, 35, 40};
	/** The gauge value per grid */
	public final static int GAUGE_PER_GRID = 100;
	/** The max soul gauge value */
	public final static int MAX_GAUGE = MAX_GAUGE_GRID * GAUGE_PER_GRID;
	/** The minimum GOOD notes played rate to reach max gauge */
	public final static double MAX_GAUGE_RATES[][] =	{
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
	
	public final static int FIXED_START_TIME_OFFSET = 2000;	// start after 2 seconds
	public final static int FIXED_END_TIME_OFFSET = 2000;		// 2 seconds after notes end 
	public final static int BEAT_DIST = 64;	// pixel distance between two beats
	
	public final static int HIT_NONE = 0;
	public final static int HIT_FACE = 1;
	public final static int HIT_SIDE = 2;
	
	public final static int MAX_PREPROCESSED_BAR = 3;
	
	public final static int JUDGE_GOOD 		= 50;
	public final static int JUDGE_NORMAL 	= 150;
	public final static int JUDGE_MISSED 	= 217;

	// SECTION statistics
	private final class SectionStat
	{
		int iNumRolled;
		int iScore;
		int iPrecisionPlayed;
		int iPrecisionTotal;
		
		void reset()
		{
			iNumRolled = iScore = iPrecisionPlayed = iPrecisionTotal = 0;
		}
	}

	private TJAFormat iTJA;
	private TJACourse iCourse;
	private TJACommand[] iNotation;

	private PlayerMessage iPlayerMessage = new PlayerMessage();
	private Bar[] iBars = new Bar[PlayModel.MAX_PREPROCESSED_BAR];
	private IntegerRef iPreprocessedCommandIndexRef = new IntegerRef();
	private IntegerRef iPreprocessedBarIndexRef = new IntegerRef();
	private int iPlayingBarIndex;
	private int iRollingBaloonIndex;
	private int iScoreInit;
	private int iScoreDiff;
	private int iGaugePerNote;
	private SectionStat iSectionStat = new SectionStat();

	/** The adjusted offset time before first bar begins.
	 * In milliseconds.
	 * It can be positive or negative.
	 */
	private long iStartOffsetTimeMillis;
	
	/** The last adjusted event time in microseconds
	 * The event time is relative to start() is called
	 * It is always 0 after start() is called	 
	 */
	private long iLastEventTimeMicros;

	private PlayPreprocessor iPreprocessor = new PlayPreprocessor();
	
	// private SectionStat iSectionStat = new SectionStat();

	final static class PreprocessedNote
	{
		/** The note type @see contance begins with NOTE_
		 * The NOTE_NONE note is deleted after being parsed
		 * But it will become NOTE_NONE after being hit/missed/passed
		 */
		public int 		iNoteType; 
		
		/** The time offset of the note
		 * in milliseconds since beginning of its bar
		 */
		public short 	iOffsetTimeMillis;

		/** The distance offset of the note
		 * in pixels since beginning of its bar
		 */
		public int 		iOffsetPos; 
	}
	
	final static class Bar
	{
		/** The beginning time in microseconds since first playing .	 */
		public long iOffsetTimeMicros;
		
		/** Indicates whether it is pre-processed
		 * Will changed to false if this Bar has finished
		 * been played 
		 */
		public boolean iPreprocessed;
		
		/** All commands that cannot be pre-processed 
		 * fall here
		 */
		public TJACommand[] iUnprocessedCommand;
		
		/** The duration in microseconds */
		public long iDurationMicros;	
		
		/** The length in pixels */
		public int iLength;		
		
		/** The speed of one note in pixels per 1000 seconds */
		public int iSpeed;
		
		/** Indicates if there is #BRANCHSTART before next bar */
		public boolean iHasBranchStartNextBar;
		
		/** The processed notes */
		public PreprocessedNote[] iNotes = new PreprocessedNote[PlayerMessage.MAX_NOTE_POS];
		
		/** The number of compiled notes. The note 0 is omitted if not rolling */
		public int iNumPreprocessedNotes;
		
		public void addPreprocessedNote(int noteType, int origIndex, int origTotal)
		{
			PreprocessedNote noteOffset = iNotes[iNumPreprocessedNotes++];
			noteOffset.iNoteType 	= noteType;
			noteOffset.iOffsetTimeMillis = (short) (iDurationMicros * origIndex / origTotal / 1000);
			noteOffset.iOffsetPos 	=  iLength * origIndex / origTotal;
		}
	}

	// package private
	static int nextIndexOfBar(int index)
	{
		++index;
		if (index==MAX_PREPROCESSED_BAR)
			index=0;
		return index;
	}
	
	public PlayerMessage prepare(TJAFormat aTJA, int aCourseIndex)
	{
		iTJA = aTJA;
		iCourse = iTJA.iCourses[aCourseIndex];
		iPlayerMessage.reset(iCourse);
		
		// Reset internal values 
		iNotation = iCourse.iNotationSingle;
		iPreprocessedCommandIndexRef.set(-1);
		iPreprocessedBarIndexRef.set(-1);
		iPlayingBarIndex = -1;
		iRollingBaloonIndex = -1;
		if (iNotation == null)	// Play as P1 if Single STYLE is not defined
			iNotation = iCourse.iNotationP1;
		resetGauge();	// Reset iGaugePerNote
		resetScores();	// Reset the score info
		iSectionStat.reset();	// Reset the SECTION statistics
		iPreprocessor.reset(iCourse.iBPM);	// reset preprocessor values
		
		return iPlayerMessage;
	}

	public void start()
	{
		iStartOffsetTimeMillis = FIXED_START_TIME_OFFSET + (int)(iTJA.iOffset * 1000);
		iLastEventTimeMicros = 0;
		while(tryPreprocessNextBar());
		translateNotePos(
				iPlayerMessage.iNotePosArray, 
				-iStartOffsetTimeMillis * 1000,
				iBars,
				iPlayingBarIndex);
	}

	/**
	 * 
	 * @param aTimeMillisSinceStarted The time in milliseconds since
	 * start() is called 
	 * @param aHit one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * @return true if and only if the playing is not finished.
	 */
	public boolean onEvent(long aTimeMillisSinceStarted, int aHit)
	{
		long currentEventTimeMicros = 
			(aTimeMillisSinceStarted - iStartOffsetTimeMillis) * 1000;

		long lastEventTimeMicros = iLastEventTimeMicros;
		
		if (	aHit == HIT_NONE && 
				lastEventTimeMicros > currentEventTimeMicros)
			// Terrible case! The timer rewind! 
			return true;
		
		if (	aHit != HIT_NONE && 
				lastEventTimeMicros - currentEventTimeMicros > JUDGE_MISSED)
			// Although hit but too lagged
			return true;

		// TODO
 
		while(tryPreprocessNextBar());
		translateNotePos(
				iPlayerMessage.iNotePosArray, 
				currentEventTimeMicros,
				iBars,
				iPlayingBarIndex);

		iLastEventTimeMicros = currentEventTimeMicros;
		return false;	
	}

	private boolean tryPreprocessNextBar()
	{
		if (-1 == iPreprocessedBarIndexRef.get()
			|| !iBars[iPreprocessedBarIndexRef.get()].iHasBranchStartNextBar)
		{	
			// No #BRANCHSTART in next bar

			if (iPreprocessor.processNextBar(iBars, iNotation,
					iPreprocessedCommandIndexRef, iPreprocessedBarIndexRef))
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			// TODO Handle case: #BRANCHSTART in next bar
			// TODO May change iPreprocessedCommandIndex
			// TODO May change iPreprocessedBarIndex
			// TODO May add a virtual pre-processed bar
		}
		return false;
	}
	
	private void resetGauge()
	{
		int course = iCourse.iCourse;
		int level = Math.min(iCourse.iLevel, MAX_LEVEL_OF[course]) ;

		iGaugePerNote = (int) (MAX_GAUGE /
				(MAX_GAUGE_RATES[course][level] * getGaugeNotes()));

		if ((iGaugePerNote & 1) == 1)
			--iGaugePerNote;
	}

	private void resetScores() {
		TJACourse course = iCourse;
		if (course.iScoreInit > 0 && course.iScoreDiff > 0)
		{
			iScoreInit = course.iScoreInit;
			iScoreDiff = course.iScoreDiff;
		}
		else
		{
			int fullScore;	// approximate value of full score
			if (course.iLevel <= MAX_LEVEL_OF[course.iCourse])
				fullScore = FULL_SCORES[course.iCourse][course.iLevel];
			else
				fullScore = FULL_SCORES[course.iCourse][MAX_LEVEL_OF[course.iCourse]] +
				100000 * (MAX_LEVEL_OF[course.iCourse] - course.iLevel);
			float fullNormalNote = (float)fullScore / getScoreCalcNotes();
			iScoreInit = (int)Math.floor(fullNormalNote * 0.08f) * 10;
			iScoreDiff = (int)Math.floor(fullNormalNote * 0.02f) * 10;
		}
	}
	
	/** Get the total number of notes 1,2,3,4 for score calculation
	 * Choose master if encounters branches
	 * Score 20% more if in GGT,
	 * Score half if note index < 100
	 * Score doubled if the note is "big"
	 * @return
	 */
	private float getScoreCalcNotes()
	{
		float scoredNotes = 0;
		int numNotes = 0;
		TJACommand[] notation = iNotation;
		int len = notation.length;
		int i;
		
		boolean inGGT = false;
		
		for (i=0; i<len; )
		{
			TJACommand cmd = notation[i];

			switch (cmd.iCommandType)
			{
			case TJAFormat.COMMAND_TYPE_BRANCHSTART:
				i = cmd.iArgs[5];	// Go to the index of COMMAND_TYPE_M
				continue;

			case TJAFormat.COMMAND_TYPE_N:	// Encounters other branch
			case TJAFormat.COMMAND_TYPE_E:	// Encounters other branch
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

	/** Get the total number of notes 1,2,3,4 for gauge
	 * Choose master if encounters branches
	 * @return
	 */
	private int getGaugeNotes()
	{
		int gaugeNotes = 0;
		TJACommand[] notation = iNotation;
		int len = notation.length;
		int i;
		
		for (i=0; i<len; )
		{
			TJACommand cmd = notation[i];

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

					default:;
					}
				}
			}

			i++;
		}
		return gaugeNotes;
	}

	/**
	 * Translate the playing bars into the display purpose
	 * NotePos
	 * @param aNotePos [out]
	 * @param aCurrentTimeMicros
	 * @param aBars
	 * @param aPlayingBarIndex
	 * @return The number of translated note positions
	 */
	private static int translateNotePos(
			PlayerMessage.NotePos[] aNotePos,
			long aCurrentTimeMicros,
			Bar[] aBars,
			int aPlayingBarIndex)
	{
		int notePosCount = 0;
		for (int barIndex=aPlayingBarIndex;
			aBars[barIndex].iPreprocessed;
			barIndex=nextIndexOfBar(barIndex))
		{
			Bar bar = aBars[barIndex];
			// bar.iNotes may be null
			if (null==bar.iNotes)
				continue;
			
			// Cache some value to speed up
			long barRuntimeOffset = aCurrentTimeMicros - bar.iOffsetTimeMicros;
			long barSpeed = bar.iSpeed * 1000;

			// FIXME Please help to improve the performance
			// of computing notePos.iNotePos
			for (PreprocessedNote pnote : bar.iNotes)
			{
				if (NOTE_NONE == pnote.iNoteType)
					continue;
				PlayerMessage.NotePos notePos =
					aNotePos[notePosCount++];
				notePos.iNoteType = pnote.iNoteType;
				notePos.iNotePos = (int)(barRuntimeOffset / barSpeed);
			}
		}
		
		return notePosCount;
	}
	
	private static int selectBranch(TJACommand aStartBranchCommand, SectionStat aSectionStat)
	{
		// N < E < M
		// Normal < Easy < Master !
		
		int args[] = aStartBranchCommand.iArgs;
		int limitE, limitM, played;
		switch (args[0])
		{
		case TJAFormat.BRANCH_JUDGE_ROLL:
			limitE =(int)Float.intBitsToFloat(args[1]);
			limitM =(int)Float.intBitsToFloat(args[2]);
			played = aSectionStat.iNumRolled;
			break;

		case TJAFormat.BRANCH_JUDGE_SCORE:
			limitE =(int)Float.intBitsToFloat(args[1]);
			limitM =(int)Float.intBitsToFloat(args[2]);
			played = aSectionStat.iScore;
			break;

		case TJAFormat.BRANCH_JUDGE_PRECISION:
			limitE = (int) Math.ceil((aSectionStat.iPrecisionTotal * Float.intBitsToFloat(args[1])));
			limitM = (int) Math.ceil((aSectionStat.iPrecisionTotal * Float.intBitsToFloat(args[2])));
			played = aSectionStat.iPrecisionPlayed;
			break;

		default:
			return BRANCH_NONE;
		}

		// Normal, Easy, Master!
		if (played < limitE) return BRANCH_NORMAL;
		else if (played < limitM) return BRANCH_EASY;
		else return BRANCH_MASTER;
	}
}
