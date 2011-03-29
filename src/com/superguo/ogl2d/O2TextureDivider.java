package com.superguo.ogl2d;

import java.nio.*;
import java.util.*;

import android.graphics.*;
import android.opengl.GLES10;
import android.opengl.GLES11;

public class O2TextureDivider{
	protected O2Texture texture;
	protected boolean created;
	protected final static int CREATE_FROM_ROWS_AND_COLS = 1;
	protected int createMethod;
	protected int createArg0;
	protected int createArg1;
	
	protected int vboFullTexCoods[];
	protected Point[] sizes;
	private int vertCoods[] = new int[8];
	private IntBuffer vertBuf = 
		ByteBuffer.allocateDirect(32).order(null).asIntBuffer();

	protected O2TextureDivider(O2Texture texture, int createMethod, int createArg0, int createArg1, boolean doVerification)
	{
		this.texture = texture;
		this.createMethod = createMethod;
		this.createArg0 = createArg0;
		this.createArg1 = createArg1;
		if (doVerification) verify();
	}

	protected void verify()
	{
		switch (createMethod)
		{
		case CREATE_FROM_ROWS_AND_COLS:
			if (createArg0<0 || createArg1<0) throw new IllegalArgumentException();
			return;
			
		default:
			throw new UnsupportedOperationException();
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
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, texture.tex);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCoods[index]);
		GLES11.glTexCoordPointer(2, GLES10.GL_FIXED, 0, 0);
		GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);

		// draw
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
	}

	protected void create() {
		switch (createMethod)
		{
		case CREATE_FROM_ROWS_AND_COLS:
			createFromRowsAndCols(createArg0, createArg1);
			return;

		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private void createFromRowsAndCols(int rows, int cols)
	{
		int i, j, w, h;
		w = texture.width/cols;
		h = texture.height/rows;
		int count = rows*cols;
		if (count==0) return;
		
		// block sizes, using Point as size type
		sizes = new Point[count];
		Point fixedSize = new Point(w, h);
		Arrays.fill(sizes, fixedSize);
		
		// VBO
		vboFullTexCoods = new int[count];
		int fullTexCoods[] = new int[8];
		IntBuffer fullTexBuf = ByteBuffer.allocateDirect(32).order(null).asIntBuffer();
		GLES11.glGenBuffers(count, vboFullTexCoods, 0);
		int texPowOf2Width = texture.texPowOf2Width;
		int texPowOf2Height = texture.texPowOf2Height;
		for (i=0; i<rows; ++i)
			for (j=0; j<cols; ++j)
			{
				int left = j*w;
				int top = i*h;
				int right = left + w;
				int bottom = top + h;
				fullTexCoods[0] = fullTexCoods[4] = left 	<< (16-texPowOf2Width);
				fullTexCoods[1] = fullTexCoods[3] = top 	<< (16-texPowOf2Height);
				fullTexCoods[2] = fullTexCoods[6] = right  	<< (16-texPowOf2Width);
				fullTexCoods[5] = fullTexCoods[7] = bottom 	<< (16-texPowOf2Height);
				fullTexBuf.position(0);
				fullTexBuf.put(fullTexCoods);
				fullTexBuf.position(0);
				GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, vboFullTexCoods[i*cols+j]);
				GLES11.glBufferData(GLES11.GL_ARRAY_BUFFER, 32, fullTexBuf, GLES11.GL_STATIC_DRAW);
				GLES11.glBindBuffer(GLES11.GL_ARRAY_BUFFER, 0);
			}
		created = true;
	}
}
