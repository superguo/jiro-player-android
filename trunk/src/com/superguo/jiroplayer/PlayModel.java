package com.superguo.jiroplayer;

public class PlayModel {
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
	
	private TJAFormat iTJA;
	private TJAFormat.TJACourse iCourse;
	private TJAFormat.TJAPara[] iParas;
	private int iScoreInit;
	private int iScoreDiff;
	private int iScore;

	public void prepare(TJAFormat aTJA, int aCourseIndex)
	{
		// TODO reset
		iTJA = aTJA;
		iCourse = iTJA.iCourses[aCourseIndex];
		iParas = iCourse.iParasSingle;
		// Play as P1 if Single STYLE is not defined
		if (iParas==null) iParas = iCourse.iParasP1;
		resetScores();
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
		}
	}
	
	// get the total number of notes
	// choose master if encounters branches
	private int getNumNotes()
	{
		int numNotes = 0;
		//TODO
		return numNotes;
	}
}
