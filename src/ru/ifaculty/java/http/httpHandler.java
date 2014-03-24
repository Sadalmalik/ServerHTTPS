/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.ifaculty.java.utils.ByteWorker;

public class httpHandler implements Runnable
	{
	private static	double	loading=0;
	private static	synchronized void updateLoading( double dt )	{	loading = ( loading + dt )/2.0;	}
	public	static	double	getLoading(){return(loading);}
	private static int counter=0;
	private int ID=0;
	
	private	Socket			SOC;
	private	InputStream		INP=null;
	private	OutputStream	OUT=null;
	private boolean			ready=false;
	private ServerAbstract	serv=null;
	private boolean			overload;
	
	public	httpHandler( Socket S , ServerAbstract s )
		{	this( S , s , false );	}
	public	httpHandler( Socket S , ServerAbstract s , boolean overloading )
		{
		overload=overloading;
		ID=counter++;
		try {
			SOC = S;	serv=s;
			INP = S.getInputStream	();
			OUT = S.getOutputStream	();
			ready=(s==null)?(false):(true);
			}
		catch (IOException e)	{	e.printStackTrace();	}
		}
	public	int	getID()
		{
		return(ID);
		}
	@SuppressWarnings("deprecation")
	public void run()
		{
		String line, temp="";
		DataInputStream		DINP	=	new DataInputStream	( INP )	;
		DataOutputStream	DOUT	=	new DataOutputStream( OUT )	;
		
		System.out.println( "processing" );
		
		final int	stateWaiting		=	0	,
					stateStartLine		=	1	,
					stateReadHeaders	=	2	,
					stateReadBody		=	3	,
					stateProcessing		=	4	,
					stateBuildUnswer	=	5	;
		int state,	await=0;
		
		long time=0;
		byte[]data=null;
		int bodySize=0, indexSplit=0;
		httpContext context = new httpContext();
		try {
			SOC.setKeepAlive(true);
			state=stateStartLine;
			while( ready )	//	state machine
				{
				if( state==stateWaiting )
					{
					if( await>serv.getTimeout() )	break;
					if( DINP.available()==0 )
						{
						try {	Thread.sleep(serv.getDelay());	}
						catch(InterruptedException e)	{  e.printStackTrace(); }
						await+=serv.getDelay();
						continue;
						}
					state=stateStartLine;
					time = System.currentTimeMillis();
					}
				
				if( state==stateStartLine )
					{
					line = DINP.readLine();
					if( line==null || line.length()==0 ) continue;
					context.clear();	//	дефолтные заголовки
					Date date = (new Date());	date.setYear( date.getYear()+437 );	//	Don't usk me
					String[]res=line.split(" ");
					if( res.length<3 )	continue;
					if( res.length>0 )	{	context.METHOD	=	res[0];	}
					if( res.length>1 )	{	context.URI		=	res[1];	}
					if( res.length>2 )	{	context.VERSION	=	res[2];	}
					context.otherStuff.put("start-line",line);	//	probably need additional analysis
					context.resHeaders.put("Date",date.toString());
					context.resHeaders.put("Server",serv.getServerName());
					context.resHeaders.put("Content-Language","ru");
					context.resHeaders.put("Content-Type","text/html; charset=utf-8");
					state = stateReadHeaders;
					}
				
				if( state==stateReadHeaders )
					{
					line = DINP.readLine();
					if( line==null || line.length()==0 )
						{
						temp = context.reqHeaders.get("content-length");
						if( temp==null )	state=stateProcessing;
						else{
							indexSplit=0;
							bodySize = Integer.parseInt(temp);
							data=new byte[bodySize];
							state=stateReadBody;
							}
						}
					else{
						indexSplit = line.indexOf(':');
						if( indexSplit>=0 ) context.reqHeaders.put(line.substring(0,indexSplit).trim().toLowerCase(),line.substring(indexSplit+1).trim());
						continue;
						}
					}

				if( state==stateReadBody )
					{
					if( bodySize>serv.getReciveLimit() )
						{	//	дропаем слишком большой объЄм данных. »Ѕќ Ќ≈’”… ѕјћя“№ ћЌ≈ “”“ «ј—»–ј“№
						long skip=bodySize;
						while( skip>0 )	skip-=DINP.skip(skip);
						state=stateProcessing;
						}
					int len=DINP.available();
					DINP.read(data,indexSplit,len);
					indexSplit+=len;
					if( indexSplit<bodySize )	continue;
					context.reqBody=data;		data=null;
					state=stateProcessing;
					}
				
				if( state==stateProcessing )
					{
					if( context.METHOD==null || !context.METHOD.matches("GET|POST|OPTIONS|HEAD") )	{	context.resHeaders.put("Allow","GET, POST, OPTIONS, HEAD");	context.status=501;	context.end();	}
					if( context.VERSION==null || !context.VERSION.equals("HTTP/1.1") )				{	context.status=505;	context.end();	}
					String connection=context.reqHeaders.get("connection");
					if( connection!=null )
						{
						if( connection.matches("Keep-Alive") ) {	context.resHeaders.put("Connection","Keep-Alive");	SOC.setKeepAlive( true);	}
						else if( connection.matches("Close") ) {	context.resHeaders.put("Connection","Close");		SOC.setKeepAlive(false);	}
						}
					context.cookie=new httpCookie(context.reqHeaders.get("cookie"));
					if( context.METHOD.equals("OPTIONS") )
						{	context.resHeaders.put("Allow","GET, POST, OPTIONS, HEAD");	context.status=200;	context.end();	}
					int index = context.URI.indexOf('?');
					if( index>0 )
						{
						context.data__GET = pairs( context.URI.substring( index+1 ) , "&" , "=" );
						context.URI = context.URI.substring( 0 , index );
						}
					if( context.METHOD.equals("POST") )
						{
						if( context.reqBody!=null && context.reqBody.length>0 )
							{
							String type = context.reqHeaders.get("content-type");
							context.data_POST = new HashMap<String,Object>();
									if	(	type.indexOf("urlencoded")	>= 0	)	{	System.out.println( "parse body urlencoded" );	urlencoded(context);	}
							else	if	(	type.indexOf("multipart")	>= 0	)	{	System.out.println( "parse body multipart" );	multipart(context);		}
							}
						}
					if( !context.flush )
						{
						//	good version
						if( overload )
							{	serv.getHandlerOverload().apply(context);	}
						else{
							Iterator<Handler>i=serv.handlers.iterator();
							while(	i.hasNext() && !context.flush )
									i.next().apply(context);
							}
						}
					if( !context.flush )
						{	serv.getHandlerNotFount().apply(context);	}
					state=stateBuildUnswer;
					}
				
				if( state==stateBuildUnswer )
					{
					context.resHeaders.put("Content-Length",Integer.toString(context.resBody.length()));
					DOUT.writeBytes( context.getStartLine() );
					DOUT.writeBytes( "\n" );
					Iterator<Entry<String,String>>headers = context.resHeaders.entrySet().iterator();
					while( headers.hasNext() )
						{
						Entry<String,String> header = headers.next();
						DOUT.writeBytes( header.getKey()+": "+header.getValue()+"\n" );
						}
					Iterator<String>cookies = context.cookie.getOutput().iterator();
					while( cookies.hasNext() )
						{
						DOUT.writeBytes( "Set-Cookie: "+cookies.next()+"\n" );
						}
					DOUT.writeBytes( "\n" );
					DOUT.write( context.resBody.toString().getBytes( serv.getCharset() ) );
					DOUT.writeBytes( "\n" );
					DOUT.flush();
					time = System.currentTimeMillis() - time;
					updateLoading( Math.max( time , 0.5 ) );	//	ќпредел€ем зависимую нагрузку (ибо она зависит от того, какин обработчики мы повесим на сервер)
					if( !SOC.getKeepAlive() )	{	break;	}
					else{	state=stateWaiting;	await=0;	}
					}
				}
			SOC.setKeepAlive(false);
			DINP.close();	INP.close();
			DOUT.close();	OUT.close();
			SOC.close();
			}
		catch (IOException e)
			{	e.printStackTrace();	}
		}
	public void end()
		{
		ready=false;
		}
	public HashMap<String,String> pairs( String data , String sep , String eqa )
		{
		HashMap<String,String>map = new HashMap<String,String>();
		String[]pairs = data.split(sep);
		for( String pair : pairs )
			{
			String[]s = pair.split(eqa,2);
			if( s.length==2 )	map.put( s[0] , s[1] );
			}
		return( map );
		}
	public void urlencoded(httpContext context)
		{
		String[]pairs=new String( context.reqBody ).split("&");
		for( String pair : pairs )
			{
			String[]s = pair.split("=",2);
			if( s.length==2 )context.data_POST.put( s[0] , s[1] );
			if( s.length==1 )context.data_POST.put( s[0] , "true" );
			}
		}
	public static final Pattern content_type	= Pattern.compile("boundary=([^;$]+)");
	public static final Pattern header_name		= Pattern.compile("name=\"([^\"]+)\"");
	public static final Pattern header_file		= Pattern.compile("filename=\"([^\"]+)\"");
	public static final Pattern header_mime		= Pattern.compile("Content-Type: ([^\n$]+)");
	public void multipart(httpContext context)
		{
		byte[]boundary;
		Integer unparsed=0;
		Matcher m = content_type.matcher( context.reqHeaders.get("content-type") );
		if( m.find() )
			{
			boundary = ("\r\n--"+m.group(1)+"\r\n").getBytes() ;
			byte[][]blocks = ByteWorker.split( context.reqBody , boundary );
			for( byte[]block : blocks )
				{
				if( block.length < 43 )	{	unparsed++;	continue;	}
				
				byte[][]parts = ByteWorker.split(block,("\r\n\r\n").getBytes() , 2 );
				
				if( parts.length<2 )	{	unparsed++;	continue;	}
				String pHead = new String( parts[0] , serv.getCharset() );
				byte[]	pBody	=	parts[1];
				
				String fName=null, fFile=null, fMime=null;

				m = header_name.matcher( pHead );	if( m.find() )	{	fName=m.group(1);	}
				m = header_file.matcher( pHead );	if( m.find() )	{	fFile=m.group(1);	}
				m = header_mime.matcher( pHead );	if( m.find() )	{	fMime=m.group(1);	}

				System.out.println(  );
				System.out.println( "HEADER" );
				System.out.println( pHead );
				System.out.println( "SIZES" );
				System.out.println( parts[0].length+" "+pHead.length() );
				System.out.println( "PARSE" );
				System.out.println( "fName	"+fName );
				System.out.println( "fFile	"+fFile );
				System.out.println( "fMime	"+fMime );
				System.out.println(  );
				
				if( fName==null )		{	unparsed++;	continue;	}
				if( fFile==null )		{	context.data_POST.put( fName , new String(pBody) );	}
				else{
					inFile file = new inFile( fFile , (fMime!=null)?(fMime):("unknown") );
					try	{
						FileOutputStream FOUT = new FileOutputStream( file.temp );
						FOUT.write( pBody );
						FOUT.flush();	FOUT.close();
						context.data_POST.put( fName , file );
						}
					catch( IOException e )	{	e.printStackTrace();	}
					}
				}
			context.data_POST.put("unparsed",unparsed);
			}
		
		}
	}
