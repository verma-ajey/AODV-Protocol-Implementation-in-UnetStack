import java.util.*
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.Router
import org.arl.unet.PDU

class AodvDaemon extends UnetAgent {

def phy
def myAddr
def Source_id
def Source_Seq_No =0
List<int[]> Cache = new ArrayList<int[]>();      // Holds seq no and dst,src,Hop_Count to avoid forwarding loops
List<int[]> RoutingTable = new ArrayList<int[]>();   // Holds actual routing information   Seq No, Dst, nextHop, Hop_Count

def pdu = PDU.withFormat{
    length(12)
    uint8('Type')
    uint8('Hop_Count')
    uint8('Source_Id')
    uint8('Dest_Id')
    uint32('Source_Seq_No')
    uint32('Dest_Seq_No')
  }

def data_pdu = PDU.withFormat{
    length(3)
    uint8('Type')
    uint8('Destination')
    uint8('Data')
  }

   int Update_Source_Seq_No(){
     return(++Source_Seq_No);
   }

   int Update_Hop_Count(int T_Hop_Count)
   {
     return ++T_Hop_Count;
   }

int Check_Routing_Table(int T_Dest_Id)   // Returns node address if any entry found, else 0, if no route entry exist
  {
      for(int[] row : RoutingTable)
      {
        if(row[1] == T_Dest_Id)
          return row[2]
      }
      return 0;
  }

int Check_Cache_Table(long T_Seq_No, int T_Dest_Id)   // Returns 0 if any cache entry found, else 1, if no cache entry exist
  {
    for(int[] row : Cache)
    {
      if(row[0] == T_Seq_No)
      {
        if(row[1] == T_Dest_Id)
          return 0;
      }
    }
    return 1;
  }

void Create_Backward_Route(long T_Source_Seq_No, int T_Source_Id, int T_Next, int T_Hop_Count, int Updated_T_Hop_Count)
  {
      //2 entries will be added here, one for immediate neighbor which sent RREQ and one for Source of RREQ with updated hop count
    if(T_Source_Id != T_Next)
        RoutingTable.add([T_Source_Seq_No, T_Next, T_Next, 1] as int[]);

    RoutingTable.add([T_Source_Seq_No, T_Source_Id, T_Next, Updated_T_Hop_Count] as int[]);
  }

void Create_Forward_Route(long T_Source_Seq_No, int T_Source_Id, int T_Next, int T_Hop_Count, int Updated_T_Hop_Count)    // while forwarding RREP, 1 row entry for data to be sent after Route finding phase.
  {
    if(T_Source_Id != T_Next)
        RoutingTable.add([T_Source_Seq_No, T_Next, T_Next, 1] as int[])
    RoutingTable.add([T_Source_Seq_No, T_Source_Id, T_Next, Updated_T_Hop_Count] as int[])
  }


void startup(){
  def nodeinf = agentForService Services.NODE_INFO
  subscribe topic(nodeinf)

  phy = agentForService Services.PHYSICAL
  subscribe topic(phy)
  myAddr = nodeinf.address

  Source_id = nodeinf.address
  if(myAddr == 1)
  {
    add new WakerBehavior(20000, {
      def rreq_pdu = pdu.encode([Type : 1,
                                 Hop_Count :0,
                                 Source_Id : Source_id,
                                 Dest_Id:4,
                                 Source_Seq_No : Update_Source_Seq_No(),
                                 Dest_Seq_No : 0 ])   //Dynamic Source and Destination to be added

      phy << new DatagramReq(to: Address.BROADCAST, protocol: Protocol.USER, data:rreq_pdu)
    })
  }
}


void processMessage(Message msg) {
  def bytes
    if (msg instanceof RxFrameNtf && msg.protocol == Protocol.USER)
    {
        //decode pdu here
        bytes =  pdu.decode(msg.data)
        //System.out.println(bytes.Dest_Id)

        //check if this node itself is destination node,  if yes then Unicast RREP Packet to source , else .....
        if(bytes.Dest_Id == myAddr)
        {
   //          //RREP Functionality To be added
   //          //First update cache table and routing table with backward route, after that

                 Create_Backward_Route(bytes.Source_Seq_No, bytes.Source_Id, msg.from, bytes.Hop_Count, Update_Hop_Count(bytes.Hop_Count))
                 Cache.add([bytes.Source_Seq_No, bytes.Dest_Id, bytes.Source_Id, Update_Hop_Count(bytes.Hop_Count)] as int[])
   //          // [Construct RREP PDU and Unicast it back to source using a different protocol to differentiate between RREQ and RREP]

                System.out.println("-------Cache Table at Node"+myAddr + "---------")
                for(int []chk: Cache)
                {

                    System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                }
                System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                     for(int []chk: RoutingTable)
                    {

                        System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                    }


            def rrep_pdu = pdu.encode([ Type:2,
                                        Hop_Count:0,
                                        Source_Id:Source_id,
                                        Dest_Id:bytes.Source_Id,
                                        Source_Seq_No: Update_Source_Seq_No(),
                                        Dest_Seq_No:bytes.Source_Seq_No ])                         // RREP will contain destination seq. no.

             def prev = Check_Routing_Table(bytes.Source_Id)
             phy << new DatagramReq(to:prev, protocol:Protocol.DATA,data:rrep_pdu)

         }


   //        //else ....check for cache table entry ,
   //        //if seq no is same, check dest node id,  if dest id also same dont forward the packet
   //                                                                             //else forward the packet
         else{

              if(Check_Cache_Table(bytes.Source_Seq_No, bytes.Dest_Id))    //To avoid Re-Forwarding of same RREQ packet
              {
   //              // Update cache table here for individual Nodes

                Cache.add([bytes.Source_Seq_No, bytes.Dest_Id, bytes.Source_Id, Update_Hop_Count(bytes.Hop_Count)] as int[])
                // if(myAddr ==3){
                // System.out.println("-------Cache Table at Node"+myAddr + "---------")
                // for(int []chk: Cache)
                // {
                //
                //     System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                // }
                // }
                   // Check for routing table , if route to dest is available, send  datagram using phy agent otherwise broadcast again and update backward route
                def next = Check_Routing_Table(bytes.Dest_Id)
                // Since RREQ must be forwarded, Construct a Intermediate RREQ with updated Seq no and Hop Count
                def I_rreq_pdu = pdu.encode([ Type:1,
                                              Hop_Count: Update_Hop_Count(bytes.Hop_Count),
                                              Source_Id : bytes.Source_Id,
                                              Dest_Id: bytes.Dest_Id,
                                              Source_Seq_No : Update_Source_Seq_No(),
                                              Dest_Seq_No : 0])
                if(next)
                {
                     // Route entry already exist,  forward Intermediate RREQ to Next hop address
                   phy << new DatagramReq(to:Address.next, protocol:Protocol.USER,data:I_rreq_pdu)
                }
                else  // Create backward Route Entry and Broadcast the RREQ further with updated RREQ packet
                {
                   Create_Backward_Route(bytes.Source_Seq_No, bytes.Source_Id, msg.from, bytes.Hop_Count, Update_Hop_Count(bytes.Hop_Count))
                     // if(myAddr ==3)
                     // {
                     //   System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                     //   for(int []chk: RoutingTable)
                     //   {
                     //     System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                     //   }
                     // }
                     phy << new DatagramReq(to:Address.BROADCAST, protocol : Protocol.USER, data:I_rreq_pdu)
                  }
                }
                else{
                  //don't Forward the packet (Discard the Packet to avoid re-forwarding)
                }
              }
        }
    if(msg instanceof RxFrameNtf && msg.protocol == Protocol.DATA)  // If recieved message is a RREP with DATA protocol
    {
        def r_bytes =  pdu.decode(msg.data)                //decode PDU here
                                                      //check if current node is the source node of rreq or not
          if(r_bytes.Dest_Id == myAddr)
          {
            Create_Forward_Route(r_bytes.Source_Seq_No, r_bytes.Source_Id, msg.from, r_bytes.Hop_Count, Update_Hop_Count(r_bytes.Hop_Count))
            System.out.println("Route Found to Destination");

            def dst = Check_Routing_Table(r_bytes.Source_Id)
            def phantom = data_pdu.encode([ Type:0,
                                            Destination: r_bytes.Source_Id,
                                            Data:10])
            phy << new DatagramReq(to:dst, protocol:Protocol.MAX, data:phantom)
            // System.out.println("-------Routing Table at Node" + myAddr+" ---------")
            //                                 for(int []chk: RoutingTable)
            //                                 {
            //                                   System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
            //                                 }
          }

          else                                                   //since rrep are unicast messages just update routing table, no need to update cache table as reforwarding of rreps won't be there
          {
                                                                  //Create forward route entry
            Create_Forward_Route(r_bytes.Source_Seq_No, r_bytes.Source_Id, msg.from, r_bytes.Hop_Count, Update_Hop_Count(r_bytes.Hop_Count))
                                                                   // create a intermediate RREP with updated hop count  and sequence no
            def I_rrep_pdu = pdu.encode([ Type:2, Hop_Count: Update_Hop_Count(r_bytes.Hop_Count),
                                          Source_Id : r_bytes.Source_Id,
                                          Dest_Id :r_bytes.Dest_Id,
                                          Source_Seq_No : Update_Source_Seq_No(),
                                          Dest_Seq_No : 0])

                                          if(myAddr ==2)
                                          {
                                            System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                                            for(int []chk: RoutingTable)
                                            {
                                              System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                                            }
                                          }
            def prev = Check_Routing_Table(r_bytes.Dest_Id)
            phy << new DatagramReq(to:prev, protocol:Protocol.DATA,data:I_rrep_pdu)

           }
    }
    if(msg instanceof RxFrameNtf && msg.protocol == Protocol.MAX)
    {
      def d_bytes = data_pdu.decode(msg.data)
      System.out.println("Node " + myAddr +"here" );
      if(myAddr != d_bytes.Destination){
      def dst = Check_Routing_Table(d_bytes.Destination)
      def I_data_pdu = data_pdu.encode([Type:0,
                                Destination: d_bytes.Destination,
                                Data:d_bytes.Data ])
      phy << new DatagramReq(to:dst, protocol:msg.protocol,data:I_data_pdu)
      }
      else if (myAddr == d_bytes.Destination){
        System.out.println("DAta Reached Succesfully to node " +myAddr + ": " +d_bytes.Data)
      }
    }
  }
}
