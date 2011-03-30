package com.superguo.ogl2d;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

class O2ResourceBitmapTexture extends O2Texture {
	int resId;
	
	O2ResourceBitmapTexture(boolean managed, int resId, Resources res)
	{
		super(managed);
		this.resId = resId; 
		if (O2Director.instance.iGl != null) recreate();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	public void recreate() {
		if (O2Director.instance.iGl == null) return;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inScaled = false;
		Bitmap bitmap = BitmapFactory.decodeResource(
				O2Director.instance.getResources(), resId, opts);
		createTexFromBitmap(bitmap);
		available = true;
	}
}
