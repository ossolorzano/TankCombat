package a3.network;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import a3.TankBattlesGame;
import a3.objects.Tank;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.networking.client.GameConnectionClient;

public class GameClient extends GameConnectionClient{
	private TankBattlesGame game;
	private UUID id;
	
	public GameClient(InetAddress remoteAddr, int remotePort, TankBattlesGame tGame) throws IOException {
		super(remoteAddr, remotePort, ProtocolType.TCP);
		game = tGame;
		id = UUID.randomUUID();
		game.setUUID(id);
	}
	//get ID
	public UUID getID(){
		return id;
	}
	//Process Packets
	protected void processPacket(Object msg){
		String message = (String)msg;
		String[] msgTokens=message.split(","); //splits string into its separate arguments/substrings
		//JOIN
		if(msgTokens[0].compareTo("join")==0){	//incoming join package
			if(msgTokens[1].compareTo("success")==0){
				game.setIsConnected(true);
				game.getPlayer().setDead(true);
				game.setSpawn(msgTokens[2]);
				System.out.println("Successfully connected to server.");
			}
			else if(msgTokens[1].compareTo("failure")==0){
				game.setIsConnected(false);
				System.out.println("Couldn't connect to server.");
				
			}
		}
		//CREATE
		if(msgTokens[0].compareTo("create")==0){
			if(msgTokens[1]!=id.toString()){
				game.createNewGhost(UUID.fromString(msgTokens[1]),Float.parseFloat(msgTokens[2]),Float.parseFloat(msgTokens[3]),Float.parseFloat(msgTokens[4]),msgTokens[5],msgTokens[6]);
			}
		}
		//WANTS DETAILS
		if(msgTokens[0].compareTo("wantsDetails")==0){
			sendDetails(UUID.fromString(msgTokens[1]), game.getPlayer());
		}
		//DETAILS
		if(msgTokens[0].compareTo("details")==0){
			game.updateGhost(UUID.fromString(msgTokens[1]),Float.parseFloat(msgTokens[2]),Float.parseFloat(msgTokens[3]),Float.parseFloat(msgTokens[4]),msgTokens[5],msgTokens[6],msgTokens[7],msgTokens[8]);
		}
		//BULLETCREATE
		if(msgTokens[0].compareTo("bulletCreate")==0){
			UUID shootingTankID = UUID.fromString(msgTokens[1]);
			game.createBullet(shootingTankID);
		}
		//BYE
		if(msgTokens[0].compareTo("bye")==0){
			UUID leavingTankID = UUID.fromString(msgTokens[1]);
			game.removeTank(leavingTankID);
		}
		//LOST
		if(msgTokens[0].compareTo("lost")==0){
			game.setLost();
		}
	}
	//SEND JOINED
	public void sendJoinMessage(){
		try{
			sendPacket(new String("join,"+id.toString()));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND BYE
	public void sendByeMessage(){
		try{
			sendPacket(new String("bye,"+id.toString()));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND CREATE
	public void sendCreateMessage(Point3D location, Tank player){
		try{
			String message = new String("create,"+player.getUUID().toString());
			message+=","+location.getX()+","+location.getY()+","+location.getZ()+","+player.isNPC()+","+player.getModelNumber();
			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND DETAILS
	public void sendDetails(UUID senderID, Tank player){
		try{
			String message = new String("details,"+id.toString());
			Matrix3D transform = new Matrix3D(player.getPhysicsObject().getTransform());
			Vector3D translateVec = transform.getCol(3);
			message+=","+senderID.toString();
			message+=","+translateVec.getX()+","+translateVec.getY()+","+translateVec.getZ();
			message+=","+player.getLocalRotation().toString();
			message+=","+player.getTankHead().getLocalRotation().toString();
			message+=","+player.isNPC();
			message+=","+player.getModelNumber();
			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND WANTS DETAILS
	public void sendWantsDetails(){
		try{
			String message = new String("wantsDetails,"+id.toString());
			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND BULLET
	public void sendBullet(UUID tankID){
		try{
			String message = new String("bulletCreate,"+tankID);
			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	//SEND WON
	public void sendWon(){
		try{
			String message = new String("won,"+id.toString());
			sendPacket(message);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
}
