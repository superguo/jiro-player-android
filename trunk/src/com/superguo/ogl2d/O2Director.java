package com.superguo.ogl2d;

import java.util.HashMap;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.*;
import android.opengl.*;
import android.view.*;
import android.content.*;

public final class O2Director extends GLSurfaceView {
	static O2Director instance;
	public final static boolean isSingleProcessor = 
		java.lang.Runtime.getRuntime().availableProcessors() == 1;
	GL10 gl;
	Context appContext;
	O2SpriteManager spriteManager;
	HashMap<Long, Paint> paints;
	O2InternalRenderer renderer;
	O2Scene currentScene;
	Object sceneSync;
	
	public O2Director createInstance(Context appContext)
	{
		instance = new O2Director(appContext);
		return instance;
	}
	
	O2Director(Context appContext)
	{
		super(appContext);
		
		if (!isSingleProcessor) sceneSync = new Object();
		spriteManager = new O2SpriteManager(appContext);
		paints = new HashMap<Long, Paint>(5);
		paints.put(new Long(0), new Paint());
		renderer = new O2InternalRenderer(this);
		setRenderer(renderer);
	}
	
	public final static O2Director getInstance()
	{
		return instance;
	}
	
	public O2SpriteManager getSpriteManager()
	{
		return spriteManager;
	}

	public synchronized long addPaint(Paint p)
	{
		long id = android.os.SystemClock.elapsedRealtime();
		paints.put(new Long(id), new Paint(p));
		return id;
	}

	public synchronized Paint getPaint(long id)
	{
		return paints.get(new Long(id));
	}
	
	public synchronized void removePaint(long id)
	{
		paints.remove(new Long(id));
	}

	public void setCurrentScene(O2Scene scene)
	{
		O2Scene orig = currentScene;
		if (orig!=null) orig.onLeavingScene();
		
		currentScene = scene;
		if (currentScene!=null) currentScene.onEnteringScene();
	}
	
	@Override
	public void onResume()
	{
		queueEvent(new Runnable() {
			
			@Override
			public void run() {
				if (isSingleProcessor)
				{
					if (currentScene!=null)
						currentScene.onResume();
				}
				else
				{
					synchronized (sceneSync) {
						if (currentScene!=null)
							currentScene.onResume();
					}
				}
			}
		});
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		queueEvent(new Runnable() {
			@Override
			public void run() {
				if (currentScene!=null)
					currentScene.onPause();
			}
		});
		super.onPause();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isSingleProcessor)
		{
			if (currentScene != null)
				currentScene.addMotionEvent(event);
		}
		else
		{
		synchronized (sceneSync) {
			if (currentScene != null)
				currentScene.addMotionEvent(event);
		}
		}
		
		return super.onTouchEvent(event);
	}

}
