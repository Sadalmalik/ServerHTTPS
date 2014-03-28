package example;

import java.util.HashMap;

import ru.ifaculty.java.console.console;
import ru.ifaculty.java.http.HTTPSServerStarter;
import ru.ifaculty.java.http.Handler;
import ru.ifaculty.java.http.httpContext;

public class Router extends Handler
	{
	public	static	void	main(String[]args)
		{
	//		Лучше так:
	//	HashMap<String,String>conf = config.load( "configuration.txt" );
	//		Но для текущего примера я забью конфиг руками:
		HashMap<String,String>conf = new HashMap<String,String>();
		
		conf.put("http-port","80");
		conf.put("https-port","443");
		conf.put("ip","127.0.0.1");
		
		conf.put("TimeOut",		"5000"		);	//	5 секунд на коннект, потом он закрывается
		conf.put("Delay",		"50"		);	//	Интервал проверки на новые запросы в коннекте. То есть 50 мс = 20 раз в секунду.
		conf.put("Name",		"Papuas"	);	//	РЕКОМЕНДУЕМОЕ Имя сервера :3
		conf.put("RootFolder",	"www"		);	//	Папка. Пока никак не используется, только создаётся
		conf.put("TempFolder",	"www\tmp"	);	//	Папка для временных файлов. В ней хранятся файлы переданные в POST.
		
		//	Хранилище ключей. Необходимо для HTTPS
		conf.put("storeFileName",	"my Server Keystore");
		conf.put("storeKeyPass",	"my Server Keystore Key");
		conf.put("storeAliasPass",	"my Server Keystore Key Key");
		conf.put("isClientSecure",	"false");
		
		if( HTTPSServerStarter.start(new Router(), conf) )
			{
			final console con = new console();
			con.setCommand("stop", new console.Action()
				{
				public void use(String[] params)	{	HTTPSServerStarter.stop();	con.getPrinter().println("Server stopped");	}
				public String description()			{	return "Stopping server";	}
				});
			con.start();
			}
		}
	
	public void apply(httpContext context)
		{
		context.end("<html>"
				+ "<head>"
				+ "<title>Papuas server!</title>"
				+ "</head>"
				+ "<body>"
				+ "<H1>Welcome!</H1>"
				+ "</body>"
				+ "</html>");
		}
	}
