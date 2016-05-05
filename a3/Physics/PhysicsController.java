package a3.Physics;

import java.util.Vector;

import a3.objects.Tank;
import graphicslib3D.Matrix3D;
import graphicslib3D.Vector3D;
import sage.physics.IPhysicsEngine;
import sage.physics.IPhysicsObject;
import sage.physics.PhysicsEngineFactory;
import sage.terrain.TerrainBlock;

public class PhysicsController {
	private IPhysicsEngine physicsEngine;
	private IPhysicsObject groundPlaneP;
	private Vector<Tank> tankList;
	private Vector<Tank> removeList;
	private float mass;
	private float[] size = {1,2.2f,2};
	private float bounciness;
	private float friction;
	private float damping;
	
	public PhysicsController(){
		String engine = "sage.physics.JBullet.JBulletPhysicsEngine";
		physicsEngine = PhysicsEngineFactory.createPhysicsEngine(engine);
		physicsEngine.initSystem();
		tankList = new Vector<Tank>();
		removeList = new Vector<Tank>();
		mass = 10.0f;
		friction = 1.0f;
		damping=0.9f;
		bounciness = 0.1f;
		float[] gravity = {0,-10,0};
		physicsEngine.setGravity(gravity);
	}
	
	public void createInitWorld(TerrainBlock terrain){
		float up[] = {0,1,0};
		groundPlaneP = physicsEngine.addStaticPlaneObject(physicsEngine.nextUID(), terrain.getWorldTransform().getValues(), up, 0.0f);
		groundPlaneP.setBounciness(bounciness);
		terrain.setPhysicsObject(groundPlaneP);
	}
	
	public void updatePhysicsWorld(){
		for(Tank t : tankList){
			if(t.isDead()){
				removeList.addElement(t);
			}
			Vector3D translateVec = new Vector3D();
			Matrix3D mat = new Matrix3D(t.getPhysicsObject().getTransform());
			translateVec = mat.getCol(3);
			t.getLocalTranslation().setElementAt(0, 3, translateVec.getX());
			t.getLocalTranslation().setElementAt(1, 3, 1.1f);
			t.getLocalTranslation().setElementAt(2, 3, translateVec.getZ());
		}
		for(Tank t : removeList){
			tankList.removeElement(t);
		}
		removeList.clear();
	}
	//add physics objects to new tanks
	public void addTank(Tank tank){
		IPhysicsObject tankP = physicsEngine.addBoxObject(physicsEngine.nextUID(), mass, tank.getWorldTransform().getValues(), size);
		tankP.setBounciness(bounciness);
		tankP.setFriction(friction);
		tankP.setDamping(damping, 0);
		tank.setPhysicsObject(tankP);
		tankList.addElement(tank);
	}
	//Getters and setters
	public IPhysicsEngine getPhysicsEngine(){
		return physicsEngine;
	}
}
