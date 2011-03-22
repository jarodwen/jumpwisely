/**
 *	Topk_Research TopKAlgsTest.java
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
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.topk_algs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataInstPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValueComparator;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree.BPlusLeafNode;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree.BPlusTree;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;

/**
 * 
 * @author jarodwen
 *
 */
public class TopKAlgsTest {
	/**
	 * The vector containing all the data, which is used
	 * to build the indices and also implement naive top-
	 * k algorithm
	 */
	static Vector<DataInstPair> data_set;
	/**
	 * The main index for the data, which is indexed with
	 * the index order (insert order) from the original
	 * data text file.
	 */
	static BPlusTree bp_main_tree;
	/**
	 * The list of the trees for the additional indices
	 * on each attribute
	 */
	static HashMap<Integer, BPlusTree> tree_lists;
	/**
	 * The maximum value for each attribute
	 */
	static double[] max_data;
	/**
	 * The minimum value for each attribute
	 */
	static double[] min_data;
	/**
	 * Buffer manager.
	 */
	static BufferManager bm;
	/**
	 * The size of the top-k items
	 */
	static int k = 20;
	/**
	 * The number of attributes
	 */
	static int data_item_count;
	/**
	 * Flag for the score function. Based on the projection requirement:
	 * http://www.cs.ucr.edu/~mvieira/cs236/project.html,
	 * three types of score functions are required:
	 * (1) sum, when the value is 0;
	 * (2) Only NBA: sum( 0.9*t.attr2, 0.1*t.attr3, t.attr5), when the value is 1;
	 * (3) Only NBA: sum( 0.2*t.attr1, 0.7*t.attr2, t.attr3, 0.9*t.attr4), when the value is 2.
	 * More details can be found from {@link #score_func(Vector)}.
	 */
	static int nba_score_type = 2;
	
	public static void main(String[] args) throws IOException{
		// Load data and build trees
		BufferedReader bufFile = new BufferedReader(new FileReader(args[0]));
		String strTemp = "";
		data_set = new Vector<DataInstPair>();
		while ((strTemp = bufFile.readLine()) != null) {
			if(strTemp.trim().equalsIgnoreCase(""))
				continue;
			String[] elems = strTemp.trim().split("\\s");
			int key = Integer.valueOf(elems[0]);
			Vector<Double> value_list = new Vector<Double>();
			boolean bound_init = false;
			for(int i = 1; i < elems.length; i++){
				double dval = Double.valueOf(elems[i]);
				value_list.add(dval);
				// Initialize the max/min list
				if(max_data == null){
					data_item_count = elems.length - 1;
					max_data = new double[data_item_count];
					min_data = new double[data_item_count];
				}
				if (!bound_init) {
					max_data[i - 1] = dval;
					min_data[i - 1] = dval;
					bound_init = true;
				} else {
					if (dval >= max_data[i - 1])
						max_data[i - 1] = dval;
					if (dval <= min_data[i - 1])
						min_data[i - 1] = dval;
				}
			}
			data_set.add(new DataInstPair(new DataKey(key), new DataValue(value_list)));
		}
		// Get the dimension of the data
		data_item_count = data_set.get(0).value.values.size();
		// Naive topk query
		naive();
		// Setup the indices for top-k query
		bm = new BufferManager();
		bp_main_tree = new BPlusTree(bm);
		for(int i = 0; i < data_set.size(); i++) {
			bp_main_tree.update(data_set.get(i).key, data_set.get(i).value);
		}
		System.out.println(bm.currentStatus());
		ta_original();
		System.out.println(bm.currentStatus());
		// re-create indices for sorted algorithms
		create_sorted_lists();
		System.out.println(bm.currentStatus());
		ta_sorted();
		System.out.println(bm.currentStatus());
		nra();
		System.out.println(bm.currentStatus());
	}
	
