/**
 *	Topk_Research DataKeyComparator.java
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
