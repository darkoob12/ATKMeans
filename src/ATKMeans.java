import java.util.ArrayList;
import java.util.HashMap;


/**
 * our early version of auction based clustering algorithm
 * @author Shahab
 *
 */
public class ATKMeans {
	//fields
	public boolean silent;	//controling the output of data to the console
	public Dataset refData;	//reference to the stored data set
	public double tol;
	public HashMap<Integer, HashMap<Integer, Double>> bidTable;
	
	// these two numbers are stored and then performance evaluation measure
	// of the algorithm is their improvement over these values 
	private double sseBase;		//SSE after first iteration of kmeans
	private double lBase;	//L measure after first iteration of kmeans
	
	// these are evaluation measures of our algorithm
	// ATKMean
	public double impSSE;
	public double impL;
	
	// these are for saving improvements done by KMeans over the ..
	// .. initial clusters
	public double tempSSE;
	public double tempL;
	
	//constructor
	/**
	 * basic constructor makes simple initialization
	 * @param argData : loaded data
	 */
	public ATKMeans(Dataset argData,int argK){
		refData = argData;
		tol = .000005;		// this performance will be reached after less than 20 iterations
		//initializing cluster centers
		if (refData.Clusters.isEmpty()) {
			refData.numClusters = argK;		//setting proper number of clusters
			refData.inferLideal();
			refData.initClustersRandomly();		//best way for generating random points
												//and since this initialization is the same for all algorithms
												// this comparison would be fair
			// forming initial clusters
			refData.updateMembers();
			sseBase = refData.computeSSE();
			lBase = refData.computeL();
			
		} else {
			System.out.println("ERROR Clusters are NOT empty!");
			System.exit(-1);
		}
		bidTable = new HashMap<Integer, HashMap<Integer, Double>>();
		impSSE = 0;
		impL = 0;
		silent = false;
	}
	
	
	//methods
	
