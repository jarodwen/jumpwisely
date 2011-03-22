/**
 *	Topk_Research DataObject.java
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

import java.util.Vector;

public class DataObject {
	
	Vector<DimValuePair> dim_values;
	int value;
	
	public DataObject(Vector<DimValuePair> dim_values, int value){
		Vector<DimValuePair> dvps = new Vector<DimValuePair>();
		for(int i = 0; i < dim_values.size(); i++){
			dvps.add(dim_values.get(i).copy());
		}
		this.dim_values = dvps;
		this.value = value;
	}
	
	public int getDimValue(int dim){
		for(int i = 0; i < this.dim_values.size(); i++){
			if(this.dim_values.get(i).dim == dim){
				return this.dim_values.get(i).value;
			}
		}
		throw new UnknownError("Cannot find the dimension " + dim + " from " + this.toString());
	}
	
	public void setDimValue(int dim, int dim_value){
		for(int i = 0; i < this.dim_values.size(); i++){
			if(this.dim_values.get(i).dim == dim){
				this.dim_values.get(i).setValue(dim_value);
				return;
			}
		}
		throw new UnknownError("Set value " + dim_value + " for dim " + dim + " out of the dimensions of " + this.toString() + "!");
	}
	
	public boolean isDominatedBy(DataObject another_obj){
		if(another_obj.getDimNum() != this.getDimNum()){
			throw new UnknownError("Compare two objects with different dimensions: " + this.toString() + " and " + another_obj.toString());
		}else{
			for(DimValuePair dv_pair : dim_values){
				if(dv_pair.getValue() > another_obj.getDimValue(dv_pair.dim)){
					return false;
				}
			}
			return true;
		}
	}
	
	public int getValue(){
		return this.value;
	}
	
	public int getDimNum(){
		return this.dim_values.size();
	}
	
	public Vector<DimValuePair> getDims(){
		Vector<DimValuePair> dim_list = new Vector<DimValuePair>();
		for(int i = 0; i < this.dim_values.size(); i++){
			dim_list.add(new DimValuePair(this.dim_values.get(i).dim, 0));
		}
		return dim_list;
	}
	
	public Vector<DimValuePair> getDimsWithValues(){
		Vector<DimValuePair> dim_list = new Vector<DimValuePair>();
		for(int i = 0; i < this.dim_values.size(); i++){
			dim_list.add(new DimValuePair(this.dim_values.get(i).dim, this.dim_values.get(i).value));
		}
		return dim_list;
	}
	
	public String toXMLString(){
		String xmlstr = "<DataObject dims=\"" + this.getDimNum() + "\" value=\"" + value + "\">";
		for(int i = 0; i < dim_values.size(); i++){
			xmlstr += dim_values.get(i).toString();
		}
		return xmlstr + "</DataObject>";
	}
	
	/**
	 * Return the size of the object, which is the sum of
	 * - dimension
	 * - value on each dimension
	 * - the weight value assigned
	 * 
	 * @return	the size of the object in byte
	 */
	public int getSize(){
		return this.getFullSize();
	}
	
	/**
	 * Project the data object onto a dimension, which 
	 * returns another data object with a lower dimensionality.
	 * @param dim	The dim to be projected
	 * @return	The data object after projected.
	 */
	public DataObject projectOnToDim(int dim){
		DataObject newObj = this.copy();
		newObj.dim_values.remove(newObj.getIndexOfDim(dim));
		return newObj;
	}
	
	public String toString(){
		return this.toXMLString();
	}
	
	/**
	 * Return the dim of the dim-value pair at the
	 * given index.
	 * @param i
	 * @return
	 */
	public int getDimAtIndex(int i){
		if(i < 0 || i >= this.getDimNum())
			return -1;
		else
			return this.dim_values.get(i).dim;
	}
	
	/**
	 * Return the index of the dim-value pair for a 
	 * given dim.
	 * @param dim
	 * @return
	 */
	public int getIndexOfDim(int dim){
		for(int i = 0; i < this.dim_values.size(); i++){
			if(this.dim_values.get(i).dim == dim)
				return i;
		}
		return -1;
	}
	
	public void setValue(int value){
		this.value = value;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof DataObject){
			DataObject dobj = (DataObject)obj;
			if(dobj.getDimNum() == this.getDimNum()){
				for(int i = 0; i < this.getDimNum(); i++){
					int jj = -1;
					for(int j = 0; j < this.dim_values.size(); j++){
						if(this.dim_values.get(j).getDim() == dobj.getDims().get(i).getDim() 
								&& this.dim_values.get(j).getValue() == dobj.dim_values.get(i).getValue()){
							jj = j;
							break;
						}else{
							continue;
						}
					}
					if(jj < 0){
						return false;
					}else{
						continue;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public boolean isSimlar(Object obj){
		if(obj instanceof DataObject){
			DataObject dobj = (DataObject)obj;
			if(dobj.getDimNum() == this.getDimNum()){
				for(int i = 0; i < this.getDimNum(); i++){
					boolean isFound = false;
					for(int j = 0; j < this.dim_values.size(); j++){
						if(this.dim_values.get(j).dim == dobj.getDims().get(i).dim)
							isFound = true;
					}
					if(isFound)
						continue;
					else
						return false;
				}
				return true;
			}
		}
		return false;
	}
	
	public DataObject copy(){
		Vector<DimValuePair> new_dim_values = new Vector<DimValuePair>();
		for(DimValuePair dvp : dim_values){
			new_dim_values.add(dvp.copy());
		}
		return new DataObject(new_dim_values, this.value);
	}
	
	public DataObject removeDim(int dim){
		DataObject new_obj = this.copy();
		new_obj.dim_values.remove(new_obj.getIndexOfDim(dim));
		return new_obj;
	}
	
	public boolean projectable(DataObject obj){
		if(this.getDimNum() >= obj.getDimNum()){
			for(DimValuePair dvp : obj.dim_values){
				if(this.getIndexOfDim(dvp.dim) < 0)
					return false;
			}
			return true;
		}
		return false;
	}
	
	public boolean projectable(Vector<DimValuePair> dim_list){
		if(this.getDimNum() >= dim_list.size()){
			for(DimValuePair dvp : dim_list){
				if(this.getIndexOfDim(dvp.dim) < 0)
					return false;
			}
			return true;
		}
		return false;
	}
	
	public DataObject projectOnToDims(Vector<DimValuePair> dim_list){
		DataObject newObj = this.copy();
		if(newObj.projectable(dim_list)){
			int i = 0;
			while(i < newObj.getDimNum()){
				boolean proj_flag = true;
				for(int j = 0; j < dim_list.size(); j++){
					if(dim_list.get(j).dim == newObj.dim_values.get(i).dim){
						proj_flag = false;
					}
				}
				if(proj_flag){
					newObj.dim_values.remove(i);
					continue;
				}
				i++;
			}
			return newObj;
		}else{
			return null;
		}
		
	}
	
	/**
	 * Return the actual size of this data object.
	 * 
	 * Notice that although only two elements {@link #dim_values} and {@link #value} 
	 * are in this class, when dumping an object onto the disk, we also need to store
	 * the number of dimensions as an integer onto the disk, in case that when we need
	 * to load this object, we can know when to stop loading.
	 * 
	 * @return
	 */
	public int getFullSize(){
		// The size of a data object consists of three elements: number of dimensions,
		// dimensionality, and the value.
		if(this.value != 0)
			return 4 + this.dim_values.size() * 4 + 4;
		else
			return 0;
	}
	
}
