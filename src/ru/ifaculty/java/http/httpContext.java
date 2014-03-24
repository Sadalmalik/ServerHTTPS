/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class httpContext
	{
	public HashMap		<String,String>reqHeaders=new HashMap		<String,String>();	//	заголовки запроса
	public LinkedHashMap<String,String>resHeaders=new LinkedHashMap	<String,String>();	//	заголовки ответа, LinkedHashMap для порядка заголовков
	public HashMap		<String,Object>otherStuff=new HashMap		<String,Object>();	//	Барахло
	public httpCookie	cookie	=	null	;
	public byte[]		reqBody	=	null	;
	public String		METHOD	=	null	;
	public String		URI		=	null	;
	public String		VERSION	=	null	;
	public HashMap		<String,String>data__GET=new HashMap		<String,String>();	//	Данные GET запроса. Чистро строки
	public HashMap		<String,Object>data_POST=new HashMap		<String,Object>();	//	Данные POST запроса - строки и файлы ( смотри класс inFile )
	public int			status	=	200		;
	public boolean		flush	=	false	;
	public void clear()
		{
		reqHeaders.clear();
		resHeaders.clear();
		otherStuff.clear();
		cookie	=	null	;
		reqBody	=	null	;
		METHOD	=	null	;
		URI		=	null	;
		VERSION	=	null	;
		data__GET.clear();
		data_POST.clear();
		status	=	200		;
		flush	=	false	;
		resBody.delete(0,resBody.length());
		}
	public String getStartLine()
		{
		switch( status )
			{
			case 200:return"HTTP/1.1 200 OK";
			case 303:return"HTTP/1.1 303 See Other";
			case 400:return"HTTP/1.1 400 Let me go, wonderful grass!";
			case 501:return"HTTP/1.1 501 Not Implemented";
			case 503:return"HTTP/1.1 503 Service Unavailable";
			case 505:return"HTTP/1.1 505 HTTP Version Not Supported";
			default	:return"HTTP/1.1 500 I have no idea WTF i must to do T___T";
			}
		}
	public void contentTypeByExtension( String ext )
		{
		if( ext.toLowerCase().equals("html") )	resHeaders.put("Content-Type","text/html; charset=utf-8");
		if( ext.toLowerCase().equals("css") )	resHeaders.put("Content-Type","text/css");
		if( ext.toLowerCase().equals("js") )	resHeaders.put("Content-Type","text/javascript");
		if( ext.toLowerCase().equals("jpg") )	resHeaders.put("Content-Type","image/jpg");
		if( ext.toLowerCase().equals("png") )	resHeaders.put("Content-Type","image/png");
		if( ext.toLowerCase().equals("bmp") )	resHeaders.put("Content-Type","image/bmp");
		}
	public StringBuilder resBody=new StringBuilder();
	public void write( String text )	{	if( !flush )	resBody.append( text ) ;			}
	public void write( byte[]text )		{	if( !flush )	resBody.append( text ) ;			}
	public void end( String text )		{	if( !flush )	resBody.append( text ) ;	end();	}
	public void end(  )					{	flush = true;	}
	}
