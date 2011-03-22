/**
 *	Topk_Research IOSetup.java
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
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io;

/**
 * @author jarodwen
 *
 */
public class IOSetup {

	public static int DATA_SIZE = 2000;
	public static int QUERY_SIZE = 200;
	
	public static double PAGE_FILL_FACTOR = 0.9;
	public static double PAGE_UNDERFLOW_FACTOR = 0.4;
	/**
	 * The swapping policy. 
	 * 
	 * 0: Random swapping.
	 * 1: LRU 
	 * 2: MRU
	 * 3: NU
	 * 4: OU
	 * 5: LLT
	 * 6: SLT
	 * 
	 */
	public static int BUFFER_TYPE = 1;
	public static int PAGE_SIZE = 1024;
	public static int BUFFER_CAPACITY = 100;
	public static boolean IS_BULKLOAD = false;
	public static int DIMENSIONALITY = 2;
	public static int DATA_DIVIATION = 500000;
	public static long GENERATE_SEED = 20091110;
	public static int DATA_DISTRIBUTION = 1;
	
	public static boolean IS_DEBUG = false;
	
}
