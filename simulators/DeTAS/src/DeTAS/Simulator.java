
package DeTAS;

/**
 *
 * @author Mehdi
 */
public class Simulator {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SimulatorEngine simul= new SimulatorEngine();
		for (int i = 4; i < 5; i++)
		   simul.simulate(i);
	}

}