
package sixTUS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.Collections;
import sun.misc.Queue;
/**
 *
 * @author Mehdi
 *
 */
public class SimulatorEngine {

	public static final int MAX_NET_SIZE = 50;
	public static final int MAX_NUM_NEIGHBORS = 10;
	public static final int MIN_NUM_NEIGHBORS = 2;

	public static final int MAX_TIME_SLOT = 101;
	public static final int MAX_CH_OFFSET = 16;

    //++++++++++++++++++++++++++++++++++
    public static final int MAX_NUM_PACKETS = 4;
    public static final int MIN_NUM_PACKETS = 4;
    //++++++++++++++++++++++++++++++++++
	public boolean[][] network = new boolean[MAX_NET_SIZE][MAX_NET_SIZE];// matrix
																			// matching
																			// neighbours
    public int[] Queue = new int[MAX_NET_SIZE];// matrix
																			// number of
																			// packets of nodes
	public Node[] nodes = new Node[MAX_NET_SIZE];
	//private int num_links_requested;

	public SimulatorEngine() {

	}

	/**
	 * requires the number of links to be allocated to each neigbour
	 *
	 * @param links
	 */
	public void simulate() throws Exception {
		Random ran = new Random();
	//	this.num_links_requested = links;

		for (int i = 0; i < MAX_NET_SIZE; i++) {
			for (int j = 0; j < MAX_NET_SIZE; j++) {
				network[i][j] = false; // initialize network
                Queue[i] = 0;
			}
		}

		// create the nodes:
		for (int i = 0; i < MAX_NET_SIZE; i++) {
			nodes[i] = new Node(i);
            //+++++++++++++++++++++++++++++++
            // select number of packets for that node
			int num_pack = ran.nextInt(MAX_NUM_PACKETS);
			if (num_pack < MIN_NUM_PACKETS) {
				num_pack = MIN_NUM_PACKETS;
			}
            nodes[i].setNumPackets(num_pack);
            //+++++++++++++++++++++++++++++++
		}

		// create the network randomly.
		for (int i = 0; i < MAX_NET_SIZE; i++) {
			// select number of neighbors for that node
			int num_nei = ran.nextInt(MAX_NUM_NEIGHBORS);
			if (num_nei < MIN_NUM_NEIGHBORS) {
				num_nei = MIN_NUM_NEIGHBORS;
			}           
			// set them in the network structure. pick them randomly too.
			for (int j = 0; j < num_nei; j++) {
				int h = ran.nextInt(MAX_NET_SIZE);
				while (h == i) {
					h = ran.nextInt(MAX_NET_SIZE);// we don't want to say that
													// we are neighbours of
													// ourselves
				}
				network[i][h] = true;// set it as the neighbour
				network[h][i] = true;// set it as the neighbour
			}
		}
		// compute how many neighbors has each node and update each neighbor
		// with that info
		this.computeNumNeighbors();
        //++++++++++++++++++++++++++++++++++++++++
        //converting network graph to DoDAG
        CreateDoDAG();
        // reset the nodes:
			for (int i = 0; i < MAX_NET_SIZE; i++) {
				nodes[i].setPrefferedParent(findPrefferedParent(i));
			}
        setQueueToParents();
        for (int i = 0; i < MAX_NET_SIZE; i++) {			
            System.out.print(" "+ Queue[i]);          
          }
          System.out.println();
		// for each node in the network request several links proper to num of packets to random neighbours
        for (int i = 0; i < MAX_NET_SIZE; i++) {
            System.out.println("************************ requesting " + nodes[i].getNumPackets()
					+ " links from node " + i + " , links to: ");
			for (int k = 0; k < nodes[i].getNumPackets(); k++) {
               int j= nodes[i].getPrefferedParent();
                System.out.println(" Destination of packet "+(k+1)+": "+ j);
						if (network[i][j]) {
							// these are neighbors
							int slotNumber = ran.nextInt(MAX_TIME_SLOT);// pick
																		// random
																		// ts
																		// and
																		// ch.offset
							int channelOffset = ran.nextInt(MAX_CH_OFFSET);
							nodes[i].scheduleLink(slotNumber, channelOffset,
									Cell.SlotType.TX, j);
							nodes[j].scheduleLink(slotNumber, channelOffset,
									Cell.SlotType.RX, i);
						}				
				
			}
           }
          printResult();
			// reset the nodes:
			for (int i = 0; i < MAX_NET_SIZE; i++) {
				nodes[i] = new Node(i);
			}
			// compute how many neighbors has each node and update each neighbor
			// with that info
			this.computeNumNeighbors();
        //++++++++++++++++++++++++++++++++++++++
	}

