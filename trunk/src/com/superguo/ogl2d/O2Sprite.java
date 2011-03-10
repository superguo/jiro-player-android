package com.superguo.ogl2d;
import java.nio.*;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.*;

public abstract class O2Sprite {
	protected boolean available;
	protected boolean managed;
	protected int tex;
	protected int width;
	protected int height;
	private final static int fullTexCoods[] = {0, 0, 1, 0, 0, 1, 1, 1};
	private final static IntBuffer fullTexBuf = IntBuffer.wrap(fullTexCoods);
	private float vertCoods[] = new float[8];
	private float texCoods[] = new float[8];
	private FloatBuffer vertBuf = FloatBuffer.allocate(8);
	private FloatBuffer texBuf = FloatBuffer.allocate(8);
	
	protected O2Sprite(boolean managed)
	{
		this.managed = managed;
		available = false;
	}
	
	public void dispose()
	{
		int texArr[] = { tex };
		if (O2Director.inGlContext)
			GLES10.glDeleteTextures(1, texArr, 0);
		available = false;
	}

	protected void createTexFromBitmap(Bitmap bmp)
	{
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
	}
	
	public final void draw(GL10 gl, int tagetX, int targetY)
	{
		vertCoods[0] = vertCoods[1] = vertCoods[3] = vertCoods[4] = 0;
		vertCoods[2] = vertCoods[6] = width;
		vertCoods[5] = vertCoods[7] = height;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		gl.glVertexPointer(2, GLES10.GL_FLOAT, 0, vertBuf);
		gl.glTexCoordPointer(2, GLES10.GL_FIXED, 0, fullTexBuf);
		
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public final void draw(GL10 gl, int srcX, int srcY, int srcWidth, int srcHeight, int tagetX, int targetY)
	{
		vertCoods[0] = vertCoods[4] = tagetX;
		vertCoods[1] = vertCoods[3] = targetY;
		vertCoods[2] = vertCoods[6] = tagetX + srcWidth;
		vertCoods[5] = vertCoods[7] = targetY + srcHeight;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);
		
		texCoods[0] = texCoods[4] = (float)srcX / width;
		texCoods[1] = texCoods[3] = (float)srcY / height;
		texCoods[2] = texCoods[6] =  (float)(srcX + srcWidth) / width;
		texCoods[5] = texCoods[7] =  (float)(srcY + srcHeight) / height;
		
		texBuf.position(0);
		texBuf.put(texCoods);
		texBuf.position(0);
		
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
}
