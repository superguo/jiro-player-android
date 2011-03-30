package com.superguo.ogl2d;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

class O2InternalAssetBitmapTexture extends O2Texture {
	
	String assetPath;
	
	O2InternalAssetBitmapTexture(boolean managed, String assetPath, AssetManager assetMan)
	{
		super(managed);
		this.assetPath = managed ? new String(assetPath) : assetPath;
		if (O2Director.instance.iGl != null) recreate();
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
				O2Director.instance.getContext().getAssets().open(assetPath));
			createTexFromBitmap(bitmap);
			available = true;
		}
		catch(IOException e)
		{
			available = false;
		}
	}
}
