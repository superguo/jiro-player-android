package com.superguo.jiroplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import com.superguo.ogl2d.*;

public class GameActivity extends Activity {
	// GLSurfaceView gameView;
	private O2Director mDirector;
	private GameModel mGameModel;
	private PlayScene mPlayScene;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	@Override
	public void onStart() {
		mDirector = O2Director.createInstance(getApplicationContext(),
				new O2Director.Config(512, 384));
		mGameModel = new GameModel();
		mPlayScene = new PlayScene(mDirector, mGameModel);
		setContentView(mDirector);
		mDirector.setCurrentScene(mPlayScene);
		/*
		 * gameView = new GLSurfaceView(this); gameView.setRenderer(new
		 * GameRenderer(this)); setContentView(gameView);
		 */
		super.onStart();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStop() {
		if (mDirector != null) {
			mDirector.dispose();
			mDirector = null;

			FPSHolder.getInstance().dispose();
			mPlayScene = null;
			mGameModel = null;
		}
		super.onStop();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent e) {
		mDirector.fastTouchEvent(e);
		getWindow().superDispatchTouchEvent(e);
		return true;
	}

}
