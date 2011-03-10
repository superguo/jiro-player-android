package com.superguo.ogl2d;

import java.io.IOException;
import java.util.*;

import android.graphics.*;

public class O2SpriteManager {
	
	private android.content.Context appContext;
	private HashSet<O2Sprite> spriteSet;
	
	O2SpriteManager(android.content.Context appContext)
	{
		this.appContext = appContext;
		spriteSet = new HashSet<O2Sprite>(10);
	}
	
	O2Sprite conditionalAdd(O2Sprite sprite)
	{
		if (sprite.managed) spriteSet.add(sprite);
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
	
	public O2Sprite createFromAsset(String assetPath, boolean managed) throws IOException
	{
		return conditionalAdd(new O2AssetBitmapSprite(managed, assetPath, appContext.getAssets()));
	}

	public void recreateManaged() throws IOException
	{
		for (O2Sprite sprite : spriteSet)
		{
			if (sprite.managed)
			{
				if (sprite instanceof O2BitmapSprite)
				{
					((O2BitmapSprite)sprite).create();
				}
				else if (sprite instanceof O2ResourceBitmapSprite)
				{
					((O2ResourceBitmapSprite)sprite).create(appContext.getResources());
				}
				else if (sprite instanceof O2AssetBitmapSprite)
				{
					((O2AssetBitmapSprite)sprite).create(appContext.getAssets());
				}
			}
		}
	}
}
