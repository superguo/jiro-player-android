package com.superguo.ogl2d;

import java.io.IOException;

import android.content.res.AssetManager;
import android.graphics.*;

public class O2AssetBitmapSprite extends O2Sprite {
	
	String assetPath;
	
	O2AssetBitmapSprite(boolean managed, String assetPath, AssetManager assetMan) throws IOException
	{
		super(managed);
		this.assetPath = new String(assetPath); 
		if (O2Director.inGlContext) create(assetMan);
	}

	void create(AssetManager assetMan) throws IOException
	{
		Bitmap bitmap = BitmapFactory.decodeStream(assetMan.open(assetPath));
		createTexFromBitmap(bitmap);
		available = true;
	}
	
	@Override
	public void dispose() {
		assetPath = null;
		super.dispose();
	}
}
