/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree;

import java.util.Collections;
import java.util.Vector;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataEntryPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataInstPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKeyComparator;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.IOSetup;

/**
 * @author jarodwen
 *
 */
public class BPlusLeafNode extends BPlusNode {
	
	public Vector<DataInstPair> data_list;
	public BPlusLeafNode left_sibling_ptr;
	public BPlusLeafNode right_sibling_ptr;
	
	public BPlusLeafNode(BufferManager BUFFER){
		data_list = new Vector<DataInstPair>();
		left_sibling_ptr = null;
		right_sibling_ptr = null;
		this.BUFFER = BUFFER;
	}
	
	public BPlusLeafNode(Vector<DataInstPair> _content, BPlusLeafNode l_sptr, BPlusLeafNode r_sptr, BufferManager BUFFER){
		data_list = _content;
		left_sibling_ptr = l_sptr;
		right_sibling_ptr = r_sptr;
		this.BUFFER = BUFFER;
	}

	/* (non-Javadoc)
	 * @see data_model.in_mem.bplustree.BPlusNode#delete(data_model.data_element.DataKey)
	 */
	@Override
	public int delete(DataKey key) {
		boolean isfound = false;
		int rtn = -1;
		int i = 0;
		while(i < this.data_list.size()){
			if(this.data_list.get(i).key.equals(key)){
				this.data_list.remove(i);
				rtn = i;
				continue;
			}
			i++;
		}
		if(isfound){
			BUFFER.insert(this.hashCode(), true);
		}else{
			BUFFER.insert(this.hashCode());
		}
		return rtn;
	}

	/* (non-Javadoc)
	 * @see data_model.in_mem.bplustree.BPlusNode#search(data_model.data_element.DataKey)
	 */
	@Override
	public DataValue search(DataKey key) {
		BUFFER.insert(this.hashCode());
		for(int i = 0; i < data_list.size(); i++) {
			if(data_list.get(i).key.equals(key))
				return data_list.get(i).value;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see data_model.in_mem.bplustree.BPlusNode#update(data_model.data_element.DataKey, data_model.data_element.DataValue)
	 */
	@Override
	public void update(DataKey key, DataValue value) {
		boolean isfound = false;
		BUFFER.insert(this.hashCode(), true);
		for(int i = 0; i < this.data_list.size(); i++){
			if(this.data_list.get(i).key.equals(key)){
				this.data_list.get(i).value = value;
				isfound = true;
			}
		}
		if(!isfound){
			this.data_list.add(new DataInstPair(key, value));
		}
	}

	/* (non-Javadoc)
	 * @see data_model.in_mem.bplustree.BPlusNode#overflowing()
	 */
	@Override
	public boolean overflowing() {
		return this.get_size() >= IOSetup.PAGE_SIZE * IOSetup.PAGE_FILL_FACTOR;
	}
	
	public int get_size(){
		if(data_list.size() > 0) {
			return 8 + data_list.size() * data_list.get(0).get_size();
		}else{
			return 8;
		}
	}

	@Override
	public DataEntryPair split() {
		BUFFER.insert(this.hashCode(), true);
		Collections.sort(data_list, new DataKeyComparator());
		int split_count = data_list.size() / 2;
		// Move half of the content to new leaf
		Vector<DataInstPair> content_new_leaf = new Vector<DataInstPair>();
		while(data_list.size() > split_count){
			content_new_leaf.add(0, data_list.remove(data_list.size() - 1));
		}
		// Create the new leaf, and adjust the sibling pointer
		BPlusLeafNode new_sibling = new BPlusLeafNode(content_new_leaf, this, this.right_sibling_ptr, this.BUFFER);
		this.right_sibling_ptr = new_sibling;
		// Create new index entry, which is pointing to the new sibling node
		DataEntryPair new_sibling_entry = new DataEntryPair(new DataKey(this.data_list.get(this.data_list.size() - 1).key), new_sibling);
		return new_sibling_entry;
	}

	@Override
	public void merge(DataEntryPair sibling, boolean isright) {
		BUFFER.insert(this.hashCode(), true);
		BUFFER.insert(sibling.hashCode(), true);
		if(sibling == null)
			return;
		if(isright){
			while(((BPlusLeafNode)sibling.child).data_list.size() > 0){
				this.data_list.add(((BPlusLeafNode)sibling.child).data_list.remove(0));
			}
			this.right_sibling_ptr = this.right_sibling_ptr.right_sibling_ptr;
		}else{
			while(this.data_list.size() > 0){
				((BPlusLeafNode)sibling.child).data_list.add(this.data_list.remove(0));
			}
			((BPlusLeafNode)sibling.child).right_sibling_ptr = this.right_sibling_ptr;
		}
	}

	@Override
	public boolean underflowing() {
		return this.get_size() < IOSetup.PAGE_SIZE * IOSetup.PAGE_UNDERFLOW_FACTOR;
	}

	public String toString(){
		String rtn_str = "<LEAF_NODE>";
		for(int i = 0; i < data_list.size(); i++) {
			rtn_str += data_list.get(i);
		}
		return rtn_str + "</LEAF_NODE>";
	}

	@Override
	public BPlusLeafNode get_leftmost_leaf() {
		BUFFER.insert(this.hashCode());
		return this;
	}
	
	public DataInstPair get_item(int i) {
		BUFFER.insert(this.hashCode());
		if(i < this.data_list.size() && i >= 0){
			return this.data_list.get(i);
		}else{
			return null;
		}
	}
	
}
