package a3.npc.actions;

import a3.TankBattlesGame;
import a3.actions.RotateLeftAction;
import a3.actions.RotateRightAction;
import a3.objects.Tank;
import graphicslib3D.Vector3D;
import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;

public class Attack extends BTAction{
	private Tank player, npc;
	private float adjacent, opposite, theta, thetaOffset;
	private TankBattlesGame game;
	private RotateRightAction rotateRight;
	private RotateLeftAction rotateLeft;
	
	public Attack(Tank player, Tank npc, TankBattlesGame game){
		this.player=player;
		this.npc=npc;
		this.game = game;
		rotateRight = new RotateRightAction(npc, null);
		rotateLeft = new RotateLeftAction(npc, null);
		thetaOffset = 10;
		
	}
	@Override
	protected BTStatus update(float time) {
		Vector3D dir = new Vector3D(0,0,1);
		dir = dir.mult(npc.getTankHead().getWorldRotation());
		adjacent = (float)(player.getWorldTranslation().elementAt(2, 3)-npc.getWorldTranslation().elementAt(2, 3));
		opposite = (float)(player.getWorldTranslation().elementAt(0, 3)-npc.getWorldTranslation().elementAt(0, 3));
		theta = (float) (Math.atan2(opposite,adjacent) * (180/Math.PI));
		theta-=(float)(Math.atan2(dir.getX(), dir.getZ())*(180/Math.PI));
		//System.out.println(theta);
		if(theta<thetaOffset&&theta>-thetaOffset){
			//System.out.println("Reload: "+ npc.getReloadTimer());
			if(npc.getReloadTimer()<1){
				game.createBullet(npc.getUUID());	//creating objects directly from here creates problems
				//game.getThisClient().sendBullet(npc.getUUID());
			}
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
