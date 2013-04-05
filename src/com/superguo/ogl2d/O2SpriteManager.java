package com.superguo.ogl2d;

import java.util.*;
//import java.util.concurrent.*;

import javax.microedition.khronos.opengles.*;

public class O2SpriteManager {
	static final class SptComp implements Comparator<O2Sprite> {

		public int compare(O2Sprite a, O2Sprite b) {
			if (a.zorder == b.zorder)
				return a.mId - b.mId;
			else
				return a.zorder - b.zorder;
		}

	}

	// /< currently not thread safe
	protected SortedSet<O2Sprite> mSpriteSet;
	protected int mMaxId;

	O2SpriteManager() {
		mSpriteSet = new TreeSet<O2Sprite>(new SptComp());
		/*
		 * if (O2Director.isSingleProcessor) iSpriteMap = new HashMap<O2Sprite,
		 * O2Sprite>(10); else iSpriteMap = new ConcurrentHashMap<O2Sprite,
		 * O2Sprite>(10);
		 */
	}

	public static O2SpriteManager getInstance() {
		return O2Director.sInstance.mSpriteManager;
	}

	public void drawAllSprites(GL10 gl) {
		for (O2Sprite sprite : mSpriteSet) {
			sprite.draw(gl);
		}
	}

	public void addSprite(O2Sprite aSprite) {
		aSprite.mId = ++mMaxId;
		mSpriteSet.add(aSprite);
	}

	public void removeSprite(O2Sprite aSprite) {
		mSpriteSet.remove(aSprite);
	}
}
