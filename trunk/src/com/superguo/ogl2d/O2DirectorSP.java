package com.superguo.ogl2d;

import android.content.Context;
import android.view.MotionEvent;

class O2DirectorSP extends O2Director{

	O2DirectorSP(Context appContext) {
		super(appContext);
		// TODO Auto-generated constructor stub
	}

	public void setCurrentScene(O2Scene scene)
	{
		setCurrentSceneUnsafe(scene);
	}

	public O2Scene getCurrentScene()
	{
		return currentScene;
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent event) {
		if (currentScene != null)
			currentScene.addMotionEventUnsafe(event);
		
		return true;
	}
}