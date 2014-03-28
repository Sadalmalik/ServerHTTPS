/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.LinkedList;

public abstract class ServerAbstract
	{
	//************************************************************************************************//
	private	long	TimeOut		=	5*1000		;
	public	void	setTimeout		( int t )	{	TimeOut=Math.max(Math.min(t*1000L,30*60*1000L),1000);	}
	public	long	getTimeout		(		)	{	return( TimeOut );										}
	
	private	String	ServerName	=	"Papuas"	;
	public	void	setServerName	( String s ){	ServerName=(s==null)?(ServerName):(s);					}
	public	String	getServerName	(		)	{	return( ServerName );									}
	
	private	long	Delay		=	50			;
	public	void	setDelay		( long t )	{	Delay=Math.max(Math.min(t,TimeOut/2),10);				}
	public	long	getDelay		(		 )	{	return( Delay );										}
	
	private	long	ReciveLimit	=	8*1024*1024	;
	public	void	setReciveLimit	( long l )	{	ReciveLimit=Math.min(Math.min(l,1024),8*1024*1024);		}
	public	long	getReciveLimit	(		 )	{	return( ReciveLimit );									}
	
	private	Charset	useCharset;		{	useCharset = Charset.availableCharsets().get("UTF-8");	if(useCharset==null)useCharset=Charset.defaultCharset();	}
	public	void	setCharset		( String c )	{	Charset use = Charset.availableCharsets().get(c);	if( use!=null ) useCharset=use;	}
	public	Charset	getCharset		(		   )	{	return( useCharset );	}
	//************************************************************************************************//
	
	

	//************************************************************************************************//
	protected	ServerSocket		serv=null;
	protected	InetSocketAddress	addr=null;
	protected	boolean				work=true;
	
	public ServerAbstract(int port)						{	addr = new InetSocketAddress(port);		}
	public ServerAbstract(String ip, int port)			{	addr = new InetSocketAddress(ip,port);	}
	public ServerAbstract(String ip)					{	addr = new InetSocketAddress(ip,80);	}
	
	public		int		RPS=0;
	protected	int		counter=0;
	public		void	stop()	{	work = false;	}
	protected	Thread	accompany = null;
	public		void	start()
		{
		//	BLACK MAGIC
		accompany = new Thread( new Runnable(){public void run()
			{while(work){RPS=counter;counter=0;try{Thread.sleep(1000);}catch(Exception e){e.printStackTrace();}}}
		});
		accompany.start();

		try	{
			serv=createSocket();
			if( serv==null )
				{
				work=false;
				throw new Exception("Can't create server!");
				}
			serv.bind(addr);
			while(work)
				{
				//	SOME VERY EVIL CODE HERE			very...						VERY...				EEVEEEEL!!!!!
				(new Thread(new httpHandler(serv.accept(),this,( RPS > ( 1000 / httpHandler.getLoading() ) )))).start();
				counter++;
				}
			serv.close();
			}
		catch (Exception e)
			{	e.printStackTrace();	}
		}
	protected	abstract	ServerSocket	createSocket() throws IOException;
	//************************************************************************************************//
	
	

	//************************************************************************************************//
	public	LinkedList<Handler>handlers = new LinkedList<Handler>();
	public	void addHandler( Handler H )	{	handlers.add(H);	}
	public	void delHandler( Handler H )	{	handlers.remove(H);	}
	
	private	Handler	overload	=	new Handler()
		{
		public void apply(httpContext context)
			{
			context.status=503;
			context.resHeaders.put("Retry-After","300");
			context.write("<html><body><center><H1>Server is overloaded.</H1>Server can't process your request, please, try later.</center></body></html>");
			context.end();
			}
		};
	public	void	setHandlerOverload( Handler H )	{	if(H!=null){overload=H;}	}
	public	Handler	getHandlerOverload(  )			{	return( overload );			}

	private	Handler	notFount	=	new Handler()
		{
		public void apply(httpContext context)
			{
			context.status=404;
			context.write("<html><body><center><H1>Error: 404</H1><br>Page not found</center></body></html>");
			context.end();
			}
		};
	public	void	setHandlerNotFount( Handler H )	{	if(H!=null){notFount=H;}	}
	public	Handler	getHandlerNotFount(  )			{	return( notFount );			}
	//***********************************************************notFount************************************//
	}