	/**
	 * The unsorted version of the TA algorithm. 
	 * 
	 * In this algorithm, although random access is supported, data
	 * is not sorted in indices of attributes. The search follows a
	 * round-robin fashion to visit all the attribute lists.
	 * 
	 * Notice that in this case, the threshold doesn't work since we
	 * cannot get the maximum value on each attribute through sorted
	 * access. In this way, this algorithm works in the naive way of
	 * scoring all objects in the data set and return the top-k list.
	 */
	private static void ta_original(){
		Vector<DataInstPair> topk = new Vector<DataInstPair>();
		int data_item_count = data_set.get(0).value.values.size();
		tree_lists = new HashMap<Integer, BPlusTree>();
		for(int i = 0; i < data_item_count; i++){
			tree_lists.put(i, new BPlusTree(bm));
		}
		/**
		 * Initialize the lists for attributes. Notice that in the original
		 * version, each list is indexed with the insert order of the data,
		 * that is, the values are not sorted.
		 */
		for(int i = 0; i < data_set.size(); i++) {
			for(int j = 0; j < data_item_count; j++){
				tree_lists.get(j).update(data_set.get(i).key, new DataValue(data_set.get(i).value.values.get(j)));
			}
		}
		/**
		 * Search the lists in a round-robin fashion to get the top-k objects.
		 */
		BPlusLeafNode sleaf = tree_lists.get(0).root.get_leftmost_leaf();
		int item_index = 0;
		while(sleaf != null){
			Vector<Double> val_list = new Vector<Double>();
			// Get the top score for the visiting list
			DataInstPair d = sleaf.get_item(item_index);
			if(d == null){
				sleaf = sleaf.right_sibling_ptr;
				item_index = 0;
				continue;
			}
			DataKey key = d.key;
			val_list.add(d.value.values.get(0));
			// Randomly access other lists for other attributes
			for(int j = 1; j < data_item_count; j++){
				val_list.add(tree_lists.get(j).search(key).values.get(0));
			}
			insert_in_order(new DataInstPair(key, new DataValue(val_list)), topk);
			item_index++;
		}
		print_topk(topk);
	}
	
	/**
	 * TA_Sorted algorithm.
	 * 
	 * This algorithm utilizes the threshold to terminate the 
	 * algorithm early, when 
	 * (1) There are at least k objects in the candidate list;
	 * (2) the next data item to be checked has an upper bound
	 * score value smaller than the score of the k-th object in
	 * the list.
	 * The capability of both sorted and random accesses guarantee
	 * the correctness of the pruning. 
	 */
	private static void ta_sorted(){
		Vector<DataInstPair> topk = new Vector<DataInstPair>();
		// TA-sorted algorithm
		/**
		 * We maintain a hash set for the data in the top-k candidate
		 * list so that data in the set will not be inserted twice.
		 */
		HashSet<Double> item_seen = new HashSet<Double>();
		System.out.println("Start TA-Sorted algorithm.");
		Vector<Double> threshold = new Vector<Double>();
		HashMap<Integer, BPlusLeafNode> lnodes = new HashMap<Integer, BPlusLeafNode>();
		// Get the starting leaf nodes for each attributes
		for(int j = 0; j < data_item_count; j++){
			lnodes.put(j, tree_lists.get(j).root.get_leftmost_leaf());
			/**
			 * Initialize the threshold. Since the first value in data is the 
			 * original key, we skip it and add up the second value.
			 */
			threshold.add(lnodes.get(j).get_item(0).value.values.get(1));
		}
		int[] item_index = new int[data_item_count];
		while (!end_ta_s(lnodes)) {
			for (int j = 0; j < data_item_count; j++) {
				BPlusLeafNode leaf = lnodes.get(j);
				if (leaf != null) {
					DataInstPair d = leaf.get_item(item_index[j]);
					if (d == null) {
						leaf = leaf.right_sibling_ptr;
						lnodes.put(j, leaf);
						item_index[j] = 0;
						continue;
					}
					DataKey key = new DataKey(d.value.values.get(0));
					DataValue original_v = bp_main_tree.search(key);
					/**
					 * if(topk.size() > 0)
					 *
					 * System.out.println(score_func(threshold) + ":" + score_func(original_v) + ":" + score_func(topk.get(topk.size() - 1)) + ":" + key.toString() + original_v.toString());
					 * */
					if (topk.size() >= k && score_func(topk.get(k - 1)) >= score_func(threshold)) {
						refine_k(topk);
						print_topk(topk);
						return;
					} else {
						if(!item_seen.contains(key.get_key())){
							item_seen.add(key.get_key());
							insert_in_order(new DataInstPair(key, original_v),
									topk);
						}
						threshold.set(j, d.value.values.get(1));
						item_index[j] = item_index[j] + 1;
					}
				}
			}
		}
		print_topk(topk);
	}
	/**
	 * Decide whether to terminate the TA_Sorted algorithm on by testing if all
	 * the attribute lists have been processed.
	 * @param list
	 * @return
	 */
	private static boolean end_ta_s(HashMap<Integer, BPlusLeafNode> list){
		boolean rtn = true;
		for(int k : list.keySet()){
			if(list.get(k)!= null)
				rtn = false;
		}
		return rtn;
	}
	
