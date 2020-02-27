import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.*
org.arl.unet.phy.TxFrameNtf
import org.arl.unet.net.RouteDiscoveryProtocol
import org.arl.unet.services.*

// documentation for the 'aodv route discovery' phase
doc['aodv_initiate'] = '''Route Discovery using AODV Protocol'''


initiate = { addr, count = 1 ->
  println "FINDING ROUTE TO $addr"
  count.times
  {
            //intiate << new RouteDiscoveryReq(to:4)
  }
}
