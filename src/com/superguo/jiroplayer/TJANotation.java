package com.superguo.jiroplayer;

/**
 * The compiled TJA notation optimized for play time. <br>
 * So some TJA commands mentioned in {@link TJAFormat}  are remade. <br>
 * And some are eliminated: 
 * <ul>
 * <li>#DELAY</li>
 * <li>#SCROLL</li>
 * <li>#N</li>
 * <li>#E</li>
 * <li>#M</li>
 * <li>#BPMCHANGE</li>
 * </ul>
 * And the following commands are added:
 * <ul>
 * <li>note commands</li>
 * <li>#EXITBRANCH</li>
 * </ul>
 * @author superguo
 */
public final class TJANotation {
	public final static int NOTE_EMPTY		= 0;
	public final static int NOTE_FACE		= 1;
	public final static int NOTE_SIDE		= 2;
	public final static int NOTE_BIG_FACE	= 3;
	public final static int NOTE_BIG_SIDE	= 4;
	public final static int NOTE_LENDA		= 5;
	public final static int NOTE_BIG_LENDA	= 6;
	public final static int NOTE_BALLOON	= 7;
	public final static int NOTE_POTATO		= 9;
	public final static int NOTE_ROLLING_END = 8;
	// TODO Add more commands
	
	public static final class TJABar {
		/** The start time when the bar enters the center 
		 * of the beat since the beginning of the game */
		public long beatTimeMillis;
		
		/** The time when the bar appears in the screen 
		 * to player, always earlier than beatTimeMillis */
		public long appearTimeMillis;
		
		/** The bar's scrolling speed in pixels per 1024 seconds */
		public int speed;
		
		public TJACommandInfo[] commands;
	}
	
	public static final class TJACommandInfo {
		/** The note/command value */
		public int command;
		
		/** The time when the note/command hit the beat right 
		 * since the beginning of the game */
		public long beatTimeMillis;
		
		/** The scroll distance in pixels between the note/command
		 *  and the beat at the beginning of the game */
		public long beatDistance;
		
		/** The extra arguments for the command. It is null if no 
		 * arguments are needed. It is always null for every note */
		public int commandArgs[];
	}
}
