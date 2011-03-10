package com.superguo.ogl2d;

import android.graphics.*;

public class O2BitmapSprite extends O2Sprite{
	
	private Bitmap bitmap;
	
	O2BitmapSprite(boolean managed, Bitmap bitmap)
	{
		super(managed);
		this.bitmap = managed ? bitmap.copy(bitmap.getConfig(), false) : bitmap;
		if (O2Director.inGlContext) create();
	}

	void create()
	{
		createTexFromBitmap(bitmap);
		available = true;
	}
	
	@Override
	public void dispose()
	{
		if (bitmap!=null && managed) bitmap.recycle();
		super.dispose();
	}
}