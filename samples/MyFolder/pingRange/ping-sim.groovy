//! Simulation: 3-node network with ping daemons
///////////////////////////////////////////////////////////////////////////////
///
/// To run simulation:
///   bin/unet samples/ping/ping-sim
///
///////////////////////////////////////////////////////////////////////////////

import org.arl.fjage.RealTimePlatform
import org.arl.unet.sim.channels.*
import org.arl.unet.phy.*


///////////////////////////////////////////////////////////////////////////////
// display documentation

println '''
3-node network with ping daemons
--------------------------------

You can interact with node 1 in the console shell. For example, try:
> ping 2
> help ping

When you are done, exit the shell by pressing ^D or entering:
> shutdown
'''

///////////////////////////////////////////////////////////////////////////////
// simulator configuration

platform = RealTimePlatform

channel.model = ProtocolChannelModel
channel.soundSpeed = 1500.mps           // c
channel.communicationRange = 1000.m     // Rc
channel.detectionRange = 1800.m         // Rd
channel.interferenceRange = 3000.m      // Ri
channel.pDetection = 1                  // pd
channel.pDecoding = 1                   // pc
// run simulation forever
simulate {
  node '1', address: 1, location: [0.km, 0, 0], shell: true, stack: { container ->
    container.add 'ranging', new Ranging() 
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    container.shell.addInitrc "${script.parent}/fshrc.groovy"
  }
  node '2', address: 2, location: [1.km, 0, 0], shell:5102, stack: { container ->
    container.add 'ping', new PingDaemon()
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
  }
  node '3', address: 3, location: [2.km, 0, 0], shell: 5103, stack: { container ->
    container.add 'ping', new PingDaemon()
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
  }
  node '4', address: 4, location: [3.km, 0, 0], shell: 5104, stack: { container ->
    container.add 'ping', new PingDaemon()
    container.add 'ranging', new Ranging()
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
  }
}
