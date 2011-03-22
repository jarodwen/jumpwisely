/**
 *	RandomProjection RunFindMotif.java
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
package edu.ucr.cs.jumpwisely.randomprojection;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author janie
 *
 */
public class RunFindMotif {

	/**
	 * The alphabet sets. 
	 * - alphabet - the set with index
	 * - alphabet_count - the set with count
	 */
	public static Hashtable<Character, Integer> alphabet_count;
	public static Hashtable<Character, Integer> alphabet;
	/**
	 * The length of each background sequence, or referred as n
	 */
	public static int background_seq_length;
	/**
	 * The set of background sequences, or referred as B
	 */
	public static Vector<String> background_sequences;
	/**
	 * The length of the hidden motif, or referred as l
	 */
	public static int motif_length;
	/**
	 * The number of corruption allowed in the motif, or referred
	 * as d
	 */
	public static int motif_corruptions;
	/**
	 * The k value of the projection positions
	 */
	public static int projection_value;
	/**
	 * The threshold of the contents of a bucket, s
	 */
	public static int bucket_threshold;
	/**
	 * The number of iterations for the RP algorithm, m
	 */
	public static int rp_loop;
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// Load Parameters
		if(args.length < 3 || args.length > 8) {
			print_usage();
			return;
		}
		motif_length = Integer.valueOf(args[0]);
		motif_corruptions = Integer.valueOf(args[1]);
		background_sequences = new Vector<String>();
		// Load background sequences in FASTA format
		Logger.debug("Loading data...");
		BufferedReader bufFile = new BufferedReader(new FileReader(args[2]));
		String strTemp = "";
		alphabet = new Hashtable<Character, Integer>();
		alphabet_count = new Hashtable<Character, Integer>();
		StringBuilder seq_builder = new StringBuilder();
		while((strTemp = bufFile.readLine())!=null){
			// Skip the empty line
			if(strTemp.trim().equalsIgnoreCase("")){
				continue;
			}
			// Mark the beginning of the reading
			if(strTemp.trim().startsWith(">")){
				if(seq_builder.length()!=0){
					background_sequences.add(seq_builder.toString());
					if (background_seq_length == 0 || background_seq_length >= seq_builder.length()) {
						background_seq_length = seq_builder.length();
					}
					seq_builder = new StringBuilder();
				}
				continue;
			}
			// Read the sequence into the container
			for (int i = 0; i < strTemp.length(); i++) {
				if (alphabet.containsKey(strTemp.charAt(i))) {
					int tmp_int = alphabet_count.get(strTemp.charAt(i));
					alphabet_count.put(strTemp.charAt(i), tmp_int + 1);
				} else {
					alphabet.put(strTemp.charAt(i), alphabet.size());
					alphabet_count.put(strTemp.charAt(i), 0);
				}
			}
			seq_builder.append(strTemp.trim());
			continue;
		}
		// Load the last one.
		if(seq_builder.length()!=0){
			background_sequences.add(seq_builder.toString());
			if (background_seq_length == 0 || background_seq_length >= seq_builder.length()) {
				background_seq_length = seq_builder.length();
			}
		}
		Logger.debug("Load finished. Start tiding.");
		for(int i = 0; i < background_sequences.size(); i++){
			if(background_sequences.get(i).length() > background_seq_length){
				background_sequences.set(i, background_sequences.get(i).substring(0, background_seq_length));
			}
		}
		if(args.length > 3){
			int arg_index = 3;
			while(arg_index < args.length) {
				if(args[arg_index].equalsIgnoreCase("-k")) {
					projection_value = Integer.valueOf(args[arg_index + 1]);
					arg_index += 2;
				} else if (args[arg_index].equalsIgnoreCase("-s")) {
					bucket_threshold = Integer.valueOf(args[arg_index + 1]);
					arg_index += 2;
				} else if (args[arg_index].equalsIgnoreCase("-m")) {
					rp_loop = Integer.valueOf(args[arg_index + 1]);
					arg_index += 2;
				} else if (args[arg_index].equalsIgnoreCase("-d")) {
					Logger.log_switch = true;
					arg_index ++;
				} else if (args[arg_index].equalsIgnoreCase("-a")) {
					// Optional: estimate parameters
					estimate_parameters();
					break;
				} else {
					print_usage();
					return;
				}
			}
		}
		// Random Projection
		RandomProjection rp = new RandomProjection();
		Logger.debug("Initialize Random Projector");
		rp.do_rp();
	}

	/**
	 * Output the usage instruction.
	 */
	public static void print_usage(){
		System.out.println("Usage: RunFindMotif <motif_length> <motif_corruptions> <file_for_sequences>");
		System.out.println("                    [-k <projection_value>]");
		System.out.println("                    [-s <bucket_threshold>]");
		System.out.println("                    [-d]");
		return;
	}
	
	/**
	 * Estimate the parameters for:
	 * - the projection parameter k;
	 * - the threshold of the size of the bucket s.
	 */
	public static void estimate_parameters(){
		projection_value = motif_length - motif_corruptions - 1;
		//bucket_threshold = 2 * background_sequences.size() * (background_seq_length - motif_length + 1) / (int)(Math.pow(4, projection_value));
		//if(bucket_threshold == 0)
			bucket_threshold = 200;
		rp_loop = 2;
	}
}
