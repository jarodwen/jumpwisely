package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io;

import java.util.HashMap;
import java.util.Random;

/**
 * @author jarodwen
 *
 */
public class SwapPolicy {

	public int findOneForSwap(HashMap<Integer, Item> items) {
		// return items.size() - 1;
		switch (IOSetup.BUFFER_TYPE) {
		case 0:
			return RandomPage(items);
		case 1:
			return LRUPolicy(items);
		case 2:
			return MRUPolicy(items);
		case 3:
			return NUPolicy(items);
		case 4:
			return OUPolicy(items);
		case 5:
			return LLTPolicy(items);
		case 6:
			return SLTPolicy(items);
		default:
			return LRUPolicy(items);
		}
	}

	private int RandomPage(HashMap<Integer, Item> items) {
		Random rand = new Random(System.currentTimeMillis());
		return ((Item)items.values().toArray()[rand.nextInt(items.size())]).getId();
	}

	private int LRUPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getReAccessCount();
		for (Item item : items.values()) {
			// Find the item with the smaller re-access count and assign it to return_id
			if (ts_flag >= item.getReAccessCount() && !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getReAccessCount();
			}
		}
		return return_id;
	}

	private int MRUPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getReAccessCount();
		for (Item item : items.values()) {
			// Find the item with the larger re-access count and assign it to return_id
			if (ts_flag <= item.getReAccessCount() && !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getReAccessCount();
			}
		}
		return return_id;
	}

	private int NUPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getLatestAccessTime();
		for (Item item : items.values()) {
			// Find the item with the latest re-access time and assign it to return_id
			if (ts_flag <= item.getLatestAccessTime() && !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getLatestAccessTime();
			}
		}
		return return_id;
	}

	private int OUPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getLatestAccessTime();
		for (Item item : items.values()) {
			// Find the item with the oldest re-access time and assign it to return_id
			if (ts_flag >= item.getLatestAccessTime() && !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getLatestAccessTime();
			}
		}
		return return_id;
	}

	private int LLTPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getLifeTime();
		for (Item item : items.values()) {
			// Find the item with the longest life time and assign it to return_id
			if (ts_flag <= item.getLifeTime()
					&& !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getLifeTime();
			}
		}
		return return_id;
	}

	private int SLTPolicy(HashMap<Integer, Item> items) {
		int return_id = 0;
		if (items.size() <= 0) {
			return return_id;
		}
		long ts_flag = ((Item)items.values().toArray()[0]).getLifeTime();
		for (Item item : items.values()) {
			// Find the item with the shortest re-access time and assign it to return_id
			if (ts_flag >= item.getLatestAccessTime()
					&& !item.isPinned) {
				return_id = item.getId();
				ts_flag = item.getLifeTime();
			}
		}
		return return_id;
	}
}
