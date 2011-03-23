package com.superguo.ogl2d;

import android.content.Context;
import android.view.MotionEvent;

class O2DirectorMP extends O2Director {

	O2DirectorMP(Context appContext) {
		super(appContext);
		// TODO Auto-generated constructor stub
	}

	public void setCurrentScene(O2Scene scene)
	{
		synchronized (sceneAccessMutex) {
			setCurrentSceneUnsafe(scene);
		}
	}

	public O2Scene getCurrentScene()
	{
		synchronized (sceneAccessMutex) {
			return currentScene;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		synchronized (sceneAccessMutex) {
			if (currentScene != null)
				currentScene.addMotionEventUnsafe(event);
		}
		
		return super.onTouchEvent(event);
	}
}
