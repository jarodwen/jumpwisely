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
