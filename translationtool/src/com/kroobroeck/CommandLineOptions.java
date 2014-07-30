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
	public List<String> parameters = new ArrayList<String>();

	@Parameter(names = {"--origin", "-o"}, description = "The path to the original strings.xml file")
	public String origin;

	@Parameter(names = {"--sync", "-s"}, description = "Sort the xml file according to the string names")
	public boolean sort = false;
}
