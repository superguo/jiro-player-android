

package com.superguo.jiroplayer;

import javax.microedition.khronos.opengles.*;

import android.graphics.*;

import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private O2Sprite bgSprite;
	private O2Sprite textSprite;
	/*
	private Paint paint;
	private Typeface typeface;
	private long paintId;
	*/
	
	public PlayScene(O2Director director, GameModel gameModel)
	{
		super(director);
		/*
		paint = new Paint();
		typeface = Typeface.createFromAsset(director.getContext().getAssets(), "a-otf-kanteiryustd-ultra.otf");
		paint.setTypeface(typeface);
		paintId = director.addPaint(paint);
		*/
	}
	

	@Override
	public void onEnteringScene() {
		O2SpriteManager mgr = director.getSpriteManager();
		bgSprite = mgr.createFromResource(R.drawable.bg, true);
		textSprite = mgr.createFromString("hello", true);
	}

	@Override
	public void onLeavingScene() {
		bgSprite.dispose();
		bgSprite = null;
		
		textSprite.dispose();
		textSprite = null;
		
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(GL10 gl) {
		bgSprite.draw(0, 0);
		textSprite.draw(0, 0);
	}


}
