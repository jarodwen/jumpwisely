/**
 *	Topk_Research DimComparator.java
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
