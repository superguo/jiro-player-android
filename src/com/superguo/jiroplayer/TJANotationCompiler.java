package com.superguo.jiroplayer;

import com.superguo.jiroplayer.TJAFormat.TJACommand;
import com.superguo.jiroplayer.TJAFormat.TJACourse;

public class TJANotationCompiler {

	public static final int NOTATION_INDEX_SINGLE = 0;
	public static final int NOTATION_INDEX_P1 = 1;
	public static final int NOTATION_INDEX_P2 = 2;
	private TJANotation mNotation;
	private TJAFormat mTja;
	private TJACommand[] mNotationCommands;
	private long mPlayTimeMillis;
	
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
		TJACourse course = tja.courses[courseIndex];
		if (course == null) {
			return null;
		}
		TJACommand[][] uncompiledNotations = { 
				course.notationSingle, course.notationP1, course.notationP2 };
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
		// TODO
	}
}
