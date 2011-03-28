package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import android.graphics.*;

public class O2SpriteManager {
	
	private android.content.Context appContext;
	private AbstractMap<O2Sprite, O2Sprite> spriteMap;
	private AbstractMap<O2SpriteSheet, O2SpriteSheet> spriteSheetMap;
	
	O2SpriteManager(android.content.Context appContext)
	{
		this.appContext = appContext;
		if (O2Director.isSingleProcessor)
		{
			spriteMap = new HashMap<O2Sprite, O2Sprite>(10);
			spriteSheetMap = new HashMap<O2SpriteSheet, O2SpriteSheet>(10);
		}
		else
		{
			spriteMap = new ConcurrentHashMap<O2Sprite, O2Sprite>(10);
			spriteSheetMap = new ConcurrentHashMap<O2SpriteSheet, O2SpriteSheet>(10);
		}
	}
	
	final O2Sprite conditionalAdd(O2Sprite sprite)
	{
		if (sprite.managed) spriteMap.put(sprite, sprite);
		return sprite;
	}
	
	public final O2Sprite createFromBitmap(Bitmap bmp, boolean managed)
	{
		return conditionalAdd(new O2BitmapSprite(managed, bmp));
	}
	
	public final O2Sprite createFromResource(int resId, boolean managed)
	{
		return conditionalAdd(new O2ResourceBitmapSprite(managed, resId, appContext.getResources()));
	}
	
	public final O2Sprite createFromAsset(String assetPath, boolean managed)
	{
		return conditionalAdd(new O2AssetBitmapSprite(managed, assetPath, appContext.getAssets()));
	}

	public final O2Sprite createFromString(String text, long paintId, boolean managed)
	{
		return conditionalAdd(new O2StringSprite(managed, text, paintId));
	}

	public final O2Sprite createFromString(String text, boolean managed)
	{
		return conditionalAdd(new O2StringSprite(managed, text, 0));
	}

	public final O2BufferSprite createBuffer(int width, int height)
	{
		return (O2BufferSprite)conditionalAdd(new O2BufferSprite(width, height));
	}
	
	public final O2SpriteSheet createSpriteSheetWithRowsAndCols(
			O2Sprite sprite, int rows, int cols)
	{
		O2SpriteSheet sheet = new O2SpriteSheet(sprite, O2SpriteSheet.CREATE_FROM_ROWS_AND_COLS, rows, cols, true);
		spriteSheetMap.put(sheet, sheet);
		return sheet;
	}
	
	public void recreateManaged()
	{
		for (O2Sprite sprite : spriteMap.keySet())
		{
			if (sprite.managed) sprite.recreate();
		}
		for (O2SpriteSheet sheet : spriteSheetMap.keySet())
		{
			if (!sheet.created) sheet.create();
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
