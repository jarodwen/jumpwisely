/**
 *	RandomProjection Utilities.java
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

public class Utilities {

	public static double Log2(int i){
		return (double) (Math.log(i)/Math.log(2.0));
	}
	
	public static double Log2(double i){
		return (double) (Math.log(i)/Math.log(2.0));
	}
	
	public static double[][] laplace_padding(double[][] table){
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				table[i][j] += (0.1/(1 + 0.1 * table.length)); 
			}
		}
		return table;
	}
	
	public static double[][] logize(double[][] table){
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				table[i][j] = Log2(table[i][j]); 
			}
		}
		return table;
	}
	
	public static double[][] delogize(double[][] table){
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				table[i][j] = Math.pow(2, table[i][j]); 
			}
		}
		return table;
	}
}
