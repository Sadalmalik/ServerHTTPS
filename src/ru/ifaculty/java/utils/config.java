/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class config
	{
	private	config(){}
	public	static LinkedHashMap<String,String> load( String name )
		{
		if( name==null ) return( null );
		File conf = new File(name);
		if( !conf.exists() )	{	return( null );	}
		LinkedHashMap<String,String>map = new LinkedHashMap<String,String>();
		try	{
			String line, pair[];
			BufferedReader READER = new BufferedReader(	new FileReader(conf)	);
			while ( (line = READER.readLine()) != null )
				{
				if( line.isEmpty() )		{	continue;	}
				if( line.charAt(0)=='#' )	{	continue;	}
				pair = line.split("=",2);
				map.put(pair[0].trim(),pair[1].trim());
				}
			READER.close();
			return( map );
			}
		catch (FileNotFoundException e)	{	e.printStackTrace();	}
		catch (IOException e)			{	e.printStackTrace();	}
		return( null );
		}
	public	static void save( String name , Map<String,String>map )
		{
		if( map==null ) return;
		if( name==null ) return;
		File conf = new File(name);
		try {
			BufferedWriter WRITER = new BufferedWriter(	new FileWriter(conf)	);
			Entry<String,String>entr=null;
			Iterator<Entry<String,String>>iter = map.entrySet().iterator();
			while( iter.hasNext() )
				{
				entr = iter.next();
				WRITER.write(entr.getKey()+" = "+entr.getValue()+"\r\n");
				}
			WRITER.close();
			}
		catch (IOException e)	{	e.printStackTrace();	}
		}
	}
