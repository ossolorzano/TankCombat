package a3.npc.actions;

import java.util.Random;
import java.util.Vector;

import a3.actions.MoveBackwardAction;
import a3.actions.MoveForwardAction;
import a3.actions.RotateLeftAction;
import a3.actions.RotateRightAction;
import a3.objects.Tank;
import sage.ai.behaviortrees.BTAction;
import sage.ai.behaviortrees.BTStatus;
import sage.input.action.IAction;
import sage.terrain.TerrainBlock;

public class Wander extends BTAction{
	private Random ran;
	private MoveForwardAction moveForward;
	private MoveBackwardAction moveBackward;
	private RotateLeftAction rotateLeft;
	private RotateRightAction rotateRight;
	private IAction currentAction;
	private float timer;
	public Wander(Tank npc, TerrainBlock terrain){
		ran = new Random();
		moveForward = new MoveForwardAction(npc);
		moveBackward = new MoveBackwardAction(npc);
		rotateLeft = new RotateLeftAction(npc, null);
		rotateRight = new RotateRightAction(npc, null);
		timer = 0;
		currentAction = moveForward; //needs initial action
	}
	@Override
	protected BTStatus update(float time) {
		timer+=time;
		//switches actions every few seconds
		if(timer>1000){
			switch(ran.nextInt(4)){
				case 0: moveForward.performAction(time, null);
						currentAction=moveForward;
					break;
				case 1: moveBackward.performAction(time, null);
						currentAction=moveBackward;
					break;
				case 2: rotateLeft.performAction(time, null);
						currentAction=rotateLeft;
					break;
				case 3: rotateRight.performAction(time, null);
						currentAction=rotateRight;
					break;
			}
			timer=0;
		}
		currentAction.performAction(time, null);	//continues doing actions between changing actions
		return BTStatus.BH_SUCCESS;
	}

}
