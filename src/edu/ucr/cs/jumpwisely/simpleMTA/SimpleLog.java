/**
 *	SimpleMTA SimpleLog.java
 *
 *  Copyright (C) 2010 JArod Wen
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 **/
package edu.ucr.cs.jumpwisely.simpleMTA;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SimpleLog implements Runnable {
	
	BufferedWriter bufWriter;
	String threadID;
	
	public SimpleLog(String ThreadID){
		threadID = ThreadID;
		try {
			bufWriter = new BufferedWriter(new FileWriter("Log", true));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run(){
		
	}
	
	public void print(String strContent){
		Date dDate = new Date();
		DateFormat dFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		String strDate = dFormat.format(dDate) +"("+ threadID +")";
		try {
			System.out.print(strDate+" "+strContent);
			bufWriter.write(strDate+" "+strContent);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bufWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void println(String strContent){
		Date dDate = new Date();
		DateFormat dFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		String strDate = dFormat.format(dDate) +"("+ threadID +")";
		try {
			System.out.println(strDate+" "+strContent);
			bufWriter.write(strDate+" "+strContent+"\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			bufWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void println(Exception e){
		String strContent = e.toString();
		Date dDate = new Date();
		DateFormat dFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		String strDate = dFormat.format(dDate) +"("+ threadID +")";
		try {
			System.out.println(strDate+" "+strContent+"\n");
			bufWriter.write(strDate+" "+strContent);
		} catch (IOException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		try {
			bufWriter.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public void println(int i){
		println(String.valueOf(i));
	}
	
	public void closeLog(){
		try {
			bufWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
