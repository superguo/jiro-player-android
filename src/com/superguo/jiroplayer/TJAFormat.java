package com.superguo.jiroplayer;

public final class TJAFormat {
	public final static int COURSE_EASY 	= 0;
	public final static int COURSE_NORMAL 	= 1;
	public final static int COURSE_HARD 	= 2;
	public final static int COURSE_ONI 		= 3;
	public final static int COURSE_EDIT 	= 4;
	
	// global
	public String 	iTitle;
	public int 		iLevel;
	public String	iWave;
	public float	iOffset;	// -5 ~ +5
	public float	iDemoStart;
	public float	iSongVol = 100.0f;	// 0 ~ 100, default 100
	public float	iSeVol = 100.0f;		// 0 ~ 100, default 100
	
	public final static class TJACourse
	{
		public int		iCourse = COURSE_ONI;	//
		public float 	iBPM;		// 50 ~ 250
		public int		iBalloon;	// number of balloons
		public int		iScoreInit;	// 1 ~ 100000
		public int		iScoreDiff;	// 1 ~ 100000
	}
	
	public TJACourse iCourses[];
}
