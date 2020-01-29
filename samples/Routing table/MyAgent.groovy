import java.util.*
import java.lang.*;
import java.io.*;
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

  rout.add(flag++,flag++,flag++,flag++);
  rout.add(new int[] {flag++,flag++,flag++,flag++)};
  rout.add(new int[] {flag++,flag++,flag++,flag++});
  rout.add(new int[] {flag++,flag++,flag++,flag++});

  for(int []row : rout)
  {
    System.out.println(row[0]);
  }
}
}
