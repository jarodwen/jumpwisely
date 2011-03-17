/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataEntryPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;

/**
 * @author jarodwen
 *
 */
public abstract class BPlusNode {
	
	public BufferManager BUFFER;

	public abstract DataValue search(DataKey key);
	
	public abstract void update(DataKey key, DataValue value);
	
	public abstract int delete(DataKey key);
	
	public abstract boolean overflowing();
	
	public abstract boolean underflowing();
	
	public abstract DataEntryPair split();
	
	public abstract void merge(DataEntryPair sibling_entry, boolean isright);
	
	public abstract int get_size();
	
	public abstract BPlusLeafNode get_leftmost_leaf();
}
