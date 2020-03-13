import java.util.*
import java.text.SimpleDateFormat
import java.util.Calendar
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.Router
import org.arl.unet.PDU

class AodvDaemon extends UnetAgent {

def ACTIVE_ROUTE_TIMEOUT = 3000 //ms
def MY_ROUTE_TIMEOUT = 2*ACTIVE_ROUTE_TIMEOUT
def ALLOWED_HELLO_LOSS = 2
def HELLO_INTERVAL = 1000  //ms
def RREQ_RETRIES = 2
def RREQ_RATELIMIT = 10
def RERR_RATELIMIT = 10

def NET_DIAMETER = 35
def NODE_TRAVERSAL_TIME = 40  //ms

def TIMEOUT_BUFFER = 2

def TTL_START = 1
def TTL_INCREMENT = 2
def TTL_THRESHOLD = 7
def LOCAL_ADD_TTL = 2
def MAX_REPAIR_TTL = 0.3*NET_DIAMETER

def NEXT_HOP_WAIT = NODE_TRAVERSAL_TIME + 10
def NET_TRAVERSAL_TIME = 2*NODE_TRAVERSAL_TIME*NET_DIAMETER

def PATH_DISCOVERY_TIME = 2*NET_TRAVERSAL_TIME

def BLACKLIST_TIMEOUT = RREQ_RETRIES*NET_TRAVERSAL_TIME

def K = 5  //Knob
def DELETE_PERIOD =  K* Math.max(ACTIVE_ROUTE_TIMEOUT,HELLO_INTERVAL)


def phy
def myAddr
def Source_id
def Source_Seq_No =0
def Rreq_Id =0

List<long[]> Cache = new ArrayList<long[]>();      // Holds source_seq_no,dst_id,src_id,Hop_Count,dest_seq_no to avoid forwarding loops
                                                    // ---------------------------------------------------------------------------------
                                                    // |   source_Seq_No    dst_Id     src_id   Hop_Count     dst_seq_no      Timestamp |
                                                    // |                                                                                |
                                                    // |                                                                                |
                                                    // |                                                                                |
                                                    // ----------------------------------------------------------------------------------
List<int[]> RoutingTable = new ArrayList<int[]>();   // Holds actual routing information   source_Seq_No, Dst_id, nextHop, Hop_Count,dest_seq_no
                                                    // ---------------------------------------------------------------------------------
                                                    // |   source_Seq_No    dst_Id     nextHop   Hop_Count     dst_seq_no     Timestamp |
                                                    // |                                                                                |
                                                    // |                                                                                |
                                                    // |                                                                                |
                                                    // ----------------------------------------------------------------------------------

def pdu = PDU.withFormat{

    uint8('Type')
    uint8('Hop_Count')
    uint8('Source_Id')
    uint8('Dest_Id')
    uint16('Source_Seq_No')
    uint16('Dest_Seq_No')
    uint16('Rreq_Id')
    int8('D')
    int8('G')
    int8('U')
}

