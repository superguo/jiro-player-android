package com.superguo.ogl2d;

import com.superguo.ogl2d.O2Director.Config;

import android.content.Context;
import android.view.MotionEvent;

class O2DirectorSP extends O2Director{

	O2DirectorSP(Context appContext, Config config) {
		super(appContext, config);
		// TODO Auto-generated constructor stub
	}

	public void setCurrentScene(O2Scene scene)
	{
		setCurrentSceneUnsafe(scene);
	}

	public O2Scene getCurrentScene()
	{
		return iCurrentScene;
	}

	@Override
	public synchronized boolean onTouchEvent(MotionEvent event) {
		if (iCurrentScene != null)
			iCurrentScene.addMotionEventUnsafe(event);
		
		return true;
	}
}
