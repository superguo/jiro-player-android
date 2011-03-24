package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import android.graphics.*;

public class O2SpriteManager {
	
	private android.content.Context appContext;
	private AbstractMap<O2Sprite, O2Sprite> spriteMap;
	
	O2SpriteManager(android.content.Context appContext)
	{
		this.appContext = appContext;
		if (O2Director.isSingleProcessor)
			spriteMap = new HashMap<O2Sprite, O2Sprite>(10);
		else
			spriteMap = new ConcurrentHashMap<O2Sprite, O2Sprite>(10);
	}
	
	O2Sprite conditionalAdd(O2Sprite sprite)
	{
		if (sprite.managed) spriteMap.put(sprite, sprite);
		return sprite;
	}
	
	public O2Sprite createFromBitmap(Bitmap bmp, boolean managed)
	{
		return conditionalAdd(new O2BitmapSprite(managed, bmp));
	}
	
	public O2Sprite createFromResource(int resId, boolean managed)
	{
		return conditionalAdd(new O2ResourceBitmapSprite(managed, resId, appContext.getResources()));
	}
	
	public O2Sprite createFromAsset(String assetPath, boolean managed)
	{
		return conditionalAdd(new O2AssetBitmapSprite(managed, assetPath, appContext.getAssets()));
	}

	public O2Sprite createFromString(String text, long paintId, boolean managed)
	{
		return conditionalAdd(new O2StringSprite(managed, text, paintId));
	}

	public O2Sprite createFromString(String text, boolean managed)
	{
		return conditionalAdd(new O2StringSprite(managed, text, 0));
	}

	public O2BufferSprite createBuffer(int width, int height)
	{
		return (O2BufferSprite)conditionalAdd(new O2BufferSprite(width, height));
	}
	
	public void recreateManaged()
	{
		for (O2Sprite sprite : spriteMap.keySet())
		{
			if (sprite.managed) sprite.recreate();
		}
	}
	
	void markAllNA()
	{
		for (O2Sprite sprite : spriteMap.keySet())
		{
			sprite.available = false;
		}
	}
}
