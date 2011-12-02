package com.superguo.jiroplayer;

import java.io.*;

public final class TJAFormat {
	// unsupported header: GAME, LIFE
	public final static int COURSE_EASY 	= 0;
	public final static int COURSE_NORMAL 	= 1;
	public final static int COURSE_HARD 	= 2;
	public final static int COURSE_ONI 		= 3;
	public final static int COURSE_EDIT 	= 4;
	public final static int COURSE_TOWER 	= 5;
	public final static int SIDE_NORMAL		= 1;
	public final static int SIDE_EX			= 2;
	public final static int SIDE_BOTH		= 3;
	public final static int BRANCH_JUDGE_ROLL 		= 0;
	public final static int BRANCH_JUDGE_PRECISION 	= 1;
	public final static int BRANCH_JUDGE_SCORE 		= 2;
	public final static int COMMAND_TYPE_NOTE		= 0; 	// iNotes
	public final static int COMMAND_TYPE_BPMCHANGE 	= 1; 	// iFloatArg
	public final static int COMMAND_TYPE_GOGOSTART 	= 2;
	public final static int COMMAND_TYPE_GOGOEND 	= 3;
	public final static int COMMAND_TYPE_MEASURE  	= 4; 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
	public final static int COMMAND_TYPE_SCROLL 	= 5; 	// float(0.1 - 16.0)
	public final static int COMMAND_TYPE_DELAY 		= 6; 	// float(>0.001)
	public final static int COMMAND_TYPE_SECTION 	= 7;
	public final static int COMMAND_TYPE_BRANCHSTART  = 8; 	// BRANCH_JUDGE_*(r/p/s, int), X(float), Y(float), #N index, #E index, #M index, exit index(may be invalid) 
	public final static int COMMAND_TYPE_BRANCHEND 	= 9;
	public final static int COMMAND_TYPE_N 			= 10;
	public final static int COMMAND_TYPE_E 			= 11;
	public final static int COMMAND_TYPE_M 			= 12;
	public final static int COMMAND_TYPE_LEVELHOLD 	= 13;
	public final static int COMMAND_TYPE_BARLINEOFF = 14;
	public final static int COMMAND_TYPE_BARLINEON 	= 15;

	// global
	public String 	iTitle;
	public String 	iSubTitle;
	public int		iSide	= SIDE_BOTH;
	public String	iWave;
	public float	iOffset;	// -5 ~ +5
	public float	iDemoStart = 0.0f;
	public float	iSongVol = 100.0f;	// 0 ~ 100, default 100
	public float	iSeVol = 100.0f;		// 0 ~ 100, default 100
	public boolean	iBMScroll = false;
	public boolean	iHBScroll = false;
	public TJACourse iCourses[];

	public final static class TJACourse
	{
		public int		iCourse = COURSE_ONI;	//
		public int 		iLevel;		// 1 ~ 12
		public boolean	iHasBranch;
		public float 	iBPM;		// 50 ~ 250
		public int[]	iBalloon;	// number of balloons
		public int		iScoreInit;	// 1 ~ 100000, 0 means auto
		public int		iScoreDiff;	// 1 ~ 100000, 0 means auto
		public TJACommand[] iNotationSingle; 	// cannot be null if iStyle is STYLE_SIGNLE
		public TJACommand[] iNotationP1; 	// cannot be null if iStyle is STYLE_DOUBLE
		public TJACommand[] iNotationP2;	// cannot be null if iStyle is STYLE_DOUBLE
	}

	public final static class TJACommand
	{
		public TJACommand()
		{		}
		
		public TJACommand(int commandType)
		{	iCommandType = commandType;		}
		
		public int iCommandType;
		public int iArgs[];
	}
	
	public final static class TJAFormatException extends RuntimeException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7734235891270702334L;

		public TJAFormatException(int iLineNo, String iLine, String msg)
		{
			super("Line " + iLineNo + " " + msg + "\n" + (iLine == null ? "" : iLine));
		}
		
		public TJAFormatException(int iLineNo, String iLine, Throwable r)
		{
			super("Line " + iLineNo + "\n" + (iLine == null ? "" : iLine), r);
		}
	}

	public TJAFormat()
	{	}
	
	public static TJAFormat fromReader(TJAFormatParser parser, BufferedReader reader)  throws IOException
	{
		TJAFormat self = new TJAFormat();
		parser.parse(self, reader);
		return self;
	}
}
