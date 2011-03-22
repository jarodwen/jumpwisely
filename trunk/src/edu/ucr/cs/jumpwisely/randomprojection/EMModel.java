/**
 *	RandomProjection EMModel.java
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

/**
 * @author jarodwen
 *
 */
public class EMModel {

	/**
	 * The maximum iterations for the EM runs
	 */
	public static int EM_MAXITERS = 2;
	
	/**
	 * The distribution of the motif occurrence position, or Y.
	 * This is a t * (n-l+1) matrix, where y_{ij} is the prob
	 * of the l-mer starting at position j in the i-th training
	 * sequence, according to W.
	 */
	double[][] distr_Y;
	
	/**
	 * The distribution of the base occurrence at a specific 
	 * position in the hidden motif. This is the distribution
	 * to be trained, or W, which is a |A|*l matrix where w_{ij}
	 * is the prob of the j-th position in the motif is the
	 * i-th alphabet.
	 */
	double[][] distr_W;
	
	public EMModel() {
		distr_Y = new double[RunFindMotif.background_sequences.size()][RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1];
		distr_W = new double[RunFindMotif.alphabet.size()][RunFindMotif.motif_length];
	}
	
	public EMModel(double[][] distr_W_0) {
		distr_Y = new double[RunFindMotif.background_sequences.size()][RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1];
		distr_W = new double[RunFindMotif.alphabet.size()][RunFindMotif.motif_length];
		// Initialize the W_0
		distr_W = distr_W_0;
	}
	
	/**
	 * The estimation step for EM model. The step estimate the 
	 * expectation of the 
	 */
	public void EStep(){
		Logger.debug("Start E-step");
		Logger.debug_table(distr_W);
		Utilities.laplace_padding(distr_W);
		Logger.debug_table(distr_W);
		Utilities.logize(distr_W);
		Logger.debug_table(distr_W);
		/**
		 *  Calculate the Y, which is the distribution of positions of the motif
		 *  in the training sequences
		 */
		for(int i = 0; i < RunFindMotif.background_sequences.size(); i++) {
			// Pick the i-th sequence
			String seq = RunFindMotif.background_sequences.elementAt(i);
			for(int j = 0; j < RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1; j++) {
				// Initialize the Y_{ij}
				distr_Y[i][j] = 0;
				for(int k = 0; k < RunFindMotif.motif_length; k++){
					Character chr = seq.charAt(j + k);
					distr_Y[i][j] += this.distr_W[RunFindMotif.alphabet.get(chr)][k];
				}
			}
		}
		Logger.debug_table(distr_Y);
		// Normalize the Y matrix so that the sum of each row is 1.
		for(int i = 0; i < RunFindMotif.background_sequences.size(); i++){
			double sum = 0;
			// Get sum of the prob in the i-th row
			for(int j = 0; j < RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1; j++) {
				sum += Math.pow(2, distr_Y[i][j]);
			}
			// Normalize all the prob in the i-th row.
			for(int j = 0; j < RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1; j++) {
				distr_Y[i][j] = distr_Y[i][j] - Utilities.Log2(sum);
			}
		}
		Logger.debug_table(distr_Y);
	}
	
	/**
	 * The maximization step for EM model. In this step we use the Y matrix
	 * generated in E-step to calculate the refined W.
	 */
	public void MStep(){
		Logger.debug("Start M-step");
		double[][] distr_W_refined = new double[RunFindMotif.alphabet.size()][RunFindMotif.motif_length];
		for(int i = 0; i < RunFindMotif.background_sequences.size(); i++) {
			// Pick the i-th sequence
			String seq = RunFindMotif.background_sequences.elementAt(i);
			for(int j = 0; j < RunFindMotif.background_seq_length - RunFindMotif.motif_length + 1; j++) {
				int k = 0;
				Character chr = seq.charAt(j);
				while(j - k >= 0 && k < RunFindMotif.motif_length) {
					distr_W_refined[RunFindMotif.alphabet.get(chr)][k] += Math.pow(2, distr_Y[i][j-k])/ RunFindMotif.background_sequences.size();
					k++;
				}
			}
		}
		distr_W = distr_W_refined;
		Logger.debug_table(distr_W);
	}
	
	public double[][] training_model(){
		for(int i = 0; i < EM_MAXITERS; i++){
			EStep();
			MStep();
		}
		Logger.debug("Model training finished");
		return distr_W;
	}
}
