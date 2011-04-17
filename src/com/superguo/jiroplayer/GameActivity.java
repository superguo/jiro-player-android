package com.superguo.jiroplayer;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import com.superguo.ogl2d.*;

public class GameActivity extends Activity
	{
	//GLSurfaceView gameView;
	O2Director director;
	GameModel gameModel;
	PlayScene playScene;
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    @Override
    public void onStart()
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
        		WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
        director = O2Director.createInstance(
        		getApplicationContext(),
        		new O2Director.Config(512, 384));
        gameModel = new GameModel();
        playScene = new PlayScene(director, gameModel);
        setContentView(director);
        director.setCurrentScene(playScene);
        /*
        gameView = new GLSurfaceView(this);
        gameView.setRenderer(new GameRenderer(this));
        setContentView(gameView);
        */
        super.onStart();
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    }

    @Override
    public void onResume()
    {
    	super.onResume();
    }

    @Override
    public void onStop ()
    {
    	director.dispose();
    	director = null;
    	FPSHolder.getInstance().dispose();
    	playScene = null;
    	gameModel = null;
    	super.onStop();
    }
    
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent e)
    {
    	director.fastTouchEvent(e);
    	getWindow().superDispatchTouchEvent(e);
    	return true;
    }
    
}
