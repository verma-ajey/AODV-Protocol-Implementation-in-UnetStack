import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.RouteDiscoveryProtocol

def rdpp = agentForService Services.ROUTE_MAINTENANCE
subscribe rdpp

def req = new RouteDiscoveryReq(6)

if(it instanceof RouteDiscoveryNtf)
{
	List<Integer> Arr = it.getRoute()
	for(int i =0;i<Arr.size(); i++)
	{
		println "${Arr.get(i)}"
	}
}
