package com.superguo.jiroplayer;

import java.io.*;
import java.nio.*;
import java.util.*;
import java.util.regex.*;

public final class TJAFormat {
	// unsupported header: GAME, LIFE
	public final static int COURSE_EASY 	= 0;
	public final static int COURSE_NORMAL 	= 1;
	public final static int COURSE_HARD 	= 2;
	public final static int COURSE_ONI 		= 3;
	public final static int COURSE_EDIT 	= 4;
	public final static int COURSE_TOWER 	= 5;
	public final static int SIDE_NORMAL		= 1;
	public final static int SIDE_EX			= 2;
	public final static int SIDE_BOTH		= 3;
	public final static int STYLE_SINGLE	= 1;
	public final static int STYLE_DOUBLE	= 2;
	public final static int BRANCH_JUDGE_ROLL 		= 0;
	public final static int BRANCH_JUDGE_PRECISION 	= 1;
	public final static int BRANCH_JUDGE_SCORE 		= 2;
	public final static int COMMAND_TYPE_NOTE		= 0; 	// iNotes
	public final static int COMMAND_TYPE_BPMCHANGE 	= 1; 	// iFloatArg
	public final static int COMMAND_TYPE_GOGOSTART 	= 2;
	public final static int COMMAND_TYPE_GOGOEND 	= 3;
	public final static int COMMAND_TYPE_MEASURE  	= 4; 	// iIntArg / iIntArg2, 0 < iIntArg < 100, 0 < iIntArg2 < 100
	public final static int COMMAND_TYPE_SCROLL 	= 5; 	// iFloatArg, 0.1 - 16.0
	public final static int COMMAND_TYPE_DELAY 		= 6; 	// iFloatArg, >0.001
	public final static int COMMAND_TYPE_SECTION 	= 7;
	public final static int COMMAND_TYPE_BRANCHSTART  = 8; 	// iIntArg, iFloatArg, iFloatArg2
	public final static int COMMAND_TYPE_BRANCHEND 	= 9;
	public final static int COMMAND_TYPE_N 			= 10;
	public final static int COMMAND_TYPE_E 			= 11;
	public final static int COMMAND_TYPE_M 			= 12;
	public final static int COMMAND_TYPE_LEVELHOLD 	= 13;
	public final static int COMMAND_TYPE_BARLINEOFF = 14;
	public final static int COMMAND_TYPE_BARLINEON 	= 15;

	// global
	public String 	iTitle;
	public String 	iSubTitle;
	public int		iSide	= SIDE_BOTH;
	public String	iWave;
	public float	iOffset;	// -5 ~ +5
	public float	iDemoStart = 0.0f;
	public float	iSongVol = 100.0f;	// 0 ~ 100, default 100
	public float	iSeVol = 100.0f;		// 0 ~ 100, default 100
	public boolean	iBMScroll = false;
	public boolean	iHBScroll = false;
	
	public final static class TJACourse
	{
		public int		iCourse = COURSE_ONI;	//
		public int 		iLevel;
		public float 	iBPM;		// 50 ~ 250
		public int		iStyle = STYLE_SINGLE;		
		public int[]	iBalloon;	// number of balloons
		public int		iScoreInit;	// 1 ~ 100000, 0 means auto
		public int		iScoreDiff;	// 1 ~ 100000, 0 means auto
		public TJAPara[] iParasP1; 	// cannot be null
		public TJAPara[] iParasP2;	// cannot be null if iStyle is STYLE_DOUBLE
	}
	
	public final static class TJAPara
	{
		TJACommand[] 	iCommands;
		int[]		 	iNotes;
	}
	
	public final static class TJACommand
	{
		public TJACommand()
		{		}
		
		public TJACommand(int commandType)
		{	iCommandType = commandType;		}
		
		public int iCommandType;
		public int iIntArg;
		public int iIntArg2;
		public float iFloatArg;
		public float iFloatArg2;
	}
	
	public final static class TJAFormatException extends RuntimeException
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -7734235891270702334L;

		public TJAFormatException(int iLineNo, String iLine, String msg)
		{
			super("Line " + iLineNo + " " + msg + "\n" + (iLine == null ? "" : iLine));
		}
		
