//! Simulation: of void

import org.arl.fjage.*
import org.arl.unet.sim.channels.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.mac.*
import org.arl.unet.net.Router.*
import org.arl.unet.link.*
import org.arl.fjage.RealTimePlatform.*
import org.arl.unet.phy.Ranging.*

channel.model = ProtocolChannelModel
platform = org.arl.fjage.RealTimePlatform


channel.soundSpeed = 1500.mps           // c
channel.communicationRange = 100.m      // Rc
channel.detectionRange = 2500.m         // Rd
channel.interferenceRange = 3000.m      // Ri
channel.pDetection = 1                  // pd
channel.pDecoding = 1                   // pc

//def T = 5.mins  

// run the simulation infinately
simulate {
    node '1', remote: 1101, address: 1, location: [ 0.m, 0.m, 0.m], shell: true, stack: { container ->
    container.shell.addInitrc "${script.parent}/sink.groovy"
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    container.add 'link', new org.arl.unet.link.ReliableLink()
   // container.add 'mac', new MySimplestMac();
    //container.add 'load', new LoadGenerator(1, 0.01);
    }
  
    node '2', remote: 1102, address: 2, location: [ 0.m, 0.m, -35.m], shell: 5102, stack: { container ->
    //container.add 'node_agent', new node_agent();
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();   
    container.add 'router', new org.arl.unet.net.Router();
    container.add 'link', new org.arl.unet.link.ReliableLink()
   // container.add 'mac', new MySimplestMac();
   // container.add 'load', new LoadGenerator(1, 0.01);

    }
    
   
    node '3', remote: 1103, address: 3, location: [0.m, 0.m, -75.m], shell: 5103, stack: { container ->
    //container.add 'node_agent', new node_agent();    
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();
    container.add 'router', new org.arl.unet.net.Router();
    container.add 'link', new org.arl.unet.link.ReliableLink()
    //container.add 'mac', new MySimplestMac();
    //container.add 'load', new LoadGenerator(1, 0.01);

    }
   
    node '4', remote: 1104, address: 4, location: [0.m, 0.m, -120.m], shell: 5104, stack: {container ->
    //container.add 'node_agent', new node_agent();
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();    
    container.add 'router', new org.arl.unet.net.Router();
    container.add 'link', new org.arl.unet.link.ReliableLink()
    //container.add 'mac', new MySimplestMac();
    //container.add 'load', new LoadGenerator(1, 0.01);

   }

    node '5', remote: 1105, address: 5, location: [0.m, 99.m, -118.m], shell: 5105, stack: {container ->
    //container.add 'node_agent', new node_agent();
    container.add 'rdp', new org.arl.unet.net.RouteDiscoveryProtocol();    
    container.add 'router', new org.arl.unet.net.Router();
    container.add 'link', new org.arl.unet.link.ReliableLink()
    //container.add 'mac', new MySimplestMac();
   // container.add 'load', new LoadGenerator(1, 0.01);

   }
}

