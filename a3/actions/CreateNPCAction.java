package a3.actions;

import a3.npc.NPCController;
import net.java.games.input.Event;
import sage.input.action.AbstractInputAction;

public class CreateNPCAction extends AbstractInputAction{
	private NPCController npcController;
	public CreateNPCAction(NPCController npcController){
		this.npcController = npcController;
	}
	@Override
	public void performAction(float time, Event e) {
		npcController.createNPC();
	}

}
