package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import javax.microedition.khronos.opengles.*;

public class O2SpriteManager {
	AbstractMap<O2Sprite, O2Sprite> iSpriteMap;
	
	O2SpriteManager()
	{
		if (O2Director.isSingleProcessor)
			iSpriteMap = new HashMap<O2Sprite, O2Sprite>(10);
		else
			iSpriteMap = new ConcurrentHashMap<O2Sprite, O2Sprite>(10);
	}
	
	public static O2SpriteManager getInstance()
	{
		return O2Director.instance.iSpriteManager;
	}
	
	public void drawAllSprites(GL10 gl)
	{
		for (O2Sprite sprite : iSpriteMap.keySet())
		{
			sprite.draw(gl);
		}
	}
	
	public void addSprite(O2Sprite aSprite)
	{
		iSpriteMap.put(aSprite, aSprite);
	}
}