  def rp_pdu = PDU.withFormat{

      uint8('Type')
      uint8('Hop_Count')
      uint8('Source_Id')
      uint8('Dest_Id')
      uint16('Dest_Seq_No')
}


// def data_pdu = PDU.withFormat{
//     length(3)
//     uint8('Type')
//     uint8('Destination')
//     uint8('Data')
//   }

int Update_Source_Seq_No(){
     return(++Source_Seq_No);
}

int Update_Rreq_Id(){
  return (++Rreq_Id)
}

long Get_Current_Time(){
  def timeStart = new Date()
  long seconds = timeStart.getTime()

  // Calendar c1 = Calendar.getInstance();
  //
  // c1.set(Calendar.MONTH, 01);
  //
  // c1.set(Calendar.DATE, 01);
  //
  // c1.set(Calendar.YEAR, 2015);
  //
  // Date dateOne = c1.getTime();
  // long t = dateOne.getTime();
  //
  // return (seconds - t);
  return seconds
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

int Check_Cache_Table(long T_Rreq_Id, long T_Source_Id , long T_Dest_Id)   // Returns 0 if any cache entry found, else 1, if no cache entry exist
  {
    long temp_timestamp = Get_Current_Time()
//    Iterator<long[]> ite = Cache.iterator();


    for(long[] t_row : Cache)
    {
      if((temp_timestamp - t_row[4]) > PATH_DISCOVERY_TIME)
      {
        Cache.remove(t_row)
      }
    }

    for(long[] row : Cache)
    {
      if(row[0] == T_Rreq_Id)
      {
        if(row[1] == T_Source_Id && row[2] == T_Dest_Id)
          return 0;
      }
    }
    return 1;
  }

void Create_Backward_Route(int T_Source_Seq_No, int T_Source_Id, int T_Next, int T_Hop_Count, int Updated_T_Hop_Count)
  {
      //2 entries will be added here, one for immediate neighbor which sent RREQ and one for Source of RREQ with updated hop count
    // if(T_Source_Id != T_Next)
    //     RoutingTable.add([T_Source_Seq_No, T_Next, T_Next, 1] as int[]);

    RoutingTable.add([T_Source_Seq_No, T_Source_Id, T_Next, Updated_T_Hop_Count] as int[]);
  }

void Create_Forward_Route(int T_Source_Seq_No, int T_Source_Id, int T_Next, int T_Hop_Count, int Updated_T_Hop_Count)    // while forwarding RREP, 1 row entry added for data to be sent after Route finding phase.
  {
    // if(T_Source_Id != T_Next)
    //     RoutingTable.add([T_Source_Seq_No, T_Next, T_Next, 1] as int[])
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
    add new WakerBehavior(5000, {
      def rreq_pdu = pdu.encode([Type : 1,
                                 Hop_Count :0,
                                 Source_Id : Source_id,
                                 Dest_Id:4,
                                 Source_Seq_No : Update_Source_Seq_No(),
                                 Dest_Seq_No : 0 ,
                                 Rreq_Id : Update_Rreq_Id(),
                                 G : 0,
                                 D : 1,
                                 U : 1])   //Dynamic Source and Destination to be added
      def temp = pdu.decode(rreq_pdu)
      Cache.add([temp.Rreq_Id,temp.Source_Id,temp.Dest_Id,temp.Source_Seq_No,Get_Current_Time()] as long[])
      Cache.add([4,5,6,4,1] as long[])
//---------------------------------------------------------------------------------------------------------------------------------------------
      System.out.println("-------Cache Table at Node"+myAddr + "---------")
      for(long []chk: Cache)
      {

          System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3]+" "+ chk[4])
      }
//---------------------------------------------------------------------------------------------------------------------------------------------
      phy << new DatagramReq(to: Address.BROADCAST, protocol: Protocol.USER, data:rreq_pdu)
    })

    add new WakerBehavior(10000, {
      System.out.println(Get_Current_Time())
      Check_Cache_Table(4,5,6)
      System.out.println("-------Cache Table at Node"+myAddr + "---------")
      for(long []chk: Cache)
      {

          System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3]+" "+ chk[4])
      }
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
                 Cache.add([bytes.Rreq_Id, bytes.Source_Id, bytes.Dest_Id,bytes.Source_Seq_No, Get_Current_Time()] as long[])

   //          // [Construct RREP PDU and Unicast it back to source using a different protocol to differentiate between RREQ and RREP]
//--------------------------------------------------------------------------------------------------------------------------------------
                System.out.println("-------Cache Table at Node"+myAddr + "---------")
                for(long []chk: Cache)
                {

                    System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3]+" "+ chk[4])
                }
                System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                for(int []chk: RoutingTable)
                {

                    System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                }
