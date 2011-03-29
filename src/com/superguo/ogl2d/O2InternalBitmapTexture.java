package com.superguo.ogl2d;

import android.graphics.*;

class O2InternalBitmapTexture extends O2Texture{
	
	private Bitmap bitmap;
	
	O2InternalBitmapTexture(boolean managed, Bitmap bitmap)
	{
		super(managed);
		this.bitmap = managed ? bitmap.copy(bitmap.getConfig(), false) : bitmap;
		if (O2Director.instance.gl!=null) recreate();
	}
	
	@Override
	public void dispose()
	{
		if (bitmap!=null && managed) bitmap.recycle();
		super.dispose();
	}

	@Override
	public void recreate()
	{
		createTexFromBitmap(bitmap);
		available = true;		
	}
}