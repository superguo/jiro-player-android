package com.superguo.ogl2d;

import java.util.*;

import javax.microedition.khronos.opengles.GL10;

import android.os.Parcel;
import android.view.*;

public abstract class O2Scene {
	
	public static final int MAX_EVENT = 128;
	private MotionEvent motionEventQ[];
	private int motionEventHead;
	private int motionEventTail;
	
	protected O2Scene()
	{
		motionEventQ = new MotionEvent[MAX_EVENT+1];
		motionEventHead = motionEventTail = 0;
	}
	
	protected final MotionEvent getMotionEvent()
	{
		// using a condition is faster than a virtual function
		// using synchronized is faster than wait()+notify()
		if (O2Director.isSingleProcessor)
		{
			return getMotionEventUnsafe();
		}
		else
		{
			synchronized (motionEventQ) {
				return getMotionEventUnsafe();
			}
		}
	}
	
	private final MotionEvent getMotionEventUnsafe()
	{
		if (motionEventHead==motionEventTail) return null;
		MotionEvent val = motionEventQ[motionEventHead];
		if (motionEventHead==MAX_EVENT) motionEventHead = 0;
		else motionEventHead++;
		return val;
	}

	final void addMotionEventUnsafe(MotionEvent e)
	{
		if (motionEventHead == motionEventTail + 1 ||
			motionEventTail == MAX_EVENT && motionEventHead==0)
			// full!! we do nothing, though
			return;

		motionEventQ[motionEventTail] = MotionEvent.obtain(e);
		if (motionEventTail == MAX_EVENT)
			motionEventTail = 0;
		else
			motionEventTail++;
	}
	
	public void onEnteringScene()
	{	}
	public void onLeavingScene()
	{	}
	public abstract void onPause();
	public abstract void onResume();
	public abstract void draw(GL10 gl);
}
