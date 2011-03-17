/**
 * 
 */
package edu.ucr.cs.jumpwisely.topk_research.data_model.data_element;

import java.util.Comparator;

/**
 * @author janie
 *
 */
public class DataValueComparator implements Comparator<DataInstPair> {
	
	int dim;
	
	public DataValueComparator(int dim){
		this.dim = dim;
	}

	@Override
	public int compare(DataInstPair o1, DataInstPair o2) {
		double d1 = o1.value.values.get(dim);
		double d2 = o2.value.values.get(dim);
		if(d1 == d2)
			return 0;
		else 
			return (d1 < d2)?1:-1;
	}

}
