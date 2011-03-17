package edu.ucr.cs.jumpwisely.topk_research.data_model.data_mdint;

import java.util.Comparator;

public class DimComparator implements Comparator<DataObject> {
	
	int dim;
	
	public DimComparator(int dim){
		this.dim = dim;
	}

	@Override
	public int compare(DataObject o1, DataObject o2) {
		// TODO Auto-generated method stub
		if(o1.getDimValue(dim) > o2.getDimValue(dim))
			return 1;
		else if(o1.getDimValue(dim) < o2.getDimValue(dim))
			return -1;
		else
			return 0;
	}

}
