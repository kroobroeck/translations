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
		//Parse commands
		CommandLineOptions commandLineOptions = new CommandLineOptions();
		new JCommander( commandLineOptions, args );

		HashMap<String, List<Inspect>> originMap = new HashMap<String, List<Inspect>>();
		HashMap<String, List<Inspect>> localMap = new HashMap<String, List<Inspect>>();

		scanTranslations( originMap, commandLineOptions.origin );
		scanTranslations( localMap, commandLineOptions.local );

		//Cross evaluation, check for errors
		boolean hasError = false;

		for ( String key : originMap.keySet() )
		{
			if ( originMap.get( key ).size() > 2 )
			{
				hasError = true;

				System.out.println( "File " + commandLineOptions.origin );
				System.out.println( "\tContains " + originMap.get( key ).size() + " name " + key );
				System.out.println( "\tDetails:" );
				for ( Inspect inspect : originMap.get( key ) )
				{
					System.out.println( "\t\t" + inspect.toString() );
				}
				System.out.print( "\n" );
			}
			if ( !localMap.containsKey( key ) )
			{
				hasError = true;

				System.out.println( "File " + commandLineOptions.local );
				System.out.println( "\tDoesn't contain " + key );
				System.out.println( "\tLinenumber " + originMap.get( key ).get( 0 ).lineNumber + " in " + commandLineOptions.origin + "\n" );

				if ( commandLineOptions.sync )
				{
					//If sync is on, write to localizations file
					localMap.put( key, originMap.get( key ) );
					localMap.get( key ).get( 0 ).value = "%%" + localMap.get( key ).get( 0 ).value + "%%";
				}
			}
		}

		for ( String key : localMap.keySet() )
		{
			if ( localMap.get( key ).size() > 2 )
			{
				hasError = true;

				System.out.println( "File " + commandLineOptions.local );
				System.out.println( "\tContains " + localMap.get( key ).size() + " name " + key );
				System.out.println( "\tDetails:" );
				for ( Inspect inspect : localMap.get( key ) )
				{
					System.out.println( "\t\t" + inspect.toString() );
				}
				System.out.print( "\n" );
			}
			if ( !originMap.containsKey( key ) )
			{
				hasError = true;

				System.out.println( "File " + commandLineOptions.origin );
				System.out.println( "\tDoesn't contain " + key );
				System.out.println( "\tLinenumber " + localMap.get( key ).get( 0 ).lineNumber + " in " + commandLineOptions.local + "\n" );
			}
		}

		//If no errors occured and -format is set, middle is reformatted.
		if ( !hasError && commandLineOptions.format )
		{
			System.out.println( "No errors found, the newly sorted localizations file can be found " + commandLineOptions.local + "\n\n" );
			sortLocalizations( localMap, commandLineOptions.local );
		} else if ( hasError && commandLineOptions.sync )
		{
			System.out.println( "Errors found, synced localization with orginal, newly sorted localizations file can be found " + commandLineOptions.local + "\n\n" );
			sortLocalizations( localMap, commandLineOptions.local );
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
			Pattern pattern = Pattern.compile( "<string name=\"(.*?)(_.*?)\".*?>(.*)</string>" );
			Matcher matcher = pattern.matcher( line );

			while ( matcher.find() )
			{
				inspect = new Inspect();
				inspect.lineNumber = lineNumber;
				inspect.group = matcher.group( 1 );
				inspect.name = matcher.group( 1 ) + matcher.group( 2 );
				inspect.value = matcher.group( 3 );
				inspect.line = line;

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
			writer.println( "\t<string name=\"" + key + "\" formatted=\"false\">" + sortedMap.get( key ).get( 0 ).value + "</string>" );
		}

		writer.println( "</resources>" );
		writer.close();
	}
}
