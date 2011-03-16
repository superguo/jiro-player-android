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

	public void onSurfaceCreated(GL10 arg0, EGLConfig arg1) {
		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		GLES10.glDisable(GLES10.GL_DITHER);
	}
	
	public void onSurfaceChanged(GL10 arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	public void onDrawFrame(GL10 arg0) {
		// TODO Auto-generated method stub
		
	}
}
