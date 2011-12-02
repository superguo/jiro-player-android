package com.superguo.jiroplayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.superguo.jiroplayer.TJAFormat.*;

public final class TJAFormatParser
{
	// parse states
	private float 				  iBPM;
	private TJAFormat 			  iFormat;
	private int					  iLineNo;
	private String				  iLine;
	private LinkedList<TJACourse> iParsedCourses 	= new LinkedList<TJACourse>();
	private LinkedList<TJACommand>  iParsedCommands;
	private TJACourse 			  iParsingCourse;
	private IntBuffer			  iParsingNotes;
	private boolean				  iIsParsingDouble;
	private boolean				  iIsParsingP2;
	private boolean				  iIsStarted;
	private boolean				  iIsGoGoStarted;
	private boolean				  iIsBranchStarted;
	private TJACommand  iCommandOfStartedBranch;
	
	public void parse(TJAFormat format, BufferedReader reader) throws IOException
	{
		iFormat = format;
		iLineNo = 0;
		iParsingCourse = new TJACourse();
		iParsedCommands = new LinkedList<TJACommand>();
		iParsingNotes = IntBuffer.wrap(new int[500]);
		System.gc();

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
			format.iCourses = iParsedCourses.toArray(new TJACourse[iParsedCourses.size()]);
	}
	
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

