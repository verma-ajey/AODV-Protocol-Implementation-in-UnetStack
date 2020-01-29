//! Simulation: AODV- 8 node Simulation
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.net.*
import org.arl.fjage.Agent.*
import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.*
import org.arl.unet.sim.channels.*
import org.arl.unet.phy.*
import org.arl.unet.phy.Physical.*
import org.arl.unet.DatagramReq
import org.arl.unet.net.Router
import org.arl.unet.Services
import org.arl.unet.Services.*

platform = RealTimePlatform
channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps           // c
channel.communicationRange = 100.m     // Rc
channel.detectionRange = 100.m         // Rd
channel.interferenceRange = 100.m      // Ri
channel.pDetection = 1                  // pd
channel.pDecoding = 1                   // pc

simulate {
  def n1 = node '1', address: 1, location: [0.m, 0.m, 0.m], shell: true, stack:"$home/etc/setup"

  def n2 = node '2', address: 2, location: [0.m, 100.m, 0.m], shell: true, stack:"$home/etc/setup"

  def n3 = node '3', address: 3, location: [0.m, 200.m, 0.m], shell: true, stack:"$home/etc/setup"

  def n4 = node '4', address: 4, location: [0.m, 300.m, 0.m], shell: true, stack:"$home/etc/setup"

}
