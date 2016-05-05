package a3.network;

import java.io.IOException;

public class ServerStarter {
	public static void main(String[] args) {
		try {
			new GameServer(8950);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
