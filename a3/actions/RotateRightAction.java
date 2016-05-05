package a3.actions;

import a3.controller.MouseController;
import a3.objects.Tank;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.scene.Group;

public class RotateRightAction extends AbstractInputAction{
	private Group tankBody;
	private float rotateSpeed;
	private Vector3D worldUpAxis;
	private MouseController mc;
	public RotateRightAction(Tank tank, MouseController mc){
		tankBody = tank;
		this.mc=mc;
		rotateSpeed = tank.getRotateSpeed();
		worldUpAxis = new Vector3D(0,1,0);
	}
	@Override
	public void performAction(float time, Event e) {
		if(!((Tank)tankBody).isDead()){
			tankBody.rotate(-rotateSpeed*time, worldUpAxis);
			if(mc!=null){
				mc.increaseOffset(rotateSpeed*time);
			}
		}
	}
}
