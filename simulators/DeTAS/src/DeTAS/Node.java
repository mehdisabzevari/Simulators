
package DeTAS;

/**
 *
 * @author Mehdi
 */
import DeTAS.Cell.SlotType;

public class Node {

	public static final int MAX_TIME_SLOT = 101;
	public static final int MAX_CH_OFFSET = 16;

	private int id;
    //+++++++++++++++++++++++++++++++
    private int rank;
    private int PrefferedParent;
    //+++++++++++++++++++++++++++++++
	private int numAllocated;
	private int numCollisions;
	private int numIdenticallyAllocated;
	private Cell[][] slotframe   = new Cell[MAX_TIME_SLOT][MAX_CH_OFFSET];

	private int numNeighbors;
    //+++++++++++++++++++++++++++++++
    private int numPackets;
    private int microScheduleSlotOffset;
    private int microScheduleChannelOffset;
    //+++++++++++++++++++++++++++++++

	public Node(int id){
		this.id=id;
        this.rank=65536;
        this.PrefferedParent=-1;
        microScheduleChannelOffset = 0;
        microScheduleSlotOffset = 0;
		for (int i=0;i<MAX_TIME_SLOT;i++){
			for (int j=0;j<MAX_CH_OFFSET;j++){
				slotframe[i][j]= new Cell(i, j, -1, Cell.SlotType.OFF);
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getNumAllocated() {
		return numAllocated;
	}

	public void setNumAllocated(int numAllocated) {
		this.numAllocated = numAllocated;
	}

	public int getNumCollisions() {
		return numCollisions;
	}

	public void setNumCollisions(int numCollisions) {
		this.numCollisions = numCollisions;
	}

	public void scheduleLink(int slotNumber, int channelOffset,SlotType type, int node) {

 		Cell candidate= new Cell(slotNumber, channelOffset, node, type);
 		//check for collisions
 		Cell ce=slotframe[slotNumber][channelOffset];
 		if (ce.getType()==Cell.SlotType.OFF && ce.getTarget()==-1){
 	    	slotframe[slotNumber][channelOffset]=candidate;
 			this.numAllocated++;
 		}else{
 			if (ce.compareTo(candidate)==0){
 				//rescheduling the same cell
 				this.numIdenticallyAllocated++;
 				//System.out.println("Collision occurred by allocating the same cell at slotNumber " + slotNumber + " ch.Offset "+ channelOffset + " at Node " + this.id + " with neighbour " + node + " " +ce);
 			}else{
 				this.numCollisions++;
 			    //System.out.println("Collision occurred at slotNumber " + slotNumber + " ch.Offset "+ channelOffset + " at Node " + this.id + " with neighbour " + node + " " +ce);
 			}
 		}

	}

	public int getNumIdenticallyAllocated() {
		return numIdenticallyAllocated;
	}

	public void setNumIdenticallyAllocated(int numIdenticallyAllocated) {
		this.numIdenticallyAllocated = numIdenticallyAllocated;
	}

	public int getNumNeighbors() {
		return numNeighbors;
	}

	public void setNumNeighbors(int numNeighbors) {
		this.numNeighbors = numNeighbors;
	}
    //+++++++++++++++++++++++++++++++
    public int getNumPackets() {
		return numPackets;
	}

	public void setNumPackets(int numPackets) {
		this.numPackets = numPackets;
	}

     public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

     public int getPrefferedParent() {
		return PrefferedParent;
	}

	public void setPrefferedParent(int PrefferedParent) {
		this.PrefferedParent = PrefferedParent;
	}

    public int getMicroScheduleSlotOffset() {
		return microScheduleSlotOffset;
	}

	public void setMicroScheduleSlotOffset(int microScheduleSlotOffset) {
		this.microScheduleSlotOffset = microScheduleSlotOffset;
	}

     public int getMicroScheduleChannelOffset() {
		return microScheduleChannelOffset;
	}

	public void setMicroScheduleChannelOffset(int microScheduleChannelOffset) {
		this.microScheduleChannelOffset = microScheduleChannelOffset;
	}
   //+++++++++++++++++++++++++++++++
}
