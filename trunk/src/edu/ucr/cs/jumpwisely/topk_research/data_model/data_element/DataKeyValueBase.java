/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.data_element;

/**
 * @author jarodwen
 *
 */
public abstract class DataKeyValueBase {
	
	public DataKey key;
	
	public abstract int compareTo(DataKeyValueBase obj);
	
	public abstract int get_size();
}
