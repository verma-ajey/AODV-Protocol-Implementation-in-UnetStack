import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.nodeinfo.*

// documentation for the 'ping' command
doc['ping'] = '''ping - ping a remote node'''

/*ParameterReq req = new ParameterReq(agentForService(Services.NODE_INFO))
req.get(NodeInfoParam.address)
//req.setIndex(0)
ParameterRsp rsp = (ParameterRsp) agent.request(req, 1000)
//def self = ((Number)rsp.get(NodeInfoParam.address)).intValue()*/
subscribe phy
// add a closure to define the 'ping' command
ping = { addr, count = 1 ->
  println "PING $addr"
  count.times { myAddr ->
    def timeStart = new Date()
    phy << new DatagramReq(to: Address.BROADCAST, protocol: Protocol.USER, data:[addr,myAddr])
    def txNtf = receive(TxFrameNtf, 1000)
    def rxNtf  = receive({it instanceof RxFrameNtf && it.from == addr && it.protocol == Protocol.DATA}, 5000)
    def rxNtf2 = receive({it instanceof RxFrameNtf && it.from != addr && it.protocol == Protocol.DATA}, 5000)
    def rxNtf3 = receive({it instanceof RxFrameNtf && it.protocol == Protocol.USER},5000)
    def rxNtf4 = receive({it instanceof RxFrameNtf && it.from != addr && it.protocol == Protocol.DATA}, 6000)
      if (txNtf && rxNtf && rxNtf.from == addr)
        println "Response from ${rxNtf.from}: time=${(rxNtf.rxTime-txNtf.txTime)/1000} ms"
      else
        println 'Target node is out of Range of Source Node,Trying via middle Nodes in Range'
      if(rxNtf2)
      {
        println "Msg Was Forwarded through ${rxNtf2.from} : time=${(rxNtf2.rxTime-txNtf.txTime)/1000} ms"
        //unsubscribe phy
      }
      if(!rxNtf3 && !rxNtf2 && !rxNtf)
      {
        println "Source Node Is Isolated!"
      }
      if(!rxNtf && !rxNtf2 && !rxNtf4)
      {
        println "Destination Unreachable!"
        //unsubscribe phy
      }
  }
}
