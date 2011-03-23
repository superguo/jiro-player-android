package com.superguo.jiroplayer;

import com.superguo.ogl2d.*;
import java.io.*;
import android.app.Activity;
import android.content.res.*;
import android.media.*;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

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
        director = O2Director.createInstance(getApplicationContext());
        gameModel = new GameModel();
        playScene = new PlayScene(gameModel);
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
