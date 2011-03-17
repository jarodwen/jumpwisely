package edu.ucr.cs.jumpwisely.topk_research.data_model.in_mem.virtual_io;

/**
 * @author jarodwen
 *
 */
public class IOSetup {

	public static int DATA_SIZE = 2000;
	public static int QUERY_SIZE = 200;
	
	public static double PAGE_FILL_FACTOR = 0.9;
	public static double PAGE_UNDERFLOW_FACTOR = 0.4;
	/**
	 * The swapping policy. 
	 * 
	 * 0: Random swapping.
	 * 1: LRU 
	 * 2: MRU
	 * 3: NU
	 * 4: OU
	 * 5: LLT
	 * 6: SLT
	 * 
	 */
	public static int BUFFER_TYPE = 1;
	public static int PAGE_SIZE = 1024;
	public static int BUFFER_CAPACITY = 100;
	public static boolean IS_BULKLOAD = false;
	public static int DIMENSIONALITY = 2;
	public static int DATA_DIVIATION = 500000;
	public static long GENERATE_SEED = 20091110;
	public static int DATA_DISTRIBUTION = 1;
	
	public static boolean IS_DEBUG = false;
	
}
