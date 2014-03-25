/**
 *	Author:		Kaleb(Sadalmalik) (i just use different nicknames :D, i'm Gleb)
 *	License:	http://www.wtfpl.net/txt/copying/
**/

package ru.ifaculty.java.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;

import com.sun.javafx.collections.MappingChange.Map;

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
 *	UPD:	Пока менял нижеследующий код тестов - строки внезапно начали убивать гораздо больше знаков.
 *			Есть подозрение что это из-за перехода самих исходников на UTF-9
 *			В любом случае даже при вписывании разных charset-ов всё равно какие-то байты убиваются
 *
 *	UPD(2):	Сделал глобальный тест всех доступных Charset-ов. Как оказалось, их ДОХРЕНА О_О
 *			Тем не менее применение строк к бинарным данным всё равно недопустимо, а моя реализация
 *			работы с байтовыми массивами всё равно даёт большую производительность.
 *			...
 *			Так что вся эта бодяга здесь остаётся чисто как теоретическое исследование.
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
	//***  Тут специальные тесты всякого что под руку попалось   *************************************//
	public	static	String	hex( byte v )
		{
		final char[]x = "0123456789ABCDEF".toCharArray();
		int t = (v<0?v+256:v);	return( x[t>>4] +""+ x[t&0xF] );
		}
	public	static	void	main(String[]args)	//	Это тесты для демонстрации
		{
		byte[] dataA, dataB;
		byte[][]	dataC;
		//	тесты String
		System.out.println();
		System.out.println("Массовый тест всех Charset-ов");
		System.out.println();

		dataA=new byte[256];
		for( int i=0 ; i<256 ; i++ )	{	dataA[i]=(byte)i;	}
		
		Entry<String, Charset>e;
		Iterator<Entry<String, Charset>> iter = Charset.availableCharsets().entrySet().iterator();

		int change=0;
		int unavailable=0;
		System.out.println( "	всего доступно:	" + Charset.availableCharsets().size() );
		System.out.println();
		
		while( iter.hasNext() )
			{
			e = iter.next();
			System.out.println( "Charset	:	"+e.getKey() );
			try	{
				Charset ch = e.getValue();
				dataB = (new String( dataA , ch )).getBytes( ch );
				if( dataA.length != dataB.length )
					{
					System.out.println("	Charset изменяет размер знаков. Сравнение невозможно");
					System.out.println();
					}
				else{
					int counter=0;
					for( int i=0, j=0 ; i<256 ; i++ )
					if( dataA[i] != dataB[i] )
						{
						counter++;
						System.out.print( "	[ " + hex(dataA[i]) + " -> " + hex(dataB[i]) + " ]" );	//	Выводим все отличия
						if( j%16==15 )	System.out.println();
						j++;
						}
					System.out.println();
					System.out.println("	Charset изменяет "+counter+" знаков");
					if( counter==0 )change++;
					System.out.println();
					}
				}
			catch( Exception U )
				{
				System.out.println("	Charset не поддерживается системой");
				System.out.println();	unavailable++;
				}
			}
		System.out.println();
		System.out.println("Charset-ы, не меняющие знаки:	"+change);
		System.out.println("Charset-ы, не поддерживаемые системой:	"+unavailable+" (ЧТО ОНИ ЗАБЫЛИ В СПИСКЕ?)");
		System.out.println();
		
		
		
		//	Сравнение скорости работы String.split и моей реализации split
		System.out.println();
		System.out.println("Сравнение работы String.split и ByteWorker.split");
		System.out.println();
		
		long t1, t2;
		long dt1, dt2;
		
		String	strA = "   test  !  string    ";
		String	strB = "!";
		String	strC[];
		
		dataA = strA.getBytes();
		dataB = strB.getBytes();
		t1=System.nanoTime();
		dataC = split( dataA , dataB );
		t2=System.nanoTime();
		System.out.println();
		System.out.println("ByteWorker.split");
		System.out.println("byte	"+dataA.length);	dt1=(t2-t1);
		System.out.println("time1	"+dt1);	
		System.out.println("size	"+dataC.length);
		
		t1=System.nanoTime();
		strC = strA.split( strB );
		t2=System.nanoTime();
		System.out.println();
		System.out.println("String.split");
		System.out.println("byte	"+strA.length());	dt2=(t2-t1);
		System.out.println("time2	"+dt2);	
		System.out.println("size	"+strC.length);
		System.out.println();
		System.out.println( "time2 / time1 = "+ ((double)dt2)/((double)dt1) );

		System.out.println();
		System.out.println();
		
		for( int i=0 ; i<dataC.length ; i++ )
			{
			System.out.println();
			if( dataC[i]==null ){	System.out.print(i+"	null ");	}
			else{	System.out.print(i+"	");	for( int j=0 ; j<dataC[i].length ; j++ )	System.out.print(dataC[i][j]+" ");	}
			}
		System.out.println();
		int i=0;
		for( String s : strC )
			{
			System.out.println();
			if( s==null ){	System.out.print(i+"	null ");	}
			else{	System.out.print(i+"	");	byte[]x = s.getBytes(); for( int j=0 ; j<x.length ; j++ )	System.out.print(x[j]+" ");	}
			i++;
			}
		}
	//************************************************************************************************//
	
	
	
	//***	Тут собственно полезная функциональность   ***********************************************//
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
	//************************************************************************************************//
	}
