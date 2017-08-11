import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.io.*;


/**
 * this class manages data and performs operations on them which are
 * the same in all algorithms
 * @author Shahab
 *
 */
public class Dataset {
	//fields
	public HashMap<Integer, Instance> Examples;	//examples with a zero based id
	public HashMap<Integer, Cluster> Clusters;	//clusters with a zero based id
	public int dim;			// dimension of each example
	public double distanceMatrix[][];	//mutual distance between two Instances
	
	public int numData;		//number of instances in the data set
	public int numClusters; // number of clusters we are going to create
	public int lIdeal;		//ideal number of instances per cluster

	//constructors
	public Dataset(int argDim) {
		dim = argDim;
		Examples = new HashMap<Integer, Instance>();
		Clusters = new HashMap<Integer, Cluster>();
		numData = 150;	//default value
		numClusters = 3; //default value
		inferLideal();
	}
	
	//methods
	/**
	 * computes ideal number of examples per cluster
	 */
	public void inferLideal() {
		lIdeal = numData / numClusters; 
	}
	/**
	 * sum of squared errors from centers
	 * @return
	 */
	public double computeSSE() {
		double ret = 0;
		for (Cluster c : Clusters.values()) {
			ret += c.computeSSE(Examples);
		}
		return ret;
	}
	
	/**
	 * writing data in the dataset to a file in csv format
	 * @param path output file
	 */
	public void writeCSV(String path) {
		
	}
	
	/**
	 * reading data from a csv file
	 * @param path input file
	 **/
	public void readCSV(String path, String sep) {
		//clear Examples 
		if ((Examples == null) || (!Examples.isEmpty())) {
			Examples = new HashMap<Integer, Instance>();
		}
		
		try {
			int k = 0;
			int inDim = -1;	//dimension of input data
			String strLine;  //storing a line from file
			BufferedReader br = new BufferedReader(new FileReader(path));
			
			String strTemp = "";	//temporary stored for conversion

			StringTokenizer st = null;

			while ((strLine = br.readLine()) != null) {
				st = new StringTokenizer(strLine, sep);
				
				//determining dimension of data
				if (inDim < 0) {
					inDim= st.countTokens();
					this.dim = inDim;
				}
				Instance instNew = new Instance(k, inDim);

				int i = 0;
 				while (st.hasMoreTokens()) {
					strTemp = st.nextToken();
					instNew.vector[i] = Double.parseDouble(strTemp);
					i++;
				}
 				Examples.put(k, instNew);
				k++;
			}
			dim =inDim;
			numData = Examples.size();
			
			br.close();
		} catch (Exception e) {
			//Auto-generated catch block
			e.printStackTrace();
		}finally{
			//Nothing to do;
		}
	}
	
	/**
	 * print each cluster with it's size
	 * and the two objectives SSE and L
	 */
	public void showStat() {
		System.out.println("____________________________________________________");
		for (Cluster c : Clusters.values()) {
			System.out.println(c.id + " - size => " + c.size());
		}
		System.out.println("SSE = " + computeSSE());
		System.out.println("L = " + computeL());
		System.out.println("____________________________________________________");
	}
	
	/**
	 * L measure indicates equal partition objective of the cluster
	 * @return
	 */
	public double computeL() {
		double ret = 0;
		for (Cluster c : Clusters.values()) {
			ret += Math.pow(c.size() - lIdeal, 2);
		}
		return ret;
	}
	
	/**
	 * will display all vectors in the console 
	 */
	public void dispData() {
		for (Entry<Integer, Instance> en : Examples.entrySet()) {
			String s = en.getKey() + " => " + en.getValue();
			System.out.println(s);
		}
	}
	
	/**
	 * randomly creates center points for clusters
	 * this function was for the first random data that 
	 * i generated with random means for each cluster
	 */
	public void initializeClusters() {
		Random rnd = new Random(System.currentTimeMillis());
		//creating cluster objects and adding them to the list
		for (int i = 0;i < numClusters;i++) {
			Cluster temp = new Cluster(i, dim);
			for (int d = 0;d < dim;d++) {
				temp.center[d] = rnd.nextDouble() * 8;
			}
			Clusters.put(i, temp);
		}
	}
	/**
	 * this will be used for real data sets
	 * the British Town Data and German Town Data
	 * this should find bounds for data in each  dimension and 
	 * generate random numbers in appropriate ranges uniformly 
	 * @Note : when this function is being called the data should be completely loaded
	 * in the data set object
	 */
	public void initClustersRandomly() {
		// creating a matrix for saving the bounds
		double dimBounds[][] = new double[dim][];
		for (int i = 0;i < dim;i++) {
			dimBounds[i] = new double[2]; //first column is for Min and second one is for Max
			dimBounds[i][0] = Double.MAX_VALUE;			//this column is for minimum 
			dimBounds[i][1] = Double.MIN_VALUE;			//this column is maximum
		}
		
		for (Instance inst : Examples.values()) {
			for (int i = 0;i < inst.dim;i++) {
				if (inst.vector[i] < dimBounds[i][0])
					dimBounds[i][0] = inst.vector[i];
				if (inst.vector[i] > dimBounds[i][1])
					dimBounds[i][1] = inst.vector[i];
			}
		}
		
		// now we know bounds for each dimension 
		// will create cluster objects and set random numbers for their centers
		Random rnd = new Random(System.currentTimeMillis());
		for (int k = 0;k < numClusters;k++) {
			Cluster newClust = new Cluster(k, dim);
			for (int i = 0;i < dim;i++) {
				newClust.center[i] = dimBounds[i][0] + rnd.nextGaussian() * (dimBounds[i][1] - dimBounds[i][0]);
			}
			Clusters.put(k, newClust);
		}
	}
	
