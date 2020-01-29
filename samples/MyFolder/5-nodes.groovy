//! Simulation: 3-node network with ping daemons

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.*
import org.arl.unet.phy.*
import org.arl.unet.net.*
import org.arl.unet.link.*
import org.arl.unet.mac.aloha.*


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
  node '1', address: 1, location: [0.m, 0.m, 0.m], shell: true, stack: { container ->
    container.add 'ranging', new Ranging() 
    container.add 'rdp', new RouteDiscoveryProtocol();
    container.add 'router', new Router();
    //container.add 'link', new ReliableLink();
  }
  node '2', address: 2, location: [70.m, 0.m, 0.m], shell:5102, stack: { container ->
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    //container.add 'link', new ReliableLink();
  }
  node '3', address: 3, location: [-70.m, 0.m, 0.m], shell: 5103, stack: { container ->
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    //container.add 'link', new ReliableLink();
  }
  node '4', address: 4, location: [0.m, 70.m, 0.m], shell: 5104, stack: { container ->
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    //container.add 'link', new ReliableLink();
  }
  node '5', address: 5, location: [0.m, -70.m, 0.m], shell: 5105, stack: { container ->
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    //container.add 'link', new ReliableLink();
  }
}
