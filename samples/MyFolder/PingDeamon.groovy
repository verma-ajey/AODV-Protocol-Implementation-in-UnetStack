import org.arl.fjage.*
import org.arl.unet.*

class PingDeamon extends UnetAgent{
	final static int PING_PROTOCOL = Protocol.USER
	void startup()
	{
		def phy = agentForService Services.PHYSICAL
		subscribe topic(phy)
	}

	void processMessage(Message msg)
	{
		if(msg instanceof DatagramNtf && msg.protocol == PING_PROTOCOL)
			send new DatagramReq(recipent : msg.sender, to : msg.from, protocol: Protocol.DATA)
	}
}