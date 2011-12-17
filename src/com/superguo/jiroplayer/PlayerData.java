package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACourse;

/** PlayModel's publicly visible data are all in this class
 * 
 * @author superguo
 *
 */
public final class PlayerData
{
	public final static class NotePos
	{
		public int iNoteType;
		public int iNotePos;
	}
	
	public final static int MAX_NOTE_POS = 64;	

	public int iScore;
	public int iAddedScore;
	public int iNumCombo;
	public int iNumMaxCombo;
	public int iNumMaxNotes;
	public int iNumHitNotes;
	public int iNumTotalRolling;

	public int iNoteFrom;
	public int iNoteTo;
	public NotePos[] iNotePosArray = new NotePos[MAX_NOTE_POS];
	public int iBranch;
	public boolean iIsGGT;
	public int iRollingState;		
	public int iRollingCount;	// balloon/potato down counter / bar up counter
								// balloon/potato:
								// 0 means just finished
								// -1 means failed
								// -2 means no lenda
	public boolean iIsCompletedEnded;
	
	public void reset(TJACourse aCourse)
	{
		// Reset current score
		iScore = 0;
		iAddedScore = 0;
		
		// Note positions
		iNoteFrom = 0;
		iNoteTo = 0;
		
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
		
		iIsCompletedEnded = false;
	}
}
