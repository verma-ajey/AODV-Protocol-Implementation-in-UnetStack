import org.arl.fjage.Message
import org.arl.unet.*
import org.arl.unet.net.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*

class MyAgent extends UnetAgent {
  
  final static int PING_PROTOCOL = Protocol.USER
  int addr
  void startup() {
    def phy = agentForService Services.PHYSICAL
    subscribe topic(phy)

    def noded = agentForService Services.NODE_INFO
    addr = noded.address
 
  }

  void processMessage(Message msg) {/*
    if(msg instanceof RouteDiscoveryNtf )
      system.out.println "Dest Reachable"
    else
      system.out.println "Unreachable"


      /*
      add new WakerBehavior(rnd.next(2), {
        //wait for 0,1,2 second everytime 
      })
    /*if (msg instanceof DatagramNtf && msg.protocol == PING_PROTOCOL && addr != msg.data[0])
      send new DatagramReq(recipient: msg.sender, to: Address.BROADCAST, protocol: Protocol.USER, data:[msg.data[0],msg.data[1]])

    if(msg instanceof DatagramNtf && msg.protocol == PING_PROTOCOL && addr == msg.data[0])
    	send new DatagramReq(recipient: msg.sender, to: msg.from, protocol: Protocol.DATA, data:[msg.data[0],msg.data[1]])

    if(msg instanceof DatagramNtf && msg.protocol == Protocol.DATA && addr != msg.data[1])
    		send new DatagramReq(recipient: msg.sender, to: Address.BROADCAST, protocol: Protocol.DATA, data:[msg.data[0],msg.data[1]])
    */}
}