//--------------------------------------------------------------------------------------------------------------------------------------
                def dst_seq = Math.max(bytes.Dest_Seq_No,Update_Source_Seq_No())

            def rrep_pdu = rp_pdu.encode([ Type:2,
                                        Hop_Count:0,
                                        Source_Id:Source_id,
                                        Dest_Id:bytes.Source_Id,
                                        Dest_Seq_No: dst_seq])                         // RREP will contain destination seq. no.

             def prev = Check_Routing_Table(bytes.Source_Id)
             phy << new DatagramReq(to:prev, protocol:Protocol.DATA,data:rrep_pdu)

         }


   //        //else ....check for cache table entry ,
   //        //if seq no is same, check dest node id,  if dest id also same dont forward the packet
   //                                                                             //else forward the packet
         else{

              if(Check_Cache_Table(bytes.Rreq_Id,bytes.Source_Id ,bytes.Dest_Id))    //To avoid Re-Forwarding of same RREQ packet
              {
   //              // Update cache table here for individual Nodes

                Cache.add([bytes.Rreq_Id, bytes.Source_Id, bytes.Dest_Id,bytes.Source_Seq_No, Get_Current_Time()] as long[])
//------------------------------------------------------------------------------------------------------------------------------------------------------
                // if(myAddr ==3){
                // System.out.println("-------Cache Table at Node"+myAddr + "---------")
                // for(int []chk: Cache)
                // {
                //
                //     System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                // }
                // }
                   // Check for routing table , if route to dest is available, send  datagram using phy agent otherwise broadcast again and update backward route
//-----------------------------------------------------------------------------------------------------------------------------------------------------------
                def next = Check_Routing_Table(bytes.Dest_Id)
                // Since RREQ must be forwarded, Construct a Intermediate RREQ with updated Seq no and Hop Count
                def I_rreq_pdu = pdu.encode([ Type:1,
                                              Hop_Count: Update_Hop_Count(bytes.Hop_Count),
                                              Source_Id : bytes.Source_Id,
                                              Dest_Id: bytes.Dest_Id,
                                              Source_Seq_No : bytes.Source_Seq_No,
                                              Dest_Seq_No : bytes.Dest_Seq_No,
                                              Rreq_Id : bytes.Rreq_Id,
                                              G : bytes.G,
                                              D : bytes.D,
                                              U : bytes.U])
                if(next)
                {
                     // Route entry already exist,  forward Intermediate RREQ to Next hop address
                   phy << new DatagramReq(to:Address.next, protocol:Protocol.USER,data:I_rreq_pdu)
                }
                else  // Create backward Route Entry and Broadcast the RREQ further with updated RREQ packet
                {
                   Create_Backward_Route(bytes.Source_Seq_No, bytes.Source_Id, msg.from, bytes.Hop_Count, Update_Hop_Count(bytes.Hop_Count))
//----------------------------------------------------------------------------------------------------------------------------------------
                     // if(myAddr ==3)
                     // {
                     //   System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                     //   for(int []chk: RoutingTable)
                     //   {
                     //     System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                     //   }
                     // }
//------------------------------------------------------------------------------------------------------------------------------------------
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
        def r_bytes =  rp_pdu.decode(msg.data)                //decode PDU here
                                                      //check if current node is the source node of rreq or not
          if(r_bytes.Dest_Id == myAddr)
          {
            Create_Forward_Route(r_bytes.Dest_Seq_No, r_bytes.Source_Id, msg.from, r_bytes.Hop_Count, Update_Hop_Count(r_bytes.Hop_Count))
            System.out.println("Route Found to Destination");

            System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                 for(int []chk: RoutingTable)
                {

                    System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                }

            def dst = Check_Routing_Table(r_bytes.Source_Id)
            // def phantom = data_pdu.encode([ Type:0,
            //                                 Destination: r_bytes.Source_Id,
            //                                 Data:10])
            //  phy << new DatagramReq(to:dst, protocol:Protocol.MAX, data:phantom)
            // System.out.println("-------Routing Table at Node" + myAddr+" ---------")
            //                                 for(int []chk: RoutingTable)
            //                                 {
            //                                   System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
            //                                 }
          }

          else                                                   //since rrep are unicast messages just update routing table, no need to update cache table as reforwarding of rreps won't be there
          {
                                                                  //Create forward route entry
            Create_Forward_Route(r_bytes.Dest_Seq_No, r_bytes.Source_Id, msg.from, r_bytes.Hop_Count, Update_Hop_Count(r_bytes.Hop_Count))
                                                                   // create a intermediate RREP with updated hop count  and sequence no
            def I_rrep_pdu = rp_pdu.encode([ Type:2,
                                          Hop_Count: Update_Hop_Count(r_bytes.Hop_Count),
                                          Source_Id : r_bytes.Source_Id,
                                          Dest_Id :r_bytes.Dest_Id,
                                          Dest_Seq_No : r_bytes.Dest_Seq_No])
//--------------------------------------------------------------------------------------------------------------------------
                                          if(myAddr ==2)
                                          {
                                            System.out.println("-------Routing Table at Node" + myAddr+" ---------")
                                            for(int []chk: RoutingTable)
                                            {
                                              System.out.println(chk[0]+" "+ chk[1] +" "+ chk[2] +" "+ chk[3])
                                            }
                                          }
//--------------------------------------------------------------------------------------------------------------------------
            def prev = Check_Routing_Table(r_bytes.Dest_Id)
            phy << new DatagramReq(to:prev, protocol:Protocol.DATA,data:I_rrep_pdu)

           }
    }
    if(msg instanceof RxFrameNtf && msg.protocol == Protocol.MAX)
    {
      if (myAddr == msg.data[0])
      {
        System.out.println("DAta Reached Succesfully to node " +myAddr + ": " +msg.data[2])
      }
      else if(myAddr != msg.data[0] && msg.from != Check_Routing_Table(msg.data[0]) )
      {
        System.out.println("Node " + myAddr +"here" );
         def dst = Check_Routing_Table(msg.data[0])
         phy<< new DatagramReq(to:dst,protocol:msg.protocol,data:msg.data)
       }


      // def d_bytes = data_pdu.decode(msg.data)
      // System.out.println("Node " + myAddr +"here" );
      // if(myAddr != d_bytes.Destination)
      // {
      //   def dst = Check_Routing_Table(d_bytes.Destination)
      //   def I_data_pdu = data_pdu.encode([Type:0,
      //                           Destination: d_bytes.Destination,
      //                           Data:d_bytes.Data ])
      //   phy << new DatagramReq(to:dst, protocol:msg.protocol,data:I_data_pdu)
      // }
      // else if (myAddr == d_bytes.Destination)
      // {
      //   System.out.println("DAta Reached Succesfully to node " +myAddr + ": " +d_bytes.Data)
      // }
    }
  }
}
