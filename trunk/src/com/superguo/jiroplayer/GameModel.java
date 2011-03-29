package com.superguo.jiroplayer;

import java.lang.reflect.*;
import com.superguo.ogl2d.*;

public final class GameModel {
	public static class Layout implements Cloneable
	{
		public int iMTaikoY = 90;
		public int iScrollFieldHeight=56;
		public int iScrollFieldY=125;
		public int iSENotesY=165;
		public int iNormaGaugeWidth=256;
		public int iNormaGaugeX=348;
		public int iNormaGaugeY=33;
		public int iScoreX=450;
		public int iScoreY=76;
		public int iAddScoreX=450;
		public int iAddScoreY=55;
		public int iRollBalloonX=197;
		public int iRollBalloonY=35;
		public int iRollNumberX=183;
		public int iRollNumberY=27;
		public int iBurstBalloonX=200;
		public int iBurstBalloonY=77;
		public int iBurstNumberX=200;
		public int iBurstNumberY=77;
		public int iComboBalloonX=220;
		public int iComboBalloonY=77;
		public int iComboNumberX=220;
		public int iComboNumberY=77;
		public int iCourseSymbolX=185;
		public int iCourseSymbolY=45;
		public int iPlayerCharacterX=100;
		public int iPlayerCharacterY=45;
		public int iPlayerCharacterBalloonX=133;
		public int iPlayerCharacterBalloonY=130;
		public int iDancerY=275;
		public int iSongTitleY=190;
		public int iBranchBalloonX=175;
		public int iBranchBalloonY=37;
		public Layout clone()
		{
			try	{
				return (Layout)super.clone();
			}
			catch(CloneNotSupportedException e) {
				throw new Error("Unknown Error", e);
			}
		}
	}
	
	private Layout iDefaultLayout = new Layout();
	public Layout iLayout;
	
	public GameModel()
	{
		iLayout = (Layout) iDefaultLayout.clone();
	}
}
