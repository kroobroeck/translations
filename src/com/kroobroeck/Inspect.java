package com.kroobroeck;

/**
 * Created by the awesome :
 * User: klaas
 */
public class Inspect
{
	public String line;
	public String name;
	public String value;
	public int lineNumber;
	public String group;

	@Override
	public String toString()
	{
		return 	"On Linenumber: " + lineNumber + " " + line;
	}
}