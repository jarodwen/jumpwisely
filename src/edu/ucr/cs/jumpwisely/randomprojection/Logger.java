/**
 *	RandomProjection Logger.java
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
package edu.ucr.cs.jumpwisely.randomprojection;

import java.util.HashMap;

/**
 * @author janie
 *
 */
public class Logger {

	public static boolean log_switch = true;
	
	
	public static void debug(String str){
		if(log_switch)
			System.out.println("==>== Debug(" + System.currentTimeMillis()+"): " + str);
	}
	
	public static void debug_table(double[][] table){
/*		if(!log_switch)
			return;
		String table_str = "";
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				table_str += String.valueOf(table[i][j] + "\t");
			}
			table_str += "\n";
		}
		System.out.println("==>== Debug: Table");
		System.out.print(table_str);*/
	}
	
	public static void debug_buckets(HashMap<Integer, Integer> buck_stat){
		if(!log_switch)
			return;
		String his_str = "";
		for(int key : buck_stat.keySet()){
			his_str += key + ":" + buck_stat.get(key) + "\t";
		}
		System.out.println("==>== Debug: Bucket Histograph: " + his_str);
	}
}