	/**
	 * this run of algorithm is using a different inference method for determining
	 * highest bidder, in each auction all the examples will be sold
	 */
	public void dRun() {
		// forming clusters using kmeans
		KMeans();

		if (!silent){
			System.out.println("kmeans algoritm finished...");
			refData.showStat();
		}
		improveDisp();
		tempSSE = impSSE;
		tempL = impL;
		// computing mutual distances of all examples
		refData.preProcess();
		if (!silent)
		System.out.println("preprocess finsished...");

		// each cluster set initial price for her extra examples
		initialPrices();
		// each player bids for all examples in the sale list
		startBiding();
		
		/**
		 * Main loop of the auction , there would be a number of successive auction in this loop
		 * and in each auction all the examples will be sold to the highest bidder
		 */
		while (!bidTable.isEmpty()) {
			// first determine  the highest bidder for each example
			HashMap<Integer, Integer> winnerList = new HashMap<Integer, Integer>(); //(biddieID , bidderID) 
			for (Integer instanceID : bidTable.keySet()) {
				int highestBidder = -1;	//playerID with maximum bid
				double maxBid = Double.MIN_VALUE;
				for (int player : bidTable.get(instanceID).keySet()) {
					if (bidTable.get(instanceID).get(player) > maxBid) {
						highestBidder = player;
						maxBid = bidTable.get(instanceID).get(player);
					}
				}
				winnerList.put(instanceID, highestBidder);
				//System.out.println(highestBidder + " Bids " + maxBid + " for " + instanceID);
			}			
			// now we know highest bidder for each example
			// we should sell the examples to them and make way for the next auction
			for (Integer exId : winnerList.keySet()) {
				refData.changeMembership(exId, winnerList.get(exId));
			}
			
			// some clusters may lose some example 
			// bid table must constructed again
			bidTable.clear();
			
			// now all clusters determine their new role and
			// resources will set price for their extra examples			
			initialPrices();
			// all other clusters which are bidder will bid for available examples
			startBiding();
		} // end of while loop
		
		//showing results 
		if (!silent) {
			System.out.println("Finieshed.....");
			refData.showStat();
		}
		improveDisp();



	}
	
	
	/**
	 * run the algorithm and determines the final clusters
	 * clusters are stored as class internal data available to all methods
	 */
	public void run() {
		// forming clusters using kmeans
		KMeans();

		if (!silent){
			System.out.println("kmeans algoritm finished...");
			refData.showStat();
		}
		improveDisp();
		tempSSE = impSSE;
		tempL = impL;
		
		// computing mutual distances of all examples
		refData.preProcess();
		if (!silent)
		System.out.println("preprocess finsished...");
		
		// each cluster set initial price for her extra examples
		initialPrices();
		
		// each player bids for all examples in the sale list
		startBiding();
			
		
		// main loop of the auction
		// the loop should end while no player buys any example
		// Note : as the algorithm proceeds players tend to pay less price for purchasing 
		// examples because their need will reduce
		//int iter = 0;
		while (!bidTable.isEmpty()) {
			//iter++;
			// first determine  the highest bidder for each example
			HashMap<Integer, Integer> winnerList = new HashMap<Integer, Integer>(); //(biddieID , bidderID) 
			for (Integer instanceID : bidTable.keySet()) {
				int highestBidder = -1;	//playerID with maximum bid
				double maxBid = Double.MIN_VALUE;
				for (int player : bidTable.get(instanceID).keySet()) {
					if (bidTable.get(instanceID).get(player) > maxBid) {
						highestBidder = player;
						maxBid = bidTable.get(instanceID).get(player);
					}
				}
				winnerList.put(instanceID, highestBidder);
				//System.out.println(highestBidder + " Bids " + maxBid + " for " + instanceID);
			}
						
			// now we should find which of the winning player will exploit more benefit from buying 
			// an example in this auction
			int winnerID = -1;
			double maxProfit = Double.MIN_VALUE;
			
			
			for (Integer id : winnerList.keySet()) {
				Cluster clstOwner = refData.Clusters.get(refData.Examples.get(id).clusterId);	
				double profit = bidTable.get(id).get(winnerList.get(id)) - clstOwner.saleBasket.get(id);
				if (profit > maxProfit) {
					winnerID = id;
					maxProfit = profit; 
				}
			}
			// now we know that in this iteration of the algorithm
			// which player buys which example from which resource
			if (!silent)
			System.out.println("number of winners : " + winnerList.size());
			Cluster clstOwner = refData.Clusters.get(refData.Examples.get(winnerID).clusterId);
			if (!silent)
			System.out.println("Price : " + clstOwner.saleBasket.get(winnerID) + ", Bid : " + bidTable.get(winnerID).get(winnerList.get(winnerID)));
			buy(winnerList.get(winnerID), winnerID);
		
		}  // end of while loop
		//showing results 
		if (!silent) {
			System.out.println("Finieshed.....");
			refData.showStat();
		}
		improveDisp();
		
	}
	

	/**
	 * this function will move the sold example to buyers cluster
	 * and will update the bid table
	 */
	public void buy(int playerID, int instanceID) {
		// first we should update the bidTable
		// removing the sold example
		bidTable.remove(instanceID);
		
		// now we will exchange the example between clusters
		refData.changeMembership(instanceID, playerID);
		refData.Clusters.get(playerID);
		//now the player should bid again for all available examples in the sale list
		Cluster player = refData.Clusters.get(playerID);
		player.setRole(refData.lIdeal);
		if (!player.isResource) {	// if not a resource then bid for examples in the bid table
			for (Integer exId : bidTable.keySet()){
				Cluster clstOwner = refData.Clusters.get(refData.Examples.get(exId).clusterId);
				double newBid = player.bid2(refData, exId);
				if (newBid > clstOwner.saleBasket.get(exId)) {
					bidTable.get(exId).put(playerID, Double.valueOf(newBid));
				} else {
					bidTable.get(exId).remove(playerID);
				}
			}
		}
		
		cleanBidTable();
	}
	
