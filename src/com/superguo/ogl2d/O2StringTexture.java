package com.superguo.ogl2d;

import android.graphics.*;

public class O2StringTexture extends O2Texture {

	String text;
	long paintId;

	protected O2StringTexture(boolean managed, String text, long paintId) {
		super(managed);
		this.text = managed ? new String(text) : text;
		this.paintId = paintId; 
		if (O2Director.instance.gl != null) recreate();
	}

	@Override
	public void dispose()
	{
		super.dispose();
	}

	@Override
	public void recreate()
	{
		Paint paint = O2Director.instance.getPaint(paintId);
		Rect rect = new Rect();
		paint.setTextAlign(Paint.Align.LEFT);
		paint.getTextBounds(text, 0, text.length(), rect);
		Bitmap bitmap = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(text, 0, -rect.top, paint);
		createTexFromBitmap(bitmap);
	}
}
