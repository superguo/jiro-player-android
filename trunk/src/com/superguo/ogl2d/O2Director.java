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
	static O2Director sInstance;
	public static final boolean sIsSingleProcessor = 
		Runtime.getRuntime().availableProcessors() == 1;
	Context				mAppContext;
	GL10 				mGl;
	O2TextureManager 	mTextureManager;
	O2SpriteManager 	mSpriteManager;
	Map<Long, Paint> 	mPaints;
	protected O2Scene 	mCurrentScene;
	protected Object 	mSceneAccessMutex;
	
	Config 				mConfig;
	LpDpTransformation	mLpDpTransformation;
	
	EGL10 				mEGL;
	EGLDisplay 			mEGLDisplay;
	EGLConfig			mEGLConfig;
	EGLSurface			mEGLSurface;
	EGLContext			mEGLContext;
	
	private Handler 	mDrawHandler;

	public static class Config {
		public int width;
		public int height;

		public Config(int width, int height) {
			this.width = width;
			this.height = height;
		}

		public Config() {
			width = height = 0;
		}
	}

	static class LpDpTransformation {
		public float scale;
		public float xOffset;
		public float yOffset;
	}
	
	public static O2Director createInstance(Context appContext, Config config) {
		sInstance = new O2Director(appContext, config);
		return sInstance;
	}
	
	O2Director(Context appContext, Config config) {
		super(appContext);
		getHolder().setFormat(PixelFormat.RGB_565);
		mAppContext = appContext;
		this.mConfig = config==null ? new Config() : config;
		mLpDpTransformation = new LpDpTransformation();
		
		if (!sIsSingleProcessor) mSceneAccessMutex = new Object();
		mTextureManager = new O2TextureManager();
		mSpriteManager = new O2SpriteManager();
		mPaints = sIsSingleProcessor ?
			new HashMap<Long, Paint>(5)
				:
			new ConcurrentHashMap<Long, Paint>(5);
		Paint defaultPaint = new Paint(Paint.FAKE_BOLD_TEXT_FLAG);
		defaultPaint.setColor(Color.rgb(255, 255, 255));
		defaultPaint.setAntiAlias(true);
		mPaints.put(Long.valueOf(0), defaultPaint);
		mDrawHandler = new Handler();
		getHolder().addCallback(this);
	}
	
	public static final O2Director getInstance() {
		return sInstance;
	}
	
	public final void dispose()	{
		setCurrentScene(null);
		sInstance = null;
	}
	
	public final O2TextureManager getTextureManager() {
		return mTextureManager;
	}
	
	public final O2SpriteManager getSpriteManager() {
		return mSpriteManager;
	}

	public long addPaint(Paint p) {
		long id = android.os.SystemClock.elapsedRealtime();
		mPaints.put(id, new Paint(p));
		return id;
	}

	public Paint getPaint(long id) {
		return mPaints.get(id);
	}
	
	public void removePaint(long id) {
		mPaints.remove(id);
	}

	public final void setCurrentScene(O2Scene scene) {
		setCurrentSceneUnsafe(scene);
	}

	public final O2Scene getCurrentScene() {
		return mCurrentScene;
	}
	
	protected final void setCurrentSceneUnsafe(O2Scene scene) {
		O2Scene orig = mCurrentScene;
		if (orig != null)
			orig.onLeavingScene();

		mCurrentScene = scene;
		if (mCurrentScene != null)
			mCurrentScene.onEnteringScene();
	}
	
	public float toXLogical(float xDevice) {
		return (Math.abs(mLpDpTransformation.scale) < 1e-5) ? xDevice : 
			xDevice	/ mLpDpTransformation.scale - mLpDpTransformation.xOffset;
	}

	public float toYLogical(float yDevice) {
		return (Math.abs(mLpDpTransformation.scale) < 1e-5) ? yDevice 
				: yDevice / mLpDpTransformation.scale - mLpDpTransformation.yOffset;
	}

	public float toXDevice(float xLogical) {
		return (Math.abs(mLpDpTransformation.scale) < 1e-5) ? xLogical
				: (xLogical + mLpDpTransformation.xOffset) * mLpDpTransformation.scale;
	}
	
	public float toYDevice(float yLogical) {
		return (Math.abs(mLpDpTransformation.scale) < 1e-5) ? yLogical
				: (yLogical + mLpDpTransformation.yOffset) * mLpDpTransformation.scale;
	}
	
	/**
	 * Must be called when app is to be sresumed
	 */
	public void onResume() {
		O2Scene scene = getCurrentScene();
		if (scene != null)
			scene.onResume();
	}
	
	/**
	 * Must be called when app is to be paused
	 */
	public void onPause() {
		O2Scene scene = getCurrentScene();
		if (scene != null)
			scene.onPause();
	}	
	
	public void surfaceCreated(SurfaceHolder holder) {
		if (mGl == null) {
			createGl();
			sizeChanged(getWidth(), getHeight());
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (mGl != null) {
			sizeChanged(width, height);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		destroyGl();
	}

	public void run() {
		if (mGl != null) {
			drawFrame();
			if (!mEGL.eglSwapBuffers(mEGLDisplay, mEGLSurface)) {
				destroyGl();
				createGl();
			}
		}
		mDrawHandler.post(this);
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
		if (mCurrentScene == null) {
			return false;
		}
		return mCurrentScene.onTouchEvent(e);
	}
	
	private final void createGl() {
		EGL10 egl = mEGL = (EGL10)EGLContext.getEGL();
		mEGLDisplay = mEGL.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
		int[] eglVersion = {0, 0};
		egl.eglInitialize(mEGLDisplay, eglVersion);
		
		int attribs[] = {
				EGL10.EGL_BLUE_SIZE, 5,
				EGL10.EGL_GREEN_SIZE, 6,
				EGL10.EGL_RED_SIZE, 5,
				EGL10.EGL_ALPHA_SIZE, 0,
				EGL10.EGL_DEPTH_SIZE, 0,
				EGL10.EGL_STENCIL_SIZE, 0,
				EGL10.EGL_NONE
		};
		
		int[] numElgConfigs = new int[1];
		egl.eglChooseConfig(mEGLDisplay, attribs, null, 0, numElgConfigs);
		EGLConfig[] eglConfigs = new EGLConfig[numElgConfigs[0]];
		egl.eglChooseConfig(mEGLDisplay, attribs, eglConfigs, numElgConfigs[0], numElgConfigs);

		int value[] = new int[1];
		for (EGLConfig config : eglConfigs)	{
			int r = egl.eglGetConfigAttrib(mEGLDisplay, config, EGL10.EGL_RED_SIZE, value) ? value[0] : 0;
			int g = egl.eglGetConfigAttrib(mEGLDisplay, config, EGL10.EGL_GREEN_SIZE, value) ? value[0] : 0;
			int b = egl.eglGetConfigAttrib(mEGLDisplay, config, EGL10.EGL_BLUE_SIZE, value) ? value[0] : 0;
			int a = egl.eglGetConfigAttrib(mEGLDisplay, config, EGL10.EGL_ALPHA_SIZE, value) ? value[0] : 0;
			if (r==5 && g==6 && b==5 && a==0) {
				mEGLConfig = config;
				break;
			}
		}
		if (mEGLConfig==null) {
			throw new IllegalArgumentException("no config found");
		}
		
		mEGLSurface = egl.eglCreateWindowSurface(mEGLDisplay, mEGLConfig, getHolder(), null);
		mEGLContext = egl.eglCreateContext(mEGLDisplay, mEGLConfig, EGL10.EGL_NO_CONTEXT, null);
		egl.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, mEGLContext);
		mGl = (GL10)mEGLContext.getGL();
		
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

        mTextureManager.recreateManaged();
        
		mDrawHandler.post(this);
	}
	
	private final void sizeChanged(int width, int height) {
		O2Director.Config config = mConfig;
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
		if (config.width > 0 && config.height > 0) {
			/* (xLogical + xOffset) * scale  = xDeivce
			 * (yLogical + yOffset) * scale  = yDeivce
			 */
			float scale;
			float xOffset;
			float yOffset;
			
			if (config.width * height > width * config.height) {
				scale = (float)width/config.width;
				yOffset = (height/scale - config.height) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);				
				GLES10.glTranslatef(0.0f, yOffset, 0.0f);
				
				mLpDpTransformation.scale = scale;
				mLpDpTransformation.xOffset = 0.0f;
				mLpDpTransformation.yOffset = yOffset;
			} else {
				scale = (float)height/config.height;
				xOffset = (width/scale - config.width) / 2.0f;
				
				GLES10.glScalef(scale, scale, 1.0f);
				GLES10.glTranslatef(xOffset, 0.0f, 0.0f);
				
				mLpDpTransformation.scale = scale;
				mLpDpTransformation.xOffset = xOffset;
				mLpDpTransformation.yOffset = 0.0f;
			}
			
			// set the clipping rect
			GLES10.glScissor(
					(int)toXDevice(0),
					(int)toYDevice(0),
					(int)toXDevice(config.width),
					(int)toYDevice(config.height));
		}
		if (mCurrentScene!=null) {
			mCurrentScene.onSizeChanged();
		}
	}

	private final void drawFrame() {
		GLES10.glDisable(GLES10.GL_SCISSOR_TEST);
		GLES10.glClear(GLES10.GL_COLOR_BUFFER_BIT);
		GLES10.glEnable(GLES10.GL_SCISSOR_TEST);
		O2Scene s = mCurrentScene;
		if (s!=null)	s.preDraw(mGl);
		mSpriteManager.drawAllSprites(mGl);
		if (s!=null)	s.postDraw(mGl);
	}
	
	private final void destroyGl() {
		mDrawHandler.removeCallbacks(this);
		mTextureManager.markAllNA();
		if (mEGLSurface!=null && mEGLSurface!=EGL10.EGL_NO_SURFACE)
		{
			mEGL.eglMakeCurrent(mEGLDisplay, EGL10.EGL_NO_SURFACE,
					                         EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			mEGL.eglDestroySurface(mEGLDisplay, mEGLSurface);
		}
		mEGLSurface = null;
		mGl = null;
	}
}