	/**
	 * NRA algorithm.
	 * 
	 * This algorithm works for the case where only sorted accesses to
	 * the attribute lists are available. 
	 * 
	 * In order to terminate the algorithm early, both upper bound and
	 * lower bound are maintained to check whether a data should be a
	 * candidate or be pruned. The top-k list keep updated until we can
	 * be sure that the data to be checked has a upper bound score less
	 * than the lower bound score of the k-th data in the top-k list.
	 * 
	 * In the actual implementation, a tricky part without clear notification
	 * in the original paper is that when a candidate should be removed
	 * out of the candidate list, the upper bound threshold for the 
	 * termination should also be updated if the data removed has higher
	 * value on any attributes. Without this step, the algorithm may
	 * terminate early with wrong results.
	 */
	private static void nra(){
		Vector<DataInstPair> topk = new Vector<DataInstPair>();
		// NRA algorithm
		// Get the starting leaf nodes for each attributes
		/**
		 * Here we maintain a hash map for all the data in the top-k candidate list
		 * and each of them contains the values we have seen from the sorted accesses
		 * on different lists. All the unseen values will be 0 by default.
		 */
		HashMap<Double, DataInstPair> item_seen = new HashMap<Double, DataInstPair>();
		System.out.println("Start NRA algorithm.");
		Vector<Double> threshold = new Vector<Double>();
		HashMap<Integer, BPlusLeafNode> lnodes = new HashMap<Integer, BPlusLeafNode>();
		for(int j = 0; j < data_item_count; j++){
			lnodes.put(j, tree_lists.get(j).root.get_leftmost_leaf());
			/**
			 * Initialize the threshold. Since the first value in data is the 
			 * original key, we skip it and add up the second value.
			 */
			threshold.add(lnodes.get(j).get_item(0).value.values.get(1));
		}
		int[] item_index = new int[data_item_count];
		while (!end_ta_s(lnodes)) {
			for (int j = 0; j < data_item_count; j++) {
				BPlusLeafNode leaf = lnodes.get(j);
				if (leaf != null) {
					DataInstPair d = leaf.get_item(item_index[j]);
					if (d == null) {
						leaf = leaf.right_sibling_ptr;
						lnodes.put(j, leaf);
						item_index[j] = 0;
						continue;
					}
					DataKey key = new DataKey(d.value.values.get(0));
					if(key.get_key() == 40.0) 
						System.out.print("");
					double item_val = d.value.values.get(1);
					// Maintain upper bound, lower bound and actual scores.
					Vector<Double> ub_values = new Vector<Double>();
					Vector<Double> lb_values = new Vector<Double>();
					Vector<Double> hm_values = new Vector<Double>();
					// Get the upper and lower bounds
					if(item_seen.containsKey(key.get_key())){
						DataInstPair dp_seen = item_seen.get(key.get_key());
						for(int i = 0; i < data_item_count; i++){
							if(i == j){
								ub_values.add(item_val);
								lb_values.add(item_val);
								hm_values.add(item_val);
							}else if(dp_seen.value.values.get(i) == 0){
								ub_values.add(threshold.get(i));
								lb_values.add(min_data[i]);
								hm_values.add(0.0);
							}else{
								ub_values.add(dp_seen.value.values.get(i));
								lb_values.add(dp_seen.value.values.get(i));
								hm_values.add(dp_seen.value.values.get(i));
							}
						}
					}else{
						for(int i = 0; i < data_item_count; i++){
							if(i == j){
								ub_values.add(item_val);
								lb_values.add(item_val);
								hm_values.add(item_val);
							}else{
								ub_values.add(threshold.get(i));
								lb_values.add(min_data[i]);
								hm_values.add(0.0);
							}
						}
					}
					// Check the stop condition
					double max_threshold = score_func(ub_values);
					for(int jj = 0; jj < data_item_count; jj++){
						double tmp_max = threshold.get(jj) + max_threshold - ub_values.get(jj);
						if(tmp_max > max_threshold)
							max_threshold = tmp_max;
					}
					if(topk.size() >= k && max_threshold <= score_func(topk.get(k - 1))){
						refine_k(topk);
						print_topk(topk);
						return;
					}
					/**
					 *  If the data is contained in the list, remove it and re-insert to 
					 *  keep its order updates.
					 */
					if(item_seen.containsKey(key.get_key())){
						int ii = find_data_from_list(key, topk);
						if (ii >= 0)
							topk.remove(ii);
					}
					/**
					 *  Mark the data to be "seen". Note that even this data has been checked, 
					 *  this line works as an update to the data in the hash map.
					 */
					item_seen.put(key.get_key(), new DataInstPair(key, new DataValue(hm_values)));
					// Find the correct position for insertion.
					int i = 0;
					for(; i < topk.size(); i++){
						if(score_func(lb_values)>score_func(topk.get(topk.size() - i - 1))){
							continue;
						}else if(score_func(lb_values) == score_func(topk.get(topk.size() - i - 1))){
							DataInstPair tmp_dp = item_seen.get(topk.get(i).key.get_key());
							Vector<Double> tmp_values = new Vector<Double>();
							for(int l = 0; l < data_item_count; l++){
								if(tmp_dp.value.values.get(l) == 0)
									tmp_values.add(threshold.get(l));
								else
									tmp_values.add(tmp_dp.value.values.get(l));
							}
							if(score_func(ub_values) > score_func(tmp_values))
								continue;
						}
						break;
					}
					// Insert item.
					if(topk.size() <= 0)
						topk.add(new DataInstPair(key, new DataValue(lb_values)));
					else
						topk.insertElementAt(new DataInstPair(key, new DataValue(lb_values)), topk.size() - i);
					/**
					 *  Maintain only k candidates; when removing the extra ones, keep
					 *  in mind that the threshold may be updated.
					 */
					while(topk.size() > k){
						DataInstPair dp_remove = topk.remove(topk.size() - 1);
						for(int i1 = 0; i1 < data_item_count; i1++){
							double dval_tmp = dp_remove.value.values.get(i1);
							if(dval_tmp > threshold.get(i1))
								threshold.set(i1, dval_tmp);
						}
					}
					item_index[j] = item_index[j] + 1;
					// Update the upper bound value
					if(item_val > threshold.get(j))
						threshold.set(j, item_val);
				}
			}
		}
		print_topk(topk);
	}
	
