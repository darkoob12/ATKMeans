import java.util.HashMap;
import java.util.Map.Entry;


/**
 * this class contains some static functions for general operations 
 * @author Shahab
 *
 */
public class Utility {
	
	/**
	 * Euclidean distance of two vectors
	 * @param X	first vector
	 * @param Y	second vector
	 * @return	a real positive number
	 */
	public static double distance(Double X[], Double Y[])
	{
		if (X.length != Y.length)
		{
			System.out.println("Vectors dimension dose not match...");
			return -1;
		}
		double ret = 0;
		for (int i = 0;i < X.length;i++)
		{
			ret += Math.pow(X[i] - Y[i], 2);
		}
		//ret = Math.sqrt(ret);
		return ret;
	}
	
	public static double distance(Instance X, Instance Y) {
		return distance(X.vector, Y.vector);
	}
	public static double distance(Instance X, Cluster C) {
		return distance(X.vector, C.center);
	}
	/**
	 * overload for double data type
	 * when comparing there is no difference between a number and its square
	 */
	public static double distance(double X[], double Y[])
	{
		if (X.length != Y.length)
		{
			System.out.println("Vectors dimension dose not match...");
			return -1;
		}
		double ret = 0;
		for (int i = 0;i < X.length;i++)
		{
			ret += Math.pow(X[i] - Y[i], 2);
		}
		//ret = Math.sqrt(ret);
		return ret;
	}

	
	/**
	 * Simply adds two vectors element by element
	 * 
	 */
	public static double[] sum(double X[], double Y[]) {
		double ret[] = new double[X.length];
		for (int i = 0;i < ret.length;i++) {
			ret[i] = X[i] + Y[i];
		}
		return ret;
	}
	
	/**
	 * divides first argument's elements by the second argument
	 */
	public static double[] scalarDivide(double X[], double s) {
		double ret[] = new double[X.length];
		for (int i = 0;i < ret.length;i++) {
			ret[i] = X[i] / s;
		}
		return ret;
	}
	
	/**
	 * returns key for top k maximum values in an array 
	 * @param list a collection of key/value pairs
	 * @param k number of returned keys
	 * @return an array of size k
	 */
	public static int[] topMax(HashMap<Integer, Double> list, int k) {
		// create an array of keys in the list
		int mems[] = new int[list.size()];
		int index = 0;
		// filling the array with keys in the list
		for (Entry<Integer, Double> en : list.entrySet()) {
			mems[index] = en.getKey();
			index++;
		}

		// now we sort the new array according to their corresponding values in the list
		for (int i = 0;i < mems.length - 1;i++) {
			for (int j = i + 1;j < mems.length;j++) {
				if (list.get(mems[j]) > list.get(mems[j])) {
					int hold = mems[j];
					mems[j] = mems[i];
					mems[i] = hold;
				}
			}
		}
		// storing top members for returning 
		int ret[] = new int[k];
		for (int i = 0;i < ret.length;i++) {
			ret[i] = mems[i];
		}
		return ret;
	}
	
	/**
	 * check whether a number is in a list or not
	 */
	public static boolean isContains(int list[], int token) {
		boolean ret = false;
		for (int i : list) {
			if (i == token) {
				ret = true;
				break;
			}
		}
		return ret;
	}
	
	/**
	 * check whether a number is in a list or not
	 */
	public static boolean isContains(Integer list[], Integer token) {
		boolean ret = false;
		for (int i : list) {
			if (i == token) {
				ret = true;
				break;
			}
		}
		return ret;
	}

	
	/**
	 * this function computes Jain's Index of  
	 * @return
	 */
	public static double fairMix(double v1, double v2) {
		double ret = Math.pow(v1 + v2, 2);
		ret /= Math.pow(v1, 2) + Math.pow(v2, 2);
		return (ret / 2);
	}
	
	/**
	 * geometric mean will be used to evaluate the fairness of algorithm 
	 */
	public static double geometricMean(double v1, double v2) {
		return Math.sqrt(v1 * v2);
	}
	
	/**
	 * returns weighted sum of values
	 * @param v1	first value	
	 * @param v2	second value
	 * @param alpha	weight for first value
	 * @param beta	weight for second value
	 * @return	a real number
	 */
	public static double weightedMix(double v1, double v2, double alpha, double beta) {
		return (v1*alpha + v2*beta);
	}
}
