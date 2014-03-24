/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.utils;

import java.util.Random;

public class Krypto
	{
	//************************************************************************************************//
	public static Random rand;
	static
		{
		long seed = System.currentTimeMillis();
		seed = seed^(seed<<37)^(seed>>12)^(seed>>53)^(seed<<19)^(seed>>>21);
		rand = new Random( seed );
		}
	//************************************************************************************************//
	
	
	
	//************************************************************************************************//
	private static byte[] DPOS = ("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").getBytes();
	public static String generateKey( int LEN )
		{	return( generateKey( LEN , (byte[])null ) );	}
	public static String generateKey( int LEN , String POS )
		{	return( generateKey( LEN , POS.getBytes() ) );	}
	public static String generateKey( int LEN , byte[] POS )
		{
		byte[]out=new byte[LEN];
		byte[]inp=((POS==null)?DPOS:POS);
		for( int i=0 ; i<LEN ; i++ )	out[i]=inp[rand.nextInt(inp.length)];
		return( new String(out) );
		}
	//************************************************************************************************//
	
	
	
	//************************************************************************************************//
	private static String hex( char[]in )
		{
		byte[]num="0123456789ABCDEF".getBytes();
		byte[]out=new byte[in.length*2];
		for( int i=0 ; i<in.length ; i++ )
			{
			out[ i*2 ]=num[(in[i]>>4)];
			out[i*2+1]=num[(in[i]&15)];
			}
		return new String(out);
		}
	public static String aggregateHashHex( String _data )						{	return( hex(aggregateHash(_data)) );	}
	public static String aggregateHashHex( String _data , int len )				{	return( hex(aggregateHash(_data,len)) );	}
	public static String aggregateHashHex( String _data , int len , int over )	{	return( hex(aggregateHash(_data,len,over)) );	}
	public static char[] aggregateHash( String data )			{	return( aggregateHash( data , 3 , 8 ) );	}
	public static char[] aggregateHash( String data , int len )	{	return( aggregateHash( data , len , 8 ) );	}
	public static char[] aggregateHash( String _data , int len , int over )
		{
		if( len<3 ) len=3;
		
		byte[]inp=_data.getBytes();
		byte[]data=new byte[Math.max(inp.length,len+over)];
		int i;
		for( i=0 ;	i<inp.length	;	i++	) data[i]=inp[i];
		for(	 ;	i<data.length	;	i++	) data[i]='_';
		
		char[]hash=new char[len];
		hash[0]=(char)0xFC;	//	מע באכהû
		hash[1]=(char)0x5A;
		hash[2]=(char)0x82;
		for( i=3 ; i<len ; i++ )
			hash[i]=alpha( hash[i-2]^hash[i-1] + hash[i-3] );
		int temp;
		
		for( i=0 ; i<data.length ; i++ )
			{
			temp=0;
			for( int j=0 ; j<len-1 ; j++ )	{	temp=temp+hash[j];	}
			temp = alpha(hash[len-1]+data[i]) + omega(temp) ;
			for( int j=len-1 ; j>0 ; j-- )	{	hash[j]=hash[j-1];	}
			hash[0]=(char)(temp&0xFF);
			}
		
		return( hash );
		}
	private static char alpha( int n )	{	n=(n>>3)^(n<<1)^(n<<4);	return(char)( ( n^(n>>8) )&0xFF );	}
	private static char omega( int n )	{	n=(n>>5)^(n<<2)^(n<<3);	return(char)( ( n^(n>>8) )&0xFF );	}
	//************************************************************************************************//
	}
