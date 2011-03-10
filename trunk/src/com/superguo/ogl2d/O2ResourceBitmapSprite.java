package com.superguo.ogl2d;

import java.io.IOException;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class O2ResourceBitmapSprite extends O2Sprite {
	int resId;
	
	O2ResourceBitmapSprite(boolean managed, int resId, Resources res)
	{
		super(managed);
		this.resId = resId; 
		if (O2Director.inGlContext) create(res);
	}

	void create(Resources res)
	{
		Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
		createTexFromBitmap(bitmap);
		available = true;
	}
	
	@Override
	public void dispose() {
		super.dispose();
	}
}
