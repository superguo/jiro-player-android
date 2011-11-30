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
	public final static int BRANCH_JUDGE_ROLL 		= 0;
	public final static int BRANCH_JUDGE_PRECISION 	= 1;
	public final static int BRANCH_JUDGE_SCORE 		= 2;
	public final static int COMMAND_TYPE_NOTE		= 0; 	// iNotes
	public final static int COMMAND_TYPE_BPMCHANGE 	= 1; 	// iFloatArg
	public final static int COMMAND_TYPE_GOGOSTART 	= 2;
	public final static int COMMAND_TYPE_GOGOEND 	= 3;
	public final static int COMMAND_TYPE_MEASURE  	= 4; 	// X(int) / Y(int)( 0 < X < 100, 0 < Y < 100)
	public final static int COMMAND_TYPE_SCROLL 	= 5; 	// float(0.1 - 16.0)
	public final static int COMMAND_TYPE_DELAY 		= 6; 	// float(>0.001)
	public final static int COMMAND_TYPE_SECTION 	= 7;
	public final static int COMMAND_TYPE_BRANCHSTART  = 8; 	// BRANCH_JUDGE_*(r/p/s, int), X(float), Y(float), #N index, #E index, #M index, exit index(may be invalid) 
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
		public int 		iLevel;		// 1 ~ 12
		public float 	iBPM;		// 50 ~ 250
		public int[]	iBalloon;	// number of balloons
		public int		iScoreInit;	// 1 ~ 100000, 0 means auto
		public int		iScoreDiff;	// 1 ~ 100000, 0 means auto
		public TJACommand[] iNotationSingle; 	// cannot be null if iStyle is STYLE_SIGNLE
		public TJACommand[] iNotationP1; 	// cannot be null if iStyle is STYLE_DOUBLE
		public TJACommand[] iNotationP2;	// cannot be null if iStyle is STYLE_DOUBLE
	}

	public final static class TJACommand
	{
		public TJACommand()
		{		}
		
		public TJACommand(int commandType)
		{	iCommandType = commandType;		}
		
		public int iCommandType;
		public int iArgs[];
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
	LinkedList<TJACourse> iParsedCourses 	= new LinkedList<TJACourse>();
	LinkedList<TJACommand>  iParsedCommands;
	TJACourse 			  iParsingCourse;
	IntBuffer			  iParsingNotes;
	boolean				  iIsParsingDouble;
	boolean				  iIsParsingP2;
	boolean				  iIsStarted;
	boolean				  iIsGoGoStarted;
	boolean				  iIsBranchStarted;
	TJACommand			  iCommandOfStartedBranch;
	boolean				  iHasSection;
	
	private static String tidy(String iLine)
	{
		String r = iLine;
		int commentPos = r.indexOf("//");
		if (commentPos>=0)
			r = r.substring(0, commentPos);
		r = r.trim();
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
		iParsingCourse = new TJACourse();
		iParsedCommands = new LinkedList<TJACommand>();
		iParsingNotes = IntBuffer.wrap(new int[500]);
		
		for (	iLine = reader.readLine();
				iLine != null;
				iLine = reader.readLine())
		{
			++iLineNo;
			iLine = tidy(iLine);
			if (iLine.length()==0) continue;
			
			char firstChar = iLine.charAt(0);
			int colonPos;
			if (firstChar=='#') // command begins with #
			{
				if (iParsingNotes.position()>0)
					emitNotes();

				iLine = iLine.toUpperCase();
				parseCommandOfCurrentLine();
			}
			else if (Character.isDigit(firstChar)) // 0-9, it is note
			{
				parseNotesOfCurrentLine();
			}
			else if ((colonPos = iLine.indexOf(':')) >= 0)
			{
				try
				{
					if (iIsStarted)
						throwEx("No header allowed after #START without #END");
					
					if (iParsingNotes.position()>0)
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
			throwEx("Missing #END");
		
		if (	iParsingCourse.iNotationSingle != null ||
				iParsingCourse.iNotationP1 != null &&
				iParsingCourse.iNotationP2 != null)
		{
			emitCourse();
		}
		
		if (iParsingCourse.iNotationP1 != null &&
			iParsingCourse.iNotationP2 == null)
		{
			throwEx("Missing #START P2");
		}
		
		if (iParsedCourses.size()==0)
			throwEx("Missing #START");
		else
			iCourses = iParsedCourses.toArray(new TJACourse[iParsedCourses.size()]);
	}

	private void parseCommandOfCurrentLine() {
		String fields[];
		
		// judge if started
		if (!iIsStarted &&
			!iLine.startsWith("#START") && 
			!iLine.equals("#BMSCROLL") &&
			!iLine.equals("#HBSCROLL"))
		{
			throwEx("Must put #START before this command");
		}
		
		if (iLine.startsWith("#START"))
		{
			// can start?
			if (iIsStarted)
				throwEx("Cannot put #START here");
			fields = iLine.split("\\s");

			if (fields.length==1)
			{
				if (iIsParsingDouble)
					throwEx("Must #START P1 here");
				if (iParsingCourse.iNotationSingle!=null)
					throwEx("Cannot #START again");
				
				iIsStarted = true;
			}
			else if	(fields.length==2)
			{
				fields[1] = fields[1].trim();
				iIsParsingDouble = true;
				if (!iIsParsingDouble)
					throwEx("Must #START here");

				if (fields[1].equals("P1"))
				{
					if (iParsingCourse.iNotationP1!=null)
						throwEx("Cannot #START P1 again");
					iIsStarted = true;
					iIsParsingP2 = false;
				}
				else if (fields.length==2 && fields[1].equals("P2"))
				{
					if (iParsingCourse.iNotationP1==null)
						throwEx("Must #START P1 first");

					if (iParsingCourse.iNotationP2!=null)
						throwEx("Cannot #START P2 again");
				
					iIsStarted = true;
					iIsParsingP2 = true;
				}
				else
					throwEx("Unknown #START command");
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
			if (iIsBranchStarted)	// #BRANCHSTART without #BRANCHEND
				emitBranch();
			emitNotation();
		}
		else if (iLine.startsWith("#BPMCHANGE"))
		{
			fields = iLine.split("\\s");
			if (fields.length!=2)
				throwEx("Unknown #BPMCHANGE command");
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_BPMCHANGE));
		}
		else if (iLine.equals("#GOGOSTART"))
		{
			if (iIsGoGoStarted) throwEx("Already exist #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = true;
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_GOGOSTART));
		}
		else if (iLine.equals("#GOGOEND"))
		{
			if (!iIsGoGoStarted) throwEx("Missing #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = false;
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_GOGOEND));
		}
		else if (iLine.startsWith("#MEASURE"))
		{
			Pattern p = Pattern.compile("#MEASURE\\s+(\\d+)\\s*/\\s*(\\d+)");
			Matcher m = p.matcher(iLine);
			if (m.find())
			{
				TJACommand cmd = new TJACommand(COMMAND_TYPE_MEASURE);
				cmd.iArgs = new int[2];
				cmd.iArgs[0] = Integer.parseInt(m.group(1));
				cmd.iArgs[1] = Integer.parseInt(m.group(2));
				if (cmd.iArgs[0]<0 || cmd.iArgs[0]>100 || cmd.iArgs[1]<0 || cmd.iArgs[1]>100)
					throwEx("#MEASURE arguments must be 1-99");
				iParsedCommands.add(cmd);
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
			float arg = Float.parseFloat(fields[1]);
			if (arg<0.1f || arg>16.0f)
				throwEx("#SCROLL arguments must be 0.1f - 16.0f");
			cmd.iArgs = new int[1];
			cmd.iArgs[0] = Float.floatToIntBits(arg);
			iParsedCommands.add(cmd);
		}
		else if (iLine.startsWith("#DELAY"))
		{
			fields = iLine.split("\\s");
			if (fields.length!=2)
				throwEx("Unknown #DELAY command");
			TJACommand cmd = new TJACommand(COMMAND_TYPE_DELAY);
			float arg = Float.parseFloat(fields[1]);
			arg = (float) (Math.floor(arg*1000)/1000);
			cmd.iArgs[0] = Float.floatToIntBits(arg);

			iParsedCommands.add(cmd);
		}
		else if (iLine.equals("#SECTION"))
		{
			iHasSection = true;
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_SECTION));
		}
		else if (iLine.startsWith("#BRANCHSTART"))
		{
			fields = iLine.substring(12).trim().split(",");
			if (fields.length!=3) throwEx("Unknown #BRANCHSTART command");
			fields[0] = fields[0].trim();
			if (fields[0].length()!=1) throwEx("Unknown #BRANCHSTART command");
			
			if (iIsBranchStarted)	// #BRANCHSTART again
				emitBranch();

			TJACommand cmd = new TJACommand(COMMAND_TYPE_BRANCHSTART);
			cmd.iArgs = new int[7];

			switch (fields[0].charAt(0))
			{
			case 'R':
				cmd.iArgs[0] = BRANCH_JUDGE_ROLL;
				break;
			case 'P':
				cmd.iArgs[0] = BRANCH_JUDGE_PRECISION;
				break;
			case 'S':
				cmd.iArgs[0] = BRANCH_JUDGE_SCORE;
				break;
			default:
				throwEx("Unknown #BRANCHSTART command");
			} 
			cmd.iArgs[1] = Float.floatToIntBits(Float.parseFloat(fields[1]));
			cmd.iArgs[2] = Float.floatToIntBits(Float.parseFloat(fields[2]));
			iParsedCommands.add(cmd);
			iIsBranchStarted = true;
			iCommandOfStartedBranch = cmd;
		}
		else if (iLine.equals("#N"))
		{
			if (!iIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_N));
			
			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[3])
				throwEx("Duplicated #N");
			iCommandOfStartedBranch.iArgs[3] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#E"))
		{
			if (!iIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_E));

			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[4])
				throwEx("Duplicated #E");
			iCommandOfStartedBranch.iArgs[4] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#M"))
		{
			if (!iIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_M));

			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[5])
				throwEx("Duplicated #M");
			iCommandOfStartedBranch.iArgs[5] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#BRANCHEND"))
		{
			emitBranch();
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_BRANCHEND));
			iIsBranchStarted = false;
		}
		else if (iLine.equals("#LEVELHOLD"))
		{
			iParsedCommands.add(new TJACommand(COMMAND_TYPE_LEVELHOLD));
		}
	} 
	
	private void emitNotation()
	{
		if (iParsedCommands.size()==0)
		{
			throwEx("No notes at all!");
		}
		else
		{
			TJACommand[] notation = iParsedCommands.toArray(
					new TJACommand[iParsedCommands.size()]);
			iParsedCommands.clear();

			boolean hasNote = false;
			for (TJACommand cmd:notation)
			{
				if (COMMAND_TYPE_NOTE == cmd.iCommandType)
				{
					hasNote = true;
					break;
				}
			}
			
			if (!hasNote)
				throwEx("No note at all!");

			if (!iIsParsingDouble)
				iParsingCourse.iNotationSingle = notation;
			else if (!iIsParsingP2)
			{
				iParsingCourse.iNotationP1 = notation;
				iIsParsingP2 = false;
			}
			else
			{
				iParsingCourse.iNotationP2 = notation;
				iIsParsingDouble = false;
			}
		}
		iIsStarted = false;
		iHasSection = false;
		iIsBranchStarted = false;
	}
	
	private void emitCourse()
	{
		iParsedCourses.add(iParsingCourse);
		iParsingCourse = new TJACourse();
		iIsParsingP2 = false;
		iIsParsingDouble = false;		
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
				iParsingNotes.put(c-'0');
			else if (Character.isSpace(c))
				continue;
			else if (c==',')
				emitNotes();
		}
	}

	private void emitNotes()
	{
		if (iParsingNotes.position()<=0) return;

		TJACommand cmd = new TJACommand(COMMAND_TYPE_NOTE);
		cmd.iArgs = new int[iParsingNotes.position()];
		System.arraycopy(
				iParsingNotes.array(), 0, cmd.iArgs, 0,
				iParsingNotes.position());
		iParsedCommands.add(cmd);
		iParsingNotes.clear();
	}
	
	private void setHeader(String name, String value)
	{
		// omit the empty value string
		if (value.length() == 0) return;
		
		if (name.equals("TITLE"))
			iTitle = new String(value);
		else if (name.equals("LEVEL"))
		{
			iParsingCourse.iLevel = Integer.parseInt(value);
			if (iParsingCourse.iLevel<1 || iParsingCourse.iLevel>12)
				throwEx("Bad level");
		}
		else if (name.equals("BPM"))
		{
			iParsingCourse.iBPM = Float.parseFloat(value);
			if (iParsingCourse.iBPM < 50 || iParsingCourse.iBPM > 250)
				throwEx("BPM must be 50-250");
		}
		else if (name.equals("WAVE"))
			iWave = new String(value);
		else if (name.equals("OFFSET"))
			iOffset = Float.parseFloat(value);
		else if (name.equals("BALLOON"))
		{
			String[] balloons = value.split(",");
			iParsingCourse.iBalloon = new int[balloons.length];
			for (int i=0; i<iParsingCourse.iBalloon.length; ++i)
				iParsingCourse.iBalloon[i] = Integer.parseInt(balloons[i].trim()); 
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
			iParsingCourse.iScoreInit = Integer.parseInt(value);
			if (iParsingCourse.iScoreInit<0)
				throwEx("SCOREINIT must be positive");
		}
		else if (name.equals("SCOREDIFF"))
		{
			iParsingCourse.iScoreDiff = Integer.parseInt(value);
			if (iParsingCourse.iScoreDiff<0)
				throwEx("SCOREDIFF must be positive");
		}
		else if (name.equals("COURSE"))
		{
			if (	iParsingCourse.iNotationSingle != null ||
					iParsingCourse.iNotationP1 != null &&
					iParsingCourse.iNotationP2 != null)
			{
				emitCourse();
			}
			
			if (iParsingCourse.iNotationP1 != null &&
				iParsingCourse.iNotationP2 == null)
			{
				throwEx("Missing #START P2");
			}
			
			value = value.trim();
			
			if (value.equalsIgnoreCase("Easy") || value.equals(COURSE_EASY))
				iParsingCourse.iCourse = COURSE_EASY;
			
			else if (value.equalsIgnoreCase("Normal") || value.equals(COURSE_NORMAL))
				iParsingCourse.iCourse = COURSE_NORMAL;
			
			else if (value.equalsIgnoreCase("Hard") || value.equals(COURSE_HARD))
				iParsingCourse.iCourse = COURSE_HARD;
			
			else if (value.equalsIgnoreCase("Oni") || value.equals(COURSE_ONI))
				iParsingCourse.iCourse = COURSE_ONI;
			
			else if (value.equalsIgnoreCase("Edit") || value.equals(COURSE_EDIT))
				iParsingCourse.iCourse = COURSE_EDIT;

			else if (value.equalsIgnoreCase("Tower") || value.equals(COURSE_TOWER))
				iParsingCourse.iCourse = COURSE_TOWER;
			
			else throwEx("Unknown course");
		}
		else if (name.equals("STYLE"))
		{
			if (value.equalsIgnoreCase("Single"))
				iIsParsingDouble = false;
			else if (value.equalsIgnoreCase("Double") || value.equalsIgnoreCase("Couple"))
				iIsParsingDouble = true;
			else
				throwEx("STYLE must be Single, Double or Couple");
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
	
	private void emitBranch()
	{
		if (iCommandOfStartedBranch.iArgs[3]==0)
			throwEx("Missing #N");
		else if (iCommandOfStartedBranch.iArgs[4]==0)
			throwEx("Missing #E");
		else if (iCommandOfStartedBranch.iArgs[5]==0)
			throwEx("Missing #M");

		iCommandOfStartedBranch.iArgs[6] = iParsedCommands.size();
	}
}
