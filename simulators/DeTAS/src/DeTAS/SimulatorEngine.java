
package DeTAS;

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
    public static int MAX_NUM_PACKETS = 4;
    public static int MIN_NUM_PACKETS = 4;
    public static final int microScheduleWidth = 3;
    //++++++++++++++++++++++++++++++++++
	public boolean[][] network = new boolean[MAX_NET_SIZE][MAX_NET_SIZE];// matrix
																			// matching
																			// neighbours
    public int[] Queue = new int[MAX_NET_SIZE];// matrix
																			// number of
																			// packets of nodes
	public Node[] nodes = new Node[MAX_NET_SIZE];

	public SimulatorEngine() {

	}

	
	public void simulate(int numLinks) throws Exception {
        MIN_NUM_PACKETS = MAX_NUM_PACKETS = numLinks;
		Random ran = new Random();

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
        // finding prefferd parent with min rank for nodes
			for (int i = 0; i < MAX_NET_SIZE; i++) {
				nodes[i].setPrefferedParent(findPrefferedParent(i));
			}
        setQueueToParents();
        System.out.print(" Queue of nodes: ");
        for (int i = 0; i < MAX_NET_SIZE; i++) {			
            System.out.print(" "+ Queue[i]);          
          }
          System.out.println();
         //+++micro scheduling+++//Even-scheduling
		// for each node in the network request several links proper to num of packets to micro scheduling
        for (int i = 1; i < MAX_NET_SIZE; i++) {
            System.out.println("************************ requesting " + nodes[i].getNumPackets()
					+ " links from node " + i + " , links to: ");
			for (int k = 0; k < Queue[i]; k++) {
                //send packets to preffered parent
                int j= nodes[i].getPrefferedParent();
                int slotNumber =0 , channelOffset =0;
                System.out.println(" target "+(k+1)+" is: "+ j);
                            if ( (nodes[i].getRank()+1) %2 == 0) {
                                 slotNumber = ran.nextInt(2*nodes[i].getNumPackets());// pick ts and ch.offset in microSchedule
							     channelOffset = ran.nextInt(MAX_CH_OFFSET);
                                                                
                            /*    while ( (nodes[nodes[j].getPrefferedParent()].getMicroScheduleSlotOffset() + Queue[nodes[j].getPrefferedParent()]) < slotNumber ) {
                                    slotNumber = ran.nextInt(MAX_TIME_SLOT);                                    
                                }*/
                                 //if slotNumber is not even choose nearest even to it
                                 if (slotNumber%2 != 0)
                                      slotNumber++;
                                 if (slotNumber > 2*nodes[i].getNumPackets())
                                     slotNumber -=2;
                                 slotNumber += Queue[i]- 2*nodes[i].getNumPackets();
                                 slotNumber += nodes[i].getMicroScheduleSlotOffset();
                                 channelOffset = ((nodes[i].getRank()+1) - 2) % 3;
                                 channelOffset += nodes[i].getMicroScheduleChannelOffset();
                                 System.out.println(" slotNumber: "+ slotNumber+" channelOffset: "+channelOffset);
                                 nodes[i].scheduleLink(slotNumber, channelOffset,
									Cell.SlotType.TX, j);
                                 channelOffset = ((nodes[i].getRank()+1) - 1) % 3;
                                 channelOffset += nodes[i].getMicroScheduleChannelOffset();
							     nodes[j].scheduleLink(slotNumber, channelOffset,
									Cell.SlotType.RX, i);
                            }
                            else {
                                  slotNumber = ran.nextInt(2*nodes[i].getNumPackets());// pick ts and ch.offset in microSchedule
							     channelOffset = ran.nextInt(MAX_CH_OFFSET);
                                
                           /*     while ( (nodes[nodes[j].getPrefferedParent()].getMicroScheduleSlotOffset() + Queue[nodes[j].getPrefferedParent()]) < slotNumber ) {
                                    slotNumber = ran.nextInt(MAX_TIME_SLOT);                                    
                                }*/
                                  //if slotNumber is not odd choose nearest odd to it
                                if (slotNumber%2 == 0)
                                    slotNumber++;
                                 if (slotNumber > 2*nodes[i].getNumPackets())
                                     slotNumber -=2;
                                 slotNumber += Queue[i]- 2*nodes[i].getNumPackets();
                                 slotNumber += nodes[i].getMicroScheduleSlotOffset();
                                 channelOffset = ((nodes[i].getRank()+1) - 2) % 3;
                                 channelOffset += nodes[i].getMicroScheduleChannelOffset();
                                 System.out.println(" slotNumber: "+ slotNumber+" channelOffset: "+channelOffset);
                                 nodes[i].scheduleLink(slotNumber, channelOffset,
									Cell.SlotType.TX, j);
                                 channelOffset = ((nodes[i].getRank()+1) - 1) % 3;
                                 channelOffset += nodes[i].getMicroScheduleChannelOffset();
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
					Queue[i] += 2*nodes[j].getNumPackets() ;//set Qi double
                }
			}
            //add num of packets of node to its Qi
                Queue[i] += 2*nodes[i].getNumPackets() ;
           }
           
         /*  for (int j = 0; j < MAX_NET_SIZE; j++) {
               Queue[j] *=2;
			}*/
           //set block of micro schedule in slotframe for nodes
           System.out.println(" blocks of scheduling: ");
           int slotOffset = 0;
           int channelOffset = 0;
			for (int j = 0; j < MAX_NET_SIZE; j++) {
                if( nodes[j].getPrefferedParent() == 0 ) {
                        if ( (slotOffset + Queue[j]) < MAX_TIME_SLOT ) {
                            nodes[j].setMicroScheduleSlotOffset(slotOffset);
                            nodes[j].setMicroScheduleChannelOffset(channelOffset);
                            slotOffset += Queue[j];
                        }
                        else {
                            channelOffset += microScheduleWidth;
                            slotOffset = 0;
                            nodes[j].setMicroScheduleSlotOffset(slotOffset);
                            nodes[j].setMicroScheduleChannelOffset(channelOffset);
                            slotOffset += Queue[j];
                        }
                        System.out.println(" Routing Graph "+j+"; Qi: "+Queue[j]+" MicroScheduleSlotOffset: "+nodes[j].getMicroScheduleSlotOffset()+" , MicroScheduleChannelOffset: "+nodes[j].getMicroScheduleChannelOffset());
                }
			}
           //in every block of scheduling, 2Qi should be partitioned between one hop childs
           for (int i = 1; i < MAX_NET_SIZE; i++) {
               int timeSlot = 0;
               for (int j = 1; j < MAX_NET_SIZE; j++) {
                   if ( nodes[j].getPrefferedParent() == i ) {
                       //first set scheduled timeSlot and channel offset of microScheduling to childs of routin Graphs
                      nodes[j].setMicroScheduleChannelOffset(nodes[i].getMicroScheduleChannelOffset());
                      nodes[j].setMicroScheduleSlotOffset(nodes[i].getMicroScheduleSlotOffset());

                      nodes[j].setMicroScheduleSlotOffset(nodes[j].getMicroScheduleSlotOffset() + timeSlot);
                      timeSlot += Queue[j];
                   }
               }
           }
            for (int j = 1; j < MAX_NET_SIZE; j++) {
               if (nodes[j].getPrefferedParent() !=0 && nodes[j].getPrefferedParent()!=-1 ){
                    System.out.println(j +" is Child of Parent "+nodes[j].getPrefferedParent()+"; Qi: "+Queue[j]+" MicroScheduleSlotOffset: "+nodes[j].getMicroScheduleSlotOffset()+" , MicroScheduleChannelOffset: "+nodes[j].getMicroScheduleChannelOffset());
               }
           }

	}

    public int minRank(int nodeIndex) {
        int min = 65536;

			for (int j = 0; j < MAX_NET_SIZE; j++) {
                if(network[nodeIndex][j])
					if(min > nodes[j].getRank())
                        min = nodes[j].getRank();
			}

        return min;
	}
     public int findPrefferedParent(int nodeIndex) {
        int parent = 65536;

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
