package com.superguo.ogl2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class O2ResourceBitmapSprite extends O2Sprite {
	int resId;
	
	O2ResourceBitmapSprite(boolean managed, int resId, Resources res)
	{
		super(managed);
		this.resId = resId; 
		if (O2Director.instance.gl != null) recreate();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void recreate() {
		if (O2Director.instance.gl == null) return;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(
				O2Director.instance.getResources(), resId, opts);
		createTexFromBitmap(bitmap);
		available = true;
	}
}
