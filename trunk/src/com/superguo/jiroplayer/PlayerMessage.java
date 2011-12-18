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
	public final static int NOTE_JUDGED_NONE = 0;
	public final static int NOTE_JUDGED_GOOD = 1;
	public final static int NOTE_JUDGED_NORMAL = 2;
	public final static int NOTE_JUDGED_MISSED = 3;
	public final static int NOTE_JUDGED_PASSED = 4;

	public int iScore;
	public int iAddedScore;
	public int iNumCombo;
	public int iNumMaxCombo;
	public int iNumMaxNotes;
	public int iNumHitNotes;
	public int iNumTotalRolling;

	public int iNotePosCount;
	public NotePos[] iNotePosArray = new NotePos[MAX_NOTE_POS];
	public int iNoteJudged;
	public int iBranch;
	public boolean iIsGGT;
	public int iRollingState;		
	public int iRollingCount;	// balloon/potato down counter / bar up counter
								// balloon/potato:
								// 0 means just finished
								// -1 means failed
								// -2 means no lenda
	public boolean iFinalEnd;
	
	public void reset(TJACourse aCourse)
	{
		// Reset current score
		iScore = 0;
		iAddedScore = 0;
		
		// The number of note position
		iNotePosCount = 0;
		
		// Indicate the current note is played good,
		// play normal, missed, passed or not judged yet
		iNoteJudged = NOTE_JUDGED_NONE;
		
		// Branch state
		if (aCourse.iHasBranch)
			iBranch = PlayModel.BRANCH_NORMAL;
		else
			iBranch = PlayModel.BRANCH_NONE;
		
		// GO-GO-TIME state
		iIsGGT = false;
		
		// Rolling states
		iRollingState = PlayModel.ROLLING_NONE;
		iRollingCount = 0;

		// reset some counters
		iNumMaxCombo = iNumMaxNotes = iNumHitNotes = iNumTotalRolling = 0;
		
		iFinalEnd = false;
	}
}
