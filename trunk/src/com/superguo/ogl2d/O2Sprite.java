package com.superguo.ogl2d;
import java.nio.*;
//import javax.microedition.khronos.opengles.*;
import android.graphics.*;
import android.opengl.*;

public abstract class O2Sprite {
	public final static int MAX_SIZE = 1024;
	protected boolean available;
	protected boolean managed;
	protected int tex;
	protected int width;
	protected int height;
	protected int texPowOf2Width;	
	protected int texPowOf2Height;
	protected int vboFullTexCood;

	private int vertCoods[] = new int[8];
	private int texCoods[] = new int[8];
	private IntBuffer vertBuf = 
		ByteBuffer.allocateDirect(32).order(null).asIntBuffer();
	private IntBuffer texBuf = 
		ByteBuffer.allocateDirect(32).order(null).asIntBuffer();
	
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
		width = bmp.getWidth();
		height = bmp.getHeight();
		
		if (width > MAX_SIZE || height > MAX_SIZE)
			throw new IllegalArgumentException("bitmap width/height larger than 1024");
		
		if (width <=0 || height <= 0)
			throw new IllegalArgumentException("bitmap width/height <= 0");
		
		boolean hasEnlarged = false;

		// pad the size of bitmap to the power of 2
		texPowOf2Width = 31 - Integer.numberOfLeadingZeros(width);
		if (Integer.lowestOneBit(width) != Integer.highestOneBit(width))
		{
			++texPowOf2Width;
			hasEnlarged = true;
		}

		texPowOf2Height = 31 - Integer.numberOfLeadingZeros(height);
		if (Integer.lowestOneBit(height) != Integer.highestOneBit(height))
		{
			++texPowOf2Height;
			hasEnlarged = true;
		}
		
		if (hasEnlarged)
		{
			Bitmap standardBmp = Bitmap.createBitmap(
					1<<texPowOf2Width,
					1<<texPowOf2Height,
					Bitmap.Config.ARGB_4444
					);
			standardBmp.setDensity(bmp.getDensity());
			Canvas canvas = new Canvas(standardBmp);
			canvas.setDensity(bmp.getDensity());
			canvas.drawBitmap(bmp, 0.0f, 0.0f, new Paint());
			createTexFromStandardBitmap(standardBmp);
			canvas = null;
			standardBmp.recycle();
			standardBmp = null;
		}
		else
			createTexFromStandardBitmap(bmp);
	}
	
	private void createTexFromStandardBitmap(Bitmap bmp)
	{
		// width, height, texPowOf2Width, texPowOf2Height
		// are done in createTexFromBitmap(Bitmap)
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

        GLES10.glTexEnvf(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE,
        		GLES10.GL_REPLACE); 
		GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bmp, 0);
		
		int[] vboArr = new int[1];
		int texCoodFixedW = width<<(16-texPowOf2Width);
		int texCoodFixedH = height<<(16-texPowOf2Height);
		int fullTexCoods[] = {
				0,
				0,
				texCoodFixedW,
				0,
				0,
				texCoodFixedH,
				texCoodFixedW,
				texCoodFixedH};
		IntBuffer fullTexBuf = ByteBuffer.allocateDirect(32).order(null).asIntBuffer();
		fullTexBuf.position(0);
		fullTexBuf.put(fullTexCoods);
		fullTexBuf.position(0);

		GLES11.glGenBuffers(1, vboArr, 0);
		vboFullTexCood = vboArr[0];
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCood);
		GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 32, fullTexBuf, GLES11.GL_STATIC_DRAW);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
	}

	public final void draw(int tagetX, int targetY)
	{
		// vertex coordinations
		int vertCoods[] = this.vertCoods;
		vertCoods[0] = vertCoods[4] = tagetX  << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = (tagetX + width)   << 16;
		vertCoods[5] = vertCoods[7] = (targetY + height) << 16;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		// Specify the vertex pointers
		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, vertBuf);
		
		// Specify the texture coordinations
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, tex);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCood);
		GLES11.glTexCoordPointer(2, GLES10.GL_FIXED, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public final void draw(int srcX, int srcY, int srcWidth, int srcHeight, int tagetX, int targetY)
	{
		int vertCoods[] = this.vertCoods;

		vertCoods[0] = vertCoods[4] = tagetX  << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = (tagetX + srcWidth)   << 16;
		vertCoods[5] = vertCoods[7] = (targetY + srcHeight) << 16;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		int texCoods[] = this.texCoods;

		texCoods[0] = texCoods[4] = srcX << (16-texPowOf2Width);
		texCoods[1] = texCoods[3] = srcY << (16-texPowOf2Height);
		texCoods[2] = texCoods[6] =  (srcX + srcWidth)  << (16-texPowOf2Width);
		texCoods[5] = texCoods[7] =  (srcY + srcHeight) << (16-texPowOf2Height);
		
		texBuf.position(0);
		texBuf.put(texCoods);
		texBuf.position(0);
	
		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, vertBuf);
		GLES10.glTexCoordPointer(2, GLES10.GL_FIXED, 0, texBuf);
		
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
