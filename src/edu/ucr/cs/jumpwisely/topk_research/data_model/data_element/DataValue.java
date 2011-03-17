/**
 * 
 */
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
