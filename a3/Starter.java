package a3;

public class Starter {
	public static void main(String[] args) {
		String serverAddress = args[0];
		int serverPort = Integer.parseInt(args[1].trim());
		boolean isFS;
		if(args.length>=3){
			isFS = Boolean.parseBoolean(args[2]);
		}
		else{
			isFS=true;
		}
		new TankBattlesGame(serverAddress,serverPort, isFS).start();
	}
}
