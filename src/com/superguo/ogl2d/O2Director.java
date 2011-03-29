package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.*;
import android.opengl.*;
import android.view.*;
import android.content.*;

public abstract class O2Director extends GLSurfaceView {
	static O2Director instance;
	public final static boolean isSingleProcessor = 
		java.lang.Runtime.getRuntime().availableProcessors() == 1;
	GL10 gl;
	O2TextureManager spriteManager;
	Map<Long, Paint> paints;
	O2InternalRenderer renderer;
	protected O2Scene currentScene;
	protected Object sceneAccessMutex;
	private SceneEventQ sceneEventQ;
	Config config;
	InternalConfig internalConfig;
	
	public static class Config
	{
		public int width;
		public int height;
		public Config(int width, int height)
		{
			this.width = width;
			this.height = height;
		}
		public Config()
		{
			width = height = 0;
		}
	}

	static class InternalConfig
	{
		public float scale;
		public float xOffset;
		public float yOffset;
	}
	
	public static O2Director createInstance(Context appContext, Config config)
	{
		instance = isSingleProcessor ?
				new O2DirectorSP(appContext, config)
		:
				new O2DirectorMP(appContext, config);
		return instance;
	}
	
	O2Director(Context appContext, Config config)
	{
		super(appContext);
		this.config = config==null ? new Config() : config;
		internalConfig = new InternalConfig();
		
		if (!isSingleProcessor) sceneAccessMutex = new Object();
		spriteManager = new O2TextureManager(appContext);
		paints = isSingleProcessor ?
			new HashMap<Long, Paint>(5)
				:
			new ConcurrentHashMap<Long, Paint>(5);
		paints.put(new Long(0), new Paint());
		sceneEventQ = new SceneEventQ();
		renderer = new O2InternalRenderer(this);
		setRenderer(renderer);
	}
	
	public final static O2Director getInstance()
	{
		return instance;
	}
	
	public final O2TextureManager getSpriteManager()
	{
		return spriteManager;
	}

	public long addPaint(Paint p)
	{
		long id = android.os.SystemClock.elapsedRealtime();
		paints.put(new Long(id), new Paint(p));
		return id;
	}

	public Paint getPaint(long id)
	{
		return paints.get(new Long(id));
	}
	
	public void removePaint(long id)
	{
		paints.remove(new Long(id));
	}

	public abstract void setCurrentScene(O2Scene scene);

	public abstract O2Scene getCurrentScene();
	
	protected final void setCurrentSceneUnsafe(O2Scene scene)
	{
		O2Scene orig = currentScene;
		if (orig!=null)
			queueEvent(sceneEventQ.add(orig, SceneEventQ.EVENT_TYPE_ON_LEAVING_SCENE));
		
		currentScene = scene;
		if (currentScene!=null)
			queueEvent(sceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_ENTERING_SCENE));
	}
	
	public float toXLogical(float xDevice)
	{
		if (Math.abs(internalConfig.scale) > 1e-5)
			return xDevice / internalConfig.scale - internalConfig.xOffset;
		else
			return xDevice;
	}

	public float toYLogical(float yDevice)
	{
		if (Math.abs(internalConfig.scale) > 1e-5)
			return yDevice / internalConfig.scale - internalConfig.yOffset;
		else
			return yDevice;
	}

	public float toXDevice(float xLogical)
	{
		if (Math.abs(internalConfig.scale) > 1e-5)
			return xLogical * (internalConfig.scale + internalConfig.xOffset);
		else
			return xLogical;
	}
	
	public float toYDevice(float yLogical)
	{
		if (Math.abs(internalConfig.scale) > 1e-5)
			return yLogical * (internalConfig.scale + internalConfig.yOffset);
		else
			return yLogical;
	}
	
	@Override
	public void onResume()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
			queueEvent(sceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_RESUME));
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
		{
			queueEvent(sceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_PAUSE));
		}
		super.onPause();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		super.surfaceDestroyed(holder);
		spriteManager.markAllNA();
		gl = null;
	}
}

class SceneEventQ implements Runnable
{
	final static int EVENT_TYPE_ON_PAUSE = 1;
	final static int EVENT_TYPE_ON_RESUME = 2;
	final static int EVENT_TYPE_ON_ENTERING_SCENE = 3;
	final static int EVENT_TYPE_ON_LEAVING_SCENE = 4;
	public final static int MAX_EVENT = 100; 
	O2Scene sceneQueue[];
	int eventQueue[];
	int eventIndex;
	
	public SceneEventQ()
	{
		sceneQueue = new O2Scene[MAX_EVENT];
		eventQueue = new int[MAX_EVENT];
		eventIndex = 0;
	}
	
	public SceneEventQ add(O2Scene scene, int eventType)
	{
		if (eventIndex < MAX_EVENT)
		{
			sceneQueue[eventIndex] = scene;
			eventQueue[eventIndex++] = eventType;
		}
		return this;
	}
	
	public void run() {
		if (eventIndex>0)
		{
			--eventIndex;
			switch (eventQueue[eventIndex])
			{
			case EVENT_TYPE_ON_PAUSE:
				sceneQueue[eventIndex].onPause();
				break;
				
			case EVENT_TYPE_ON_RESUME:
				sceneQueue[eventIndex].onResume();
				break;
				
			case EVENT_TYPE_ON_ENTERING_SCENE:
				sceneQueue[eventIndex].onEnteringScene();
				break;
				
			case EVENT_TYPE_ON_LEAVING_SCENE:
				sceneQueue[eventIndex].onLeavingScene();
				break;

			default:;
			}
		}
		
	}
	
}
