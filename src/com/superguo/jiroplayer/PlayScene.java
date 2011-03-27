

package com.superguo.jiroplayer;

import javax.microedition.khronos.opengles.*;

import android.graphics.*;
import android.view.*;

import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private O2Sprite bgSprite;
	private O2Sprite textSprite;
	private O2Sprite notesSprite;
	private O2SpriteSheet notesSpriteSheet;
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
		bgSprite 	= mgr.createFromResource(R.drawable.bg, true);
		textSprite 	= mgr.createFromString("hello", true);
		notesSprite	= mgr.createFromResource(R.drawable.notes, true);
		notesSpriteSheet = notesSprite.createSpriteSheetWithRowsAndCols(2, 13);
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
		MotionEvent e = getMotionEvent();
		if (e!=null)
		{
			float x = director.toXLogical(e.getX());
			float y = director.toXLogical(e.getY());
			notesSpriteSheet.draw(0, (int)x, (int)y);
		}
	}


}
