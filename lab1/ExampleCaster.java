
import mcgui.*;
import java.util.*;

/**
 * Simple example of how to use the Multicaster interface.
 *
 * @author Andreas Larsson &lt;larandr@chalmers.se&gt;
 */
public class ExampleCaster extends Multicaster {
     ArrayList<Integer> clock = new ArrayList<Integer>();
     ArrayList<Integer> propCount = new ArrayList<Integer>();
     ArrayList<Integer> agrSeq = new ArrayList<Integer>();
	 TreeMap<Integer,String> msgHistory3 = new TreeMap<Integer,String>();
	 ArrayList<Integer> msgRcv = new ArrayList<Integer>();
	 ArrayList<TreeMap<Integer,String>> msgHistory2 = new ArrayList<TreeMap<Integer, String>>();
     int delivered  = 19;
     int myClock,maxProp,maxSelf,liveliness = 0;
     int cHosts=3;
    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
		cHosts=hosts;
		for (int i=0;i<hosts;i++){
    		clock.add(0);
			msgRcv.add(0);
			msgHistory2.add(new TreeMap<Integer, String>());
		}
    }

    /**
     * The GUI calls this module to multicast a message
     */
    public void cast(String messagetext) {
	int propSeq = 0;
	myClock++;
	clock.set(id,myClock);
        propCount.add(0);
	agrSeq.add(myClock);
        for(int i=0; i < hosts; i++) {
            /* Sends to everyone except itself */
            if(i != id) {
                bcom.basicsend(i,new ExampleMessage(id, messagetext,myClock,propSeq));
            }
        }
        mcui.debug("Sent out: \""+messagetext+"\"");
    }

    /**
     * Receive a basic message
     * @param message  The message received
     */

    public void basicreceive(int peer,Message message) {
		int msgClock = ((ExampleMessage)message).timestamp;
		int sender = message.getSender();
		int max = 0;
		// We propose a sequence number for the message		
		if (((ExampleMessage)message).proposed == 0 ){
			if (msgClock < maxSelf){// Calculating proposal
					max = maxSelf;
					maxSelf++;				
			}
			if (max<maxProp){
				max=maxProp;
			}
			bcom.basicsend(sender,new ExampleMessage(sender, ((ExampleMessage)message).text,((ExampleMessage)message).timestamp,max+1));
			clock.set(sender,msgClock);
			maxProp= max+1;
		}
		else{ 
			System.out.println(sender);
			if (sender == id ){ // Receiving a proposal, Calculating and  Sending Agreeed sequence
				if (propCount.get(msgClock-1) < hosts-1){
					if (((ExampleMessage)message).proposed >= agrSeq.get(msgClock-1)){				
					agrSeq.set(msgClock-1,((ExampleMessage)message).proposed);
					}
				propCount.set(msgClock-1,(propCount.get(msgClock-1))+1);
				System.out.println("PropCount: "+ propCount);
				if (propCount.get(msgClock-1) == cHosts -1){
						if (agrSeq.get(msgClock-1) <= maxSelf){
							maxSelf= maxSelf+1;
							agrSeq.set(msgClock-1,maxSelf);							
						}
						for(int i=0; i < hosts; i++) {
            					/* Sends to everyone except itself */
		        			if(i != id) {
		           				bcom.basicsend(i,new ExampleMessage(id, ((ExampleMessage)message).text,((ExampleMessage)message).timestamp,agrSeq.get(msgClock-1)));
								maxSelf = agrSeq.get(msgClock-1);
		        			}
						}
						System.out.println("agrSeq: " + agrSeq);
						maxProp = agrSeq.get(msgClock-1);
						msgHistory2.get(id).put((agrSeq.get(msgClock-1)),((ExampleMessage)message).text);
						msgRcv.set(id,1);
						deliver(agrSeq.get(msgClock-1),id);
					}							
				}
				else{
					System.out.println("Too many proposals"+ propCount);
				}
			}
			else { 	// Receiving  the agreed Sequencem
				msgHistory2.get(sender).put(((ExampleMessage)message).proposed,((ExampleMessage)message).text);
				msgRcv.set(sender,1);
				deliver(((ExampleMessage)message).proposed,sender);	
			}
		}
		if (myClock< 9){
		    System.out.println(clock);
		}
		else {
		    System.out.println(msgHistory3.entrySet());
		}
    }

public void deliver(int seq,int ident){
		int k = Integer.valueOf(String.valueOf(seq) + String.valueOf(ident));
		int j,key = 0;
		msgHistory3.put(k,msgHistory2.get(ident).remove(seq));
		liveliness++;
		if (liveliness == arraySum(clock)){
			try{
				while (msgHistory3.size()>0){
					j = msgHistory3.firstKey()%10;
					key = msgHistory3.firstKey();
					mcui.deliver(key, msgHistory3.remove(key), "from: "+ Integer.toString(j));
					System.out.println("Message Seq:"+ key);
					System.out.println("Delivery counter:"+ delivered);
				}
			 }catch (NoSuchElementException e){
				System.out.println("MsgHistory Empty");			
			}
		}						
	}


















	/*public void deliver(int seq,int ident){
		int k = Integer.valueOf(String.valueOf(seq) + String.valueOf(ident));
		int j,key = 0;
		msgHistory3.put(k,msgHistory2.get(ident).remove(seq));
			try{
				if (msgHistory3.firstKey() > delivered){
					liveliness++;
				
					if ((msgHistory3.size() > myClock*2 || msgHistory3.size() > 10) && liveliness> 7){
						if(arraySum(msgRcv) ==cHosts){
							delivered = msgHistory3.firstKey()+7;
							System.out.println("Adjusting Delivery number For liveliness");
						}
					}
				}
					while (msgHistory3.firstKey() <= delivered){
						j = msgHistory3.firstKey()%10;
						key = msgHistory3.firstKey();
						mcui.deliver(key, msgHistory3.remove(key), "from: "+ Integer.toString(j));
						liveliness = 0;
						System.out.println("Message Seq:"+ key);
						System.out.println("Delivery counter:"+ delivered);
						if (msgHistory3.size() > 0){
							if (msgHistory3.firstEntry().getKey() > delivered){
								delivered = delivered+10;
							}
							else {
								System.out.println("More Messages to be delivered");
							}
						}
						else {
							System.out.println("More Messages to be delivered");
							if ((delivered - key) < 13){
								delivered = delivered+10;
							}	
						}				
					}
			 }catch (NoSuchElementException e){
							
			}						
	}					
	*/


    /**
     * Signals that a peer is down and has been down for a while to
     * allow for messages taking different paths from this peer to
     * arrive.
     * @param peer	The dead peer
     */
    public void basicpeerdown(int peer) {
        mcui.debug("Peer "+peer+" has been dead for a while now!");
		cHosts = cHosts -1;
    }

 
	public int arraySum( ArrayList<Integer> list){
	int sum=0;
		for (int i : list){
			sum=sum+i;
		}
	return sum;
	}
}
