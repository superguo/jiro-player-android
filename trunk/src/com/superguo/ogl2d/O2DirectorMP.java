package com.superguo.ogl2d;

import com.superguo.ogl2d.O2Director.Config;

import android.content.Context;
import android.view.MotionEvent;

class O2DirectorMP extends O2Director {

	O2DirectorMP(Context appContext, Config config) {
		super(appContext, config);
		// TODO Auto-generated constructor stub
	}

	public void setCurrentScene(O2Scene scene)
	{
		synchronized (iSceneAccessMutex) {
			setCurrentSceneUnsafe(scene);
		}
	}

	public O2Scene getCurrentScene()
	{
		synchronized (iSceneAccessMutex) {
			return iCurrentScene;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (iSceneAccessMutex) {
			if (iCurrentScene != null)
				iCurrentScene.addMotionEventUnsafe(event);
		}
		
		return super.onTouchEvent(event);
	}
}