	private void emitNotes()
	{
		if (iParsingNotes.position()<=0) return;

		TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_NOTE);
		cmd.iArgs = new int[iParsingNotes.position()];
		System.arraycopy(
				iParsingNotes.array(), 0, cmd.iArgs, 0,
				iParsingNotes.position());
		iParsedCommands.add(cmd);
		iParsingNotes.clear();
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
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_BPMCHANGE));
		}
		else if (iLine.equals("#GOGOSTART"))
		{
			if (iIsGoGoStarted) throwEx("Already exist #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = true;
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_GOGOSTART));
		}
		else if (iLine.equals("#GOGOEND"))
		{
			if (!iIsGoGoStarted) throwEx("Missing #GOGOSTART before #GOGOEND");
			iIsGoGoStarted = false;
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_GOGOEND));
		}
		else if (iLine.startsWith("#MEASURE"))
		{
			Pattern p = Pattern.compile("#MEASURE\\s+(\\d+)\\s*/\\s*(\\d+)");
			Matcher m = p.matcher(iLine);
			if (m.find())
			{
				TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_MEASURE);
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
			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_SCROLL);
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
			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_DELAY);
			float arg = Float.parseFloat(fields[1]);
			arg = (float) (Math.floor(arg*1000)/1000);
			cmd.iArgs[0] = Float.floatToIntBits(arg);

			iParsedCommands.add(cmd);
		}
		else if (iLine.equals("#SECTION"))
		{
			iParsingCourse.iHasBranch = true;
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_SECTION));
		}
		else if (iLine.startsWith("#BRANCHSTART"))
		{
			iParsingCourse.iHasBranch = true;	// #SECTION may be missing
			fields = iLine.substring(12).trim().split(",");
			if (fields.length!=3) throwEx("Unknown #BRANCHSTART command");
			fields[0] = fields[0].trim();
			if (fields[0].length()!=1) throwEx("Unknown #BRANCHSTART command");
			
			if (iIsBranchStarted)	// #BRANCHSTART again
				emitBranch();

			TJACommand cmd = new TJACommand(TJAFormat.COMMAND_TYPE_BRANCHSTART);
			cmd.iArgs = new int[7];

			switch (fields[0].charAt(0))
			{
			case 'R':
				cmd.iArgs[0] = TJAFormat.BRANCH_JUDGE_ROLL;
				break;
			case 'P':
				cmd.iArgs[0] = TJAFormat.BRANCH_JUDGE_PRECISION;
				break;
			case 'S':
				cmd.iArgs[0] = TJAFormat.BRANCH_JUDGE_SCORE;
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
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_N));
			
			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[3])
				throwEx("Duplicated #N");
			iCommandOfStartedBranch.iArgs[3] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#E"))
		{
			if (!iIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_E));

			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[4])
				throwEx("Duplicated #E");
			iCommandOfStartedBranch.iArgs[4] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#M"))
		{
			if (!iIsBranchStarted)
				throwEx("Missing #BRANCHSTART");
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_M));

			// fill back
			if (0!=iCommandOfStartedBranch.iArgs[5])
				throwEx("Duplicated #M");
			iCommandOfStartedBranch.iArgs[5] = iParsedCommands.size()-1;
		}
		else if (iLine.equals("#BRANCHEND"))
		{
			emitBranch();
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_BRANCHEND));
			iIsBranchStarted = false;
		}
		else if (iLine.equals("#LEVELHOLD"))
		{
			iParsedCommands.add(new TJACommand(TJAFormat.COMMAND_TYPE_LEVELHOLD));
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
				if (TJAFormat.COMMAND_TYPE_NOTE == cmd.iCommandType)
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
		iIsBranchStarted = false;
	}
	
	private void emitCourse()
	{
		iParsedCourses.add(iParsingCourse);
		iParsingCourse = new TJACourse();
		iParsingCourse.iBPM = iBPM;
		iIsParsingP2 = false;
		iIsParsingDouble = false;		
		iIsStarted = false;
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

	
	private void setHeader(String name, String value)
	{
		// omit the empty value string
		if (value.length() == 0) return;
		
		if (name.equals("TITLE"))
			iFormat.iTitle = new String(value);
		else if (name.equals("LEVEL"))
		{
			iParsingCourse.iLevel = Integer.parseInt(value);
			if (iParsingCourse.iLevel<1 || iParsingCourse.iLevel>12)
				throwEx("Bad level");
		}
		else if (name.equals("BPM"))
		{
			iParsingCourse.iBPM = iBPM = Float.parseFloat(value);
			if (iParsingCourse.iBPM < 50 || iParsingCourse.iBPM > 250)
				throwEx("BPM must be 50-250");
		}
		else if (name.equals("WAVE"))
			iFormat.iWave = new String(value);
		else if (name.equals("OFFSET"))
			iFormat.iOffset = Float.parseFloat(value);
		else if (name.equals("BALLOON"))
		{
			String[] balloons = value.split(",");
			iParsingCourse.iBalloon = new int[balloons.length];
			for (int i=0; i<iParsingCourse.iBalloon.length; ++i)
				iParsingCourse.iBalloon[i] = Integer.parseInt(balloons[i].trim()); 
		}
		else if (name.equals("SONGVOL"))
		{
			iFormat.iSongVol = Float.parseFloat(value);
			if (iFormat.iSongVol<0 || iFormat.iSongVol>100)
				throwEx("SONGVOL must be 0-100");
		}
		else if (name.equals("SEVOL"))
		{
			iFormat.iSeVol = Float.parseFloat(value);
			if (iFormat.iSeVol<0 || iFormat.iSeVol>100)
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
			
			if (value.equalsIgnoreCase("Easy") || value.equals(Integer.toString(TJAFormat.COURSE_EASY)))
				iParsingCourse.iCourse = TJAFormat.COURSE_EASY;
			
			else if (value.equalsIgnoreCase("Normal") || value.equals(Integer.toString(TJAFormat.COURSE_NORMAL)))
				iParsingCourse.iCourse = TJAFormat.COURSE_NORMAL;
			
			else if (value.equalsIgnoreCase("Hard") || value.equals(Integer.toString(TJAFormat.COURSE_HARD)))
				iParsingCourse.iCourse = TJAFormat.COURSE_HARD;
			
			else if (value.equalsIgnoreCase("Oni") || value.equals(Integer.toString(TJAFormat.COURSE_ONI)))
				iParsingCourse.iCourse = TJAFormat.COURSE_ONI;
			
			else if (value.equalsIgnoreCase("Edit") || value.equals(Integer.toString(TJAFormat.COURSE_EDIT)))
				iParsingCourse.iCourse = TJAFormat.COURSE_EDIT;

			else if (value.equalsIgnoreCase("Tower") || value.equals(Integer.toString(TJAFormat.COURSE_TOWER)))
				iParsingCourse.iCourse = TJAFormat.COURSE_TOWER;
			
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
			iFormat.iDemoStart = Float.parseFloat(value);
		else if (name.equals("SIDE"))
		{
			if (value.equalsIgnoreCase("Normal") || value.equals(TJAFormat.SIDE_NORMAL))
				iFormat.iSide = TJAFormat.SIDE_NORMAL;
			
			else if (value.equalsIgnoreCase("Ex") || value.equals(TJAFormat.SIDE_EX))
				iFormat.iSide = TJAFormat.SIDE_EX;
			
			else if (value.equalsIgnoreCase("Both") || value.equals(TJAFormat.SIDE_BOTH))
				iFormat.iSide = TJAFormat.SIDE_BOTH;
		}
		else if (name.equals("SUBTITLE"))
			iFormat.iSubTitle = new String(value);
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