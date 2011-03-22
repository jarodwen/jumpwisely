/**
 *	Topk_Research DataValueComaprator.java
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