	/*
	 * randomly selects some points among the available examples 
	 * as cluster centers
	 */
	public void initClstRandPoints() {
		Random rnd = new Random(System.currentTimeMillis());
		ArrayList<Integer> usedExamples = new ArrayList<Integer>();
		//create cluster object;
		for (int i = 0; i < numClusters;i++) {
			Cluster temp = new Cluster(i, dim);
			while (true) { 
				int id = rnd.nextInt(numData);
				if (!usedExamples.contains(id)) {
					temp.center = Examples.get(id).vector;
					usedExamples.add(id);
					break;
				}
			}
			Clusters.put(i, temp);
		}
	}
	
	/**
	 * computes distance between points
	 */
	public void preProcess() {
		int numData = Examples.size();
		if (distanceMatrix == null) {
			// creating a matrix of size the same as data size
			distanceMatrix = new double[numData][];
			for (int i = 0;i < numData;i++) {
				distanceMatrix[i] = new double[numData];
			}
		}
		for (int i = 0;i < numData;i++) {
			for (int j = 0;j < numData;j++) {
				distanceMatrix[i][j] = Utility.distance(Examples.get(i), Examples.get(j));
			}
		}
	}
	
	
	/**
	 * remove an example from a cluster
	 * and adds it to the second argument cluster
	 */
	public void changeMembership(int sourceInstance, int destinCluster) {
		int firstCluster = Examples.get(sourceInstance).clusterId;
		Clusters.get(firstCluster).delete(sourceInstance);
		Clusters.get(firstCluster).changed = true;
		Clusters.get(destinCluster).add(sourceInstance);
		Clusters.get(destinCluster).changed = true;
		Examples.get(sourceInstance).clusterId = destinCluster;
	}
	
	
	/**
	 * determines cluster for each example by finding nearest
	 * cluster center to the example
	 */
	public void updateMembers() {
		for (Instance ex : Examples.values()) {
			int curID = 0;			// id of the nearest cluster
			double minDist = Integer.MAX_VALUE;		//distance to the nearest cluster
			for (Cluster c : Clusters.values()) {
				double tempDist = ex.distanceTo(c);
				if (tempDist < minDist) {
					curID = c.id;
					minDist = tempDist;
				}
			}
			// now we know the nearest cluster
			changeMembership(ex.id, curID);	//applying the nears cluster
		}
	}
	/**
	 * determine an instance's cluster and change it's member ship if need
	 * @param ex an Instance object
	 *	This function is here because we need access to whole data.
	 */
	public void updateMemberShip(Instance ex) {
		Cluster selClst = null;	//selected cluster
		double MinDist = Double.MAX_VALUE;		//minimum distance between this instance and any cluster
		// for each Cluster check distance to center
		for (Cluster c : Clusters.values()) {
			double tmpDist = c.distanceTo(ex);
			if (tmpDist < MinDist) {
				selClst = c;
				MinDist = tmpDist;
			}
		}
		//change Membership
		if (selClst != null) {
			Clusters.get(ex.clusterId).delete(ex.id);
			Clusters.get(ex.clusterId).changed = true;
			selClst.add(ex.id);
			selClst.changed = true;
			ex.clusterId = selClst.id;			
		} else {
			System.out.println("ERROR, no cluster selected");
		}
	}
	
	/**
	 * generates random points according two paper DATA-A random data 
	 * @param n number of data points
	 */
	public void randomDS1(int n) {
		this.numData = n;
		this.dim = 2;
		// create new table for examples
		// and actually this is for clearing previous data
		Examples = new HashMap<Integer, Instance>(numData);
		
		Random rnd = new Random(System.currentTimeMillis());
		
		double Mu = 0;
		double Sig = 2;
		
		for (int i = 0;i < numData;i++) {
			Mu = rnd.nextDouble() * 10;		// a mean that is uniformly generated between 0 and 10
			Instance tmp = new Instance(i, dim);
			for (int d = 0;d < dim;d++) {
				tmp.vector[d] = (rnd.nextGaussian() * Sig) + Mu;
			}
			Examples.put(i, tmp);
		}
		
	}
	
	/**
	 * This function dose not follows the paper instructions
	 * and just generates some random data
	 * @param numData	
	 * @param numClusters	for each cluster i will generate a random mean(0<Mu<10)
	 */
	public void fillRandom() {
		// checking the availability of Hash table
		if (Examples == null){
			Examples = new HashMap<Integer, Instance>();
		} else if (!Examples.isEmpty()) {
			Examples.clear();
		}
	
		int k = 0;
		Random rnd = new Random(System.currentTimeMillis());
		double mean = 1;
		for (int i = 0;i < numClusters;i++) {
			for (int j = 0;j < lIdeal;j++) {
				mean = rnd.nextDouble() * 8;
				Instance temp = new Instance(k, dim);
				for (int d = 0;d < dim;d++) {
					temp.vector[d] = rnd.nextGaussian() + mean;
				}
				Examples.put(k, temp);
				k++;
			}
		}
		while (k < numData) {
			Instance temp = new Instance(k, dim);
			for (int d = 0;d < dim;d++) {
				temp.vector[d] = rnd.nextGaussian() + mean;
			}
			Examples.put(k, temp);
			k++;
		}
	}
}
