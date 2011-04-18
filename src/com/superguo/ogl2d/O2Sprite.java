package com.superguo.ogl2d;

import javax.microedition.khronos.opengles.*;

public class O2Sprite {
	
	// id to distinct itself from other O2Sprite
	protected int id;
	
	// create from texture
	protected O2Texture iTexture;
	
	// create from texture slices
	protected O2TextureSlices iTexSli;
	protected int iIndexOfSli;
	
	// public properties
	public int iHalign;
	public int iValign;
	public int iX;
	public int iY;
	public int iZOrder;
	
	public O2Sprite(O2Texture aTexture)
	{
		iTexture = aTexture;
	}

	public O2Sprite(O2TextureSlices aTexSli, int anIndex)
	{
		iTexSli = aTexSli;
		iIndexOfSli = anIndex;
	}
	
	public void draw(GL10 gl)
	{
		if (null!=iTexture)
			iTexture.draw(iX, iY, iHalign, iValign);
		else
			iTexSli.draw(iIndexOfSli, iX, iY, iHalign, iValign);
	}
}
