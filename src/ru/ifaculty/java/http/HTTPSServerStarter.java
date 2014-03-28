/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.File;
import java.util.HashMap;

import ru.ifaculty.java.utils.config;

public class HTTPSServerStarter
	{
	private	HTTPSServerStarter(){}
	
	public	static	void	start( Handler mainHandler , String configFile )
		{
		start( mainHandler , config.load( configFile ) );
		}
	public	static	ServerAbstract	http__Serv;
	public	static	ServerAbstract	https_Serv;
	public	static	void	stop()
		{
		if( http__Serv!=null )	http__Serv.work=false;
		if( https_Serv!=null )	https_Serv.work=false;
		}
	public	static	boolean	start( Handler mainHandler , HashMap<String,String>conf )
		{
		String	TMP		=	null	;
		String	SECURE	=	null	;
		String	IP		=	null	;
		int		HPORT	=	80		;
		int		SPORT	=	443		;
		
		if( conf!=null )
			{
			SECURE = conf.get("secure") ;
			if( (IP=conf.get	("http-port"))!=null )	HPORT=Integer.parseInt(IP);
			if( (IP=conf.get	("https-port"))!=null )	SPORT=Integer.parseInt(IP);
			IP	=	conf.get	("ip");
			}
		if( IP	==null )	IP	="127.0.0.1";
		
		int TimeOut	=	-1	;
		int	Delay	=	-1	;
		String	name=	null;
		String	fold=	null;
		String	temp=	null;
		if( conf!=null )
			{
			if( (TMP=conf.get("TimeOut"))!=null )	TimeOut =	Integer.parseInt(TMP)	;
			if( (TMP=conf.get("Delay"))!=null )		Delay	=	Integer.parseInt(TMP)	;
			if( (TMP=conf.get("Name"))!=null )		name	=	TMP						;
			if( (TMP=conf.get("RootFolder"))!=null )fold	=	TMP						;
			if( (TMP=conf.get("TempFolder"))!=null )temp	=	TMP						;
			}
		
		if( conf==null )						{	System.out.println("Can't create server! Need options file!");	return( false );	}

		if( SECURE!=null )		https_Serv=new ServerHTTPS	(	IP	,	SPORT	,	SECURE	);
		else					https_Serv=new ServerHTTPS	(	IP	,	SPORT	,	conf	);
		http__Serv=new ServerHTTP	(	IP	,	HPORT			);
		
		if( TimeOut>=0 )	{	https_Serv.setTimeout(TimeOut)	;	http__Serv.setTimeout(TimeOut)	;	}
		if( Delay>=0 )		{	https_Serv.setDelay(Delay)		;	http__Serv.setDelay(Delay)		;	}
		if( name!=null )	{	https_Serv.setServerName(name)	;	http__Serv.setServerName(name)	;	}
		if( temp!=null )	{	inFile.SetFolder( temp );	}
		
		http__Serv.addHandler	(	new Handler()
			{
			public void apply(httpContext context)	//	Редирект с HTTP на HTTPS 
				{
				context.status = 303 ;
				context.resHeaders.put("Location","https://127.0.0.1"+context.URI);
				context.end();
				}
			} );
		https_Serv.addHandler	(	mainHandler	);
		
		if( fold!=null )
			{
			File dir = new File(fold);
			if( !dir.exists() || !dir.isDirectory() )
				dir.mkdir();
			}
		
		System.out.println( "Starting server on "+IP+":"+HPORT+":"+SPORT );
		
		Thread _http__ = new Thread( new Runnable(){	public void run()	{	http__Serv.start();	}	} );
		Thread _https_ = new Thread( new Runnable(){	public void run()	{	https_Serv.start();	}	} );
		
		_http__.start();
		_https_.start();
		
		try {
			Thread.sleep(10);
			if( !http__Serv.work || !https_Serv.work )
				{
				http__Serv.work=https_Serv.work=false;
				return( false );
				}
			}
		catch (InterruptedException e)	{	e.printStackTrace();	}
		return( true );
		}
	}
