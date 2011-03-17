/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.data_element;

import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree.BPlusNode;

/**
 * @author jarodwen
 *
 */
public class DataEntryPair extends DataKeyValueBase{

	/**
	 * ke-value pair.
	 */
	public BPlusNode child;
	
	public DataEntryPair(){
		key = new DataKey(0);
		child = null;
	}
	
	public DataEntryPair(DataKey key, BPlusNode child){
		this.key = key;
		this.child = child;
	}
	
	public int compareTo(DataKeyValueBase obj){
		if(this.key.key < obj.key.key){
			return -1;
		}else if(this.key.key > obj.key.key){
			return 1;
		}else{
			return 0;
		}
	}

	@Override
	public int get_size() {
		return key.get_size() + 4;
	}
	
	public String toString(){
		return "<ENTRY><KEY>" + key.toString() + "</KEY><REF>" + child.toString() + "</REF></ENTRY>";
	}
}
