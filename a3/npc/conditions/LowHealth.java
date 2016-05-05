package a3.npc.conditions;

import a3.objects.Tank;
import sage.ai.behaviortrees.BTCondition;

public class LowHealth extends BTCondition{
	private Tank npc;
	public LowHealth(Tank npc, boolean toNegate) {
		super(toNegate);
		this.npc=npc;
	}

	@Override
	protected boolean check() {
		if(npc.getHP()<30){
			return true;
		}
		return false;
	}

}
