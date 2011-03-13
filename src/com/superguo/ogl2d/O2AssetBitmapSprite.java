package com.superguo.ogl2d;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

public class O2AssetBitmapSprite extends O2Sprite {
	
	String assetPath;
	
	O2AssetBitmapSprite(boolean managed, String assetPath, AssetManager assetMan)
	{
		super(managed);
		this.assetPath = managed ? new String(assetPath) : assetPath;
		if (O2Director.instance.gl != null) recreate();
	}

	void create(AssetManager assetMan) throws IOException
	{
	}
	
	@Override
	public void dispose() {
		assetPath = null;
		super.dispose();
	}

	@Override
	public void recreate()
	{
		try
		{
			Bitmap bitmap = BitmapFactory.decodeStream(
				O2Director.instance.appContext.getAssets().open(assetPath));
			createTexFromBitmap(bitmap);
			available = true;
		}
		catch(IOException e)
		{
			available = false;
		}
	}
}
