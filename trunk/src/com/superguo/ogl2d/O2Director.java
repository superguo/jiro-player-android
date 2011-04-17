package com.superguo.ogl2d;

import java.util.*;
import java.util.concurrent.*;

import javax.microedition.khronos.egl.*;
import javax.microedition.khronos.opengles.*;

import android.graphics.*;
import android.opengl.*;
import android.os.Handler;
import android.view.*;
import android.content.*;

public class O2Director extends SurfaceView implements SurfaceHolder.Callback, Runnable {
	static O2Director instance;
	public final static boolean isSingleProcessor = 
		java.lang.Runtime.getRuntime().availableProcessors() == 1;
	Context				iAppContext;
	GL10 				iGl;
	O2TextureManager 	iTextureManager;
	O2SpriteManager 	iSpriteManager;
	Map<Long, Paint> 	iPaints;
	protected O2Scene 	iCurrentScene;
	protected Object 	iSceneAccessMutex;
	
	Config 				iConfig;
	InternalConfig		iInternalConfig;
	
	EGL10 				iEGL;
	EGLDisplay 			iEGLDisplay;
	EGLConfig			iEGLConfig;
	EGLSurface			iEGLSurface;
	EGLContext			iEGLContext;
	
	private Handler 	iDrawHandler;

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
		instance = new O2Director(appContext, config);
		return instance;
	}
	
	O2Director(Context appContext, Config config)
	{
		super(appContext);
		getHolder().setFormat(PixelFormat.RGB_565);
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
		Paint defaultPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
		defaultPaint.setColor(Color.rgb(255, 255, 255));
		defaultPaint.setAntiAlias(true);
		iPaints.put(new Long(0), defaultPaint);
		iDrawHandler = new Handler();
		getHolder().addCallback(this);
	}
	
	public final static O2Director getInstance()
	{	return instance;	}
	
	public final void dispose()
	{
		setCurrentScene(null);
		instance = null;
	}
	
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

	public final void setCurrentScene(O2Scene scene)
	{
		setCurrentSceneUnsafe(scene);
	}

	public final O2Scene getCurrentScene()
	{
		return iCurrentScene;
	}
	
	protected final void setCurrentSceneUnsafe(O2Scene scene)
	{
		O2Scene orig = iCurrentScene;
		if (orig!=null)
			orig.onLeavingScene();
		
		iCurrentScene = scene;
		if (iCurrentScene!=null)
			iCurrentScene.onEnteringScene();
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
			return (xLogical + iInternalConfig.xOffset) * iInternalConfig.scale;
		else
			return xLogical;
	}
	
	public float toYDevice(float yLogical)
	{
		if (Math.abs(iInternalConfig.scale) > 1e-5)
			return (yLogical + iInternalConfig.yOffset) * iInternalConfig.scale;
		else
			return yLogical;
	}
	
	/**
	 * Must be called when app is to be sresumed
	 */
	public void onResume()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
			scene.onResume();
	}
	
	/**
	 * Must be called when app is to be paused
	 */
	public void onPause()
	{
		O2Scene scene = getCurrentScene();
		if (scene!=null)
			scene.onPause();
	}
	
	
	public void surfaceCreated(SurfaceHolder holder) {
		if (iGl==null)
		{
			createGl();
			sizeChanged(getWidth(), getHeight());
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if (iGl!=null)
		{
			sizeChanged(width, height);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		destroyGl();
	}

	public void run() {
		if (iGl!=null)
		{
			drawFrame();
			if (!iEGL.eglSwapBuffers(iEGLDisplay, iEGLSurface))
			{
				destroyGl();
				createGl();
			}
		}
		iDrawHandler.post(this);
	}
/*
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (iCurrentScene!=null)
			return iCurrentScene.onTouchEvent(e);
		else
			return false;
	}
*/
	public boolean fastTouchEvent(MotionEvent e) {
		if (iCurrentScene!=null)
			return iCurrentScene.onTouchEvent(e);
		else
			return false;
	}
	
	private final void createGl()
	{
		EGL10 egl = iEGL = (EGL10)EGLContext.getEGL();
		iEGLDisplay = iEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		int[] eglVersion = {0, 0};
		egl.eglInitialize(iEGLDisplay, eglVersion);
		
		int attribs[] = {
				EGL10.EGL_SURFACE_TYPE, EGL10.EGL_WINDOW_BIT,
				EGL10.EGL_BLUE_SIZE, 5,
				EGL10.EGL_GREEN_SIZE, 6,
				EGL10.EGL_RED_SIZE, 5,
				EGL10.EGL_NONE
		};
		EGLConfig[] eglConfigs = new EGLConfig[1];
		int[] numElgConfigs = new int[1];
		egl.eglChooseConfig(iEGLDisplay, attribs, eglConfigs, 1, numElgConfigs);
		iEGLConfig = eglConfigs[0];
		
		iEGLSurface = egl.eglCreateWindowSurface(iEGLDisplay, iEGLConfig, getHolder(), null);
		iEGLContext = egl.eglCreateContext(iEGLDisplay, iEGLConfig, EGL10.EGL_NO_CONTEXT, null);
		egl.eglMakeCurrent(iEGLDisplay, iEGLSurface, iEGLSurface, iEGLContext);
		iGl = (GL10)iEGLContext.getGL();
		
		GLES10.glClearColorx(0, 0, 0, 0);
		
		// enable vertex array and texture 2d
		GLES10.glEnableClientState(GLES10.GL_VERTEX_ARRAY);
		GLES10.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		GLES10.glEnable(GLES10.GL_TEXTURE_2D);
		
		// improve the performance 
		GLES10.glDisable(GLES10.GL_DITHER);
		GLES10.glHint(GLES10.GL_PERSPECTIVE_CORRECTION_HINT, GLES10.GL_FASTEST);
		GLES10.glShadeModel(GLES10.GL_FLAT);

        GLES10.glEnable(GLES10.GL_BLEND);
        GLES10.glBlendFunc(GLES10.GL_SRC_ALPHA, GLES10.GL_ONE_MINUS_SRC_ALPHA);

        iTextureManager.recreateManaged();
        
		iDrawHandler.post(this);
	}
	
	private final void sizeChanged(int width, int height)
	{
		O2Director.Config config = iConfig;
		GLES10.glViewport(0, 0, width, height);
		// for a fixed camera, set the projection too
		// float ratio = (float) width / height;
		GLES10.glMatrixMode(GL10.GL_PROJECTION);
		GLES10.glLoadIdentity();
		//GLES10.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		GLES10.glOrthof(0.0f, width,0.0f,  height, 0.0f, 1.0f);

        // map to normal screen coordination
        GLES10.glMatrixMode(GLES10.GL_MODELVIEW);
        GLES10.glLoadIdentity();
        // Magic offsets to promote consistent rasterization.
        GLES10.glTranslatef(0.375f, height + 0.375f, 0.0f);
        GLES10.glRotatef(180.0f, 1.0f, 0.0f, 0.0f);
		if (config.width > 0 && config.height > 0)
		{
			/* (xLogical + xOffset) * scale  = xDeivce
			 * (yLogical + yOffset) * scale  = yDeivce
			 */
			float scale;
			float xOffset;
			float yOffset;
			
			if (config.width * height > width * config.height)
			{
				scale = (float)width/config.width;
				yOffset = (height/scale - config.height) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);				
				GLES10.glTranslatef(0.0f, yOffset, 0.0f);
				
				iInternalConfig.scale = scale;
				iInternalConfig.xOffset = 0.0f;
				iInternalConfig.yOffset = yOffset;
			}
			else
			{
				scale = (float)height/config.height;
				xOffset = (width/scale - config.width) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);
				GLES10.glTranslatef(xOffset, 0.0f, 0.0f);
				
				iInternalConfig.scale = scale;
				iInternalConfig.xOffset = xOffset;
				iInternalConfig.yOffset = 0.0f;
			}
			
			// set the clipping rect
			GLES10.glEnable(GLES10.GL_SCISSOR_TEST);
			GLES10.glScissor(
					(int)toXDevice(0),
					(int)toYDevice(0),
					(int)toXDevice(config.width),
					(int)toYDevice(config.height));
		}
		if (iCurrentScene!=null) iCurrentScene.onSizeChanged();
	}

	private final void drawFrame()
	{
		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);
		O2Scene s = iCurrentScene;
		if (s!=null)	s.preDraw(iGl);
		iSpriteManager.drawAllSprites(iGl);
		if (s!=null)	s.postDraw(iGl);
	}
	
	private final void destroyGl()
	{
		iDrawHandler.removeCallbacks(this);
		iTextureManager.markAllNA();
		if (iEGLSurface!=null && iEGLSurface!=EGL10.EGL_NO_SURFACE)
		{
			iEGL.eglMakeCurrent(iEGLDisplay, EGL10.EGL_NO_SURFACE,
					                         EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			iEGL.eglDestroySurface(iEGLDisplay, iEGLSurface);
		}
		iEGLSurface = null;
		iGl = null;
	}
}
