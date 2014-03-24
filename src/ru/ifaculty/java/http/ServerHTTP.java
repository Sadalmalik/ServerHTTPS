/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerHTTP extends ServerAbstract
	{
	public ServerHTTP(int port)				{	super(port);	}
	public ServerHTTP(String ip, int port)	{	super(ip,port);	}
	public ServerHTTP(String ip)			{	super(ip,80);	}
	@Override
	protected ServerSocket createSocket() throws IOException
		{
		return new ServerSocket();
		}
	}
