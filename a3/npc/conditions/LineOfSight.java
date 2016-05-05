package a3.npc.conditions;

import a3.objects.Tank;
import sage.ai.behaviortrees.BTCondition;

public class LineOfSight extends BTCondition{
	private Tank npc;
	private Tank player;
	float distance;
	public LineOfSight(Tank player, Tank npc, boolean toNegate) {
		super(toNegate);
		this.npc = npc;
		this.player=player;
	}

	@Override
	protected boolean check() {
		//find distance between NPC and player
		distance = (float) Math.pow(Math.pow((float)(player.getWorldTranslation().elementAt(0,3)-npc.getWorldTranslation().elementAt(0, 3)),2)+Math.pow((float)(player.getWorldTranslation().elementAt(1,3)-npc.getWorldTranslation().elementAt(1, 3)),2), .5);
		//System.out.println(distance);
		if(distance<100){
			//System.out.println("Distance: "+distance);
			return true;
		}
		return false;
	}
	public float getDistance(){
		return distance;
	}
}
