package com.superguo.ogl2d;
import android.opengl.*;

public abstract class O2Sprite {
	protected boolean available;
	protected boolean managed;
	protected int tex;
	protected int width;
	protected int height;
	protected final static int fullTexCoods[] = {0, 0, 1, 0, 0, 1, 1, 1};
	
	protected O2Sprite(boolean managed)
	{
		
	}
	
	void create(android.content.res.AssetManager assetMan, String path, int tex)
	{
		
	}
	
	protected abstract void dispose();
	
	public void draw(int srcX, int srcY, int tagetX, int targetY)
	{
		fullTexCoods
	}
	public void draw(int srcX, int srcY, int srcWidth, int srcHeight, int tagetX, int targetY)
	{
		
	}
}
