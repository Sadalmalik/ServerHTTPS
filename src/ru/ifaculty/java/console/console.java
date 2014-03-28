package ru.ifaculty.java.console;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.Iterator;
import java.util.Map.Entry;

import ru.ifaculty.java.console.console.Action;

public class console implements Runnable
	{
	public	static	abstract	class	Action
		{
		private	console		con=null;
		public	void		print	(String s)	{	if(con!=null)	con.out.print(s);	}
		public	void		println	(String s)	{	if(con!=null)	con.out.println(s);	}
		public	abstract	void use(String[]params);
		public	abstract	String	description();
		};
	
	private	TreeMap<String,Action> listOfCommands = new TreeMap<String,Action>(new Comparator<String>(){public int compare(String A, String B) {return( A.compareTo(B) );}});
	public	void	setCommand( String call , Action command )	{	command.con=this;	listOfCommands.put( call , command );	}
	public	Action	getCommand( String call )					{				return	listOfCommands.get( call );				}
	
	private	Scanner		inp;
	private	PrintStream	out;
	public	Scanner		getScanner(){	return(inp);	}
	public	PrintStream	getPrinter(){	return(out);	}

	public	void	setScanner( Scanner s )			{	if(s!=null)inp=s;	}
	public	void	setPrinter( PrintStream p )		{	if(p!=null)out=p;	}
	public	void	setScanner( InputStream	s)		{	if(s!=null)inp=new Scanner(s);		}
	public	void	setPrinter( OutputStream p )	{	if(p!=null)out=new PrintStream(p);	}
	
	public	console()
		{
		basicCommands();
		//	Default	streams
		setScanner	(	System.in	);
		setPrinter	(	System.out	);
		}
	
	private	Thread	work=null;
	private	boolean	run=false;
	public	void	start()
		{
		run=true;
		if( work==null )work = new Thread(this);
		Action act = listOfCommands.get("welcome");
		if( act!=null )	act.use(null);	out.println();
		out.print("> ");
		work.start();
		}
	@SuppressWarnings("deprecation")
	public	void	stop()	{	run=false;	if( work!=null )	work.stop();	}
	public	void	run()
		{
		String line, params[];
		while( run && (line = inp.nextLine())!=null)
			{
			params = line.split(" ",2);
			Action act = listOfCommands.get(params[0]);
			if( act==null )
				{
				out.print( params[0] );
				out.println( "	:	unknown command!" );
				}
			else{
				act.use( params.length>1?params[1].split(" "):null );
				out.println();
				}
			out.print("> ");
			}
		}
	
	private	void	basicCommands()
		{
		setCommand("exit",new Action(){
			public void use(String[]params)	{	System.exit(0);				}
			public String description()			{	return("stop java core");	}
			});
		setCommand("welcome",new Action(){
			public void use(String[]params)	{	out.println("Welcome to command comsole v2.0 by Kaleb(Sadalmalik)\n\nPrint \"help\" to list all commands");	}
			public String description()			{	return("prints welcome");	}
			});
		setCommand("wtf",new Action(){
			public void use(String[]params)	{	out.println("Badger! Badger! Badger! Badger!\nBadger! Badger! Badger!\nBadger! Badger! Badger! Badger! Badger!\nMushroom!!! MUSHROOM!!!");	}
			public String description()			{	return("... OMG ... WTF ...");	}
			});
		setCommand("test",new Action(){
			public void use(String[]params)
				{
				out.println("params");
				if( params!=null )
				for( int i=0 ; i<params.length ; i++ )
					{
					out.print(i);
					out.print("	");
					out.println(params[i]);
					}
				}
			public String description()
				{
				return("This command prints the list of the arguments");
				}
			});
		setCommand("help",new Action(){
			public void use(String[]params)
				{
				if( params!=null && params.length>0 )
					{
					out.println( "The found commands:\n" );
					for( String com : params )
						{
						Action act = listOfCommands.get(com);
						out.print( com );
						if( act==null )	{	out.println( "	:	unknown command!" );	}
						else{	out.println( "	:" );out.println( listOfCommands.get(com).description() );	}
						out.println();
						}
					}
				else{
					out.println( "Available commands:\n" );
					Iterator<Entry<String, Action>> I = listOfCommands.entrySet().iterator();
					while( I.hasNext() )
						{
						Entry<String, Action>E=I.next();
						String com	=	E.getKey()	;
						Action act	=	E.getValue();
						out.print( com );
						if( act==null )	{	out.println( "	:	unknown command!" );	}
						else{	out.println( "	:" );out.println( listOfCommands.get(com).description() );	}
						out.println();
						}
					}
				}
			public	String	description()
				{
				return("Without parameters outputs the list of commands.\nIn parameters it is possible to specify some commands.");
				}
			});
		}
	}
