package ru.ifaculty.java.utils;

/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

/**
 *	@author Глеб aka Kaleb(Sadalmalik)
 *	
 *	Здесь я должен остановиться на более подробном описании "как" и "нах*я"
 *	
 *	Проблемы возникли когда я начал реализовывать парсинг http://ru.wikipedia.org/wiki/Multipart/form-data
 *	Это формат передачи данных на сервер, он нужен для загрузки файлов через формы.
 *	Ключевая особенность этого формата что в нём бинарные данные идут в перемешку с текстовым интерфейсом.
 *	
 *	Изначально я использовал String для обработки входящих данных.
 *	Данные нужно было разбивать на блоки по специальной строке-разделителю.
 *	И всё было замечательно пока я не попытался переслать файл в бинарном формате (обычный jpeg)
 *	
 *	Вот тут-то и открылась вся монструозная природа String-ов:
 *	После длительных экспериментов я выяснил что String заменяет ОДИН ЕДИНСТВЕННЫЙ БАЙТ
 *	152 -> 63
 *	ВСЕ ОСТАЛЬНЫЕ ОСТАЮТСЯ НЕИЗМЕННЫМИ!
 *	
 *	Это настоящий удар под дых! Это выше моего понимания XD
 *	И вот итог: я накатал класс для разделения байтового массива на подмассивы по другому байтовому массву.
 *	И что самое приятное: моя реализация оказалась на сотню порядков живее дефолтных строк!
 *	
 *	ориентировочно:
 *	в худшем случае разбиение выполняется в 5 раз быстрее
 *	в лучшем случае - в 250 РАЗ БЫСТРЕЕ!
 *	
 *	Это так же заставляет задуматься о том, как (криво) реализованны стандартные элементы Java
 *	А я об этом очень часто задумываюсь.
 *	
 *	Ещё один положительный момент:
 *	В процессе тестов мне помогал мой друг, а он у нас с художественным образованием
 *	Так вот картинки, прошедшие через String оказывались битыми - на них появлялся глитч различной степени тяжести.
 *	Другану это естесственно понравилось.
 *	
 *	После ряда экспериментов была созданна программа, меняющая случайные байты в файлах (создавая копии файлов)
 *	И в итоге получаются картинки с очень картинными картинами сломанности! :D
 *	Правда от String тут тоже отказались - даже её несовершенство невозможно эффективно заюзать!
 *	
 *	Отстой ваша Java. Я напишу свою.
 */

public class ByteWorker
	{
	public	static	void	main(String[]args)
		{
		byte[]dataA=new byte[256];
		for( int i=0 ; i<256 ; i++ )
			{
			dataA[i]=(byte)i;
			System.out.print( " " + i );
			}
		byte[]dataB = (new String( dataA )).getBytes() ;

		System.out.println();
		for( int i=0 ; i<256 ; i++ )
			{
			System.out.print( " " + (dataB[i]<0?dataB[i]+256:dataB[i]) );
			}

		System.out.println();
		for( int i=0 ; i<256 ; i++ )
			{
			if( dataA[i] != dataB[i] )
				System.out.println( dataA[i]+256 + " -> " + dataB[i] );
			}

		System.out.println();
		
		byte[]data1 = "   test    string    ".getBytes();
		byte[]data2 = " +".getBytes();
		
		long t1, t2;
		long dt1, dt2;
		t1=System.nanoTime();
		byte[][]data3 = split( data1 , data2 );
		t2=System.nanoTime();
		
		System.out.println();
		System.out.println("byte	"+data1.length);
		System.out.println();	dt1=(t2-t1);
		System.out.println("time	"+dt1);	
		System.out.println("size	"+data3.length);
		//*
		for( int i=0 ; i<data3.length ; i++ )
			{
			System.out.println();
			if( data3[i]==null ){	System.out.print(i+"	null ");	}
			else{	System.out.print(i+"	");	for( int j=0 ; j<data3[i].length ; j++ )	System.out.print(data3[i][j]+" ");	}
			
			}
		//*/
		
		System.out.println();
		String inp = new String(data1);
		String sep = new String(data2);
		t1=System.nanoTime();
		String[]out= inp.split(sep);
		t2=System.nanoTime();
		
		System.out.println();	dt2=(t2-t1);
		System.out.println("time	"+dt2);
		System.out.println("size	"+out.length);
		System.out.println();
		System.out.println( ((double)dt2)/((double)dt1) );
		//*
		int i=0;
		for( String s : out )
			{
			System.out.println();
			if( s==null ){	System.out.print(i+"	null ");	}
			else{	System.out.print(i+"	");	byte[]x = s.getBytes(); for( int j=0 ; j<x.length ; j++ )	System.out.print(x[j]+" ");	}
			i++;
			}
		//*/
		
		}

	public	static	int	contains( byte[]input , byte[]search )
		{	return contains( input , search , 0 );	}
	public	static	int	contains( byte[]input , byte[]search , int skip )
		{
		if( skip>=input.length-search.length ) return(-1);
		boolean flag;
		for( int i=skip, j=0 ; i<input.length-search.length ; i++ )
			{
			flag=true;
			for( j=0 ; j<search.length ; j++ )
				if( input[i+j]!=search[j] )
					{	flag=false;	break;	}
			if( !flag ) continue;
			return( i );
			}
		return(-1);
		}
	public	static	byte[][] split( byte[]input , byte[]separator )
		{	return split( input , separator , -1 );	}
	public	static	byte[][] split( byte[]input , byte[]separator , int limit )
		{
		byte[][]output=new byte[8][];
		
		int c, i=0, l=0, u=0;					//	Я надеюсь, тут всё понятно?)
		while( limit<0 || ++u<limit )
			{
			c=contains(input,separator,l);
			if( c<0 ) break;
			output[i]=new byte[c-l];
			System.arraycopy(input, l, output[i], 0, c-l);
			l=c+separator.length;	i++;
			
			if( i>=output.length )	//	растягиваем выходной массив...
				{
				byte[][]temp=new byte[output.length*2][];
				System.arraycopy(output, 0, temp, 0, output.length);
				output=temp;
				}
			}
		output[i]=new byte[input.length-l];
		System.arraycopy(input, l, output[i], 0, input.length-l);
		i++;
		
		byte[][]temp=new byte[i][];
		System.arraycopy(output, 0, temp, 0, i);
		
		return temp;
		}
	public	static	void print( byte[]input )
		{	for( byte c : input ) System.out.print( (int)c+" " );	System.out.println();	}
	}
