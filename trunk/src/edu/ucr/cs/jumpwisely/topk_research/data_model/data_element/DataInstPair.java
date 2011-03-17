/**
 * 
 */
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
