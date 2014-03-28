/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class inFile
	{
	private	static	String	folder		=	null	;
	public	static	void	SetFolder( String name )
		{
		if( name!=null )
			{
			folder=name;
			File f = new File( folder );
			if( !f.isDirectory() )f.mkdir();
			}
		}
	static	{	SetFolder("temp/");	}
	public	static	final	Pattern	extension	=	Pattern.compile("(\\.\\w+)$")	;
	public	inFile( String name , String mime )
		{
		String date = Long.toHexString(System.currentTimeMillis()) ;
		StringBuilder file=new StringBuilder(folder);	file.append("temp");
		for( int i=file.length() ; i<16 ; i++ )file.append('0');
		file.append( date );
		Matcher m = extension.matcher(name);
		if( m.find() )	file.append( m.group(1) );
		else			file.append( ".bin" );
		this.temp = new File( file.toString() );
		this.name = name ;	this.mime = mime ;
		}
	public	String	name=null;
	public	String	mime=null;
	public	File	temp=null;
	public	String	toString()
		{
		return( "[ file : name '"+name+"' , mime '"+mime+"' , temp '"+temp.getName()+"' ]" );
		}
	}
