package com.superguo.ogl2d;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.*;
import android.view.*;

public class O2Director extends GLSurfaceView {
	android.content.Context appContext;
	O2InternalRenderer renderer;
	O2SpriteManager spriteManager;
	O2Scene currentScene;
	
	public O2Director(android.content.Context appContext)
	{
		super(appContext);
		
		renderer = new O2InternalRenderer(this);
		setRenderer(renderer);
	}
	
	public void setCurrentScene(O2Scene scene)
	{
		O2Scene orinal = currentScene;
		if (orinal!=null) orinal.onLeavingScene();
		
		currentScene = scene;
		if (currentScene!=null) currentScene.onEnteringScene();
	}
	
	@Override
	public void onResume()
	{
		if (currentScene!=null) super.onResume();
	}
	
	@Override
	public void onPause()
	{
		if (currentScene!=null) super.onPause();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}

}
