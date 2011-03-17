/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.data_element;

import java.util.Comparator;

/**
 * @author jarodwen
 *
 */
public class DataKey {

	double key;
	
	/**
	 * Constructor
	 * @param _key
	 */
	public DataKey(int _key) {
		this.key = _key;
	}
	
	public DataKey(double _key) {
		this.key = _key;
	}
	
	public DataKey(DataKey okey){
		this.key = okey.key;
	}
	
	/**
	 * Equals To
	 * @param okey
	 * @return
	 */
	public boolean equals(DataKey okey){
		return this.key == okey.key;
	}
	
	/**
	 * Get the data size;
	 * @return
	 */
	public int get_size(){
		return 4;
	}
	
	public int compareTo(DataKey _key){
		if(this.key < _key.key)
			return -1;
		else if(this.key > _key.key)
			return 1;
		else
			return 0;
	}
	
	/**
	 * Comparator for keys.
	 * @author jarodwen
	 *
	 */
	class DataKeyComparator implements Comparator<DataKey> {

		/**
		 * Return the flag for the comparison between two keys
		 */
		@Override
		public int compare(DataKey o1, DataKey o2) {
			if(o1.key > o2.key){
				return 1;
			}else if (o1.key < o2.key){
				return -1;
			}else{
				return 0;
			}
		}
		
	}
	
	public double get_key(){
		return this.key;
	}
	
	public String toString(){
		return String.valueOf(this.key);
	}
}