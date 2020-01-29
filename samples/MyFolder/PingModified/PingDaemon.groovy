import org.arl.fjage.Message
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.Router

class PingDaemon extends UnetAgent {
  
  final static int PING_PROTOCOL = Protocol.USER
  int addr
  void startup() {
    def noded = agentForService Services.NODE_INFO
    addr = noded.address

    def router = agentForService Services.ROUTING
    subscribe topic(router)
  }

  void processMessage(Message msg) {
    def router = agentForService Services.ROUTING
    if (msg instanceof DatagramNtf && msg.protocol == Protocol.USER && msg.to == addr){
       router << new DatagramReq(to:msg.from, protocol:Protocol.DATA)
     }

  }
}