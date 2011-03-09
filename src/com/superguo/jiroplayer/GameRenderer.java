package com.superguo.jiroplayer;

import java.nio.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import android.opengl.*;
import android.content.Context;
import android.graphics.*;

public class GameRenderer implements GLSurfaceView.Renderer {
	
	Context context;
	int tex[];
	int texComboNumberIndex = 0;
    int width;
    int height;
    int score;

	public GameRenderer(Context context)
	{
		this.context = context;
		tex = new int[10];
		score = 0;
	}
	
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		GLES10.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.combonumber);
		GLES10.glGenTextures(1, tex, 0);
		GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, tex[texComboNumberIndex]);
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
		GLUtils.texImage2D(GLES10.GL_TEXTURE_2D, 0, bm, 0);
		bm.recycle();
	}

	public void onSurfaceChanged(GL10 gl, int width, int height) {
		this.width = width;
		this.height = height;
	     GLES10.glViewport(0, 0, width, height);
	     // for a fixed camera, set the projection too
	     float ratio = (float) width / height;
	     GLES10.glMatrixMode(GLES10.GL_PROJECTION);
	     GLES10.glLoadIdentity();
	     GLES10.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
	}
	
	public void onDrawFrame(GL10 gl) {
		/*
         * By default, OpenGL enables features that improve quality
         * but reduce performance. One might want to tweak that
         * especially on software renderer.
         */
        GLES10.glDisable(GLES10.GL_DITHER);

        GLES10.glTexEnvx(GLES10.GL_TEXTURE_ENV, GLES10.GL_TEXTURE_ENV_MODE,
        		GLES10.GL_MODULATE);

        /*
         * Usually, the first thing one might want to do is to clear
         * the screen. The most efficient way of doing this is to use
         * glClear().
         */
        GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT | GLES10.GL_DEPTH_BUFFER_BIT);

        /*
         * Now we're ready to draw some 3D objects
         */

        GLES10.glBindTexture(GLES10.GL_TEXTURE_2D, tex[texComboNumberIndex]);
        GLES10.glShadeModel(GLES10.GL_FLAT);
        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);

        GLES10.glMatrixMode(GLES10.GL_PROJECTION);
        GLES10.glPushMatrix();
        GLES10.glLoadIdentity();
        GLES10.glOrthof(0.0f, width,0.0f,  height, 0.0f, 1.0f);
       
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glPushMatrix();
        GLES10.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        GLES10.glTranslatef(0.375f, height + 0.375f, 0.0f);
        GLES10.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);

		// GLES10.glDisable(GLES10.GL_CULL_FACE);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		// GLES10.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
		GLES10.glEnableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);

		/*
		float vArr[] = { 0.0f, 0.0f, 100.0f, 0.0f, 0.0f, 30.0f, 100.0f, 30.0f };
		FloatBuffer vBuf = FloatBuffer.wrap(vArr);
		vBuf.position(0);
		gl.glVertexPointer(2, GLES10.GL_FLOAT, 0, vBuf);

		float tArr[] = { 0f, 0f, 1f, 0f, 0f, 1f, 1f, 1f };
		FloatBuffer tBuf = FloatBuffer.wrap(tArr);
		tBuf.position(0);
		gl.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, tBuf);
		GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
*/
		score += 100;
		drawNumber(gl, score, 10, 10);
		
		GLES10.glDisableClientState(GLES10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glDisableClientState(GLES10.GL_VERTEX_ARRAY);

		GLES10.glDisable(GLES10.GL_BLEND);
        GLES10.glMatrixMode(GLES10.GL_PROJECTION);
        GLES10.glPopMatrix();
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glPopMatrix();
	}
	
	void drawNumber(GL10 gl, int number, float x, float y)
	{
		final float GAP = 1.0f;
		final float DIGIT_WIDTH = 17;
		final float DIGIT_HEIGHT = 30;
		int i;
		String numberStr = Integer.toString(number);
		int len = numberStr.length();
		int digit;
		float vArr[] = new float[8];
		float tArr[] = new float[8];
		FloatBuffer vBuf = FloatBuffer.allocate(8);
		FloatBuffer tBuf = FloatBuffer.allocate(8);

		for (i=0; i<len; ++i)
		{
			digit = numberStr.charAt(i) - '0';
			float x1 = x + (DIGIT_WIDTH + GAP) * i;
			float x2 = x1 + DIGIT_WIDTH;
			float y1 = y;
			float y2 = y1 + DIGIT_HEIGHT;
			vArr[0] = x1;
			vArr[1] = y1;
			vArr[2] = x2;
			vArr[3] = y1;
			vArr[4] = x1;
			vArr[5] = y2;
			vArr[6] = x2;
			vArr[7] = y2;
			
			tArr[0] = digit * 0.1f;
			tArr[1] = 0.0f;
			tArr[2] = tArr[0] + 0.1f;
			tArr[3] = 0.0f;
			tArr[4] = digit * 0.1f;
			tArr[5] = 1.0f;
			tArr[6] = tArr[0] + 0.1f;
			tArr[7] = 1.0f;

			vBuf.position(0);
			vBuf.put(vArr);
			vBuf.position(0);
			
			tBuf.position(0);
			tBuf.put(tArr);
			tBuf.position(0);

			gl.glVertexPointer(2, GLES10.GL_FLOAT, 0, vBuf);
			gl.glTexCoordPointer(2, GLES10.GL_FLOAT, 0, tBuf);
			
			GLES10.glDrawArrays(GLES10.GL_TRIANGLE_STRIP, 0, 4);
		}
	}
}
