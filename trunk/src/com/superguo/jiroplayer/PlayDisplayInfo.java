package com.superguo.jiroplayer;

public final class PlayDisplayInfo
{
	public final static class NotePos
	{
		public int iNoteType;
		public int iNotePos;
	}
	public final static int MAX_NOTE_POS = 64;
	public final static int NOTE_SEPARATOR		= 0;
	public final static int NOTE_FACE 			= 1;
	public final static int NOTE_SIDE 			= 2;
	public final static int NOTE_BIG_FACE 		= 3;
	public final static int NOTE_BIG_SIDE 		= 4;
	public final static int NOTE_LENDA_HEAD 	= 5;
	public final static int NOTE_LENDA_BODY 	= 6;
	public final static int NOTE_LENDA_TAIL 	= 6;
	public final static int NOTE_BIG_LENDA_HEAD = 7;
	public final static int NOTE_BIG_LENDA_BODY = 8;
	public final static int NOTE_BIG_LENDA_TAIL = 9;
	public final static int NOTE_BALOON_HEAD	= 10;
	public final static int NOTE_BALOON_BODY	= 11;
	
	public final static int BRANCH_NONE			= 0;
	public final static int BRANCH_EASY			= 1;
	public final static int BRANCH_NORMAL		= 2;
	public final static int BRANCH_MASTER		= 3;
	
	public int iNumNotePos;
	public NotePos[] iNotePosArray = new NotePos[MAX_NOTE_POS];
	public int iBranch;
	public boolean iIsGGT;
	public int iBallonLenDaCount;	// balloon down counter   
									// 0 means just finished
									// -1 means failed
									// -2 means no lenda
	public int iNumCombo;
}
