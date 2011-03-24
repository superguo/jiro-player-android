package com.superguo.ogl2d;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES10;
import android.opengl.GLES11;
import android.opengl.GLSurfaceView;

class O2InternalRenderer implements GLSurfaceView.Renderer{
	O2Director director;
	
	O2InternalRenderer(O2Director director)
	{
		this.director = director;
	}

	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		director.gl = gl;
		// enable vetex array and texure 2d
		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
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
		GLES10.glViewport(0, 0, width, height);
		// for a fixed camera, set the projection too
		float ratio = (float) width / height;
		GLES10.glMatrixMode(GL10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		GLES10.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
        GLES10.glOrthof(0.0f, width,0.0f,  height, 0.0f, 1.0f);

        // map to normal screen coodination
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        GLES10.glTranslatef(0.375f, height + 0.375f, 0.0f);
        GLES10.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
	}

	public void onDrawFrame(GL10 gl) {
		if (director.currentScene!=null)
			director.currentScene.draw(gl);
	}
}
