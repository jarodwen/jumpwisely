/**
 *	Topk_Research DataKeyValueBase.java
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

/**
 * @author jarodwen
 *
 */
public class DataInstPair extends DataKeyValueBase{

	/**
	 * key value pair of the data
	 */
	public DataValue value;
	
	/**
	 * Constructor.
	 * @param _key
	 * @param _value
	 */
	public DataInstPair(DataKey _key, DataValue _value){
		this.key = _key;
		this.value = _value;
	}
	
	/**
	 * Comparator
	 * @param another_obj
	 * @return
	 */
	public int compareTo(DataKeyValueBase another_obj){
		if(this.key.key > another_obj.key.key){
			return 1;
		}else if (this.key.key < another_obj.key.key){
			return -1;
		}else{
			return 0;
		}
	}
	
	public int get_size(){
		return this.key.get_size() + this.value.get_size();
	}
	
	public String toString(){
		return "<DATA><KEY>" + key.toString() + "</KEY><VALUE>" + value.toString() + "</VALUE></DATA>";
	}
}
