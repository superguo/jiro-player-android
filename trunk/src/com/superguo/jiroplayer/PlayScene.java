

package com.superguo.jiroplayer;

import java.io.*;

import javax.microedition.khronos.opengles.*;

import android.content.res.*;
//import android.graphics.*;
import android.media.*;
import android.util.Log;
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

	private int iDrumY;
	private int iDrumX1;
	private int iDrumX2;
	
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
		O2TextureManager tmgr = director.getTextureManager();
		O2SpriteManager smgr = director.getSpriteManager();
		//iBgSprite 			= new O2Sprite(mgr.createFromResource(R.drawable.bg, true));
		//smgr.addSprite(iBgSprite);
		iMTaikoSprite		= new O2Sprite(tmgr.createFromResource(R.drawable.mtaiko, true));
		iMTaikoSprite.iValign = O2Texture.VALIGN_MIDDLE;
		iMTaikoSprite.iX	= 0;
		iMTaikoSprite.iY	= iLayout.iScrollFieldY;
		smgr.addSprite(iMTaikoSprite);
		
		iNotesSlices		= tmgr.createSpriteSheetWithRowsAndCols(
				tmgr.createFromResource(R.drawable.notes, true),
				2,
				13);
		iTargetNoteSprite 			= new O2Sprite(iNotesSlices, 0);
		iTargetNoteSprite.iValign	= O2Texture.VALIGN_MIDDLE;
		iTargetNoteSprite.iX		= PlayLayout.MTAIKO_WIDTH;
		iTargetNoteSprite.iY		= iLayout.iScrollFieldY;
		smgr.addSprite(iTargetNoteSprite);

		try
		{
			InputStream is = director.getContext().getAssets().open("utf8test.txt", AssetManager.ACCESS_RANDOM);
			BufferedReader ir = new BufferedReader(new InputStreamReader(is));
			String text1 = ir.readLine();
			String text2 = ir.readLine();
		
			iTextSprite1 = new O2Sprite(tmgr.createFromString(text1, true));
			iTextSprite1.iX = 100;
			smgr.addSprite(iTextSprite1);
			
			iTextSprite2 = new O2Sprite(tmgr.createFromString(text2, true));
			iTextSprite2.iX = 200;
			smgr.addSprite(iTextSprite2);
		}
		catch(IOException e)
		{
			
		}
	}

	@Override
	public void onLeavingScene() {
		O2SpriteManager smgr = director.getSpriteManager();

		if (iBgSprite!=null)
			smgr.removeSprite(iBgSprite);
		
		if (iMTaikoSprite!=null)
			smgr.removeSprite(iMTaikoSprite);
		
		if (iTargetNoteSprite!=null)
			smgr.removeSprite(iTargetNoteSprite);
		
		if (iTextSprite1!=null)
			smgr.removeSprite(iTextSprite1);

		if (iTextSprite2!=null)
			smgr.removeSprite(iTextSprite2);
		
		iNotesSlices = null;
		
		iSoundPool.release();
		iSoundPool = null;
	}
	
	@Override
	public void onSizeChanged(){
		iDrumY = (int) director.toYDevice(iLayout.iSENotesY);
		iDrumX1 = (int) director.toXDevice(178);
		iDrumX2 = (int) director.toXDevice(334);
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
	public boolean onTouchEvent(MotionEvent e) {
		if (e!=null && e.getAction()==MotionEvent.ACTION_DOWN && O2Director.getInstance()!=null)
		{
			long delay = android.os.SystemClock.uptimeMillis() - e.getEventTime();
			Log.i("jiro-player Scene", "touched delay=" + delay);
			if (e.getY()>iDrumY)
			{
				int x = (int) e.getX();
				if (iDrumX1<e.getX() && e.getX()<iDrumX2)
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
