/**
 *	Topk_Research BPlusNode.java
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
