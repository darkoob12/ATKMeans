import java.util.ArrayList;
import java.util.HashMap;

/**
 * this class will manage a cluster
 * it knows cluster members and can compute the cluster compaction factor
 * returns cluster size
 * @author Shahab
 *
 */
public class Cluster {
	//FIELDS
	public int id;		//a zero based id
	public int dim;		//dimension of center vector
	private double compaction;	//the SSE measure for the cluster;
	public boolean changed;				//change in members of cluster
	public ArrayList<Integer> members;		// a variable length list of instance ids
	public double center[];		//a real valued vector representative of the cluster
	public HashMap<Integer, Double> saleBasket;	//list of instances
	public boolean isResource;	//this is true for clusters with positive overhead
	public int overHead;	//Clustersize - Ideal Size	|| this Lc in the new price function

	// this is used for compromise between the two objectives 
	// when setting weights for them
	public double Mu;		// regularization factor for the bidding function
	public double Phi;		// regularization factor for the pricing function
	//CONSTRUCTORS
	/**
	 * simple constructor initializing all members with default 3 dimension vectors
	 * @param argId	id of cluster
	 */
	public Cluster(int argId) {
		id = argId;
		center = new double[3];
		members = new ArrayList<Integer>();
		saleBasket = new HashMap<Integer, Double>();
		isResource = false;
		Phi = 0.5;
		Mu = 0.5;
		changed = false;
	}
	
	/**
	 * using this constructor can we can set the dimension of vectors 
	 * @param argId	
	 * @param argDim	
	 */
	public Cluster(int argId, int argDim) {
		id = argId;
		dim = argDim;
		center = new double[dim];
		saleBasket = new HashMap<Integer, Double>();
		members = new ArrayList<Integer>();
		isResource = false;
		Phi = 0.2;
		Mu = 0.7;
		changed = false;
	}
	
	//METHODS
	
	/**
	 * adds an instance to the list of members
	 * @param exId instance id
	 */
	public void add(int exId) {
		if (!members.contains(exId)) {
			members.add(exId);
		}
	}
	
	/**
	 * proposing price for an example
	 * if cluster purchase an example this should be called again
	 */
	public double bid(Dataset argData, int instanceID) {
		return Utility.fairMix(globalDistance(argData, instanceID), Math.pow(overHead,2));// , 1-Mu, Mu);
	}
	
	
	/**
	 * new bidding function used exponential function to control effect of
	 * very large overHead
	 */
	public double bid2(Dataset argData, int instanceID) {
		double temp = globalDistance(argData, instanceID);
		if (temp == 0) {
			System.out.println("global distance is zero:");
			System.out.println(this.toString());
		}
		return (Math.exp(-1 * overHead) / temp);
	}
	
	
	/**
	 * computes sum of squared errors of members from center
	 * @param lstInstances a table of all available instances
	 */
	public double  computeSSE(HashMap<Integer,Instance> lstInstances) {
		double sum = 0;
		for (int i : members) {
			sum += distanceTo(lstInstances.get(i));
		}
		compaction = sum;
		return compaction;
	}
	
	/**
	 * deletes an instance for  cluster
	 * @param exId	instance id
	 */
	public void delete(int exId) {
		if (members.contains(exId)) {
			members.remove((Object)exId);
		}
	}
	
	/**
	 * distance to an instance in data base
	 * @param example Instance object
	 * @return
	 */
	public double distanceTo(Instance example) {
		return Utility.distance(center, example.vector);
	}
	
	/**
	 * returns an array containing ids of this clusters members
	 * @return array of ids
	 */
	public Integer[] getMembers() {
		return members.toArray(new Integer[members.size()]);
	}
	
	/**
	 * average of distances of an instance to all other members of cluster
	 * this will be used in the bid and price functions
	 * @return
	 */
	public double globalDistance(Dataset argData, int instanceID) {
		double ret = this.distanceTo(argData.Examples.get(instanceID));
/*		if (members.isEmpty()) {
			ret = this.distanceTo(argData.Examples.get(instanceID));
		} else {
			for (int i : members) {
				if (i != instanceID) {	//if the instance is not a member this will always be true
					ret += argData.distanceMatrix[i][instanceID];
				}
			}
			ret /= members.size();
		}
*/		return ret;
	}
	
	
	/**
	 * returns initial price for the specified member of the cluster
	 * @param argData	reference to the data set
	 * @param instanceID id of the instance
	 * @param rank rank of this instance in the sorted list
	 * @return
	 */
	public double initPrice(Dataset argData, int instanceID, int rank) {
		return Utility.fairMix(globalDistance(argData, instanceID), Math.pow(rank,2));//, 1 - Phi, Phi);
	}
	
	
	/**
	 * This initial price function in the new version of algorithm
	 * that uses different methods in pricing and in bidding
	 * use EXPONENTIAL function
	 * @param argData	reference to the data set
	 * @param instanceID	id of instance
	 * @return	a real number
	 */
	public double initPrice2(Dataset argData, int instanceID) {
		return (Math.exp(-1 * overHead) / globalDistance(argData, instanceID));
	}
	
	/**
	 * this is similar to the pricing function that uses exponential but this one 
	 * use rank in the function
	 */
	public Double initPrice3(Dataset argData, int instanceID, int rank) {
		return (Math.exp(-1 * rank) / globalDistance(argData, instanceID));
	}

	
	/**
	 * determine the overhead examples and sets a price for each of them
	 * @param argData	a reference to the data set 
	 */
	public void prepareBasket(Dataset argData) {
		saleBasket.clear();
		HashMap<Integer, Double> tempSum = new HashMap<Integer, Double>();
		for (int i : members) {
			tempSum.put(i, Double.valueOf(this.globalDistance(argData, i)));
		}
		
		//here we have all the distances calculated
		//we should sort the list 
		if (overHead <= 0) {
			System.err.println("overhead is non-positive for a resource.  " + this);
			System.exit(-1);
		}
		int tempBasket[] = Utility.topMax(tempSum, overHead);
		// every id in temp basket is a member of cluster -CHECKED
		for (int i = 0;i < tempBasket.length;i++) {
			saleBasket.put(tempBasket[i], initPrice2(argData, tempBasket[i]));//, i + 1));
		}
		
	}
		
	/**
	 * determines whether this cluster is a player or a resource
	 */
	public void setRole(int lIdeal) {
		overHead = size() - lIdeal;
		if (overHead > 0) {
			isResource = true;
		} else {
			isResource = false;
		}
	}
	
	
	/**
	 * returns number of cluster members
	 * @return size of cluster
	 */
	public int size() {
		return members.size();
	}
	
	/**
	 * representing the class as a string
	 */
	public String toString() {
		String ret = "< ";
		for (int i = 0;i < dim;i++) {
			ret += center[i] + " ";
		}
		ret += ">";
		return ret;
	}

	
	/**
	 * updates center of cluster by averaging among their members
	 * @param lstInstances List of cluster members
	 */
	public void updateCentesrs(HashMap<Integer, Instance> lstInstances) {
		if (members.isEmpty()) {
			// no need for updating the center
			return;	// this will prevent an divide by zero exception
		}
		double temp[] = new double[dim];
		for (int id : members) {
			//temp = Utility.sum(temp, lstInstances.get(id).vector);
			double foo[] = lstInstances.get(id).vector;
			for (int d = 0;d < dim;d++) {
				temp[d] += foo[d];
			}
		}
		//temp = Utility.scalarDivide(temp, this.members.size());
		for (int d = 0;d < dim;d++) {
			temp[d] /= this.size();
		}
		this.center = temp;
	}


}
