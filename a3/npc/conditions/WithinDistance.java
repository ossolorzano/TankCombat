package a3.npc.conditions;

import a3.objects.Tank;
import sage.ai.behaviortrees.BTBehavior;
import sage.ai.behaviortrees.BTCondition;

public class WithinDistance extends BTCondition{
	private Tank player, npc;
	private float distance;
	public WithinDistance(Tank player, Tank npc, boolean toNegate) {
		super(toNegate);
		this.player=player;
		this.npc=npc;
	}

	@Override
	protected boolean check() {
		distance = (float) Math.pow(Math.pow((float)(player.getWorldTranslation().elementAt(0,3)-npc.getWorldTranslation().elementAt(0, 3)),2)+Math.pow((float)(player.getWorldTranslation().elementAt(1,3)-npc.getWorldTranslation().elementAt(1, 3)),2), .5);
		if(distance<50){
			return true;
		}
		return false;
	}

}
