package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACourse;

/** PlayModel's publicly visible data are all in this class
 * 
 * @author superguo
 *
 */
public final class PlayerMessage
{
	/** Contain moving notes only 
	 * So notes with type of
	 * NOTE_START_ROLLING_BALOON or
	 * NOTE_START_ROLLING_POTATO
	 * are contained only when it is not in playing(rolling)
	 */
	public final static class NotePos
	{
		public int iNoteType;
		public int iNotePos;
	}
	
	public final static int MAX_NOTE_POS = 64;
	public final static int JUDGED_NONE = 0;
	public final static int JUDGED_GOOD = 1;
	public final static int JUDGED_NORMAL = 2;
	public final static int JUDGED_MISSED = 3;
	public final static int JUDGED_BREAK = 4;
	public final static int SPECIAL_ROLLING_COUNT_BALLOON_FINISHED 	= -1;
	public final static int SPECIAL_ROLLING_COUNT_BALLOON_FAILED 	= -2;
	public final static int SPECIAL_ROLLING_COUNT_POTATO_FINISHED 	= -3;
	public final static int SPECIAL_ROLLING_COUNT_POTATO_FAILED 	= -4;

	public int iCourse;
	public int iScore;

	/** The added score
	 * Drawer must set to 0 after the frame is drawn
	 */
	public int iAddedScore;
	public int iNumCombo;
	public int iNumMaxCombo;
	public int iNumMaxNotes;
	public int iNumHitNotes;
	public int iNumTotalRolling;

	/** The value is one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * Drawer must set to HIT_NONE after the frame is drawn
	 */
	//public int iHit;
	
	public int iNotePosCount;
	public NotePos[] iNotePosArray = new NotePos[MAX_NOTE_POS];
	
	/** Indicate the current note is played good,
	 * 	play normal, missed, passed or not judged yet
	 * Drawer must set to NOTE_JUDGED_NONE after the frame is drawn
	 */
	public int iNoteJudged;
	
	/** Current branch */
	public int iBranch;
	public boolean iIsGGT;
	
	/**
	 * 
	 */
	public int iRollingState;		
	
	/** Can be positive or negative or zero 
	 * Non-negative value means the rolling counter:
	 * down-counter for balloon/potato
	 * up counter for len-da bar
	 * 
	 * Negative value means the rolling ended:
	 * see SPECIAL_ROLLING_COUNT_
	 * 
	 * In the case of negative value, the drawer must set to 0 
	 * after the "rolling end" animation just starts to draw
	 * 
	 */
	public int iRollingCount;	
	
	public void reset(TJACourse aCourse)
	{
		// Reset current course
		iCourse = aCourse.iCourse;

		// Reset current score
		iScore = 0;
		iAddedScore = 0;

		// Reset hit
		//iHit = PlayModel.HIT_NONE;
		
		// Reset the number of note position
		iNotePosCount = 0;

		// Reset judge
		iNoteJudged = JUDGED_NONE;
		
		// Reset branch state
		if (aCourse.iHasBranch)
			iBranch = PlayModel.BRANCH_NORMAL;
		else
			iBranch = PlayModel.BRANCH_NONE;
		
		// Reset GO-GO-TIME state
		iIsGGT = false;
		
		// Reset rolling states
		iRollingState = PlayModel.ROLLING_NONE;
		iRollingCount = 0;

		// Reset some counters
		iNumMaxCombo = iNumMaxNotes = iNumHitNotes = iNumTotalRolling = 0;
	}
}
