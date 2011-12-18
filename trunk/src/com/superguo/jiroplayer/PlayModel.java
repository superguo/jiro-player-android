/** PlayModel the playing model for TJAFormat
 * @author superguo
 */
package com.superguo.jiroplayer;

import com.superguo.jiroplayer.PlayerMessage.NotePos;
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

	public final static int MAX_LEVEL_OF[] = { 5, 7, 8, 10 };
	public final static int MAX_DIFFICULITES = 4;
	public final static int FIXED_START_TIME_OFFSET = -2000;	// start after 2 seconds
	public final static int FIXED_END_TIME_OFFSET = 2000;		// 2 seconds after notes end 
	public final static int BEAT_DIST = 64;	// pixel distance between two beats
	
	public final static int HIT_NONE = 0;
	public final static int HIT_FACE = 1;
	public final static int HIT_SIDE = 2;
	
	public final static int MAX_PREPROCESSED_BAR = 3;
	
	public final static int JUDGE_GOOD = 50;
	public final static int JUDGE_NORMAL = 150;
	public final static int JUDGE_MISSED = 217;

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
	private int iPreprocessedCommandIndex;
	private int iPreprocessedBarIndex;
	private int iPlayingBarIndex;
	private int iRollingBaloonIndex;
	private int iScoreInit;
	private int iScoreDiff;
	private SectionStat iSectionStat = new SectionStat();
	
	private long iStartOffsetTime;		// offset since started in milliseconds
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
		public short 	iTimeOffset;

		/** The distance offset of the note
		 * in pixels since beginning of its bar
		 */
		public int 		iPosOffset; 
	}
	
	final static class Bar
	{
		/** The beginning time in microseconds since first playing .	 */
		public long iRuntimeOffset;
		
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
		public long iDuration;	
		
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
			noteOffset.iTimeOffset = (short) (iDuration * origIndex / origTotal / 1000);
			noteOffset.iPosOffset 	=  iLength * origIndex / origTotal;
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
		iPreprocessedCommandIndex = -1;
		iPreprocessedBarIndex = -1;
		iPlayingBarIndex = -1;
		iRollingBaloonIndex = -1;
		if (iNotation == null)	// Play as P1 if Single STYLE is not defined
			iNotation = iCourse.iNotationP1;	
		resetScores();	// Reset the score info
		iSectionStat.reset();	// Reset the SECTION statistics
		iPreprocessor.reset(iCourse.iBPM);	// reset preprocessor values
		
		return iPlayerMessage;
	}

	public void start()
	{
		iStartOffsetTime = FIXED_START_TIME_OFFSET + (int)(iTJA.iOffset * 1000);
		while(tryPreprocessNextBar());
		// TODO
	}

	public boolean onEvent(long timeSinceStarted, int hit)
	{
		// TODO
		return false;	
	}

	private boolean tryPreprocessNextBar()
	{
		if (-1 == iPreprocessedBarIndex
			|| !iBars[iPreprocessedBarIndex].iHasBranchStartNextBar)
		{	
			// No #BRANCHSTART in next bar

			IntegerRef preprocessedCommandIndexRef = new IntegerRef(
					iPreprocessedCommandIndex);

			IntegerRef preprocessedBarIndexRef = new IntegerRef(
					iPreprocessedBarIndex);

			if (iPreprocessor.processNextBar(iBars, iNotation,
					preprocessedCommandIndexRef, preprocessedBarIndexRef))
			{
				iPreprocessedCommandIndex = preprocessedCommandIndexRef.get();
				iPreprocessedBarIndex = preprocessedBarIndexRef.get();
			}
			
			// TODO
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
	
	/** Get the total number of notes 1,2,3,4
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
	 * Translate the playing bars into the display purpose
	 * NotePos
	 * @param aNotePos [out]
	 * @param aPlayingTimeOffset
	 * @param aBars
	 * @param aPlayingBarIndex
	 * @return The number of translated note positions
	 */
	private static int translateNotePos(
			PlayerMessage.NotePos[] aNotePos,
			long aCurrentTimeOffset,
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
			long barRuntimeOffset = aCurrentTimeOffset - bar.iRuntimeOffset;
			int barSpeed = bar.iSpeed;

			// FIXME Please help to improve the performance
			// of computing notePos.iNotePos
			for (PreprocessedNote pnote : bar.iNotes)
			{
				if (NOTE_NONE == pnote.iNoteType)
					continue;
				PlayerMessage.NotePos notePos =
					aNotePos[notePosCount++];
				notePos.iNoteType = pnote.iNoteType;
				notePos.iNotePos = (int)(barRuntimeOffset / barSpeed / 1000);
			}
		}
		
		return notePosCount;
	}
	
	private static int getBranch(TJACommand aStartBranchCommand, SectionStat aSectionStat)
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
