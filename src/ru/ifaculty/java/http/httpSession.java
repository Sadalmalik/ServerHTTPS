/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import ru.ifaculty.java.utils.Krypto;

//import java.sql.*;

public class httpSession
	{
	//************************************************************************************************//
	private	static	long	timeDelay=60*60*1000;
	public	static	void	setDelay( long t )	{	if( t>5*24*60*60 ) t=5*24*60*60;	if( t<60 ) t=60;	timeDelay=t;	}
	public	static	long	getDelay(  )		{	return(timeDelay);	}
	private	static	HashMap<String,httpSession>sessionList = new HashMap<String,httpSession>();
	public	static	httpSession	createSession()
		{
		httpSession ses=new httpSession();
		ses.key = Krypto.generateKey(16);
		while( sessionList.containsKey(ses.key) )
			ses.key = Krypto.generateKey(16);
		ses.nextTime = System.currentTimeMillis() + timeDelay;
		sessionList.put(ses.key,ses);
		return( ses );
		}
	public	static	httpSession	loadSession(String key)
		{
		httpSession ses = sessionList.get(key);
		if( ses!=null )	ses.nextTime = System.currentTimeMillis() + timeDelay;
		return( ses );
		}
	public	static	void	regular()
		{
		Iterator<Entry<String,httpSession>>iter = sessionList.entrySet().iterator();
		while( iter.hasNext() )
			{
			Entry<String,httpSession>e = iter.next();
			if( e.getValue().nextTime>System.currentTimeMillis() )
				{	sessionList.remove( e.getKey() );	}
			}
		}
	//************************************************************************************************//
	
	
	
	//************************************************************************************************//
	public	String	key	= null;
	public	Object	data= null;
	private	long	nextTime;
	private	httpSession(){}
	//************************************************************************************************//
	}
