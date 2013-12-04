package com.kroobroeck;

/**
 * Created by the awesome :
 * User: klaas
 * Date: 04/12/13
 * Time: 16:39
 */
public enum Formatted
{
	TRUE( " formatted=\"true\"" ),
	FALSE( " formatted=\"false\"" ),
	UNDEFINED( "" );

	private String tag;

	private Formatted( String tag )
	{
		this.tag = tag;
	}

	public String getTag()
	{
		return tag;
	}
}
