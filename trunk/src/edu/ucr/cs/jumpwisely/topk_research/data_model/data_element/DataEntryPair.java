/**
 *	Topk_Research DataEntryPair.java
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
