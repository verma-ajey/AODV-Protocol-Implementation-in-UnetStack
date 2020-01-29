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
    int phantom = 15
    
    def node = agentForService(Services.NODE_INFO)
    myAddr = node.address

    def timeout = 20000

    phy << new DatagramReq(to:phantom,protocol:Protocol.DATA)
    router << new DatagramReq(to:addr,protocol:Protocol.USER)
    
    def txNtf = receive(TxFrameNtf,2000)
    def rxNtf = receive({it instanceof RxFrameNtf && it.to == myAddr },timeout)
    
    def flag =0;
    
    while(NTF2 = receive({it instanceof DatagramNtf},timeout))
    {
      if(NTF2.to == myAddr)
      {
        println "Destination Reachable: Msg Was Forwarded through ${rxNtf.from}: time=${(rxNtf.rxTime-txNtf.txTime)/1000} ms"
        flag = 1
        break;
      }
    }
    if(flag == 0)
    {
      println "Destination Unrechable"
    }
  }
}