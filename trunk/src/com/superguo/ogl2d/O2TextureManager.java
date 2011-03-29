package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import android.graphics.*;

public class O2TextureManager {
	
	private android.content.Context appContext;
	private AbstractMap<O2Texture, O2Texture> spriteMap;
	private AbstractMap<O2TextureDivider, O2TextureDivider> spriteSheetMap;
	
	O2TextureManager(android.content.Context appContext)
	{
		this.appContext = appContext;
		if (O2Director.isSingleProcessor)
		{
			spriteMap = new HashMap<O2Texture, O2Texture>(10);
			spriteSheetMap = new HashMap<O2TextureDivider, O2TextureDivider>(10);
		}
		else
		{
			spriteMap = new ConcurrentHashMap<O2Texture, O2Texture>(10);
			spriteSheetMap = new ConcurrentHashMap<O2TextureDivider, O2TextureDivider>(10);
		}
	}
	
	final O2Texture conditionalAdd(O2Texture sprite)
	{
		if (sprite.managed) spriteMap.put(sprite, sprite);
		return sprite;
	}
	
	public final O2Texture createFromBitmap(Bitmap bmp, boolean managed)
	{
		return conditionalAdd(new O2InternalBitmapTexture(managed, bmp));
	}
	
	public final O2Texture createFromResource(int resId, boolean managed)
	{
		return conditionalAdd(new O2ResourceBitmapTexture(managed, resId, appContext.getResources()));
	}
	
	public final O2Texture createFromAsset(String assetPath, boolean managed)
	{
		return conditionalAdd(new O2InternalAssetBitmapTexture(managed, assetPath, appContext.getAssets()));
	}

	public final O2Texture createFromString(String text, long paintId, boolean managed)
	{
		return conditionalAdd(new O2StringTexture(managed, text, paintId));
	}

	public final O2Texture createFromString(String text, boolean managed)
	{
		return conditionalAdd(new O2StringTexture(managed, text, 0));
	}

	public final O2BufferTexture createBuffer(int width, int height)
	{
		return (O2BufferTexture)conditionalAdd(new O2BufferTexture(width, height));
	}
	
	public final O2TextureDivider createSpriteSheetWithRowsAndCols(
			O2Texture sprite, int rows, int cols)
	{
		O2TextureDivider sheet = new O2TextureDivider(sprite, O2TextureDivider.CREATE_FROM_ROWS_AND_COLS, rows, cols, true);
		spriteSheetMap.put(sheet, sheet);
		return sheet;
	}
	
	public void recreateManaged()
	{
		for (O2Texture sprite : spriteMap.keySet())
		{
			if (sprite.managed) sprite.recreate();
		}
		for (O2TextureDivider sheet : spriteSheetMap.keySet())
		{
			if (!sheet.created) sheet.create();
		}
	}
	
	void markAllNA()
	{
		for (O2Texture sprite : spriteMap.keySet())
		{
			sprite.available = false;
		}
	}
}
