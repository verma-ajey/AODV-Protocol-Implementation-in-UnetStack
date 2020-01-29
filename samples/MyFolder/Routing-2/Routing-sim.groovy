//! Simulation: 5-node network with route Discovery

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.*
import org.arl.unet.phy.*
import org.arl.unet.net.*


///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform
channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps           // c
channel.communicationRange = 100.m     // Rc
channel.detectionRange = 100.m         // Rd
channel.interferenceRange = 100.m      // Ri
channel.pDetection = 1                  // pd
channel.pDecoding = 1                   // pc
// run simulation forever
  simulate {
  node '1', address: 1, location: [0.m, 0, 0], shell: true, stack: "$home/etc/initrc-stack"
  //node '2', address: 2, location: [99.m, 0, 0], shell:5102, stack:"$home/etc/initrc-stack"
  node '3', address: 3, location: [0, 99.m, 0], shell: 5103, stack: "$home/etc/initrc-stack"
  node '4', address: 4, location: [0, -99.m, 0], shell: 5104, stack: "$home/etc/initrc-stack"
  node '5', address: 5, location: [-51.m, -70.m, 0], shell: 5105, stack: "$home/etc/initrc-stack"
  node '6', address: 6, location: [51.m, -70.m, 0], shell: 5106, stack: "$home/etc/initrc-stack"
  node '7', address: 7, location: [90.m, 108.m, 0], shell: 5107, stack: "$home/etc/initrc-stack"
  
}
s