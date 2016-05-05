package a3.actions;

import a3.TankBattlesGame;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;

public class QuitAction extends AbstractInputAction{
	private TankBattlesGame game;
	public QuitAction(TankBattlesGame g){
		game = g;
	}
	@Override
	public void performAction(float time, Event e) {
		game.setGameOver(true);
	}

}
