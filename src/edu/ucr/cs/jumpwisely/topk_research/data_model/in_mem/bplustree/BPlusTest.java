/**
 *	Topk_Research BPlusTest.java
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
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Vector;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataInstPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.IOSetup;

/**
 * @author jarodwen
 *
 */
public class BPlusTest {
	
	static Vector<DataInstPair> data_set;
	static BPlusTree bptree;

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		BufferedReader bufFile = new BufferedReader(new FileReader(args[0]));
		String strTemp = "";
		data_set = new Vector<DataInstPair>();
		while ((strTemp = bufFile.readLine()) != null) {
			if(strTemp.trim().equalsIgnoreCase(""))
				continue;
			String[] elems = strTemp.trim().split("\t");
			Vector<Double> value_list = new Vector<Double>();
			for(int i = 1; i < elems.length; i++){
				value_list.add(Double.valueOf(elems[i]));
			}
			data_set.add(new DataInstPair(new DataKey(Integer.valueOf(elems[0])), new DataValue(value_list)));
			if(data_set.size() > 10)
				break;
		}
		/**
		 * Build a new b+tree
		 */
		BufferManager BUFFER = new BufferManager();
		bptree = new BPlusTree(BUFFER);
		System.out.println("Page size: " + IOSetup.PAGE_SIZE);
		System.out.println("Data size: " + data_set.get(0).get_size());
		/**
		 * Insert the test data
		 */
		for(int i = 0; i < data_set.size(); i++) {
			DataInstPair data = data_set.get(i);
			System.out.println("******* Insert (" + data.key.toString() + "; " + data.value.toString() + ") *******");
			bptree.update(data.key, data.value);
			if(i%10 == 0)
				System.out.print("*");
		}
		if(IOSetup.IS_DEBUG) {
			System.out.print("\n");
			System.out.println(bptree.toString());
			search_test(100);
			delete_test(300);
			System.out.println(bptree.toString());
			search_test(100);
		}
		/**
		 * A simple interactive interface. Have fun!
		 */
		bufFile = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			System.out.print("Query: ");
			String str_tmp = bufFile.readLine();
			/**
			 * Exit the interface
			 */
			if(str_tmp.equalsIgnoreCase("exit")){
				System.out.println("Thanks! 88!");
				break;
			}
			String[] elem = str_tmp.split(" ");
			/**
			 * Print the help information
			 */
			if(elem.length <1){
				System.out.println("Help: search \"s key\"; update \"u key val1 val2\", delete \"d key\".");
				continue;
			}
			/**
			 * print tree structure
			 */
			if(elem[0].equalsIgnoreCase("p")){
				System.out.println(bptree.toString());
				continue;
			}
			DataKey query_key = new DataKey(Integer.valueOf(elem[1]));
			/**
			 * Search
			 */
			if(elem[0].equalsIgnoreCase("s")){
				DataValue search_value = bptree.search(query_key);
				if(search_value == null){
					System.out.println("Returned 0 result.");
				}else{
					System.out.println("Returned " + search_value.toString());
				}
			}else if(elem[0].equalsIgnoreCase("u")){
			/**
			 * Update
			 */
				Vector<Double> values = new Vector<Double>();
				values.add(Double.valueOf(elem[2]));
				values.add(Double.valueOf(elem[3]));
				DataValue update_value = new DataValue(values);
				bptree.update(query_key, update_value);
				System.out.println("Updated (" + query_key.toString() + ";" + update_value.toString() + ")");
			}else if(elem[0].equalsIgnoreCase("d")){
			/**
			 * Delete
			 */
				int del_rtn = bptree.delete(query_key);
				if(del_rtn < 0){
					System.out.println("Abort: Cannot found key " + query_key.toString() + "!");
				}else{
					System.out.println("Deleted key " + query_key.toString() + "!");
				}
			}
		}
	}

	/**
	 * An automatic test program for searching {@link #test_num} randomly
	 * generated keys. 
	 * @param test_num
	 */
	private static void search_test(int test_num){
		// Test for search
		Random rand = new Random();
		for(int i = 0; i < test_num; i++){
			int search_key = rand.nextInt(test_num);
			System.out.print("Search for: " + search_key);
			DataValue search_value = bptree.search(new DataKey(search_key));
			if(search_value == null){
				System.out.print(" is not found!\n");
			}else{
				System.out.print(" is found with value " + search_value.toString() + "!\n");
			}
		}
	}
	
	/**
	 * An automatic test program for deleting {@link #test_num} randomly
	 * generated keys.
	 * @param test_num
	 */
	private static void delete_test(int test_num){
		Random rand = new Random();
		for(int i = 0; i < test_num; i++){
			int search_key = rand.nextInt(test_num);
			System.out.println("******* Delete key: " + search_key + " *******");
			DataValue search_value = bptree.search(new DataKey(search_key));
			if(search_value == null){
				System.out.print("Key is not found!\n");
			}
			if(bptree.delete(new DataKey(search_key)) >= 0)
				System.out.println("Key " + search_key + " is deleted!");
			search_value = bptree.search(new DataKey(search_key));
			if(search_value == null){
				System.out.println("Search key " + search_key + " is not found!");
			}else{
				System.out.println("Search key " + search_key + " is found with value " + search_value.toString());
			}
			//System.out.println(bptree.toString());
		}
	}
}
