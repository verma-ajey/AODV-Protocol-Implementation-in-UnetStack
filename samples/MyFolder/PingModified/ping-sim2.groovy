//! Simulation: 5-node network
import org.arl.fjage.*
import org.arl.fjage.Agent.*
import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import org.arl.unet.phy.*
import org.arl.unet.phy.Physical.*
import org.arl.unet.net.*
import org.arl.unet.*
import org.arl.unet.DatagramReq
import org.arl.unet.net.Router
import org.arl.unet.Services

platform = RealTimePlatform
channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps           // c
channel.communicationRange = 100.m     // Rc
channel.detectionRange = 100.m         // Rd
channel.interferenceRange = 100.m      // Ri
channel.pDetection = 1                  // pd
channel.pDecoding = 1                   // pc

simulate {
  def n1 = node '1', address: 1, location: [0.m, 0.m, 0.m], shell: true, stack:"$home/samples/MyFolder/PingModified/initrc-stack"

  n1.startup = {
    def router = agentForService org.arl.unet.Services.ROUTING
    router.send new RouteDiscoveryNtf(to:4,nextHop:4)
    router.send new RouteDiscoveryNtf(to:2,nextHop:2)
    router.send new RouteDiscoveryNtf(to:3,nextHop:3)
    router.send new RouteDiscoveryNtf(to:5,nextHop:5)

  }
  def n2 =node '2', address: 2, location: [70.m, 0.m, 0.m], shell:5102, stack: "$home/samples/MyFolder/PingModified/initrc-stack"
  n2.startup = {
    def router = agentForService org.arl.unet.Services.ROUTING
    router.send new RouteDiscoveryNtf(to:4,nextHop:4)
    router.send new RouteDiscoveryNtf(to:1,nextHop:1)
    router.send new RouteDiscoveryNtf(to:5,nextHop:5)
    router.send new RouteDiscoveryNtf(to:3,nextHop:1)
    //router.send new RouteDiscoveryNtf(to:3,nextHop:4)
    //router.send new RouteDiscoveryNtf(to:3,nextHop:5)
  }
  def n3 = node '3', address: 3, location: [-70.m, 0.m, 0.m], shell: 5103, stack:"$home/samples/MyFolder/PingModified/initrc-stack"
  n3.startup = {
    def router = agentForService org.arl.unet.Services.ROUTING
    router.send new RouteDiscoveryNtf(to:4,nextHop:4)
    router.send new RouteDiscoveryNtf(to:1,nextHop:1)
    router.send new RouteDiscoveryNtf(to:5,nextHop:5)
    //router.send new RouteDiscoveryNtf(to:2,nextHop:4)
    router.send new RouteDiscoveryNtf(to:2,nextHop:1)
    //router.send new RouteDiscoveryNtf(to:2,nextHop:5)
  }
  def n4 = node '4', address: 4, location: [0.m, 70.m, 0.m], shell: 5104, stack:"$home/samples/MyFolder/PingModified/initrc-stack"
  n4.startup = {
    def router = agentForService org.arl.unet.Services.ROUTING
    router.send new RouteDiscoveryNtf(to:1,nextHop:1)
    router.send new RouteDiscoveryNtf(to:2,nextHop:2)
    router.send new RouteDiscoveryNtf(to:3,nextHop:3)
    router.send new RouteDiscoveryNtf(to:5,nextHop:1)
    //router.send new RouteDiscoveryNtf(to:5,nextHop:2)
    //router.send new RouteDiscoveryNtf(to:5,nextHop:3)
  }
  def n5 = node '5', address: 5, location: [0.m, -120.m, 0.m], shell: 5105, stack:"$home/samples/MyFolder/PingModified/initrc-stack"
  n5.startup = {
    def router = agentForService org.arl.unet.Services.ROUTING
    router.send new RouteDiscoveryNtf(to: 1 , nextHop:1)
    router.send new RouteDiscoveryNtf(to: 3 , nextHop:3)
    router.send new RouteDiscoveryNtf(to: 2 , nextHop:2)
    router.send new RouteDiscoveryNtf(to: 4 , nextHop:1)
    //router.send new RouteDiscoveryNtf(to: 4 , nextHop:2)
    //router.send new RouteDiscoveryNtf(to: 4 , nextHop:3)
  }
}