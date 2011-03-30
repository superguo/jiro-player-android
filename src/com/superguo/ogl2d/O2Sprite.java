package com.superguo.ogl2d;

import javax.microedition.khronos.opengles.*;

public class O2Sprite {
	
	public final static int HALIGN_LEFT 	= 0;
	public final static int HALIGN_MIDDLE 	= 1;
	public final static int HALIGN_RIGHT 	= 2;
	public final static int VALIGN_TOP 		= 0;
	public final static int VALIGN_MIDDLE 	= 1;
	public final static int VALIGN_BOTTOM	= 2;
	
	// create from texture
	private O2Texture iTexture;
	
	// create from texture slices
	private O2TextureSlices iTexSli;
	private int iIndexOfSli;
	
	// public properties
	public int iHalign;
	public int iValign;
	public int iX;
	public int iY;
	
	public O2Sprite(O2Texture aTexture)
	{
		iTexture = aTexture;
		O2Director.instance.iSpriteManager.iSpriteMap.put(this, this);
	}

	public O2Sprite(O2TextureSlices aTexSli, int anIndex)
	{
		iTexSli = aTexSli;
		iIndexOfSli = anIndex;
		O2Director.instance.iSpriteManager.iSpriteMap.put(this, this);
	}
	
	public void draw(GL10 gl)
	{
		if (null!=iTexture)
			iTexture.draw(iX, iY, iHalign, iValign);
		else
			iTexSli.draw(iIndexOfSli, iX, iY, iHalign, iValign);
	}
	
	public void dispose()
	{
		O2Director.instance.iSpriteManager.iSpriteMap.remove(this);
	}
}
