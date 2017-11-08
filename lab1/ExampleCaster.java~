
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
     Queue<String> msgHistory = new LinkedList<String>();
	 ArrayList<TreeMap<Integer,String>> msgHistory2 = new ArrayList<TreeMap<Integer, String>>();
     int delivered  = 1;
     int myClock,maxProp,maxSelf = 0;
     int cHosts=3;
    /**
     * No initializations needed for this simple one
     */
    public void init() {
        mcui.debug("The network has "+hosts+" hosts!");
		cHosts=hosts;
		for (int i=0;i<hosts;i++){
    		clock.add(0);
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
						//msgHistory.put(((ExampleMessage)message).proposed,((ExampleMessage)message).text);
						msgHistory2.get(id).put((agrSeq.get(msgClock-1)),((ExampleMessage)message).text);
						deliver(agrSeq.get(msgClock-1),id);
					}							
				}
				else{
					System.out.println("Too many proposals"+ propCount);
				}
			}
			else { 	// Receiving  the agreed Sequence
				//msgHistory.put(((ExampleMessage)message).proposed,((ExampleMessage)message).text);
				msgHistory2.get(sender).put(((ExampleMessage)message).proposed,((ExampleMessage)message).text);
				deliver(((ExampleMessage)message).proposed,sender);	
			}
		}
		if (myClock< 9){
		    System.out.println(clock);
		}
		else {
		    System.out.println(msgHistory2.get(id).entrySet());
		}
    }

	public void deliver(int seq,int ident){		
		Integer min = 1;
		int j = ident;
		boolean found = false;
		while (min <=seq){
			for(int i=0;i<cHosts;i++){
				for(Map.Entry<Integer,String> entry : msgHistory2.get(i).entrySet()){
					if (entry.getKey()<= min){
						j = i;
						min = entry.getKey();
						msgHistory.add(msgHistory2.get(j).remove(min));
						found= true;	
					}
					else{
						break;
					}
				}
			}
			if (!msgHistory2.get(j).containsKey(min) && found == true ){
				for(int i=0;i<=msgHistory.size();i++){
					mcui.deliver(i, msgHistory.poll(), "from"+ Integer.toString(i));
					System.out.println("Message Seq:"+ min);
				}
				min = min++;
				found = false;
			}					
			else{
				min = min +1;				
			}
		}
	}



	/*public void deliver(int seq,int ident){
		boolean found = true;
		while (delivered <= seq)	
		for(int i=0;i<cHosts;i++){
			for(Map.Entry<Integer,String> entry : msgHistory2.get(i).entrySet()){
				if (entry.getKey()== delivered){
					msgHistory.add(msgHistorymsgHistory2.get(i).remove(delivered));
				}
				else{
					break;
				}
		if (msgHistory2.get(i).containsKey(delivered)){
			mcui.deliver(i, msgHistory2.get(i).remove(delivered), "from"+ Integer.toString(i));
			System.out.println("Message Seq:"+ delivered);
			found = true;
					}						
				}
			}
			if (i==cHosts && found){
				delivered++;
			}
		}					
	}*/


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
}