		public TJAFormatException(int iLineNo, String iLine, Throwable r)
		{
			super("Line " + iLineNo + "\n" + (iLine == null ? "" : iLine), r);
		}
	}
	
	public TJACourse iCourses[];
	
	// parse states
	int					  iLineNo;
	String				  iLine;
	LinkedList<TJACourse> iTempCourses 	= new LinkedList<TJACourse>();
	LinkedList<TJAPara>   iTempParas 	= new LinkedList<TJAPara>();
	TJACourse 			  iCurrentCourse;
	TJAPara				  iCurrentPara;
	LinkedList<TJACommand>  iCurrentCommands;
	IntBuffer			  iCurrentNotes;
	boolean				  iIsParseingP1;
	boolean				  iIsStarted;
	boolean				  iIsGoGoStarted;
	boolean				  iIsBranchStarted;
	boolean				  iHasSection;
	
	private static String tidy(String iLine)
	{
		String r = iLine;
		r = r.trim();
		int commentPos = r.indexOf("//");
		if (commentPos>=0)
			r = r.substring(0, commentPos);
		return r;
	}
	
	private void throwEx(String msg) throws TJAFormatException
	{
		throw new TJAFormatException(iLineNo, iLine, msg);
	}

	private void throwEx(Throwable r) throws TJAFormatException
	{
		throw new TJAFormatException(iLineNo, iLine, r);
	}

	public TJAFormat(BufferedReader reader)  throws IOException
	{
		iLineNo = 0;
		iCurrentCourse = new TJACourse();
		iCurrentPara = new TJAPara();
		iCurrentCommands = new LinkedList<TJACommand>();
		iCurrentNotes = IntBuffer.wrap(new int[500]);
		
		for (	iLine = reader.readLine();
				iLine != null;
				iLine = reader.readLine())
		{
			++iLineNo;
			iLine = tidy(iLine);
			if (iLine.length()==0) continue;
			
			char firstChar = iLine.charAt(0);
			int colonPos;
			if (firstChar=='#') // command
			{
				if (iCurrentNotes.position()>0)
					emitNotes();

				iLine = iLine.toUpperCase();
				parseCommandOfCurrentLine();
			}
			else if (Character.isDigit(firstChar)) // notes
			{
				if (iCurrentCommands.size()>0)
					emitCommands();
				parseNotesOfCurrentLine();
			}
			else if ((colonPos = iLine.indexOf(':')) >= 0)
			{
				try
				{
					if (iIsStarted)
						throwEx("No header allowed after #START without #END");
					
					if (iCurrentNotes.position()>0)
						emitNotes();
					
					setHeader(	iLine.substring(0, colonPos).trim(),
								iLine.substring(colonPos+1).trim() );
				}
				catch(java.lang.RuntimeException e)
				{
					throwEx(e);
				}
			}
			else
			{
				throwEx("Unknown header or command");
			}
		}
		
		if (iIsStarted)
			throwEx("missing #END");
		
		if (iTempCourses.size()==0)
			throwEx("missing #START");

		if (iTempCourses.getLast().iStyle==STYLE_DOUBLE &&
			iTempCourses.getLast().iParasP2==null)
			throwEx("missing #START P2");

		iCourses = (TJACourse[])iTempCourses.toArray();
	}

	private void parseCommandOfCurrentLine() {
		String fields[];
		
		//TODO judge if started
		
		if (iLine.startsWith("#START"))
		{
			// can start?
			if (iIsStarted)
				throwEx("cannot put #START here");
			fields = iLine.split("\\s");
			if (fields.length==1 || fields.length==2 && fields[1].equals("P1"))
			{
				if (iCurrentCourse.iParasP1!=null)
					throwEx("cannot #START again");
				iIsStarted = true;
				iIsParseingP1 = true;
			}
			else if (fields.length==2 && fields[1].equals("P2"))
			{
				if (iCurrentCourse.iParasP1==null)
					throwEx("Must #START P1 first");

				if (iCurrentCourse.iParasP2!=null)
					throwEx("cannot #START again");
				
				iIsStarted = true;
				iIsParseingP1 = false;
			}
			else
				throwEx("Unknown #START command");
		}
		else if (iLine.equals("#END"))
		{
			if (!iIsStarted)
				throwEx("#END without #START");
			if (iIsGoGoStarted)
				throwEx("missing #GOGOEND");
			emitParas();
		}
		else if (iLine.startsWith("#BPMCHANGE"))
		{
			fields = iLine.split("\\s");
			if (fields.length!=2)
				throwEx("Unknown #BPMCHANGE command");
			iCurrentCommands.add(new TJACommand(COMMAND_TYPE_BPMCHANGE));
		}
		else if (iLine.equals("#GOGOSTART"))
		{
			if (iIsGoGoStarted) throwEx("Already exist #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = true;
			iCurrentCommands.add(new TJACommand(COMMAND_TYPE_GOGOSTART));
		}
		else if (iLine.equals("#GOGOEND"))
		{
			if (!iIsGoGoStarted) throwEx("Missing #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = false;
			iCurrentCommands.add(new TJACommand(COMMAND_TYPE_GOGOEND));
		}
		else if (iLine.startsWith("#MEASURE"))
		{
			Pattern p = Pattern.compile("#MEASURE (\\d+)/(\\d+)");
			Matcher m = p.matcher(iLine);
			if (m.find())
			{
				TJACommand cmd = new TJACommand(COMMAND_TYPE_MEASURE);
				cmd.iIntArg = Integer.parseInt(m.group(1));
				cmd.iIntArg2 = Integer.parseInt(m.group(2));
				if (cmd.iIntArg<0 || cmd.iIntArg>100 || cmd.iIntArg2<0 || cmd.iIntArg2>100)
					throwEx("#MEASURE arguments must be 1-99");
				iCurrentCommands.add(cmd);
			}
			else
				throwEx("Unknown #MEASURE command");
		}
		else if (iLine.startsWith("#SCROLL"))
		{
			fields = iLine.split("\\s");
			if (fields.length!=2)
				throwEx("Unknown #SCROLL command");
			TJACommand cmd = new TJACommand(COMMAND_TYPE_SCROLL);
			cmd.iFloatArg = Float.parseFloat(fields[1]);
			if (cmd.iFloatArg<0.1f || cmd.iFloatArg>16.0f)
				throwEx("#SCROLL arguments must be 0.1f - 16.0f");
			iCurrentCommands.add(cmd);
		}
		else if (iLine.startsWith("#DELAY"))
		{
			fields = iLine.split("\\s");
			if (fields.length!=2)
				throwEx("Unknown #DELAY command");
			TJACommand cmd = new TJACommand(COMMAND_TYPE_DELAY);
			cmd.iFloatArg = Float.parseFloat(fields[1]);
			cmd.iFloatArg = (float) (Math.floor(cmd.iFloatArg*1000)/1000);

			iCurrentCommands.add(cmd);
		}
		else if (iLine.equals("#SECTION"))
		{
			iHasSection = true;
			iCurrentCommands.add(new TJACommand(COMMAND_TYPE_SECTION));
		}
		// TODO
	}
	
	private void emitParas()
	{
		if (iIsParseingP1)
			iCurrentCourse.iParasP1 = (TJAPara[]) iTempParas.toArray();
		else
			iCurrentCourse.iParasP2 = (TJAPara[]) iTempParas.toArray();
		
		if (iCurrentCourse.iStyle == STYLE_SINGLE ||
			iCurrentCourse.iStyle == STYLE_DOUBLE && !iIsParseingP1)
		{
			iTempCourses.add(iCurrentCourse);
			iCurrentCourse = new TJACourse();
		}

		iTempParas.clear();
	
		iIsStarted = false;
		iHasSection = false;
		iIsBranchStarted = false;
	}
	
	private void parseNotesOfCurrentLine()
	{
		char[] lineChars = iLine.toCharArray();
		for (char c : lineChars)
		{
			if ('0'<=c && c<='9')
				iCurrentNotes.put(c-'0');
			else if (Character.isSpace(c))
				continue;
			else if (c==',')
				emitNotes();
		}
	}

	private void emitCommands() {
		if (iCurrentCommands.size()>0)
		{
			iCurrentPara.iCommands = (TJACommand[]) iCurrentCommands.toArray();
			iCurrentCommands.clear();
		}
	}

	private void emitNotes()
	{
		if (iCurrentNotes.position()>0)
		{
			iCurrentPara.iNotes = new int[iCurrentNotes.position()]; 
			System.arraycopy(
					iCurrentNotes.array(), 0, iCurrentPara.iNotes, 0,
					iCurrentNotes.position());
		}
		iTempParas.add(iCurrentPara);
		iCurrentNotes.clear();
		iCurrentPara = new TJAPara();
	}
	
	private void setHeader(String name, String value)
	{
		if (name.equals("TITLE"))
			iTitle = new String(value);
		else if (name.equals("LEVEL"))
			iCurrentCourse.iLevel = Integer.parseInt(value);
		else if (name.equals("BPM"))
		{
			iCurrentCourse.iBPM = Float.parseFloat(value);
			if (iCurrentCourse.iBPM < 50 || iCurrentCourse.iBPM > 250)
				throwEx("BPM must be 50-250");
		}
		else if (name.equals("WAVE"))
			iWave = new String(value);
		else if (name.equals("OFFSET"))
			iOffset = Float.parseFloat(value);
		else if (name.equals("BALLOON"))
		{
			String[] balloons = value.split(",");
			iCurrentCourse.iBalloon = new int[balloons.length];
			for (int i=0; i<iCurrentCourse.iBalloon.length; ++i)
				iCurrentCourse.iBalloon[i] = Integer.parseInt(balloons[i]); 
		}
		else if (name.equals("SONGVOL"))
		{
			iSongVol = Float.parseFloat(value);
			if (iSongVol<0 || iSongVol>100)
				throwEx("SONGVOL must be 0-100");
		}
		else if (name.equals("SEVOL"))
		{
			iSeVol = Float.parseFloat(value);
			if (iSeVol<0 || iSeVol>100)
				throwEx("SEVOL must be 0-100");
		}
		else if (name.equals("SCOREINIT"))
		{
			iCurrentCourse.iScoreInit = Integer.parseInt(value);
			if (iCurrentCourse.iScoreInit<0)
				throwEx("SCOREINIT must be positive");
		}
		else if (name.equals("SCOREDIFF"))
		{
			iCurrentCourse.iScoreDiff = Integer.parseInt(value);
			if (iCurrentCourse.iScoreDiff<0)
				throwEx("SCOREDIFF must be positive");
		}
		else if (name.equals("COURSE"))
		{
			value = value.trim();
			
			if (value.equalsIgnoreCase("Easy") || value.equals(COURSE_EASY))
				iCurrentCourse.iCourse = COURSE_EASY;
			
			else if (value.equalsIgnoreCase("Normal") || value.equals(COURSE_NORMAL))
				iCurrentCourse.iCourse = COURSE_NORMAL;
			
			else if (value.equalsIgnoreCase("Hard") || value.equals(COURSE_HARD))
				iCurrentCourse.iCourse = COURSE_HARD;
			
			else if (value.equalsIgnoreCase("Oni") || value.equals(COURSE_ONI))
				iCurrentCourse.iCourse = COURSE_ONI;
			
			else if (value.equalsIgnoreCase("Edit") || value.equals(COURSE_EDIT))
				iCurrentCourse.iCourse = COURSE_EDIT;

			else if (value.equalsIgnoreCase("Tower") || value.equals(COURSE_TOWER))
				iCurrentCourse.iCourse = COURSE_TOWER;
		}
		else if (name.equals("STYLE"))
		{
			if (value.equalsIgnoreCase("Double") || value.equalsIgnoreCase("Couple"))
			iCurrentCourse.iStyle = STYLE_DOUBLE;
		}
		else if (name.equals("DEMOSTART"))
			iDemoStart = Float.parseFloat(value);
		else if (name.equals("SIDE"))
		{
			if (value.equalsIgnoreCase("Normal") || value.equals(SIDE_NORMAL))
				iSide = SIDE_NORMAL;
			
			else if (value.equalsIgnoreCase("Ex") || value.equals(SIDE_EX))
				iSide = SIDE_EX;
			
			else if (value.equalsIgnoreCase("Both") || value.equals(SIDE_BOTH))
				iSide = SIDE_BOTH;
		}
		else if (name.equals("SUBTITLE"))
			iSubTitle = new String(value);
		else if (name.equals("GAME"))
			if (!value.equalsIgnoreCase("Taiko"))
				throwEx("Unsupported game mode: " + value);
	}
}
