/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.data_element;

import java.util.Comparator;

/**
 * @author jarodwen
 *
 */
public class DataKeyComparator implements Comparator<DataKeyValueBase> {

	@Override
	public int compare(DataKeyValueBase o1, DataKeyValueBase o2) {
		if(o1.key.key > o2.key.key)
			return 1;
		else if (o1.key.key < o2.key.key)
			return -1;
		else
			return 0;
	}

}
