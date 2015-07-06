package ru.simsonic.rscCommonsLibrary;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeIntervalParser
{
	public static int parseTimeInterval(final String Argument)
	{
		if(Argument.matches("^\\d+$"))
			return Integer.parseInt(Argument);
		final Matcher match = timeIntervalPattern.matcher(Argument);
		int interval = 0;
		while(match.find())
			interval += Math.round(Float.parseFloat(match.group(1)) * parseElement(match.group(2)));
		return interval;
	}
	private static final Pattern timeIntervalPattern = Pattern.compile(
		"((?:\\d+)|(?:\\d+\\.\\d+))\\s*(second|sec|minute|min|hour|day|week|month|year|s|m|h|d|w|y)");
	private static int parseElement(String suffix)
	{
		if(suffix == null || "".equals(suffix))
			return 0;
		switch(suffix.toLowerCase())
		{
		case "second":
		case "sec":
		case "s":
			return 1;
		case "minute":
		case "min":
		case "m":
			return 60;
		case "hour":
		case "h":
			return 60 * 60;
		case "day":
		case "d":
			return 60 * 60 * 24;
		case "week":
		case "w":
			return 60 * 60 * 24 * 7;
		case "month":
			return 60 * 60 * 24 * 31;
		case "year":
		case "y":
			return 60 * 60 * 24 * 365;
		}
		return 0;
	}
}
