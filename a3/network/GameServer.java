package a3.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.UUID;

import sage.networking.server.GameConnectionServer;
import sage.networking.server.IClientInfo;

public class GameServer extends GameConnectionServer<UUID>{
	private int indice;
	public GameServer(int localPort) throws IOException {
		super(localPort, ProtocolType.TCP);
		indice = 0;
		System.out.println(getIP()+"\n Port: "+localPort);	//displays valid IP address for clients to connect to
	}
	//This method taken from http://stackoverflow.com/questions/8083479/java-getting-my-ip-address
	public String getIP(){
		String ip="";
	    try {
	        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
	        while (interfaces.hasMoreElements()) {
	            NetworkInterface iface = interfaces.nextElement();
	            // filters out 127.0.0.1 and inactive interfaces
	            if (iface.isLoopback() || !iface.isUp())
	                continue;
	            
	            Enumeration<InetAddress> addresses = iface.getInetAddresses();
	            while(addresses.hasMoreElements()) {
	                InetAddress addr = addresses.nextElement();
	                ip = addr.getHostAddress();
	                System.out.println(iface.getDisplayName() + " " + ip);
	            }
	        }
	    } catch (SocketException e) {
	        throw new RuntimeException(e);
	    }
	    return ip;
	}
	//Accepts any new clients
		public void acceptClient(IClientInfo ci, Object o){
			String message = (String)o;
			String[] messageTokens=message.split(","); //splits string into its separate arguments/substrings
			
			if(messageTokens.length>0){
				if(messageTokens[0].compareTo("join")==0){
					UUID clientID = UUID.fromString(messageTokens[1]);
					addClient(ci,clientID);
					sendJoinedMessage(clientID, true);
					System.out.println("Client added: "+clientID);
				}
			}
		}
	public void sendJoinedMessage(UUID clientID, boolean success){
		try{
			String message = new String("join,");
			if(success){
				indice++;
				if(indice>5){
					indice=0;
				}
				message+= "success";
				message+= ","+indice;
			}
			else{
				message+="failure";
			}
			sendPacket(message,clientID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//PACKET PROCESSING
	public void processPacket(Object o, InetAddress senderIP, int sndPort){
		String message = (String)o;
		String[] msgTokens = message.split(",");
		if(msgTokens.length>0){
			//CREATE
			if(msgTokens[0].compareTo("create")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				String[] pos = {msgTokens[2],msgTokens[3],msgTokens[4]};
				sendCreateMessages(clientID, pos, msgTokens[5],msgTokens[6]);
				if(!Boolean.valueOf(msgTokens[5])){
					sendWantsDetailsMessages(clientID);	//Only wants details if they aren't an NPC
				}
			}
			//BYE
			if(msgTokens[0].compareTo("bye")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				removeClient(clientID);
				System.out.println("Client removed: "+clientID);
				sendByeMessages(clientID);
			}
			//DETAILS
			if(msgTokens[0].compareTo("details")==0){
				sendDetails(msgTokens);
			}
			//WANTSDETAILS
			if(msgTokens[0].compareTo("wantsDetails")==0){
				sendWantsDetailsMessages(UUID.fromString(msgTokens[1]));
			}
			//BULLETCREATE
			if(msgTokens[0].compareTo("bulletCreate")==0){
				sendBulletCreateMessages(msgTokens);
			}
			//WON
			if(msgTokens[0].compareTo("won")==0){
				UUID clientID = UUID.fromString(msgTokens[1]);
				sendLostMessages(clientID);
			}
		}
	}
	//Send create messages
	public void sendCreateMessages(UUID clientID, String[] pos, String isNPC, String modelNum){
		try{
			String message = new String("create,"+clientID.toString());
			message+=","+pos[0];
			message+=","+pos[1];
			message+=","+pos[2];
			message+=","+isNPC;
			message+=","+modelNum;
			forwardPacketToAll(message,clientID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//Send details
	public void sendDetails(String[] msgTokens){
		try{
			String message = new String("details,"+msgTokens[1]+","+msgTokens[3]+","+msgTokens[4]+","+msgTokens[5]+","+msgTokens[6]+","+msgTokens[7]+","+msgTokens[8]+","+msgTokens[9]);
			sendPacket(message,UUID.fromString(msgTokens[2]));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//Send wants details messages
	public void sendWantsDetailsMessages(UUID clientID){
		try{
			String message = new String("wantsDetails,"+clientID.toString());
			forwardPacketToAll(message,clientID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//Send bullet create Messages
	public void sendBulletCreateMessages(String[] msgTokens){
		try{
			String message = new String("bulletCreate,"+msgTokens[1]);
			UUID senderID = UUID.fromString(msgTokens[1]);
			forwardPacketToAll(message,senderID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//Send bye messages
	public void sendByeMessages(UUID clientID){
		try{
			String message = new String("bye,"+clientID.toString());
			forwardPacketToAll(message,clientID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//Send lost messages
	public void sendLostMessages(UUID clientID){
		try{
			String message = new String("lost");
			forwardPacketToAll(message,clientID);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
