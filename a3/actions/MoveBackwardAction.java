package a3.actions;

import a3.objects.*;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.scene.Group;

public class MoveBackwardAction extends AbstractInputAction{
	private Group body;
	private float movementSpeed;
	public MoveBackwardAction(Group moveable){
		body = moveable;
		movementSpeed = ((IMovable) moveable).getMovementSpeed();
	}
	@Override
	public void performAction(float time, Event e) {
		if(!((Tank)body).isDead()){
			//horizontal movement
			Matrix3D rot = body.getLocalRotation();
			Vector3D dir = new Vector3D(0,0,-1);
			dir = dir.mult(rot);
			dir.scale(movementSpeed/10*time);
			float newX=(float) (body.getPhysicsObject().getLinearVelocity()[0]+dir.getX());
			float newY=(float) (body.getPhysicsObject().getLinearVelocity()[1]+dir.getY());
			float newZ=(float) (body.getPhysicsObject().getLinearVelocity()[2]+dir.getZ());
			float [] vel = {newX, newY, newZ};
			body.getPhysicsObject().setLinearVelocity(vel);
		}
	}
}
