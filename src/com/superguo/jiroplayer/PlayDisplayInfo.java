package com.superguo.jiroplayer;

public final class PlayDisplayInfo
{
	public final static class NotePos
	{
		public int iNoteType;
		public int iNotePos;
	}
	public final static int MAX_NOTE_POS = 64;
	public final static int NOTE_SEPARATOR		= -1;
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
	public final static int BRANCH_EASY			= 1;
	public final static int BRANCH_NORMAL		= 2;
	public final static int BRANCH_MASTER		= 3;
	
	public final static int ROLLING_NONE		= 0;
	public final static int ROLLING_BAR			= 1;
	public final static int ROLLING_BALLOON		= 2;
	public final static int ROLLING_POTATO		= 3;
	
	public int iScore;
	public int iAddedScore;

	public int iNumNotePos;
	public NotePos[] iNotePosArray = new NotePos[MAX_NOTE_POS];
	public int iBranch;
	public boolean iIsGGT;
	public int iRollingState;		
	public int iRollingCount;	// balloon/potato down counter / bar up counter
								// balloon/potato:
								// 0 means just finished
								// -1 means failed
								// -2 means no lenda
	public int iNumCombo;
	
	public void reset()
	{
		// reset current score
		iScore = 0;
		iAddedScore = 0;
		
		iNumNotePos = 0;
		iBranch = BRANCH_NONE;
		iIsGGT = false;
		iRollingState = ROLLING_NONE;
		iRollingCount = 0;
	}
}
