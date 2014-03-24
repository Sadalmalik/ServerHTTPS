/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.http;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.HashMap;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import ru.ifaculty.java.utils.config;

public class ServerHTTPS extends ServerAbstract
	{
	public	ServerHTTPS(int port)											{	super(port);	}
	public	ServerHTTPS(String ip)											{	super(ip,443);	}
	public	ServerHTTPS(String ip, String conf)								{	super(ip,443);	setConfig(conf);	}
	public	ServerHTTPS(String ip, HashMap<String,String>conf)				{	super(ip,443);	setConfig(conf);	}
	public	ServerHTTPS(String ip, int port)								{	super(ip,port);	}
	public	ServerHTTPS(String ip, int port, String conf)					{	super(ip,port);	setConfig(conf);	}
	public	ServerHTTPS(String ip, int port, HashMap<String,String>conf)	{	super(ip,port);	setConfig(conf);	}
	
	private	HashMap<String,String>configuration=null;
	public	void		setConfig(String conf)					{	configuration=config.load(conf);	}
	public	void		setConfig(HashMap<String,String>conf)	{	configuration=conf;	}
	private	KeyStore	keyStorage;
	
	protected	ServerSocket createSocket() throws IOException
		{
		String	storeFileName	=	null	;
		String	storeKeyPass	=	null	;
		String	storeAliasPass	=	null	;
		boolean	isClientSecure	=	false	;
		
		try {
			if( configuration==null )	throw new Exception("Need configuration!");
			
			keyStorage = KeyStore.getInstance(KeyStore.getDefaultType());
			
			if( configuration!=null )
				{
				storeFileName	=							configuration.get("storeFileName")	;
				storeKeyPass	=							configuration.get("storeKeyPass")	;
				storeAliasPass	=							configuration.get("storeAliasPass")	;
				isClientSecure	=	Boolean.parseBoolean(	configuration.get("isClientSecure")	);
				}

			if (	storeFileName	== null
				||	storeKeyPass	== null
				||	storeAliasPass	== null	)
				{
				throw new Exception("Need more infomation in configuration file! (storeFileName, storeKeyPass, storeAliasPass)");
				}
			
			keyStorage.load( new FileInputStream(storeFileName) , storeKeyPass.toCharArray() );
			System.out.println( keyStorage );
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			return null;
			}
		
		try {
			KeyManagerFactory kmfa = KeyManagerFactory.getInstance("SunX509");
			kmfa.init( keyStorage , storeAliasPass.toCharArray() );
			SSLContext sslco = SSLContext.getInstance("SSLv3");
			sslco.init(kmfa.getKeyManagers(), null, null);
			
			javax.net.ssl.SSLServerSocketFactory ssfa = sslco.getServerSocketFactory();
			
			SSLServerSocket serverSocket = (SSLServerSocket) ssfa.createServerSocket();
			
		//	printArray	(	"Список разрешенных для использования криптографических сюит"	,	serverSocket.getEnabledCipherSuites()	);
		//	printArray	(	"Список разрешенных для использования протоколов"				,	serverSocket.getEnabledProtocols()		);
			
			serverSocket.setNeedClientAuth(isClientSecure);
			return serverSocket;
			}
		catch (Exception ex)
			{
			ex.printStackTrace();
			throw new IOException ( "Ошибка на стадии создания SSL сокета: "+ ex);
			}
		}
	/*
	protected void printArray (String s , String sa[])
		{
		StringBuffer	sb	=	new StringBuffer();
		sb.append("--------" +s +"--------\n");
		for (String b : sa)	sb.append(b+"\n");
		javax.swing.JOptionPane.showMessageDialog(null,sb,s,javax.swing.JOptionPane.INFORMATION_MESSAGE);
		}
	*/
	}