package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import android.graphics.*;

public class O2TextureManager {
	
	private AbstractMap<O2Texture, O2Texture> textureMap;
	private AbstractMap<O2TextureSlices, O2TextureSlices> textureSlicesMap;
	
	O2TextureManager()
	{
		if (O2Director.isSingleProcessor)
		{
			textureMap = new HashMap<O2Texture, O2Texture>(10);
			textureSlicesMap = new HashMap<O2TextureSlices, O2TextureSlices>(10);
		}
		else
		{
			textureMap = new ConcurrentHashMap<O2Texture, O2Texture>(10);
			textureSlicesMap = new ConcurrentHashMap<O2TextureSlices, O2TextureSlices>(10);
		}
	}
	
	public final static O2TextureManager getInstance()
	{
		return O2Director.instance.iTextureManager;
	}
	
	private final O2Texture conditionalAdd(O2Texture sprite)
	{
		if (sprite.managed) textureMap.put(sprite, sprite);
		return sprite;
	}
	
	public final O2Texture createFromBitmap(Bitmap bmp, boolean managed)
	{
		return conditionalAdd(new O2InternalBitmapTexture(managed, bmp));
	}
	
	public final O2Texture createFromResource(int resId, boolean managed)
	{
		return conditionalAdd(new O2ResourceBitmapTexture(
				managed, resId, O2Director.instance.iAppContext.getResources()));
	}
	
	public final O2Texture createFromAsset(String assetPath, boolean managed)
	{
		return conditionalAdd(new O2InternalAssetBitmapTexture(
				managed, assetPath, O2Director.instance.iAppContext.getAssets()));
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
	
	public final O2TextureSlices createSpriteSheetWithRowsAndCols(
			O2Texture sprite, int rows, int cols)
	{
		O2TextureSlices sheet = new O2TextureSlices(sprite, O2TextureSlices.CREATE_FROM_ROWS_AND_COLS, rows, cols, true);
		textureSlicesMap.put(sheet, sheet);
		return sheet;
	}
	
	public void recreateManaged()
	{
		for (O2Texture sprite : textureMap.keySet())
		{
			if (sprite.managed) sprite.recreate();
		}
		for (O2TextureSlices sheet : textureSlicesMap.keySet())
		{
			if (!sheet.iCreated) sheet.create();
		}
	}
	
	void markAllNA()
	{
		for (O2Texture sprite : textureMap.keySet())
		{
			sprite.available = false;
		}
	}
}
