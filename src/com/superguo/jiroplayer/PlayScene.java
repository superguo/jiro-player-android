

package com.superguo.jiroplayer;

import java.io.*;

import javax.microedition.khronos.opengles.*;

import android.content.res.*;
//import android.graphics.*;
import android.media.*;
import android.view.*;

import com.superguo.ogl2d.*;

public class PlayScene extends O2Scene {
	private GameModel iGameModel;
	private PlayLayout iLayout;
	
	private SoundPool iSoundPool;
	private int 	 iSoundDong;
	private int 	 iSoundKa;

	private O2Sprite iBgSprite;
	private O2Sprite iMTaikoSprite;
	private O2TextureSlices iNotesSlices;
	private O2Sprite iTargetNoteSprite;
	private O2Sprite iTextSprite1;
	private O2Sprite iTextSprite2;

	
	/*
	private Paint paint;
	private Typeface typeface;
	private long paintId;
	*/
	
	public PlayScene(O2Director director, GameModel gameModel)
	{
		super(director);
		iGameModel = gameModel;
		iLayout = gameModel.iLayout;
		
		/*
		paint = new Paint();
		typeface = Typeface.createFromAsset(director.getContext().getAssets(), "a-otf-kanteiryustd-ultra.otf");
		paint.setTypeface(typeface);
		paintId = director.addPaint(paint);
		*/
	}
	

	@Override
	public void onEnteringScene() {
		iSoundPool 	= new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
		iSoundDong 	= iSoundPool.load(director.getContext(), R.raw.dong, 1);
		iSoundKa  	= iSoundPool.load(director.getContext(), R.raw.ka, 1);
		O2TextureManager mgr = director.getTextureManager();
		//iBgSprite 			= new O2Sprite(mgr.createFromResource(R.drawable.bg, true));
		iMTaikoSprite		= new O2Sprite(mgr.createFromResource(R.drawable.mtaiko, true));
		iMTaikoSprite.iValign = O2Sprite.VALIGN_MIDDLE;
		iMTaikoSprite.iX	= 0;
		iMTaikoSprite.iY	= iLayout.iScrollFieldY;
		
		// mDebugTextSprite 	= mgr.createFromString("hello", true);
		iNotesSlices		= mgr.createSpriteSheetWithRowsAndCols(
				mgr.createFromResource(R.drawable.notes, true),
				2,
				13);
		iTargetNoteSprite 			= new O2Sprite(iNotesSlices, 0);
		iTargetNoteSprite.iValign	=  O2Sprite.VALIGN_MIDDLE;
		iTargetNoteSprite.iX		= 130;
		iTargetNoteSprite.iY		= iLayout.iScrollFieldY;
		

		try
		{
			InputStream is = director.getContext().getAssets().open("utf8test.txt", AssetManager.ACCESS_RANDOM);
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String text1 = ir.readLine();
			String text2 = ir.readLine();
		
			iTextSprite1 = new O2Sprite(mgr.createFromString(text1, true));
			iTextSprite2 = new O2Sprite(mgr.createFromString(text2, true));
			
			iTextSprite1.iX = 100;
			iTextSprite2.iX = 200;
		}
		catch(IOException e)
		{
			
		}
	}

	@Override
	public void onLeavingScene() {
		if (iBgSprite!=null)
		{
			iBgSprite.dispose();
			iBgSprite = null;
		}
		
		if (iMTaikoSprite!=null)
		{
			iMTaikoSprite.dispose();
			iMTaikoSprite = null;
		}
		
		if (iTargetNoteSprite!=null)
		{
			iTargetNoteSprite.dispose();
			iTargetNoteSprite = null;
		}
		
		if (iTextSprite1!=null)
		{
			iTextSprite1.dispose();
			iTextSprite1 = null;
		}

		if (iTextSprite2!=null)
		{
			iTextSprite2.dispose();
			iTextSprite2 = null;
		}
		
		iNotesSlices = null;
		
		iSoundPool.release();
		iSoundPool = null;
	}
	
	@Override
	public void onPause() {
		iSoundPool.autoPause();
	}

	@Override
	public void onResume() {
		iSoundPool.autoResume();
	}

	@Override
	public void preDraw(GL10 gl) {
		MotionEvent e = getMotionEvent();
		if (e!=null && e.getAction()==MotionEvent.ACTION_DOWN)
		{
			float x = director.toXLogical(e.getX());
			float y = director.toYLogical(e.getY());
			if (y>iLayout.iSENotesY)
			{
				if (128<x && x<384)
					iSoundPool.play(iSoundDong, 1.0f, 1.0f, 10, 0, 1.0f);
				else
					iSoundPool.play(iSoundKa, 1.0f, 1.0f, 10, 0, 1.0f);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e!=null && e.getAction()==MotionEvent.ACTION_DOWN && O2Director.getInstance()!=null)
		{
			float x = director.toXLogical(e.getX());
			float y = director.toYLogical(e.getY());
			if (y>iLayout.iSENotesY)
			{
				if (128<x && x<384)
					iSoundPool.play(iSoundDong, 1.0f, 1.0f, 10, 0, 1.0f);
				else
					iSoundPool.play(iSoundKa, 1.0f, 1.0f, 10, 0, 1.0f);
			}
			return true;
		}
		return false;
	}

	@Override
	public void postDraw(GL10 gl) {
		FPSHolder.getInstance().showFPS();
	}

	@Override
	public void dispose() {
		onLeavingScene();
	}
	

}
