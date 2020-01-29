import org.arl.fjage.Message
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*

class PingDaemon extends UnetAgent {
  
  final static int PING_PROTOCOL = Protocol.USER
  int addr
  void startup() {
    def phy = agentForService Services.PHYSICAL
    subscribe topic(phy)
    def router = agentForService Services.ROUTING
    subscribe topic(router)

    def noded = agentForService Services.NODE_INFO
    addr = noded.address
    println"${addr},hiiiiiiiiiiiiiiiii"
   // ParameterReq req = new ParameterReq(agentForService(Services.NODE_INFO))
  }

  void processMessage(Message msg) {
    if (msg instanceof RxFrameNtf && msg.protocol == PING_PROTOCOL && addr == msg.to)
       router << new DatagramReq(to:msg.from,protocol:Protocol.DATA)

    // if(msg instanceof DatagramNtf && msg.protocol == PING_PROTOCOL && addr == msg.data[0])
    // 	send new DatagramReq(recipient: msg.sender, to: msg.from, protocol: Protocol.DATA, data:[msg.data[0],msg.data[1]])

    // if(msg instanceof DatagramNtf && msg.protocol == Protocol.DATA && addr != msg.data[1])
    // 		send new DatagramReq(recipient: msg.sender, to: Address.BROADCAST, protocol: Protocol.DATA, data:[msg.data[0],msg.data[1]])
    }
}