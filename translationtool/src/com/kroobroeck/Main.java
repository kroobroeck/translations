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

		if ( commandLineOptions.origin != null )
		{
			scanTranslations( originMap, commandLineOptions.origin );
			scanTranslations( localMap, commandLineOptions.local );

			//Scan for wrong formatting of local file (%% or ||)
			scanForWrongFormatting( localMap, commandLineOptions.local );

			boolean localHasError = false;
			boolean originHasError = false;

			//Cross evaluation, check for errors
			for ( String key : originMap.keySet() )
			{
				if ( originMap.get( key ).size() > 2 )
				{
					localHasError = true;

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
					localHasError = true;

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
					originHasError = true;

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
					originHasError = true;

					System.out.println( "File " + commandLineOptions.origin );
					System.out.println( "\tDoesn't contain " + key );
					System.out.println( "\tLinenumber " + localMap.get( key ).get( 0 ).lineNumber + " in " + commandLineOptions.local + "\n" );
				} else if ( originMap.get( key ).get( 0 ).formatted != localMap.get( key ).get( 0 ).formatted )
				{
					System.out.println( "File " + commandLineOptions.origin );
					System.out.println( "\tKey " + key + " doesn't match the formatted value" + localMap.get( key ).get( 0 ).formatted.getTag() + "\n\tin " + commandLineOptions.local );
					System.out.println( "\tLinenumber " + localMap.get( key ).get( 0 ).lineNumber + " in " + commandLineOptions.local + "\n" );
				}
			}

			//If no errors occured and --format is set, middle is reformatted.
			if ( !localHasError && !originHasError && commandLineOptions.format )
			{
				System.out.println( "The newly sorted localizations file can be found " + commandLineOptions.local + "\n" );
				sortLocalizations( localMap, commandLineOptions.local );
			} else if ( localHasError && commandLineOptions.sync )
			{
				System.out.println( "Synced localization with orginal, newly sorted localizations file can be found " + commandLineOptions.local + "\n" );
				sortLocalizations( localMap, commandLineOptions.local );
			}
		} else
		{

			scanTranslations( localMap, commandLineOptions.local );
			//Scan for wrong formatting of local file (%% or ||)
			scanForWrongFormatting( localMap, commandLineOptions.local );

			if ( commandLineOptions.format )
			{
				sortLocalizations( localMap, commandLineOptions.local );
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
				inspect.formatted = matcher.group( 3 ).contains( "false" ) ? Formatted.FALSE : matcher.group( 3 ).equals( "" ) ? Formatted.UNDEFINED : Formatted.TRUE;

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
			writer.println( "\t<string name=\"" + key + "\"" + sortedMap.get( key ).get( 0 ).formatted.getTag() + ">" + sortedMap.get( key ).get( 0 ).value + "</string>" );
		}

		writer.println( "</resources>" );
		writer.close();
	}

	private static void scanForWrongFormatting( HashMap<String, List<Inspect>> localMap, String local )
	{
		Pattern pattern = Pattern.compile( "(^%%(.*?)%%)|(^\\|\\|(.*?)\\|\\|)" );

		for ( String key : localMap.keySet() )
		{
			Matcher matcher = pattern.matcher( (localMap.get( key ).get( 0 ).value) );

			if ( !matcher.find() )
			{
				System.out.println( "File " + local );
				System.out.println( "\tThe value of " + key + " has wrong formatting (no %% or || at beginning and/or end)" );
				System.out.println( "\tLinenumber " + localMap.get( key ).get( 0 ).lineNumber + " in " + local + "\n" );
			}
		}
	}
}
