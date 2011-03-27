package com.superguo.jiroplayer;

import com.superguo.ogl2d.*;
import java.io.*;
import android.app.Activity;
import android.content.res.*;
import android.media.*;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

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
    }
    
    @Override
    public void onStart()
    {
    	try
    	{
    	}
    	catch(Exception e)
    	{
    		
    	}
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
    	super.onStop();
    }
}
