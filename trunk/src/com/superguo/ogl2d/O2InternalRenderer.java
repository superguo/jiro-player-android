package com.superguo.ogl2d;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;
import android.opengl.*;

class O2InternalRenderer implements GLSurfaceView.Renderer{
	O2Director director;
	
	O2InternalRenderer(O2Director director)
	{
		this.director = director;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		director.gl = gl;
		
		GLES10.glClearColorx(0, 0, 0, 0);
		
		// enable vertex array and texture 2d
		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
		GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		
		// improve the performance 
		GLES10.glDisable(GLES10.GL_DITHER);
		GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);
		GLES10.glShadeModel(GLES10.GL_FLAT);

        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);

        director.spriteManager.recreateManaged();
	}
	
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		O2Director.Config config = director.config;
		GLES10.glViewport(0, 0, width, height);
		// for a fixed camera, set the projection too
		// float ratio = (float) width / height;
		GLES10.glMatrixMode(GL10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		//GLES10.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		GLES10.glOrthof(0.0f, width,0.0f,  height, 0.0f, 1.0f);

        // map to normal screen coordination
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        GLES10.glTranslatef(0.375f, height + 0.375f, 0.0f);
        GLES10.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
		if (config.width > 0 && config.height > 0)
		{
			/* (xLogical + xOffset) * scale  = xDeivce
			 * (yLogical + yOffset) * scale  = yDeivce
			 */
			float scale;
			float xOffset;
			float yOffset;
			
			if (config.width * height > width * config.height)
			{
				scale = (float)width/config.width;
				yOffset = (height/scale - config.height) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);				
				GLES10.glTranslatef(0.0f, yOffset, 0.0f);
				
				director.internalConfig.scale = scale;
				director.internalConfig.xOffset = 0.0f;
				director.internalConfig.yOffset = yOffset;
			}
			else
			{
				scale = (float)height/config.height;
				xOffset = (width/scale - config.width) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);
				GLES10.glTranslatef(xOffset, 0.0f, 0.0f);
				
				director.internalConfig.scale = scale;
				director.internalConfig.xOffset = xOffset;
				director.internalConfig.yOffset = 0.0f;
			}
			
			// set the clipping rect
			GLES10.glEnable(GLES10.GL_SCISSOR_TEST);
			GLES10.glScissor(
					(int)director.toXDevice(0),
					(int)director.toYDevice(0),
					(int)director.toXDevice(config.width),
					(int)director.toYDevice(config.height));
		}
	}

	public void onDrawFrame(GL10 gl) {
		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);
		if (director.currentScene!=null)
			director.currentScene.draw(gl);
	}
}