	private void printResult() {
		String head=null;

		//if (numlink==1){
			head = "Node,NumLinksRequested,NumNeighbors,TotalLinks,Allocated Links,Collisions,IdenticaAllocation,% Collision,% Used Links";
			System.out.println(head);
		//}

		for (int i = 0; i < MAX_NET_SIZE; i++) {
			String content = i
					+ ","
					+ nodes[i].getNumPackets()
					+ ","
					+ nodes[i].getNumNeighbors()
					+ ","
					+ (MAX_TIME_SLOT * MAX_CH_OFFSET)
					+ ","
					+ nodes[i].getNumAllocated()
					+ ","
					+ nodes[i].getNumCollisions()
					+ ","
					+ nodes[i].getNumIdenticallyAllocated()
					+ ","
					+ ((((double) nodes[i].getNumCollisions()) / ((double) nodes[i].getNumAllocated())) * 100.0)
					+ ","
					+ (((double) nodes[i].getNumAllocated() / (MAX_TIME_SLOT * MAX_CH_OFFSET)) * 100.0);

			System.out.println(content);
            //+++++++++++++++++++++++++++++++++            
                String neighbors = "";
                String nei_head = "Neighbors :";
                for (int j = 0; j < MAX_NET_SIZE; j++) {
                    if (network[i][j]){
                        neighbors += j
                        + " rank: " + nodes[j].getRank()+ " parent: "+ nodes[j].getPrefferedParent()
                        + "    " ;
                    }
                }
                this.writeToFile("neighbors" + i,nei_head, neighbors);           
            //++++++++++++++++++++++++++++++++++++
			this.writeToFile("results" + i,head, content);
		}
	}

	public void computeNumNeighbors() {
		for (int i = 0; i < MAX_NET_SIZE; i++) {
			int count = 0;
			for (int j = 0; j < MAX_NET_SIZE; j++) {
				if (network[i][j])
					count++;
			}
			nodes[i].setNumNeighbors(count);
		}
	}

    //++++++++++++++++++++++++++++++++++++++++   
    public void CreateDoDAG() throws Exception{
        Queue queue = new Queue();
        int rank = 0 , pointer = 0;
        nodes[0].setRank(rank);
        queue.enqueue(pointer);
        while(!queue.isEmpty()) {
           pointer = Integer.parseInt(queue.dequeue().toString());
           for (int j = 0; j < MAX_NET_SIZE; j++) {
                    if(network[pointer][j] && (nodes[j].getRank() > rank) ) {
                        queue.enqueue(j);
                        nodes[j].setRank(rank+1);
                    }
            }
           rank++;
        }

    }
   public void setQueueToParents() {
        // send num of packets as Qi to node's parent
           for (int i = MAX_NET_SIZE-1; i >= 0; i--) {
               //add num of packets of childs to Qi of node
			for (int j = 0; j < MAX_NET_SIZE; j++) {
                if( nodes[j].getPrefferedParent() == i ) {
					Queue[i] += nodes[j].getNumPackets() ;
                }
			}
            //add num of packets of node to its Qi
                Queue[i] += nodes[i].getNumPackets() ;
           }
          
	}

    public int minRank(int nodeIndex) {
        int min = 65535;

			for (int j = 0; j < MAX_NET_SIZE; j++) {
                if(network[nodeIndex][j])
					if(min > nodes[j].getRank())
                        min = nodes[j].getRank();
			}

        return min;
	}
     public int findPrefferedParent(int nodeIndex) {
        int parent = 65535;

			for (int j = 0; j < MAX_NET_SIZE; j++) {
                if(network[nodeIndex][j] && (minRank(nodeIndex) == nodes[j].getRank()) ){
                        parent = j;
                        break;
                }
			}

        return parent;
	}
    //++++++++++++++++++++++++++++++++++++++++

	public void writeToFile(String filename,String header, String content) {
		FileOutputStream fop = null;
		File file;

		try {
			file = new File(filename);

			if (!file.exists()) {
				file.createNewFile();
			}

			fop = new FileOutputStream(file, true);

			if (header!=null) content = header + '\n' + content;

			// get the content in bytes
			byte[] contentInBytes = content.getBytes();

			fop.write(contentInBytes);
			fop.write('\n');
			fop.flush();
			fop.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fop != null) {
					fop.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
