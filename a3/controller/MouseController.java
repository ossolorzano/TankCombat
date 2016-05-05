package a3.controller;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.UUID;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import a3.TankBattlesGame;
import a3.objects.*;

public class MouseController implements MouseListener, MouseMotionListener{
	private CameraController cc;
	private TankBattlesGame game;
	private Tank player;
	private Robot robot;
	private Point cCenter;
	private float prevMouseX, prevMouseY, curMouseX, curMouseY;
	private boolean isRecentering;
	private float offset, change;
	
	
	public MouseController(TankBattlesGame game, CameraController cc, Tank player){
		this.cc=cc;
		this.game=game;
		this.player=player;
		Dimension dim = game.getRenderer().getCanvas().getSize();
		cCenter = new Point(dim.width/2,dim.height/2);
		isRecentering = false;
		try{
			robot = new Robot();
		} catch(AWTException e){
			throw new RuntimeException("Couldn't create robot");
		}
		recenterMouse();
		prevMouseX= cCenter.x;
		prevMouseY= cCenter.y;
		Image noImage = new ImageIcon("").getImage();
		Cursor emptyCursor = Toolkit.getDefaultToolkit().createCustomCursor(noImage, new Point(0,0), "");
		game.getRenderer().getCanvas().setCursor(emptyCursor);
	}
	public void recenterMouse(){
		isRecentering = true;
		Point p = new Point(cCenter.x, cCenter.y);
		Canvas canvas = game.getRenderer().getCanvas();
		SwingUtilities.convertPointToScreen(p, canvas);
		robot.mouseMove(p.x, p.y);
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(isRecentering && cCenter.x==e.getX() && cCenter.y == e.getY()){
			isRecentering=false;
		}
		else{
			curMouseX = e.getX();
			curMouseY = e.getY();
			float mouseDeltaX = prevMouseX - curMouseX;
			float mouseDeltaY = prevMouseY - curMouseY;
			//logic here
			rotateCamera(mouseDeltaX);
			pitch(mouseDeltaY);
			//
			prevMouseX = curMouseX;
			prevMouseY = curMouseY;
			recenterMouse();
			prevMouseX = cCenter.x;
			prevMouseY = cCenter.y;
		}
	}
	
	public void rotateCamera(float deltaX){
		float azimuth =cc.getAzimuth();
		if(azimuth >360){
			azimuth= azimuth%360;
		}
		if(azimuth<-360){
			azimuth = azimuth%-360;
		}
		change=(float) (deltaX*.08);
		offset += change;
		System.out.println(offset);
		azimuth+=change; //slows down turn speed
		cc.setAzimuth(azimuth);
	}
	public void pitch(float deltaY){
		float offset = cc.getCameraOffset();
		if(offset>5){	//prevents sticking from float inaccuracy
			offset=5;
		}
		if(offset<0){	//prevents sticking from float inaccuracy
			offset=0;
		}
		if(offset<=5 && offset>=0){
			offset+=deltaY*.01;
		}
		cc.setCameraOffset(offset);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		UUID playerID = player.getUUID();
		game.createBullet(playerID);	//creating objects directly from here creates problems
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	public float getOffset(){
		if(offset<-180){
			offset+=360;
		}
		else if(offset>180){
			offset-=360;
		}
		return offset;
	}
	public void reduceOffset(float delta){
		offset -= delta;
		
	}
	public void increaseOffset(float delta){
		offset+= delta;
	}
}
