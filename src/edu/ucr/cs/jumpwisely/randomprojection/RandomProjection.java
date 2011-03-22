/**
 *	RandomProjection RandomProjection.java
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
 * 
 */
package edu.ucr.cs.jumpwisely.randomprojection;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

/**
 * @author janie
 *
 */
public class RandomProjection {
	/**
	 * The buckets for the projected k-mers
	 */
	HashMap<String, Vector<PosPair>> buckets;
	
	Vector<Integer> project_pos;
	/**
	 * Best match: the one with the highest score.
	 */
	public double best_score;
	public String best_motif;
	
	public RandomProjection(){
		buckets = new HashMap<String, Vector<PosPair>>();
		project_pos = new Vector<Integer>();
		best_score = 0;
		best_motif = "";
	}
	
	public void do_rp() throws Exception{
		BufferedReader buf_reader = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			StringBuilder rtn_output = new StringBuilder();
			Logger.debug("An new iteration starts.");
			System.out.println("Previously the best one is: " + best_motif + " with score " + best_score);
			System.out.println("Do you want to clean this history (max score, type \"y\" for yes )?");
			String str_tmp = buf_reader.readLine();
			if(str_tmp.trim().equalsIgnoreCase("y")){
				best_score = 0;
				best_motif = "";
			}
			Logger.debug("Generate random positions...");
			System.out.println("Motif search: l = " + RunFindMotif.motif_length 
					+ " d = " + RunFindMotif.motif_corruptions 
					+ " n = " + RunFindMotif.background_seq_length
					+ " k = " + RunFindMotif.projection_value
					+ " N = " + RunFindMotif.background_sequences.size()
					+ " s = " + RunFindMotif.bucket_threshold);
			project_pos = generate_k();
			Logger.debug("Start Projection.");
			make_projection();
			Logger.debug("Finished Projection.");
			System.out.print("Do you want to specify the s value, based on the bucket stat? ");
			str_tmp = buf_reader.readLine();
			if(!str_tmp.trim().equalsIgnoreCase("")){
				int new_s = Integer.valueOf(str_tmp.trim());
				RunFindMotif.bucket_threshold = new_s;
			}
			System.out.println("OK, s = " + RunFindMotif.bucket_threshold);
			Logger.debug("Searching for motif candidates.");
			rtn_output.append(find_motif());
			Logger.debug("Search ends.");
			System.out.println("Currently I think the best one is: " + best_motif + " with score " + best_score);
			System.out.print("Do you want to see the details(y or n)? ");
			str_tmp = buf_reader.readLine();
			if(str_tmp.equalsIgnoreCase("y")){
				System.out.println("I got this for you...");
				System.out.println(rtn_output.toString());
			}
			System.out.print("Do you want to do another try(y or n)? ");
			str_tmp = buf_reader.readLine();
			if(str_tmp.equalsIgnoreCase("y")){
				continue;
			}else if(str_tmp.equalsIgnoreCase("n")){
				System.out.print("Thanks! Bye! ");
				break;
			}else{
				System.out.println("I cannot understand you... So let's play again!");
				continue;
			}
		}
	}
	
	public void make_projection(){
		// Do projection
		for(int i = 0; i < RunFindMotif.background_sequences.size(); i++){
			String seq = RunFindMotif.background_sequences.get(i);
			for(int j = 0; j < RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1; j++) {
				String sub_seq = seq.substring(j, j + RunFindMotif.motif_length);
				String key = project(sub_seq);
				PosPair value = new PosPair(i, j);
				if(buckets.containsKey(key)){
					buckets.get(key).add(value);
				}else{
					Vector<PosPair> pp = new Vector<PosPair>();
					pp.add(value);
					buckets.put(key, pp);
				}
			}
		}
		// Output hash statistics
		HashMap<Integer, Integer> buck_stat = new HashMap<Integer, Integer>();
		for(String key : buckets.keySet()){
			int bucket_size = buckets.get(key).size();
			if(buck_stat.containsKey(bucket_size)){
				buck_stat.put(bucket_size, buck_stat.get(bucket_size) + 1);
			}else{
				buck_stat.put(bucket_size, 1);
			}
		}
		Logger.debug_buckets(buck_stat);
	}
	
	/**
	 * 
	 * @return
	 */
	public String find_motif(){
		StringBuilder rtn_output = new StringBuilder();
		// For each bucket, do the refinement
		for(String key : buckets.keySet()){
			Vector<PosPair> bucket = buckets.get(key);
			// Skip the bucket with items no more than s
			if(bucket.size() < RunFindMotif.bucket_threshold)
				continue;
			double[][] distr_W = new double[RunFindMotif.alphabet.size()][RunFindMotif.motif_length];
			// Initialize the W matrix using the bucket content
			for(PosPair p : bucket){
				String seq = RunFindMotif.background_sequences.get(p.L).substring(p.R, p.R + RunFindMotif.motif_length);
				for(int i = 0; i < seq.length(); i++){
					Character chr = seq.charAt(i);
					distr_W[RunFindMotif.alphabet.get(chr)][i] += 1;
				}
			}
			for(int i = 0; i < RunFindMotif.alphabet.size(); i++){
				for(int j = 0; j < RunFindMotif.motif_length; j++)
					distr_W[i][j] /= bucket.size();
			}
			// Refine W
			EMModel emm = new EMModel(distr_W);
			distr_W = emm.training_model();
			rtn_output.append("********** Bucket " + key + " **********\n");
			// Get output for motifs with scores
			rtn_output.append(get_scores(distr_W));
		}
		return rtn_output.toString();
	}
	
	/**
	 * 
	 * @param distr_W
	 * @return
	 */
	private String get_scores(double[][] distr_W) {
		Vector<String> motif_table = new Vector<String>();
		Vector<Integer> start_table = new Vector<Integer>();
		double w_score = 0;
		for(String seq : RunFindMotif.background_sequences) {
			int starting_pos = 0;
			double max_prob = 0;
			// Calculate the score for each l-mer, and find the maximum
			for(int i = 0; i < seq.length() - RunFindMotif.motif_length + 1; i++) {
				String sub_seq = seq.substring(i, i + RunFindMotif.motif_length);
				double score = 1;
				for(int j = 0; j < sub_seq.length(); j++){
					Character chr = sub_seq.charAt(j);
					score *= distr_W[RunFindMotif.alphabet.get(chr)][j];
				}
				if(score > max_prob){
					max_prob = score;
					starting_pos = i;
				}
			}
			w_score += (Utilities.Log2(max_prob) + RunFindMotif.motif_length * Utilities.Log2(RunFindMotif.alphabet.size()));
			start_table.add(starting_pos);
			motif_table.add(seq.substring(starting_pos, starting_pos + RunFindMotif.motif_length));
		}
		int[][] cons_table = new int[RunFindMotif.alphabet.size()][RunFindMotif.motif_length];
		StringBuilder rtn_output = new StringBuilder();
		rtn_output.append("Score: " + w_score + "\n");
		// Process output
		for(int i = 0; i < RunFindMotif.background_sequences.size(); i++) {
			// Generate the line for output: "index score motif"
			String motif = motif_table.get(i);
			rtn_output.append(i + "\t" + start_table.get(i) + "\t" + motif + "\n");
			// Construct the table for consensus string
			for(int j = 0; j < motif.length(); j++){
				char chr = motif.charAt(j);
				cons_table[RunFindMotif.alphabet.get(chr)][j] += 1;
			}
		}
		rtn_output.append("C\tN/A\t");
		StringBuilder best_m = new StringBuilder();
		// Find the consensus string
		for(int i = 0; i < RunFindMotif.motif_length; i++){
			Enumeration<Character> e = RunFindMotif.alphabet.keys();
			char c = e.nextElement();
			while(e.hasMoreElements()){
				char chr = e.nextElement();
				if(cons_table[RunFindMotif.alphabet.get(chr)][i] > cons_table[RunFindMotif.alphabet.get(c)][i]){
					c = chr;
				}
			}
			rtn_output.append(c);
			best_m.append(c);
		}
		rtn_output.append("\n");
		if(w_score >= best_score){
			best_score = w_score;
			best_motif = best_m.toString();
		}
		return rtn_output.toString();
	}
	
	private Vector<Integer> generate_k() {
		Vector<Integer> rtn_list = new Vector<Integer>();
		for(int i = 0; i < RunFindMotif.motif_length; i++){
			rtn_list.add(i);
		}
		Random rand = new Random();
		while(rtn_list.size() > RunFindMotif.projection_value){
			rtn_list.remove(rand.nextInt(rtn_list.size()));
		}
		return rtn_list;
	}
	
	private String project(String str) {
		StringBuilder rtn_key = new StringBuilder();
		for(int i = 0; i < project_pos.size(); i++){
			rtn_key.append(str.charAt(project_pos.get(i)));
		}
		return rtn_key.toString();
	}
}
