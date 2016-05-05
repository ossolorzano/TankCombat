package a3.npc.actions;

import a3.actions.MoveBackwardAction;
import a3.actions.MoveForwardAction;
import a3.actions.RotateLeftAction;
import a3.actions.RotateRightAction;
import a3.objects.Tank;
import graphicslib3D.Vector3D;
import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.terrain.TerrainBlock;

public class RunAway extends BTAction{
	private MoveBackwardAction moveBackward;
	private Tank player, npc;
	private RotateRightAction rotateRight;
	private RotateLeftAction rotateLeft;
	private float adjacent, opposite, theta, thetaOffset;
	
	public RunAway(Tank player, Tank npc, TerrainBlock terrain){
		this.player = player;
		this.npc = npc;
		moveBackward = new MoveBackwardAction(npc);
		rotateRight = new RotateRightAction(npc, null);
		rotateLeft = new RotateLeftAction(npc, null);
		thetaOffset=20;
	}
	@Override
	protected BTStatus update(float time) {
		Vector3D dir = new Vector3D(0,0,1);
		dir = dir.mult(npc.getTankBody().getWorldRotation());
		adjacent = (float)(player.getWorldTranslation().elementAt(2, 3)-npc.getWorldTranslation().elementAt(2, 3));
		opposite = (float)(player.getWorldTranslation().elementAt(0, 3)-npc.getWorldTranslation().elementAt(0, 3));
		theta = (float) (Math.atan2(opposite,adjacent) * (180/Math.PI));
		theta-=(float)(Math.atan2(dir.getX(), dir.getZ())*(180/Math.PI));
		//System.out.println(theta);
		if(theta<thetaOffset&&theta>-thetaOffset){
			moveBackward.performAction(time, null);
		}
		else{
			if(theta>=thetaOffset){
				rotateLeft.performAction(time, null);
			}
			else if(theta<=-thetaOffset){
				rotateRight.performAction(time, null);
			}
		}
		return BTStatus.BH_SUCCESS;
	}

}
