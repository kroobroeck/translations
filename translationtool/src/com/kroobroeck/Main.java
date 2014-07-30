package com.kroobroeck;

import com.beust.jcommander.JCommander;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
	public static void main( String[] args ) throws Exception
	{
		CommandLineOptions commandLineOptions = new CommandLineOptions();
		new JCommander( commandLineOptions, args );

		HashMap<String, List<Inspect>> originMap = new HashMap<String, List<Inspect>>();
		if ( commandLineOptions.origin != null )
		{
			scanTranslations( originMap, commandLineOptions.origin );
            
            if( commandLineOptions.sort ) {
                sortLocalizations(originMap, commandLineOptions.origin);
            }
		}
	}

	private static void scanTranslations( HashMap<String, List<Inspect>> map, String fileName ) throws Exception
	{
		File file = new File( fileName );
		Scanner in = new Scanner( file );

		String line = null;
		int lineNumber = 0;
		while ( in.hasNextLine() )
		{
			Inspect inspect = null;

			lineNumber++;

			line = in.nextLine();
			Pattern pattern = Pattern.compile( "<string name=\"(.*?)(_.*?)\".*?\"?(.*?)\"?>(.*)</string>" );
			Matcher matcher = pattern.matcher( line );

			while ( matcher.find() )
			{
				inspect = new Inspect();
				inspect.lineNumber = lineNumber;
				inspect.group = matcher.group( 1 );
				inspect.name = matcher.group( 1 ) + matcher.group( 2 );
				inspect.value = matcher.group( 4 );
				inspect.line = line;
				//inspect.formatted = matcher.group( 3 ).contains( "false" ) ? Formatted.FALSE : matcher.group( 3 ).equals( "" ) ? Formatted.UNDEFINED : Formatted.TRUE;

				List<Inspect> inspectList = null;
				if ( map.containsKey( inspect.name ) )
				{
					inspectList = map.get( inspect.name );
				} else
				{
					inspectList = new ArrayList<Inspect>();
				}
				inspectList.add( inspect );
				map.put( inspect.name.toLowerCase(), inspectList );
			}
		}
	}

	private static void sortLocalizations( HashMap<String, List<Inspect>> map, String outputdirectory ) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter( outputdirectory, "UTF-8" );

		Map<String, List<Inspect>> sortedMap = new TreeMap<String, List<Inspect>>( map );
		String previousPrefix = null;

		writer.println( "<?xml version=\"1.0\" encoding=\"utf-8\"?>" );
		writer.println( "<resources>" );

		for ( String key : sortedMap.keySet() )
		{

			if ( previousPrefix == null || !sortedMap.get( key ).get( 0 ).group.equals( previousPrefix ) )
			{
				previousPrefix = sortedMap.get( key ).get( 0 ).group;
				writer.println( "\n\t<!-- " + previousPrefix.toUpperCase() + " -->" );
			}
			writer.println( "\t<string name=\"" + key + "\"" + /*sortedMap.get( key ).get( 0 ).formatted.getTag() +*/ ">" + sortedMap.get( key ).get( 0 ).value + "</string>" );
		}

		writer.println( "</resources>" );
		writer.close();
	}
}
