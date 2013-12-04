package com.kroobroeck;

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
		if ( args.length == 3 )
		{
			String localization = args[0];
			String original = args[1];
			String outputdirectory = args[2];

			HashMap<String, List<Inspect>> valuesMap = new HashMap<String, List<Inspect>>();
			HashMap<String, List<Inspect>> localizationMap = new HashMap<String, List<Inspect>>();

			scanTranslations(valuesMap, original);
			scanTranslations( localizationMap, localization );

			boolean hasError = false;

			for ( String key : valuesMap.keySet() )
			{
				if ( valuesMap.get( key ).size() > 2 )
				{
					hasError = true;

					System.out.println( "File " + original );
					System.out.println( "\tContains " + valuesMap.get( key ).size() + " name " + key );
					System.out.println( "\tDetails:" );
					for( Inspect inspect : valuesMap.get( key ) )
					{
						System.out.println( "\t\t"+inspect.toString());
					}
				}
				if ( !localizationMap.containsKey( key ) )
				{
					hasError = true;

					System.out.println( "File " + localization );
					System.out.println( "\tDoesn't contain " + key );
					System.out.println( "\tLinenumber " + valuesMap.get( key ).get( 0 ).lineNumber + " in " + original);
				}
			}

			for ( String key : localizationMap.keySet() )
			{
				if ( localizationMap.get( key ).size() > 2 )
				{
					hasError = true;

					System.out.println( "File " + localization );
					System.out.println( "\tContains " + localizationMap.get( key ).size() + " name " + key );
					System.out.println( "\tDetails:" );
					for( Inspect inspect : localizationMap.get( key ) )
					{
						System.out.println( "\t\t"+inspect.toString());
					}
				}
				if ( !valuesMap.containsKey( key ) )
				{
					hasError = true;

					System.out.println( "File " + original );
					System.out.println( "\tDoesn't contain " + key );
					System.out.println( "\tLinenumber " + localizationMap.get( key ).get( 0 ).lineNumber + " in " + localization);
				}
			}

			if (!hasError)
			{
				System.out.println("No errors found, this is the newly sorted localizations string.xml file:\n\n");
				sortLocalizations( localizationMap, outputdirectory );
			}
		} else
		{
			System.out.println( "Usage: " );
			System.out.println( "\t<path localization strings.xml file> <Path resources values strings.xml file> <Output Path of reformated strings.xml file>" );
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
			Pattern pattern = Pattern.compile( "<string name=\"(.*?)(_.*)\">(.*)</string>" );
			Matcher matcher = pattern.matcher(line);

			while(matcher.find())
			{
				inspect = new Inspect();
				inspect.lineNumber = lineNumber;
				inspect.group = matcher.group(1);
				inspect.name = matcher.group(1) + matcher.group(2);
				inspect.value = matcher.group(3);
				inspect.line = line;

				List<Inspect> inspectList = null;
				if ( map.containsKey( inspect.name ))
				{
					inspectList = map.get( inspect.name );
				}else{
					inspectList = new ArrayList<Inspect>(  );
				}
				inspectList.add( inspect );
				map.put( inspect.name.toLowerCase(), inspectList );
			}
		}
	}

	private static void sortLocalizations( HashMap<String, List<Inspect>> map, String outputdirectory) throws FileNotFoundException, UnsupportedEncodingException
	{
		PrintWriter writer = new PrintWriter(outputdirectory, "UTF-8");

		Map<String,List<Inspect>> sortedMap = new TreeMap<String,List<Inspect>>( map );
		String previousPrefix = null;

		writer.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
		writer.println("<resources>");

		for( String key : sortedMap.keySet())
		{

			if ( previousPrefix == null || !sortedMap.get( key ).get( 0 ).group.equals( previousPrefix ))
			{
				previousPrefix = sortedMap.get( key ).get( 0 ).group;
				writer.println( "\n\t<!-- " + previousPrefix.toUpperCase() +" -->");
			}
			writer.println( "\t" + sortedMap.get( key ).get( 0 ).line.trim());

		}

		writer.println("</resources>");
		writer.close();
	}


}
