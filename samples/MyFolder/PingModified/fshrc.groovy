import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.*
import org.arl.unet.net.RouteDiscoveryProtocol
import org.arl.unet.services.*

// documentation for the 'ping' command
doc['ping'] = '''ping - ping a remote node'''


ping = { addr, count = 1 ->
  println "PING $addr"
  count.times
  { 

    def node = agentForService(Services.NODE_INFO)
    myAddr = node.address

    def timeout = 20000
    // def i = 5
    // while(i--){
    //phy << new DatagramReq(to:8,protocol:Protocol.DATA)
    router << new DatagramReq(to:addr,protocol:Protocol.USER)
    def x=currentTimeMillis();
    
    //def txNtf = receive(TxFrameNtf,2000)
    def rxNtf = receive({it instanceof RxFrameNtf && it.to == myAddr },timeout)
    
    def flag =0;
    
    while(NTF2 = receive({it instanceof DatagramNtf},timeout))
    {
      if(NTF2.to == myAddr)
      {
        println "Destination Reachable: Msg Was Forwarded through ${rxNtf.from}: time=${(rxNtf.rxTime-x)/1000} ms"
        flag = 1
        break;
      }
    }
    if(flag == 0)
    {
      println "Destination Unrechable"
    }
   //}
  }
}




    /*ParameterReq req = new ParameterReq(agentForService(Services.NODE_INFO))
req.get(NodeInfoParam.address)
//req.setIndex(0)
ParameterRsp rsp = (ParameterRsp) agent.request(req, 1000)
//def self = ((Number)rsp.get(NodeInfoParam.address)).intValue()*/
//subscribe phy
// add a closure to define the 'ping' command
//subscribe rdp

    // while(NTF = receive({it instanceof }))
    // def flag =0;
    // rdp << new RouteDiscoveryReq(to:addr)
    // timeout = 30000
    // n = []
    // while(NTF = receive({it instanceof RouteDiscoveryNtf},timeout))
    // {
    //   if(NTF.to == addr)
    //   {
    //     n << NTF.getRoute()
    //     println "Destination Is Reachable Through Route : ${n}"
    //     flag = 1;
    //     //unsubscribe rdp
    //     break;
    //   }
    // }
    // if(flag==0)
    // {
    //   println "Destination Unreachable!"
    // }


    //def router = agentForService(Services.ROUTING)
    //router<< new DatagramReq(to:addr)
    //def rxntf = receive({it instanceof RouteDiscoveryNtf}, 10000)
    /*def txNtf = receive(TxFrameNtf, 1000)
    def rxNtf  = receive({ it instanceof RxFrameNtf && it.from == addr && it.protocol == Protocol.DATA}, 5000)
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
      }*/
      /*if(rxntf)
        println "Destination Node Reachable"
      else
        println "Destination Unreachable"*/
