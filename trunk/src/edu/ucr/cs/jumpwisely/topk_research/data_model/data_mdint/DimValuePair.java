/**
 *	Topk_Research DimValuePair.java
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

public class DimValuePair {
	
	int dim;
	int value;
	
	public DimValuePair(){
		this.dim = 0;
		this.value = 0;
	}

	public DimValuePair(int dim, int value) {
		super();
		this.dim = dim;
		this.value = value;
	}

	public int getDim() {
		return dim;
	}

	public void setDim(int dim) {
		this.dim = dim;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public String toString(){
		return this.toXMLString();
	}
	
	public String toXMLString(){
		return "<Dimvalue dim=\""+this.dim+"\" value=\""+this.value+"\"/>";
	}
	
	public DimValuePair copy(){
		return new DimValuePair(this.dim, this.value);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof DimValuePair) {
			DimValuePair another_pair = (DimValuePair) obj;
			if (this.dim == another_pair.dim
					&& this.value == another_pair.value)
				return true;
			else
				return false;
		} else {
			return false;
		}
	}	
}
