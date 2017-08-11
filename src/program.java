/**
 * The program execution occurs in this class
 * @author Shahab
 *
 */
public class program {
	/**
	 * Program execution
	 * @param args
	 * command line arguments
	 */
	public static void main(String[] args) {
		Dataset nD = new Dataset(2);	//argument is dimension of instances
		//nD.readCSV("Britishtowndata.dat", " ");
		nD.readCSV("Germantowndata.dat", " ");
		//nD.randomDS1(150);
		
		ATKMeans atk = new ATKMeans(nD, 8);
		atk.run();

		
		//changeK(nD,3,10);

	}
	

	/**
	 * run algorithm for different number of clusters
	 */
	public static void changeK(Dataset nD, int lowB, int upB) {
		// performing algorithm run for different values for number of clusters k
		for (int k = lowB;k < upB+1;k++) {
			nD.Clusters.clear();
			ATKMeans alg = new ATKMeans(nD, k);
			alg.silent = true;
			double foo[] = repeateAlg(alg, 200);
			
			System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			System.out.println("K = " + k);
			System.out.println("ATKMEANS :: ");
			System.out.println("imp over SSE " + foo[0] + " |||| imp over L " + foo[1]);
			double temp = Utility.fairMix(foo[0], foo[1]);
			System.out.println("Jain's index = " + temp);
			System.out.println("KMEANS :: ");
			System.out.println("imp over SSE " + foo[2] + " |||| imp over L " + foo[3]);
			temp = Utility.fairMix(foo[2], foo[3]);
			System.out.println("Jain's index = " + temp);
			System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");	
		}

	}

	/**
	 * Repeats the algorithm for a certain number of runs
	 * and the average results
	 * @param algoritm	a object of type ATKMeans
	 * @param numRuns	number of repetitions
	 * @return an array containing average values;
	 */
	public static double[] repeateAlg(ATKMeans algorithm, int numRuns) {
		double ret[] = new double[4];	//0 -> Average Imp over SSE   1 -> Average imp over L
										//2 -> avg imp over sse for kmeans 3-> L for kmeans 
		for (int i = 0;i < numRuns;i++) {
			algorithm.refData.initClustersRandomly();
			algorithm.run();
			ret[2] += algorithm.tempSSE;
			ret[3] += algorithm.tempL;
			ret[0] += algorithm.impSSE;
			ret[1] += algorithm.impL;
		}
		
		for (int k = 0;k < ret.length;k++) 
			ret[k] /= numRuns;

		return ret;
	}
}