	/**
	 * Check whether a data key is contained in a data list
	 * @param key
	 * @param list
	 * @return
	 */
	private static int find_data_from_list(DataKey key, Vector<DataInstPair> list){
		for(int i = 0; i < list.size(); i++)
			if(list.get(i).key.equals(key))
				return i;
		return -1;
	}
	
	/**
	 * Naive algorithm, which scores all the objects and sorts them.
	 */
	private static void naive(){
		Vector<DataInstPair> topk = new Vector<DataInstPair>();
		for(DataInstPair dpair : data_set){
			if(topk.size() < k){
				topk = insert_in_order(dpair, topk);
			}else if(score_func(dpair) > score_func(topk.get(k - 1))) {
				topk = insert_in_order(dpair, topk);
			}
		}
		print_topk(topk);
	}
	
	/**
	 * Insert a data object into a list while preserving the order of the list.
	 * @param new_pair
	 * @param kset
	 * @return
	 */
	private static Vector<DataInstPair> insert_in_order(DataInstPair new_pair, Vector<DataInstPair> kset){
		int i = 0;
		for(; i < kset.size(); i++){
			if(score_func(new_pair) >= score_func(kset.get(i))){
				break;
			}
		}
		kset.insertElementAt(new_pair, i);
		while(kset.size() > k){
			kset.remove(kset.size() - 1);
		}
		return kset;
	}
	
