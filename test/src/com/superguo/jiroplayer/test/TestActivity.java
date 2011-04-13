package com.superguo.jiroplayer.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.*;
import com.superguo.ogl2d.*;

public class TestActivity extends Activity
	{
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     
    }
    
    @Override
    public void onStart()
    {
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

    }
    
    /*
    @Override
    public boolean dispatchTouchEvent(MotionEvent e)
    {
    	director.fastTouchEvent(e);
    	getWindow().superDispatchTouchEvent(e);
    	return true;
    }
    */
}
