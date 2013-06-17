package com.superguo.jiroplayer;

/**
 * The compiled TJA notation optimized for play time. <br>
 * So some TJA commands mentioned in {@link TJAFormat}  are remade. <br>
 * And some are eliminated: 
 * <ul>
 * <li>#MEASURE</li>
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
 * <li>#GOTO</li>
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

	public final static int COMMAND_EMPTY		= 0;
	public final static int COMMAND_GOGOSTART	= 1;
	public final static int COMMAND_GOGOEND		= 2;
	public final static int COMMAND_STARTBRANCH	= 3;
	public final static int COMMAND_EXITBRANCH	= 4;
	
	/** Judge type rolling */
	public static final int BRANCH_JUDGE_ROLL 		= 0;
	
	/** Judge type precision */
	public static final int BRANCH_JUDGE_PRECISION 	= 1;
	
	/** Judge type score */
	public static final int BRANCH_JUDGE_SCORE 		= 2;
	
	public Bar[] normalBranch;
	public Bar[] easyBranch;
	public Bar[] masterBranch;
	
	public static final class Bar {
		/** Indicates that whether it is a NoteBar or CommandBar.
		 * If it is set true, noteBar is not null and commandBar is null.
		 * Otherwise commandBar is not null and noteBar is null.<br>
		 * I don't make noteBar inherit {@link #CommandBar} to reduce 
		 * the class cast consuming.  */
		public boolean isNoteBar;
		
		/** The start time when the bar enters the center 
		 * of the beat since the beginning of the game */
		public long beatTimeMillis;

		/** Not null if {@link #isNoteBar} is true; null if {@link #isNoteBar} is false */
		public NoteBar noteBar;
		
		/** Not null if {@link #isNoteBar} is false; null if {@link #isNoteBar} is true */
		public Command command;
	}
	
	public static final class NoteBar {
		/** The time when the bar appears in the screen 
		 * to player, always earlier than beatTimeMillis */
		public long appearTimeMillis;

		/** The pre-computed x coordinations of the bar of evey millisecond
		 * relative to {@link #appearTimeMillis}
		 */
		public int preComputedXCoords[];
		
		/** The bar's width in pixels */
		public int width;

		/** The notes */
		public Note[] notes;
	}

	public static final class Note {
		/** The note value */
		public int noteValue;

		/** The time when the note hit the beat right 
		 * since the beginning of the game */
		public long beatTimeMillis;

	}
	
	public static class Command {
		/** The command value */
		public int commandValue;
		
		public Command(int value) {
			commandValue = value;
		}
	}
	
	public static class StartBranchCommand extends Command {

		public int normalIndex;
		public int easyIndex;
		public int masterIndex;
		public int exitIndex;
		public BranchJudge branchJudge;
		
		public StartBranchCommand() {
			super(COMMAND_STARTBRANCH);
		}
		
		public void compileTJAFormatArgs(int[] args) {
			normalIndex	= args[3];
			easyIndex	= args[4];
			masterIndex	= args[5];
			exitIndex	= args[6];
			switch (args[0]) {
			case TJAFormat.BRANCH_JUDGE_ROLL:
				branchJudge = new BranchJudgeRoll(args[1], args[2]);
				break;
			case TJAFormat.BRANCH_JUDGE_PRECISION:
				branchJudge = new BranchJudgePrecision(args[1], args[2]);
				break;
			case TJAFormat.BRANCH_JUDGE_SCORE:
				branchJudge = new BranchJudgeScore(args[1], args[2]);
				break;
			default:
				throw new RuntimeException("Invalid branch type " + args[0]);
			}
		}
		
		public static abstract class BranchJudge {
			
			/** Judge type, ranged in one of the following values:
			 * <ul>
			 * <li> {@link TJANotation#BRANCH_JUDGE_ROLL} </li>
			 * <li> {@link TJANotation#BRANCH_JUDGE_PRECISION} </li>
			 * <li> {@link TJANotation#BRANCH_JUDGE_SCORE} </li>
			 * </ul>
			 * */
			public int judgeType;
			
			public BranchJudge(int judgeType) {
				this.judgeType = judgeType;
			}
		}
		
		public static class BranchJudgeRoll extends BranchJudge {
			
			/** The minimum rolling count to reach EASY(玄人) class */
			public int easyRollingCount;
			
			/** The minimum rolling count to reach MASTER(達人) class */
			public int masterRollingCount;
			
			public BranchJudgeRoll(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_ROLL);
				easyRollingCount = tjaFormatArg1;
				masterRollingCount = tjaFormatArg2;
			}
			
		}
		
		public static class BranchJudgePrecision extends BranchJudge {

			/** The minimum beat precision in percentage
			 *  to reach EASY(玄人) class */
			public float easyBeatPrecision;
			
			/** The minimum beat precision in percentage
			 *  to reach MASTER(達人) class */
			public float masterBeatPrecision;
			
			public BranchJudgePrecision(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_PRECISION);
				easyBeatPrecision = Float.intBitsToFloat(tjaFormatArg1);
				masterBeatPrecision = Float.intBitsToFloat(tjaFormatArg2);
			}
			
		}
		
		public static class BranchJudgeScore extends BranchJudge {

			/** The minimum score to reach EASY(玄人) class */
			public int easyScore;
			
			/** The minimum score to reach MASTER(達人) class */
			public int masterScore;
			
			public BranchJudgeScore(int tjaFormatArg1, int tjaFormatArg2) {
				super(BRANCH_JUDGE_SCORE);
				easyScore = tjaFormatArg1;
				masterScore = tjaFormatArg2;
			}
			
		}
	}
	
	
}