	/**
	 * in this function we will discriminate players from resources
	 * and recourses will give an initial price for each of their overhead example 
	 */
	public void initialPrices() {
		//first we need to now which examples are for sale
		for (Cluster c : refData.Clusters.values()) {
			c.setRole(refData.lIdeal);	// by calling this function overHead for each cluster will be updated 
			if (c.isResource) {
				c.prepareBasket(refData);	// selects members for sale
				for (Integer id : c.saleBasket.keySet()) {
					// the following list will contain bidders id and their bid amount
					HashMap<Integer, Double> temp = new HashMap<Integer, Double>(); //this list will filled in another function
					bidTable.put(id, temp);
				}
			}
		}
	}
	
	
	/**
	 * each player should bid for all examples available for sale	 
	 */
	public void startBiding() {
		for (Cluster c : refData.Clusters.values()) {
			if (!c.isResource) {
				// should bid for each example in the sale list
				for (Integer instanceID : bidTable.keySet()) {
					// the following cluster should be a resource
					Cluster clustOwner = refData.Clusters.get(refData.Examples.get(instanceID).clusterId);
					if (!clustOwner.isResource)
						System.err.println("BUYING FROM A NON RESOURCE CLUSTER");
					double tempBid = c.bid2(refData, instanceID);
					// if the player can afford the initial price...
					if (tempBid > clustOwner.saleBasket.get(instanceID)) {
						bidTable.get(instanceID).put(c.id, tempBid);		// add to the list of bidders for the instance
					} else { 
//						if (!silent)
//						System.out.println("Expensive!!, Price : " + clustOwner.saleBasket.get(instanceID) + 
//						" and maxBid : " + tempBid);
					}
				}
			}
		}
		cleanBidTable();
	}
	
	/**
	 * This function will remove expensive examples with no bidder from the bid table
	 */
	public void cleanBidTable() {
		//removing examples that no one wants to buy from the bid table
		ArrayList<Integer> lst = new ArrayList<Integer>();
		for (Integer id : bidTable.keySet()) {
			if (bidTable.get(id).isEmpty()) {
				lst.add(id);
			}
		}
		for (Integer i : lst) {
			bidTable.remove((Object)i);
		}
		if (!silent)
			System.out.println("Size of Sale List : " + bidTable.size());
	}
	
	
	/**
	 * prints improvement of the algorithm over the initial clusters
	 * improvement here means how much decreased
	 */
	public void improveDisp() {
		updateImprovements();
		if (!silent) {
			System.out.println("--------------------------------------------------------------");
			System.out.println("Improvements over initial clusters : ");
			System.out.println("SSE = " + impSSE);
			System.out.println("L = " + impL);
			System.out.println("--------------------------------------------------------------");
		}
	}
	
	/**
	 * this will calculate improvements over the initial state of the clusters
	 * and the results are stored in the fields of the object
	 */
	public void updateImprovements() {	
		double nSSE = refData.computeSSE();
		impSSE = (sseBase - nSSE) / sseBase;
		if (!silent)
		System.out.println("baseSSE  = " + sseBase + ", new SSE " + nSSE);
		impSSE *= 100;
		double nL = refData.computeL();
		impL = (lBase - nL) / lBase;
		impL *= 100;
	}
	
	
	/**
	 * this will construct initial clusters using famous kmeans algorithm
	 *  //// I THINK THIS WILL WORK CORRECTLY ////
	 *  
	 *  random initialization may be different in papers algorithm
	 *  but there is no implementation detail in paper 
	 */
	public void KMeans() {
		//double oldTSSE = 0;	// this is for determining algorithm progress
		//double TSSE = 0;
		
		//int iter = 0;	//counting main loop's iterations
		boolean done = false;
		while (!done) {
			//iter++;
			//determine cluster for each instance
			refData.updateMembers();
			
			//Update cluster centers by averaging
			for (Cluster c : refData.Clusters.values()) {
				c.updateCentesrs(refData.Examples);
			}
			
			//Evaluating SSE Measure
			//TSSE = refData.computeSSE();
			
			//are we done?
			done = true;
			for (Cluster c : refData.Clusters.values()) {
				if (c.changed == true) {
					done = false;
					break;
				}
			}
			/*
			if ((iter > 500) || (Math.abs(oldTSSE - TSSE) < tol)) {
				done = true;				
			}*/
			//oldTSSE = TSSE;
			
			//resetting flags in each cluster for next iteration 
			for (Cluster c : refData.Clusters.values()) {
				c.changed = false;
			}

		} //while
	} //KMeans Mehtod
}	//ATKMeans Class

