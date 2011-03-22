/**
 *	Topk_Research BufferManager.java
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

import java.util.HashMap;

/**
 * @author jarodwen
 *
 */
public class BufferManager {

	HashMap<Integer, Item> data_items;
	SwapPolicy sw_policy;
	int io_counter;
	int swap_counter;
	//String buffer_runtime;
	int miss_counter;
	int buffer_attempts;

	public BufferManager(SwapPolicy swpolicy) {
		data_items = new HashMap<Integer, Item>();
		io_counter = 0;
		this.sw_policy = swpolicy;
		this.swap_counter = 0;
		this.miss_counter = 0;
		this.buffer_attempts = 0;
		//this.buffer_runtime = "";
	}

	public BufferManager() {
		data_items = new HashMap<Integer, Item>();
		io_counter = 0;
		this.sw_policy = new SwapPolicy();
		this.swap_counter = 0;
		this.miss_counter = 0;
		this.buffer_attempts = 0;
		//this.buffer_runtime = "";
	}

	public void insert(int id) {
		this.buffer_attempts ++;
		if(this.data_items.get(id) != null){
			this.data_items.get(id).reaccess();
			return;
		}
		this.miss_counter ++;
		while (IOSetup.BUFFER_CAPACITY - this.getPageUsed() < 1) {
			this.swap();
		}
		this.data_items.put(id, new Item(id, 1));
		this.io_counter++;
		//this.buffer_runtime += "Add\t"+id+"\n";
	}

	public void insert(int id, boolean isDirty) {
		this.buffer_attempts ++;
		if(this.data_items.get(id) != null){
			this.data_items.get(id).reaccess();
			return;
		}
		this.miss_counter ++;
		while (IOSetup.BUFFER_CAPACITY - this.getPageUsed() < 1) {
			this.swap();
		}
		this.data_items.put(id, new Item(id, 1, false, isDirty));
		this.io_counter++;
		//this.buffer_runtime += "Add\t"+id+"\n";
	}

	public void insert(int id, boolean isDirty, boolean isPinned) {
		this.buffer_attempts ++;
		if(this.data_items.get(id) != null){
			this.data_items.get(id).reaccess();
			if (isPinned) {
				this.data_items.get(id).getPinned();
			} else {
				this.data_items.get(id).getUnpinned();
			}
			return;
		}
		this.miss_counter ++;
		while (IOSetup.BUFFER_CAPACITY - this.getPageUsed() < 1) {
			this.swap();
		}
		this.data_items.put(id, new Item(id, 1, isPinned, isDirty));
		this.io_counter++;
		//this.buffer_runtime += "Add\t"+id+"\n";
	}

	public void insert(int id, int size_in_page, boolean isDirty,
			boolean isPinned) {
		this.buffer_attempts += size_in_page;
		if (this.data_items.get(id) != null) {
			if (this.data_items.get(id).size_in_page > size_in_page) {
				while (IOSetup.BUFFER_CAPACITY - this.getPageUsed()
						- (size_in_page - this.data_items.get(id).size_in_page) < 0) {
					swap();
				}
			}
			this.data_items.get(id).size_in_page = size_in_page;
			this.data_items.get(id).reaccess(isDirty);
			if (isPinned) {
				this.data_items.get(id).getPinned();
			} else {
				this.data_items.get(id).getUnpinned();
			}
			return;
		}
		this.miss_counter ++;
		while (IOSetup.BUFFER_CAPACITY - this.getPageUsed() < size_in_page) {
			this.swap();
		}
		this.data_items.put(id, new Item(id, size_in_page, isPinned, isDirty));
		this.io_counter += size_in_page;
		//this.buffer_runtime += "Add\t"+id+"\n";
	}
	
	public void pre_insert(int id){
		this.insert(id, false, true);
	}

	public void swap() {
		int item_to_swap = this.sw_policy.findOneForSwap(data_items);
		if (data_items.get(item_to_swap).isDirty) {
			if (this.getPageUsed() < data_items.get(item_to_swap).size_in_page) {
				throw new UnknownError("I/O ERROR: PAGES USED IS SMALLER THAN THE PAGES TO BE SWAPPED!");
			} else {
				this.io_counter += data_items.get(item_to_swap).size_in_page;
				this.swap_counter += data_items.get(item_to_swap).size_in_page;
				//this.buffer_runtime += "Swap\t"+data_items.get(item_to_swap).getId()+"\n";
			}
		}
		data_items.remove(item_to_swap);
	}

	public void flush() {
		while (!data_items.isEmpty()) {
			Item item = (Item) data_items.values().toArray()[0];
			//if (!item.isPinned) {
				if (item.isDirty) {
					this.io_counter += item.size_in_page;
				}
				data_items.remove(item.getId());
			//}
		}
		//this.buffer_runtime = "";
	}

	public String currentStatus() {
		return "====== Current buffer status ======" + "\n\ttotal_page_num: "
				+ IOSetup.BUFFER_CAPACITY + "\n\tpage_num_used: " + this.getPageUsed()
				+ "\n\tio_counter: " + this.io_counter + "\n\tswap_counter: "
				+ this.swap_counter
				+ "\n\tbuffer_attempts: " + this.buffer_attempts
				+ "\n\tmiss_counter: " + this.miss_counter+ "\n=================================";
	}

	public int getPageUsed() {
		int pageused = 0;
		for(Item item : data_items.values()){
			pageused += item.size_in_page;
		}
		return pageused;
	}

	public boolean isOverPrefetchThreshold() {
		int pinnedsize = 0;
		for(Item item : data_items.values()){
			if(item.isPinned){
				pinnedsize += item.size_in_page;
			}
		}
		return pinnedsize >= IOSetup.BUFFER_CAPACITY * 0.5;
	}

	public int getCurrentIO() {
		return this.io_counter;
	}

	public int getCurrentSwap() {
		return this.swap_counter;
	}
	
//	public String getBufferRuntime(){
//		return this.buffer_runtime;
//	}
	
	public int getMissCount(){
		return this.miss_counter;
	}

	public int getBufferAttempts() {
		// TODO Auto-generated method stub
		return this.buffer_attempts;
	}

}
