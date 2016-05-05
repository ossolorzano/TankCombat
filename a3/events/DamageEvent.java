package a3.events;

import sage.event.AbstractGameEvent;

public class DamageEvent extends AbstractGameEvent{
	private int damageAmount;
	public DamageEvent(){
		damageAmount=10;
	}
	public int getDamageAmount(){
		return damageAmount;
	}
}