	/**
	 * Refine the candidate list so that only k candiates are maintained.
	 * @param kset
	 * @return
	 */
	private static Vector<DataInstPair> refine_k(Vector<DataInstPair> kset){
		while(kset.size() > k){
			kset.remove(kset.size() - 1);
		}
		return kset;
	}
	
	/**
	 * The main scoring function
	 * @param val
	 * @return
	 */
	private static double score_func(Vector<Double> val){
		double score = 0.0;
		switch (nba_score_type) {
		case 0:
			for (int i = 0; i < val.size(); i++) {
				score += val.get(i);
			}
			break;
		case 1:
			score = 0.9 * val.get(1) + 0.1 * val.get(2)
					+ val.get(4);
			break;
		case 2:
			score = 0.2 * val.get(0) + 0.7 * val.get(1)
					+ val.get(2) + 0.9 * val.get(3);
			;
			break;
		default:
			for (int i = 0; i < val.size(); i++) {
				score += val.get(i);
			}
		}
		return score;
	}
	/**
	 * The data instance wrapper for the scoring function 
	 * @param dp
	 * @return
	 */
	private static double score_func(DataInstPair dp){
		return score_func(dp.value.values);
	}
	/**
	 * Print the top-k list to the screen.
	 * @param topk
	 */
	private static void print_topk(Vector<DataInstPair> topk){
		for(int i = 0; i < topk.size(); i++)
			System.out.print(topk.get(i).toString() + " " + score_func(topk.get(i)) + "\n");
	}
	/**
	 * Initialize the attribute indices.
	 */
	private static void create_sorted_lists(){
		// Initialize the tree lists
		System.out.println("Load data and build indices...");
		tree_lists = new HashMap<Integer, BPlusTree>();
		for(int i = 0; i < data_item_count; i++){
			tree_lists.put(i, new BPlusTree(bm));
		}
		
		// Add data into each list index
		for(int j = 0; j < data_item_count; j++){
			// For each attribute, sort in descent order
			Collections.sort(data_set, new DataValueComparator(j));
			// Add each data into the corresponding index
			for(int i = 0; i < data_set.size(); i++){
				/**
				 *  We also need to have the original key (original insert order), 
				 *  so we add the original key as a value item.
				 */
				Vector<Double> val = new Vector<Double>();
				// Add the original key
				val.add(data_set.get(i).key.get_key());
				// Add the attribute value
				val.add(data_set.get(i).value.values.get(j));
				tree_lists.get(j).update(new DataKey(i), new DataValue(val));
			}
		}
	}
}
