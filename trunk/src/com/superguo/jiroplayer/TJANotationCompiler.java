package com.superguo.jiroplayer;

import java.util.ArrayList;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;

public class TJANotationCompiler {

	public static final int NOTATION_INDEX_SINGLE = 0;
	public static final int NOTATION_INDEX_P1     = 1;
	public static final int NOTATION_INDEX_P2     = 2;
	private static final int BRANCH_INDEX_NORMAL = 0;
	private static final int BRANCH_INDEX_EASY   = 1;
	private static final int BRANCH_INDEX_MASTER   = 2;
	private TJANotation mNotation;
	private TJAFormat mTja;
	private TJACommand[] mNotationCommands;
	private long mPlayTimeMillis;
	private TJACourse mCourse;
	
	public static final class TJANotationCompilerException extends Exception {
	
		/**
		 * 
		 */
		private static final long serialVersionUID = -6481065923819947596L;

		public TJANotationCompilerException(int lineNo, String line, String msg) {
			super("Line " + lineNo + " " + msg + "\n"
					+ (line == null ? "" : line));
		}
	
		public TJANotationCompilerException(int lineNo, String line, Throwable r) {
			super("Line " + lineNo + "\n" + (line == null ? "" : line), r);
		}
	}
	/**
	 * Compile the specified notation of a TJA format into a compiled TJANotaion
	 * @param tja The TJA format data.
	 * @param courseIndex The index of the course stored in TJA format data.
	 * @param notationIndex The index of the notation to compile. The index is one of 
	 * 	{@link #NOTATION_INDEX_SINGLE}, {@link #NOTATION_INDEX_P1} or {@link #NOTATION_INDEX_P2}
	 * @param playTimeMillis The time to wait before the song starts to play.
	 * @return
	 */
	public TJANotation compile(TJAFormat tja, int courseIndex,
			int notationIndex, long playTimeMillis) {
		mCourse = tja.courses[courseIndex];
		if (mCourse == null) {
			return null;
		}
		TJACommand[][] uncompiledNotations = { 
				mCourse.notationSingle, mCourse.notationP1, mCourse.notationP2 };
		TJACommand[] notationCommands = uncompiledNotations[notationIndex];
		if (notationCommands == null) {
			return null;
		}
		mNotation = new TJANotation();
		mTja = tja;
		mNotationCommands = notationCommands;
		mPlayTimeMillis = playTimeMillis;
		doCompile();
		return mNotation;
	}

	private void doCompile() {
		boolean hasBranch = mCourse.hasBranch;

		ArrayList<TJANotation.Bar> normalBranch = compileBranch(BRANCH_INDEX_NORMAL);
		
		if (hasBranch) {
			ArrayList<TJANotation.Bar> easyBranch = compileBranch(BRANCH_INDEX_EASY);
			ArrayList<TJANotation.Bar> masterBranch = compileBranch(BRANCH_INDEX_MASTER);
			
			int numBars = normalBranch.size();
			for (int i=0; i<numBars; ++i) {
				// TODO Check all sub-branches' duration consistency 
			}
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
			mNotation.easyBranch   = (Bar[]) easyBranch.toArray();
			mNotation.masterBranch = (Bar[]) masterBranch.toArray();
		} else {
			mNotation.normalBranch = (Bar[]) normalBranch.toArray();
		}
	}

	private ArrayList<TJANotation.Bar> compileBranch(int brachIndex) {
		ArrayList<Bar> branch = new ArrayList<TJANotation.Bar>(mNotationCommands.length);
		// TODO
		return branch;
	}
}
