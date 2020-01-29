import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.nodeinfo.*
import org.arl.fjage.Behavior.*
import groovy.transform.*;
//@TypeChecked
//@CompileStatic



class DummyAgent extends UnetAgent {
  final static int PING_PROTOCOL = Protocol.USER
  def myntf;
  def addr;
  int dummyValue = 500;

  List<Parameter> getParameterList() {
    allOf(MyAgentParameters)
  }

class DebugReq extends org.arl.fjage.Message {
  DebugReq() {
    super(Performative.REQUEST)
  }
  String msg;
}

void startup() {
    def phy = agentForService Services.PHYSICAL
    def link = agentForService Services.LINK
    def nodeInfo = agentForService Services.NODE_INFO
    def shellAgent = agent("shell");
    
    addr = get(nodeInfo, NodeInfoParam.address)
    
    subscribe (link)
    
    add new TickerBehavior(1.second, {
      if (addr == 1) {
        link << new DatagramReq(to: 2, data: [1,2,3], reliability: true)
        shellAgent << new DebugReq(msg: "HI WORLD");
      }
      //log.warning(addr+'Hello earth!!!');
    })
  }

  Message processRequest(Message msg) {
    return msg
  }

  void processMessage(Message msg) {
    
    if (msg instanceof DatagramDeliveryNtf) {
      println(addr+" ReceivedMsg "+msg.to);  
    }
/*    if (msg instanceof DatagramNtf && msg.protocol == PING_PROTOCOL) {
      request new DatagramReq(recipient: msg.sender, to: msg.from, protocol: Protocol.DATA);
    } else if (msg instanceof DatagramDeliveryNtf) {
      myntf = msg;
      println("RECEIVEDDDNMSG");
    }*/
  }
}
