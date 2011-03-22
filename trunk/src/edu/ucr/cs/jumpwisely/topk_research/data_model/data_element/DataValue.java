/**
 *	Topk_Research DataValue.java
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

import java.util.Vector;

/**
 * @author jarodwen
 *
 */
public class DataValue {
	/**
	 * The list of integer values
	 */
	public Vector<Double> values;
	
	/**
	 * Constructor
	 * @param _values
	 */
	public DataValue(Vector<Double> _values){
		this.values = _values;
	}
	
	public DataValue(double _single_value){
		this.values = new Vector<Double>();
		this.values.add(_single_value);
	}
	
	/**
	 * get size
	 * @return
	 */
	public int get_size(){
		return this.values.size() * 8;
	}
	
	public String toString(){
		StringBuilder strbud = new StringBuilder();
		for(int i = 0; i < values.size(); i++){
			strbud.append(values.get(i).toString() + ",");
		}
		return strbud.substring(0, strbud.length() - 1);
	}
}
