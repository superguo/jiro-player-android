package com.superguo.ogl2d;

import java.nio.*;

import android.graphics.*;
import android.opengl.GLES10;
import android.opengl.GLES11;

public class O2SpriteSheet {
	protected int vboFullTexCoods[];
	protected Point[] sizes;
	protected O2Sprite sprite;
	private int vertCoods[] = new int[8];
	private IntBuffer vertBuf = 
		ByteBuffer.allocateDirect(32).order(null).asIntBuffer();

	public O2SpriteSheet(O2Sprite sprite, Rect[] rects)
	{
		this.sprite = sprite;
		// checks the number of elements in rects
		int count = rects.length;
		if (count<0) throw new IllegalArgumentException();
		if (count==0) return;
		
		// intermediate variables
		vboFullTexCoods = new int[count];
		sizes = new Point[count];
		int fullTexCoods[] = new int[8];
		IntBuffer fullTexBuf = 
			ByteBuffer.allocateDirect(32).order(null).asIntBuffer();

		GLES11.glGenBuffers(count, vboFullTexCoods, 0);
		int texPowOf2Width = sprite.texPowOf2Width;
		int texPowOf2Height = sprite.texPowOf2Height;
		int i;
		for (i=0; i<count; ++i)
		{
			sizes[i] = new Point(
					rects[i].right - rects[i].left,
					rects[i].bottom - rects[i].top);
			fullTexCoods[0] = fullTexCoods[4] = rects[i].left << (16-texPowOf2Width);
			fullTexCoods[1] = fullTexCoods[3] = rects[i].top << (16-texPowOf2Height);
			fullTexCoods[2] = fullTexCoods[6] = rects[i].right  << (16-texPowOf2Width);
			fullTexCoods[5] = fullTexCoods[7] = rects[i].bottom << (16-texPowOf2Height);
			fullTexBuf.position(0);
			fullTexBuf.put(fullTexCoods);
			fullTexBuf.position(0);
			GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCoods[i]);
			GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 32, fullTexBuf, GLES11.GL_STATIC_DRAW);
			GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
		}
	}

	public final void draw(int index, int targetX, int targetY)
	{
		int vertCoods[] = this.vertCoods;

		vertCoods[0] = vertCoods[4] = targetX << 16;
		vertCoods[1] = vertCoods[3] = targetY << 16;
		vertCoods[2] = vertCoods[6] = (targetX + sizes[index].x) << 16;
		vertCoods[5] = vertCoods[7] = (targetY + sizes[index].y) << 16;
		
		vertBuf.position(0);
		vertBuf.put(vertCoods);
		vertBuf.position(0);

		// Specify the vertex pointers
		GLES10.glVertexPointer(2, GLES10.GL_FIXED, 0, vertBuf);
		
		// Specify the texture coordinations
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, sprite.tex);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCoods[index]);
		GLES11.glTexCoordPointer(2, GLES10.GL_FIXED, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}
}
