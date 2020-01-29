import java.util.*
import java.util.*
import java.lang.*
import java.io.*
import org.arl.fjage.*
import org.arl.unet.*
import org.arl.unet.phy.*
import org.arl.unet.services.*
import org.arl.unet.nodeinfo.*
import org.arl.unet.net.Router
import org.arl.unet.PDU

class MyAgent extends UnetAgent{
  List<int[]> rout = new ArrayList<int[]>();
  def flag =0;
  void startup(){

  def nodeinf = agentForService Services.NODE_INFO
  subscribe topic(nodeinf)
  def myAddr = nodeinf.address
  rout.add([flag++,flag++,flag++,flag++] as int[]);
  rout.add([5,6,7,8] as int[]);
  rout.add([9,10,11,12] as int[]);
  rout.add([13,14,15,16] as int[]);

  if(myAddr == 1){
  for(int []row : rout)
  {
    if(row[0] == 5 ){
      System.out.println("HIIII")
    }
  }
}
}
}
