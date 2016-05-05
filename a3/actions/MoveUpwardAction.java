package a3.actions;

import a3.objects.IMovable;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;
import sage.scene.Group;

//Made for bullets, since they are rotated before moving
public class MoveUpwardAction extends AbstractInputAction{
	private Group body;
	private float movementSpeed;
	public MoveUpwardAction(Group movable){
		body =  movable;
		movementSpeed = ((IMovable) movable).getMovementSpeed();
	}
	@Override
	public void performAction(float time, Event e) {
		Matrix3D rot = body.getLocalRotation();
		Vector3D dir = new Vector3D(0,1,0);
		dir = dir.mult(rot);
		dir.scale(movementSpeed*time);
		body.translate((float)dir.getX(), (float)dir.getY(), (float)dir.getZ());
	}
}
