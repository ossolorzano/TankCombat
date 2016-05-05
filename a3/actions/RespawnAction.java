package a3.actions;

import a3.TankBattlesGame;
import a3.objects.Tank;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;

public class RespawnAction extends AbstractInputAction{
	private TankBattlesGame game;
	private Tank player;
	public RespawnAction(TankBattlesGame game, Tank player){
		this.game = game;
		this.player=player;
	}
	@Override
	public void performAction(float time, Event e) {
		System.out.println(Integer.parseInt(String.valueOf(e.getComponent())));
		if(player.isDead()){
			game.respawn(Integer.parseInt(String.valueOf(e.getComponent())));
		}
	}
}
