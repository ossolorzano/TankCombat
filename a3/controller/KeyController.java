package a3.controller;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import a3.objects.Tank;

public class KeyController implements KeyListener{
	private Tank player;
	private int oldKey;
	private boolean wKey=false, aKey=false, sKey=false, dKey=false;
	public KeyController(Tank player){
		this.player=player;
	}
	@Override
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if(oldKey!=key && !player.isDead()){
			switch(key){
				//W
				case 87:
					player.getLeftTracks().startAnimation("TracksForward");
					player.getRightTracks().startAnimation("TracksForward");
					wKey=true;
					break;
				//S
				case 83: 
					player.getLeftTracks().startAnimation("TracksBackward");
					player.getRightTracks().startAnimation("TracksBackward");
					sKey=true;
					break;
				//A
				case 65:
					if(wKey==false && sKey==false){
						player.getLeftTracks().startAnimation("TracksBackward");
						player.getRightTracks().startAnimation("TracksForward");
					}
					aKey=true;
					break;
				//D
				case 68:
					if(wKey==false && sKey==false){
						player.getLeftTracks().startAnimation("TracksForward");
						player.getRightTracks().startAnimation("TracksBackward");
					}
					dKey=true;
					break;
				default://System.out.println(key);
					break;
			}
			oldKey=key;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();
		switch(key){
			//W
			case 87:
				wKey=false;
				break;
			//A
			case 65:
				aKey=false;
				break;
			//S
			case 83:
				sKey=false;
				break;
			//D
			case 68:
				dKey=false;
				break;
		}
		//if W is released
		if(key==87 || key==83 && aKey==true){
			player.getLeftTracks().startAnimation("TracksBackward");
			player.getRightTracks().startAnimation("TracksForward");
		}
		else if(key==87 || key==83 && dKey==true){
			player.getLeftTracks().startAnimation("TracksForward");
			player.getRightTracks().startAnimation("TracksBackward");
		}
		if(wKey==false && aKey==false && sKey==false && dKey==false){
			player.getLeftTracks().startAnimation("idleAction");
			player.getRightTracks().startAnimation("idleAction");
		}
		oldKey=0;
	}
	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
}
