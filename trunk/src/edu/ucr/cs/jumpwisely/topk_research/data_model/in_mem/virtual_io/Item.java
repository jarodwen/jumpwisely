/**
 *	Topk_Research Item.java
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
package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io;

public class Item {
	
	private int id;
	boolean isPinned;
	boolean isDirty;
	int size_in_page;
	private long timestamp;
	private long timestamp_latest_access;
	int re_access_count;
	
	public Item(int id, int size, boolean isPinned){
		this.id = id;
		size_in_page = size;
		this.isPinned = isPinned;
		isDirty = false;
		timestamp = System.currentTimeMillis();
		this.timestamp_latest_access = this.timestamp;
		this.re_access_count = 0;
	}
	
	public Item(int id, int size, boolean isPinned, boolean isDirty){
		this.id = id;
		size_in_page = size;
		this.isPinned = isPinned;
		this.isDirty = isDirty;
		timestamp = System.currentTimeMillis();
		this.timestamp_latest_access = this.timestamp;
		this.re_access_count = 0;
	}
	
	public Item(int id, int size){
		this.id = id;
		size_in_page = size;
		this.isPinned = false;
		isDirty = false;
		timestamp = System.currentTimeMillis();
		this.timestamp_latest_access = this.timestamp;
		this.re_access_count = 0;
	}
	
	public int getId(){
		return this.id;
	}
	
	public long getTimestamp(){
		return this.timestamp;
	}
	
	public long getLatestAccessTime(){
		return this.timestamp_latest_access;
	}
	
	public void updateAccessTime(){
		this.timestamp_latest_access = System.currentTimeMillis();
		this.re_access_count ++;
	}
	
	public void updateSize(int pages){
		this.size_in_page = pages;
	}
	
	public int getReAccessCount(){
		return this.re_access_count;
	}
	
	public boolean equals(Item oitem){
		if(this.id == oitem.id){
			return true;
		}else{
			return false;
		}
	}
	
	public void reaccess(){
		this.timestamp_latest_access = System.currentTimeMillis();
		this.re_access_count++;
	}
	
	public void reaccess(boolean isDirty){
		this.reaccess();
		this.isDirty = isDirty;
	}
	
	public void getPinned(){
		this.isPinned = true;
	}
	
	public void getUnpinned(){
		this.isPinned = false;
	}
	
	public long getLifeTime(){
		return this.timestamp_latest_access - this.timestamp;
	}

}
