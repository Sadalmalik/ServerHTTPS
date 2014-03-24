/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

import ru.ifaculty.java.utils.Krypto;

public class httpCookie
	{
	private	static	String seed=":seeed:";
	public	static	void	setSeed(String s){if(s!=null)seed=s;}
	private	HashMap<String,String>Cookies	=	new HashMap<String,String>();	//	Куки
	private LinkedList<String>Output		=	new LinkedList<String>();
	public	httpCookie(){}
	public	httpCookie(String cookie){this.parseCookie(cookie);}
	private	boolean	wreck = false;
	public	boolean	isWreck(){return(wreck);}
	
	public LinkedList<String>	getOutput()				{	return(Output);		}
	public void		dropOutput()						{	Output.clear();		}
	public void 	setCookieProtectSeed( String s )	{	if(s!=null)seed=s;	}
	public void 	parseCookie( String cookie )
		{
		if( cookie!=null )	//	cookie!
			{
			String key, val, hash, list[]=cookie.split(";");
			for( int i=0, n ; i<list.length ; i++ )
				{
				list[i]=list[i].trim();
				n = list[i].indexOf("=");
				if( n<0 )continue;
				key = list[i].substring(0,n);
				val = list[i].substring(n+1);
				if( val.charAt(0)!='S' || val.charAt(1)!=':' ){	continue;	}
				val=val.substring(2);
				n = val.indexOf('.',val.length()-32);
				if( n<0 )continue;
				hash=val.substring(n+1);
				val	=val.substring(0,n);
				if( hash.equals(Krypto.aggregateHashHex(seed+val,16)) )
					{	Cookies.put(key,val);	}
				else{	wreck	=	true	;	}
				}
			}
		}
	public	String	getCookie(String key)									{	return( Cookies.get(key) );	}
	public	void	setCookie(String key,String val)						{	setCookie(key,val,-1,false,null,null);		}	
	public	void	setCookie(String key,String val,long life)				{	setCookie(key,val,life,false,null,null);	}
	public	void	setCookie(String key,String val,boolean http)			{	setCookie(key,val,-1,http,null,null);		}
	public	void	setCookie(String key,String val,long life,boolean http)	{	setCookie(key,val,life,http,null,null);		}
	@SuppressWarnings("deprecation")
	public	void	setCookie(String key,String val,long life,boolean http,String path,String domain)
		{
		Cookies.put(key,val);
		StringBuilder outputCookies=new StringBuilder();
		outputCookies.append(key).append("=S:").append(val).append(".").append(Krypto.aggregateHashHex(seed+val,16)).append(";");
		if( life>0 )
			{
			Date date = new Date();	date.setTime( date.getTime() + life );
			outputCookies.append(" expires=").append(date.toGMTString()).append(";");
			}
		if( path!=null )	{	outputCookies.append(" path=").append(path).append(";");	}
		if( domain!=null )	{	outputCookies.append(" domain=").append(domain).append(";");	}
		if( http )			{	outputCookies.append(" HttpOnly;");	}
		Output.add( outputCookies.toString() );
		}
	}
