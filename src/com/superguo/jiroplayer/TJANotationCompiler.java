package com.superguo.jiroplayer;

import java.util.ArrayList;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;
import com.superguo.jiroplayer.TJANotation.Bar;
import com.superguo.jiroplayer.TJANotation.Note;
import com.superguo.jiroplayer.TJANotation.NoteBar;

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
	private long mScreenWidth;
	
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
			int notationIndex, long playTimeMillis, int screenWidth) {
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
		mScreenWidth = screenWidth;
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

	private ArrayList<Bar> compileBranch(int brachIndex) {
		int length = mNotationCommands.length;
		
		/** The branch to emit */
		ArrayList<Bar> branch = new ArrayList<Bar>(length);
		
		/** The compiling bar's BPM */
		double bpm = mCourse.BPM;
		
		/** The compiling bar's X in MEASURE X/Y */
		int measureX = 4;
		
		/** The compiling bar's Y in MEASURE X/Y */
		int measureY = 4;
		
		/** The compiling bar's scroll value  */
		double scroll = 1.0;
		
		/** The compiling bar's barLine on/off state */
		boolean barLine = true;
		
		/** The compiling  bar's flag indicating if the last compiled note is rolling */
		boolean isLastNoteRolling = false;
	
		/**  The first note bar's beat time */
		double firstNoteBarBeatTime = mTja.offset * 1000.0 + mPlayTimeMillis;
		
		/** The time offset of the beginning of the current note bar */
		double preciseBeatTime = firstNoteBarBeatTime;
		
		long beatTime = (long) Math.floor(preciseBeatTime);
		
		for (int i=0; i<length; ++i) {
			TJACommand oCmd = mNotationCommands[i];
			Bar bar = new Bar();
			
			switch (oCmd.commandType) {
			case TJAFormat.COMMAND_TYPE_NOTE: {
				bar.beatTimeMillis = beatTime;
				bar.isNoteBar = true;
				NoteBar noteBar = bar.noteBar = new NoteBar();
				double preciseSpeed = bpm * PlayModel.BEAT_DIST * scroll * 1024.0 / 60.0;
				noteBar.speed = (int) preciseSpeed;
				noteBar.appearTimeMillis = (long) (beatTime - mScreenWidth / preciseSpeed);
				int[] oNotes = oCmd.args;
				ArrayList<Note> compiledNotes = new ArrayList<Note>();
				if (isLastNoteRolling) {
					// TODO
				} else {
					// TODO
				}
				// Emit notes
				noteBar.notes = (Note[]) compiledNotes.toArray();
				break;
			}
			
			// TODO
			}
			
		}
		
		return branch;
	}
}
