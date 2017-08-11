
public class Instance {
	//FIELDS
	public int id;
	public int dim;
	public double vector[];
	public int clusterId;
	
	//COMSTRUCTORS
	/**
	 * create in instance of this class
	 * set dimension to 3
	 * @param argID	instance id
	 */
	public Instance(int argID) {
		id = argID;
		dim = 3;
		vector = new double[dim];
		clusterId = 0;
	}
	
	/**
	 * this constructor allows for other dimensions than 3 
	 * @param argID		id of instance	
	 * @param argDim	dimension of vector
	 */
	public Instance(int argID, int argDim) {
		id = argID;
		dim = argDim;
		vector = new double[dim];
		clusterId = 0;
	}
	
	//METHODS
	/**
	 * distance to another instance in the data set
	 * @param otherExample	object of type instance
	 * @return
	 */
	public double distanceTo(Instance otherExample) {
		return Utility.distance(vector, otherExample.vector);
	}
	
	/**
	 * representing the class as a string
	 */
	public String toString() {
		String ret = id + " => < ";
		for (int i = 0;i < dim;i++) {
			ret += vector[i] + " ";
		}
		ret += ">";
		return ret;
	}
	
	/**
	 * distance to a cluster's center
	 * @param otherCluster reference to a cluster 
	 * @return positive real value
	 */
	public double distanceTo(Cluster otherCluster) {
		return Utility.distance(vector, otherCluster.center);
	}
}
