package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.NoteBar;

/** PlayModel's publicly visible data are all in this class
 * 
 * @author superguo
 *
 */
public final class PlayerMessage {
//	/** Contain moving notes only 
//	 * So notes with type of
//	 * NOTE_START_ROLLING_BALOON or
//	 * NOTE_START_ROLLING_POTATO
//	 * are contained only when it is not in playing(rolling)
//	 */
//	public static final class NotePos {
//		public int noteValue;
//		public int notePos;
//	}
//	
//	public static final int MAX_NOTE_POS 	= 64;
	
	public int course;
	public int score;
	public int gauge;

	/** The added score
	 * Drawer must set to 0 after the frame is drawn
	 */
	public int addedScore;
	public int numCombos;
	public int numMaxCombos;
	
	/** The number of NORMAL notes */
	public int numBeatedNormalNotes;
	
	/** The number of GOOD notes */
	public int numBeatedGoodNotes;

	/** The number of BAD+MISSED notes */
	public int numMissedOrBadNotes;

	/** The number of all rolled */
	public int numTotalRolled;

	/** The value is one of HIT_NONE, HIT_FACE and HIT_SIDE
	 * Drawer must set to HIT_NONE after the frame is drawn
	 */
	//public int iHit;

//	public LinkedList<NotePos> notePosList = new LinkedList<PlayerMessage.NotePos>(); 

	/** The action note bar is the one that appears in the screen first. */
	public NoteBar actionNoteBar;

	/** The index of the nearest coming note that is not hit or missed or broken yet. */
	public int actionNoteIndex;

	/**
	 * The next action note bar that immediately after the action note bar. It
	 * may be null.
	 */
	public NoteBar nextActionNoteBar;
	
	/** The note beated */
	public int beatedNote;
	
	/** Indicate the current note is played good,
	 * 	play normal, missed, passed or not judged yet
	 * Drawer must set to NOTE_JUDGED_NONE after the frame is drawn
	 */
	public int noteJudged;
	
	/** Current branch */
	public int branch;
	
	public boolean isGGT;
	
	/**
	 * One if following values
	 * <ul>
	 * <li>{@link PlayModel#ROLLING_NONE}</li>
	 * <li>{@link PlayModel#ROLLING_LENDA_BAR}</li>
	 * <li>{@link PlayModel#ROLLING_BIG_LENDA_BAR}</li>
	 * <li>{@link PlayModel#ROLLING_BALLOON}</li>
	 * <li>{@link PlayModel#ROLLING_POTATO}</li>
	 * </ul>
	 */
	public int rollingState;		
	
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
	public int rollingCount;
	
	public void reset(TJACourse aCourse) {
		// Reset current course
		course = aCourse.course;

		// Reset current score
		score = 0;
		addedScore = 0;
		
		gauge = 0;

		actionNoteBar = null;
		actionNoteIndex = 0;
		nextActionNoteBar = null;
		
		// Reset hit
		//iHit = PlayModel.HIT_NONE;
		
//		notePosList.clear();

		// Reset judge
		noteJudged = PlayModel.JUDGED_NONE;
		
		// Reset branch state
		if (aCourse.hasBranch)
			branch = PlayModel.BRANCH_NORMAL;
		else
			branch = PlayModel.BRANCH_NONE;
		
		// Reset GO-GO-TIME state
		isGGT = false;
		
		// Reset rolling states
		rollingState = PlayModel.ROLLING_NONE;
		rollingCount = 0;

		// Reset some counters
		numMaxCombos = numBeatedNormalNotes = numBeatedGoodNotes = numMissedOrBadNotes = numTotalRolled = 0;
	}
}
