package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.bplustree;

import java.util.Collections;
import java.util.Vector;

import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataEntryPair;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKey;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataKeyComparator;
import edu.ucr.cs.jumpwisely.topk_research.data_model.data_element.DataValue;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.BufferManager;
import edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io.IOSetup;

public class BPlusIndexNode extends BPlusNode{
	
	Vector<DataEntryPair> content;
	BPlusNode head_child;
	
	public BPlusIndexNode(BufferManager BUFFER){
		this.content = new Vector<DataEntryPair>();
		this.head_child = null;
		this.BUFFER = BUFFER;
	}
	
	public BPlusIndexNode(Vector<DataEntryPair> content, BPlusNode _head_child, BufferManager BUFFER){
		this.content = content;
		this.head_child = _head_child;
		this.BUFFER = BUFFER;
	}

	@Override
	public int delete(DataKey key) {
		BUFFER.insert(this.hashCode());
		int rtn = 0;
		BPlusNode node_to_process = null;
		int i = 0;
		for(i = 0; i < this.content.size(); i++){
			if(key.compareTo(this.content.get(i).key) > 0)
				continue;
			else 
				if(i != 0){
					node_to_process = this.content.get(i - 1).child;
					i = i - 1;
					break;
				}else{
					node_to_process = this.head_child;
					i = -1;
					break;
				}
		}
		if(node_to_process == null){
			node_to_process = this.content.get(this.content.size() - 1).child;
			i = this.content.size() - 1;
		}
		rtn = node_to_process.delete(key);
		if(node_to_process.underflowing()){
			/**
			 *  If reach the end of the list, merge the last entry with its left sibling
			 *  and remove the last entry.
			 */
			if(i == this.content.size() - 1){
				if(i == 0){
					head_child.merge(this.content.get(i), true);
					this.content.remove(i);
				}else{
					node_to_process.merge(this.content.get(i - 1), false);
					this.content.remove(i);
					if(this.content.get(i - 1).child.overflowing()){
						this.content.add(i, this.content.get(i - 1).child.split());
					}
				}
			}else{
				/**
				 * If the entry to be deleted is in the middle of the list, merge it with
				 * the entry after it, and then remove the entry after it.
				 */
				node_to_process.merge(this.content.get(i + 1), true);
				this.content.remove(i+1);
				if(node_to_process.overflowing()){
					this.content.add(i+1, node_to_process.split());
				}
			}
		}
		return rtn;
	}

	@Override
	public boolean overflowing() {
		return this.get_size() > IOSetup.PAGE_SIZE * IOSetup.PAGE_FILL_FACTOR;
	}

	@Override
	public DataValue search(DataKey key) {
		BUFFER.insert(this.hashCode());
		BPlusNode node_to_process = null;
		for(int i = 0; i < this.content.size(); i++){
			if(key.compareTo(this.content.get(i).key) > 0)
				continue;
			else if(i != 0){
				node_to_process = this.content.get(i - 1).child;
				break;
			}else{
				node_to_process = this.head_child;
				break;
			}
		}
		if(node_to_process == null){
			node_to_process = this.content.get(this.content.size() - 1).child;
		}
		return node_to_process.search(key);
	}

	@Override
	public DataEntryPair split() {
		BUFFER.insert(this.hashCode(), true);
		Collections.sort(this.content, new DataKeyComparator());
		int split_count = this.content.size() / 2;
		Vector<DataEntryPair> new_content = new Vector<DataEntryPair>();
		while(this.content.size() > split_count){
			new_content.add(0, this.content.remove(this.content.size() - 1));
		}
		DataEntryPair entry_pushed = this.content.remove(this.content.size() - 1);
		BPlusIndexNode new_index = new BPlusIndexNode(new_content, entry_pushed.child, this.BUFFER);
		this.BUFFER.insert(new_index.hashCode());
		entry_pushed.child = new_index;
		return entry_pushed;
	}

	@Override
	public void update(DataKey key, DataValue value) {
		BPlusNode node_to_process = null;
		int i = 0;
		for(; i < this.content.size(); i++){
			if(key.compareTo(this.content.get(i).key) > 0)
				continue;
			else 
				if(i != 0){
					node_to_process = this.content.get(i - 1).child;
					break;
				}else{
					node_to_process = this.head_child;
					break;
				}
		}
		if (node_to_process == null){
			node_to_process = this.content.get(this.content.size() - 1).child;
		}
		node_to_process.update(key, value);
		if(node_to_process.overflowing()){
			DataEntryPair entry_pushed = node_to_process.split();
			if(i == this.content.size())
				this.content.add(entry_pushed);
			else if(i == 0)
				this.content.add(0, entry_pushed);
			else
				this.content.add(i, entry_pushed);
		}
	}

	@Override
	public void merge(DataEntryPair sibling, boolean isright) {
		BUFFER.insert(this.hashCode(), true);
		BUFFER.insert(sibling.hashCode(), true);
		if(sibling == null)
			return;
		if(isright){
			// Handle the head child
			this.content.add(new DataEntryPair(sibling.key, ((BPlusIndexNode)sibling.child).head_child));
			// Add the other children from the right sibling
			while(((BPlusIndexNode)sibling.child).content.size() > 0){
				this.content.add(((BPlusIndexNode)sibling.child).content.remove(0));
			}
		}else{
			// Handle the head child
			((BPlusIndexNode)sibling.child).content.add(new DataEntryPair(sibling.key, this.head_child));
			// Add the other children from the left sibling
			while(this.content.size() > 0){
				((BPlusIndexNode)sibling.child).content.add(this.content.remove(0));
			}
		}
	}

	@Override
	public boolean underflowing() {
		return this.get_size() < IOSetup.PAGE_SIZE * IOSetup.PAGE_UNDERFLOW_FACTOR;
	}

	@Override
	public int get_size() {
		if(this.content.size() > 0)
			return 4 + this.content.size() * this.content.get(0).get_size();
		else
			return 4;
	}
	
	public String toString(){
		String rtn_str = "<INDEX_NODE><ENTRY key=\"H\">" + head_child.toString() + "</ENTRY>";
		for(int i = 0; i < content.size(); i++){
			rtn_str += "<ENTRY key=\"" + content.get(i).key.toString() + "\">"+ content.get(i).child.toString() + "</ENTRY>";
		}
		return rtn_str + "</INDEX_NODE>";
	}

	@Override
	public BPlusLeafNode get_leftmost_leaf() {
		if(this.head_child!=null)
			return this.head_child.get_leftmost_leaf();
		else if(this.content.size() > 0)
			return this.content.get(0).child.get_leftmost_leaf();
		else
			return null;
	}
}
