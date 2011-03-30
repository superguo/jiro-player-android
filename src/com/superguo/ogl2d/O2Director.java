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
	Context				iAppContext;
	GL10 				iGl;
	O2TextureManager 	iTextureManager;
	O2SpriteManager 	iSpriteManager;
	Map<Long, Paint> 	iPaints;
	O2InternalRenderer 	iRenderer;
	protected O2Scene 	iCurrentScene;
	protected Object 	iSceneAccessMutex;
	private SceneEventQ iSceneEventQ;
	Config 				iConfig;
	InternalConfig		iInternalConfig;

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
		iAppContext = appContext;
		this.iConfig = config==null ? new Config() : config;
		iInternalConfig = new InternalConfig();
		
		if (!isSingleProcessor) iSceneAccessMutex = new Object();
		iTextureManager = new O2TextureManager();
		iSpriteManager = new O2SpriteManager();
		iPaints = isSingleProcessor ?
			new HashMap<Long, Paint>(5)
				:
			new ConcurrentHashMap<Long, Paint>(5);
		iPaints.put(new Long(0), new Paint());
		iSceneEventQ = new SceneEventQ();
		iRenderer = new O2InternalRenderer(this);
		setRenderer(iRenderer);
	}
	
	public final static O2Director getInstance()
	{	return instance;	}
	
	public final O2TextureManager getTextureManager()
	{	return iTextureManager;	}
	
	public final O2SpriteManager getSpriteManager()
	{	return iSpriteManager;	}

	public long addPaint(Paint p)
	{
		long id = android.os.SystemClock.elapsedRealtime();
		iPaints.put(new Long(id), new Paint(p));
		return id;
	}

	public Paint getPaint(long id)
	{
		return iPaints.get(new Long(id));
	}
	
	public void removePaint(long id)
	{
		iPaints.remove(new Long(id));
	}

	public abstract void setCurrentScene(O2Scene scene);

	public abstract O2Scene getCurrentScene();
	
	protected final void setCurrentSceneUnsafe(O2Scene scene)
	{
		O2Scene orig = iCurrentScene;
		if (orig!=null)
			queueEvent(iSceneEventQ.add(orig, SceneEventQ.EVENT_TYPE_ON_LEAVING_SCENE));
		
		iCurrentScene = scene;
		if (iCurrentScene!=null)
			queueEvent(iSceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_ENTERING_SCENE));
	}
	
	public float toXLogical(float xDevice)
	{
		if (Math.abs(iInternalConfig.scale) > 1e-5)
			return xDevice / iInternalConfig.scale - iInternalConfig.xOffset;
		else
			return xDevice;
	}

	public float toYLogical(float yDevice)
	{
		if (Math.abs(iInternalConfig.scale) > 1e-5)
			return yDevice / iInternalConfig.scale - iInternalConfig.yOffset;
		else
			return yDevice;
	}

	public float toXDevice(float xLogical)
	{
		if (Math.abs(iInternalConfig.scale) > 1e-5)
			return xLogical * (iInternalConfig.scale + iInternalConfig.xOffset);
		else
			return xLogical;
	}
	
	public float toYDevice(float yLogical)
	{
		if (Math.abs(iInternalConfig.scale) > 1e-5)
			return yLogical * (iInternalConfig.scale + iInternalConfig.yOffset);
		else
			return yLogical;
	}
	
	@Override
	public void onResume()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
			queueEvent(iSceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_RESUME));
		super.onResume();
	}
	
	@Override
	public void onPause()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
		{
			queueEvent(iSceneEventQ.add(scene, SceneEventQ.EVENT_TYPE_ON_PAUSE));
		}
		super.onPause();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		super.surfaceDestroyed(holder);
		iTextureManager.markAllNA();
		iGl = null;
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
