import java.util.HashMap;
import java.util.Random;


/**
 * This is an independent version of kmeans
 * 
 * THIS IS ALSO IMPLEMENTED IN ATKMEANS CLASS
 * 
 * @author Shahab
 *
 */
public class KMeans {
	public Dataset refData;
	double tol;	//for testing stop condition
	double baseSSE;
	double baseL;
	public KMeans(Dataset argData, int k) {
		tol = 0.0001;
		refData = argData;
		refData.numClusters = k;
		if (refData.Clusters.isEmpty()) {
			refData.Clusters = new HashMap<Integer, Cluster>();
			for (int i = 0;i < k;i++) {
				Cluster tmpClst = new Cluster(i, refData.dim);
				refData.Clusters.put(i, tmpClst);
			}
		}
	}
	
	/**
	 * Main loop of the algorithm
	 */
	public void run() {
		double SSE = 0;
		double newSSE = 0;
		
		//initialize centers 
		initializeCenters();
		
		int iter = 0;
		boolean done = false;
		while (!done) {
			iter++;
			// determine membership of each example
			for (Instance ins : refData.Examples.values()) {
				refData.updateMemberShip(ins);
			}
			
			// update cluster centers according to new members
			for (Cluster c : refData.Clusters.values()) { 
				c.updateCentesrs(refData.Examples);
			}
			
			//evaluating measures
			newSSE = refData.computeSSE();
			if (iter == 1) {
				baseSSE = newSSE;
				baseL = refData.computeL();
			}
			//check for stopping condition
			if ((iter > 500) || (Math.abs(SSE - newSSE) < tol)) {
				done = true;
			}
			SSE = newSSE;
		}
		System.out.println((baseSSE - newSSE)*100/baseSSE);
		System.out.println((baseL - refData.computeL())*100/baseL);
		refData.showStat();
	}
	
	/**
	 * randomly selects cluster centers
	 */
	public void initializeCenters() {
		Random rnd = new Random(System.currentTimeMillis());
		for (Cluster c : refData.Clusters.values()) {
			for (int i = 0;i < refData.dim;i++) {
				c.center[i] = rnd.nextDouble();
			}
		}
	}
}
