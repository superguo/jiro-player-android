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
	public final static long FIXED_OFFSET = -2000;	// start after 4 seconds
	public final static int BEAT_DIST = 64;	// pixel distance between two beats
	
	public final static int HIT_NONE = 0;
	public final static int HIT_FACE = 1;
	public final static int HIT_SIDE = 2;
	
	public final static int MAX_COMPILED_PARA = 3;

	private final static class NoteOffset
	{
		public int 		iNoteType;		// see PlayDisplayInfo
		public short 	iTimeOffset;	// time offset since its para
		public int 		iPosOffset;
	}
	
	private final class CompiledPara
	{
		public long iTimeOffset;	// time offset since first para
		public float iSpeed;
		public NoteOffset[] iNoteOffset = new NoteOffset[PlayDisplayInfo.MAX_NOTE_POS];
		public int iNumNoteOffset;
		public int iNoteIndexToHit;	// only for face notes/side notes
	}
	
	private PlayDisplayInfo iDisplayInfo = new PlayDisplayInfo();

	private TJAFormat iTJA;
	private TJACourse iCourse;
	private TJAPara[] iParas;
	private int iScoreInit;
	private int iScoreDiff;
	
	private int iScore;
	private long iOffsetTime;		// offset since the first note paragraph
	private long iStartedSysTime;	// system time when started
	private int iNumMaxCombo;
	private int iNumMaxNotes;
	private int iNumHitNotes;
	private int iNumLenDa;
	
	private CompiledPara[] iCompiledParas = 
		new CompiledPara[MAX_COMPILED_PARA];
	private int iNumAvailableCompiledParas;
	private int iCurrentCompiledPara;

	public void prepare(TJAFormat aTJA, int aCourseIndex)
	{
		iTJA = aTJA;
		iCourse = iTJA.iCourses[aCourseIndex];
		iParas = iCourse.iParasSingle;
		
		// Play as P1 if Single STYLE is not defined
		if (iParas==null) iParas = iCourse.iParasP1;
		
		// reset the score info
		resetScores();
		
		// reset the display info
		iDisplayInfo = new PlayDisplayInfo();

		// TODO reset
	}

	public void start()
	{
		iOffsetTime = FIXED_OFFSET + (long)(iTJA.iOffset * 1000);
		iStartedSysTime = android.os.SystemClock.uptimeMillis(); 
	}

	public PlayDisplayInfo onHit(int hit)
	{
		
		return iDisplayInfo;	
	}
	
	private void resetScores() {
		// reset current score
		iScore = 0;
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
		int len = iParas.length;
		int i;
		
		boolean inBranch = false;
		int branchType = 0;	// makes the compiler happy
		
		boolean inGGT = false;
		
		for (i=0; i<len; )
		{
			TJACommand[] cmds = iParas[i].iCommands;
			if (cmds!=null)
			{
				TJACommand cmdBranch = findCommand(
						TJAFormat.COMMAND_TYPE_BRANCHSTART, cmds);
				if (cmdBranch != null) {
					inBranch = true;
					if (i != cmdBranch.iArgs[7]) {
						i = cmdBranch.iArgs[7];
						continue;
					}
					branchType = TJAFormat.COMMAND_TYPE_M;
				}
				else if (null != findCommand(TJAFormat.COMMAND_TYPE_N, cmds))
					branchType = TJAFormat.COMMAND_TYPE_N;
				else if (null != findCommand(TJAFormat.COMMAND_TYPE_E, cmds))
					branchType = TJAFormat.COMMAND_TYPE_E;
				else if (null != findCommand(TJAFormat.COMMAND_TYPE_BRANCHEND, cmds))
					inBranch = false;
				else if (null != findCommand(TJAFormat.COMMAND_TYPE_GOGOSTART, cmds))
					inGGT = true;
				else if (null != findCommand(TJAFormat.COMMAND_TYPE_GOGOEND, cmds))
					inGGT = false;
			}
			
			if (inBranch && branchType != TJAFormat.COMMAND_TYPE_M)
			{
				++i;
				continue;
			}
			
			for (int note : iParas[i].iNotes)
			{
				switch(note)
				{
				case 1:
				case 2:
					scoredNotes += numNotes<100 ? 0.5f : 1.0f;
					if (inGGT)
						scoredNotes += numNotes<100 ? .1f : .2f;
					++numNotes;
					break;
					
				case 3:
				case 4:
					scoredNotes += numNotes<100 ? 1.0f : 2.0f;
					if (inGGT)
						scoredNotes += numNotes<100 ? .2f : .4f;
					++numNotes;
					break;

				default:;
				}
			}

			i++;
		}
		return scoredNotes;
	}
	
	private static TJACommand findCommand(int cmdType, TJACommand[] cmds)
	{
		for (TJACommand cmd : cmds)
		{
			if (cmd.iCommandType == cmdType) return cmd;
		}
		return null;
	}
	

}
