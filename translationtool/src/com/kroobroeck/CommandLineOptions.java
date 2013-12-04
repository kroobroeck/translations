package com.kroobroeck;


import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by the awesome :
 * User: klaas
 * Date: 04/12/13
 * Time: 13:13
 */
public class CommandLineOptions
{
	@Parameter
	private List<String> parameters = new ArrayList<String>();

	@Parameter(names = {"-format" }, description = "Format the middle strings.xml file.")
	public boolean format = false;

	@Parameter(names = {"-origin"}, description = "The path to the original strings.xml file")
	public String origin;

	@Parameter(names = {"-local"}, description = "The path to the local strings.xml file")
	public String local;

	@Parameter(names = {"-sync"}, description = "Sync the origin with the local file when certain tags aren't available in the local strings.xml")
	public boolean sync = false;
}
