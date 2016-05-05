package a3.npc;

import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import a3.TankBattlesGame;
import a3.npc.actions.*;
import a3.npc.conditions.*;
import a3.objects.Tank;
import sage.ai.behaviortrees.*;

public class NPCController {
	private Vector<BehaviorTree> trees;
	private TankBattlesGame game;
	private UUID id;
	private Vector<Tank> npcList;
	
	public NPCController(TankBattlesGame game){
		this.game = game;
		trees = new Vector<BehaviorTree>();
	}
	public void createNPC(){
		id = UUID.randomUUID();
		game.createNewGhost(id, 20, 0.5f, 20, "true", Integer.toString(1));
		
	}
	public void createBehaviorTree(Tank npc){
		BehaviorTree bt = new BehaviorTree(BTCompositeType.SELECTOR);
		//layer 0
		bt.insertAtRoot(new BTSequence(1));
		bt.insertAtRoot(new Wander(npc, game.getTerrainBlock()));
		//layer1
		bt.insert(1, new LineOfSight(game.getPlayer(),npc, false));
		bt.insert(1, new BTSelector(2));
		//layer2
		bt.insert(2, new BTSequence(3));
		bt.insert(2, new BTSequence(4));
		bt.insert(2, new DriveTowardsPlayer(game.getPlayer(), npc, game.getTerrainBlock()));
		//layer3
		bt.insert(3, new LowHealth(npc, false));
		bt.insert(3, new RunAway(game.getPlayer(), npc, game.getTerrainBlock()));
		bt.insert(4, new WithinDistance(game.getPlayer(),npc, false));
		bt.insert(4, new Attack(game.getPlayer(), npc, game));
		trees.addElement(bt);
	}
	public void update(float time){
		Iterator<BehaviorTree> treeIterator = trees.iterator();
		while(treeIterator.hasNext()){
			treeIterator.next().update(time);
		}
	}
	//updates the list of NPCs
	public void updateNPCList(){
		npcList.clear();
		for(Tank t: game.getGhostAvatars()){
			if(t.isNPC()){
				npcList.addElement(t);
			}
		}
	}
}
