/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree;

import java.util.Vector;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataEntryPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;

/**
 * @author jarodwen
 *
 */
public class BPlusTree {

	/**
	 * The root node.
	 */
	public BPlusNode root;
	
	/**
	 * Buffer Manager.
	 */
	public BufferManager BUFFER;
	
	/**
	 * Constructor for the in-memory B+ tree
	 * @param dims
	 * @param BUFFER
	 */
	public BPlusTree(BufferManager BUFFER){
		this.BUFFER = BUFFER;
		root = new BPlusLeafNode(this.BUFFER);
	}
	
	public void update(DataKey key, DataValue value){
		this.root.update(key, value);
		if(this.root.overflowing()){
			DataEntryPair new_entry = this.root.split();
			Vector<DataEntryPair> new_content = new Vector<DataEntryPair>();
			new_content.add(new_entry);
			BPlusIndexNode new_root = new BPlusIndexNode(new_content, this.root, this.BUFFER);
			this.root = new_root;
		}
	}
	
	public int delete(DataKey key){
		int rtn = this.root.delete(key);
		if(this.root instanceof BPlusIndexNode)
			if(((BPlusIndexNode)root).content.size() == 0)
				this.root = ((BPlusIndexNode)root).head_child;
		return rtn;
	}
	
	public DataValue search(DataKey key){
		return this.root.search(key);
	}
	
	public String toString(){
		return "<BPTREE>" + this.root.toString() + "</BPTREE>";
	}
}
