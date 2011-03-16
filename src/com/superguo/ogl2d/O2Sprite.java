package com.superguo.ogl2d;
import java.nio.*;

import javax.microedition.khronos.opengles.*;

import android.graphics.Bitmap;
import android.opengl.*;

public abstract class O2Sprite {
	public final static int MAX_SIZE = 1024;
	protected boolean available;
	protected boolean managed;
	protected int tex;
	protected int width;
	protected int height;
	protected int texWidth;
	protected int texHeight;
	protected int vboFullTexCood;

	private float vertCoods[] = new float[8];
	private float texCoods[] = new float[8];
	private FloatBuffer vertBuf = FloatBuffer.allocate(8);
	private FloatBuffer texBuf = FloatBuffer.allocate(8);
	
	protected O2Sprite(boolean managed)
	{
		if (O2Director.instance == null)
		{
			throw new O2Exception("O2Director instance not created");
		}
		
		if (!managed && O2Director.instance.gl == null)
		{
			throw new O2Exception("Cannot create unmanaged sprite when GL surface is lost");
		}

		this.managed = managed;
		available = false;
	}
	
	public abstract void recreate();
	
	public void dispose()
	{
		if (O2Director.instance.gl != null)
		{
			int texArr[] = { tex };
			GLES10.glDeleteTextures(1, texArr, 0);
			int vboArray[] = {	vboFullTexCood };
			GLES11.glDeleteBuffers(0, vboArray, 0);
		}
		available = false;
	}

	protected void createTexFromBitmap(Bitmap bmp)
	{
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		if (w > MAX_SIZE || h > MAX_SIZE) throw new Error("bitmap width/height larger than 1024");
		if (w <=0 || h <= 0) throw new IllegalArgumentException("bitmap width/height <= 0");

		// pad the size of bitmap to the power of 2
		int actualW = w;
		int actualH = h;
		int powOf2;

		for (powOf2=0; actualW>1; actualW>>=1) powOf2++;
		actualW = (1 << powOf2);
		if (w != actualW) actualW<<=1;

		for (powOf2=0; actualH>1; actualH>>=1) powOf2++;
		actualH = (1 << powOf2);
		if (h != actualH) actualH<<=1;
		
		if (w != actualW || h != actualH)
		{
			Bitmap standardBmp = Bitmap.createBitmap(bmp, 0, 0, actualW, actualH);
			createTexFromStandardBitmap(standardBmp, w, h);
		}
		else
			createTexFromStandardBitmap(bmp, w, h);
	}
	
	private void createTexFromStandardBitmap(Bitmap bmp, int width, int height)
	{
		texWidth = bmp.getWidth();
		texHeight = bmp.getHeight();
		this.width = width;
		this.height = height;

		int texArr[] = new int[1];
		GLES10.glGenTextures(1, texArr, 0);
		tex = texArr[0];
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, tex);
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_MIN_FILTER,
        		GLES10.GL_NEAREST);
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D,
        		GLES10.GL_TEXTURE_MAG_FILTER,
        		GLES10.GL_LINEAR);

        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_S,
        		GLES10.GL_CLAMP_TO_EDGE);
        GLES10.glTexParameterf(GLES10.GL_TEXTURE_2D, GLES10.GL_TEXTURE_WRAP_T,
        		GLES10.GL_CLAMP_TO_EDGE);

        //GLES10.glTexEnvf(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE,
        	//	GLES10.GL_REPLACE); 
		GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bmp, 0);
		
		int[] vboArr = new int[1];
		float fullTexCoods[] = {0.0f, 0.0f, (float)width/texWidth, 0.0f, 0.0f, (float)height/texHeight, (float)width/texWidth, (float)height/texHeight};
		FloatBuffer fullTexBuf = FloatBuffer.wrap(fullTexCoods);

		GLES11.glGenBuffers(1, vboArr, 0);
		vboFullTexCood = vboArr[0];
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCood);
		GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 8, fullTexBuf, GLES11.GL_STATIC_DRAW);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
	}

	public final void draw(int tagetX, int targetY)
	{
		// vertex coordinations
		vertCoods[0] = vertCoods[1] = vertCoods[3] = vertCoods[4] = 0;
		vertCoods[2] = vertCoods[6] = width;
		vertCoods[5] = vertCoods[7] = height;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		GL10 gl = O2Director.instance.gl;
		gl.glVertexPointer(2, GLES10.GL_FLOAT, 0, vertBuf);
		
		// texture coordinations
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCood);
		GLES11.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public final void draw(int srcX, int srcY, int srcWidth, int srcHeight, int tagetX, int targetY)
	{
		vertCoods[0] = vertCoods[4] = tagetX;
		vertCoods[1] = vertCoods[3] = targetY;
		vertCoods[2] = vertCoods[6] = tagetX + srcWidth;
		vertCoods[5] = vertCoods[7] = targetY + srcHeight;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		texCoods[0] = texCoods[4] = (float)srcX / texWidth;
		texCoods[1] = texCoods[3] = (float)srcY / texHeight;
		texCoods[2] = texCoods[6] =  (float)(srcX + srcWidth) / texWidth;
		texCoods[5] = texCoods[7] =  (float)(srcY + srcHeight) / texHeight;
		
		texBuf.position(0);
		texBuf.put(texCoods);
		texBuf.position(0);
	
		GL10 gl = O2Director.instance.gl;
		gl.glVertexPointer(2, GLES10.GL_FLOAT, 0, vertBuf);
		gl.glTexCoordPointer(2, GLES10.GL_FIXED, 0, texBuf);
		
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public final int getWidth()
	{
		return width;
	}
	
	public final int getHeight()
	{
		return height;	
	}
	
	public final boolean isManaged()
	{
		return managed;
	}
	
	@Override
	public boolean equals (Object o)
	{
		return this == o;
	}
}
