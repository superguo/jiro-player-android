package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import javax.microedition.khronos.opengles.*;

public class O2SpriteManager {
	private final static class SptComp implements Comparator<O2Sprite>
	{

		public int compare(O2Sprite a, O2Sprite b) {
			if (a.iZOrder == b.iZOrder)
				return a.id - b.id;
			else
				return a.iZOrder - b.iZOrder;
		}
		
	}
	
	///< currently not thread safe
	protected SortedSet<O2Sprite> iSpriteSet;
	protected int iMaxId;
	
	O2SpriteManager()
	{
		iSpriteSet = new TreeSet<O2Sprite>(new SptComp());
		/*
		if (O2Director.isSingleProcessor)
			iSpriteMap = new HashMap<O2Sprite, O2Sprite>(10);
		else
			iSpriteMap = new ConcurrentHashMap<O2Sprite, O2Sprite>(10);
			*/
	}
	
	public static O2SpriteManager getInstance()
	{
		return O2Director.instance.iSpriteManager;
	}
	
	public void drawAllSprites(GL10 gl)
	{
		for (O2Sprite sprite : iSpriteSet)
		{
			sprite.draw(gl);
		}
	}
	
	public void addSprite(O2Sprite aSprite)
	{
		aSprite.id = ++iMaxId;
		iSpriteSet.add(aSprite);
	}
}
