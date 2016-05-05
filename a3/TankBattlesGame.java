package a3;

import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.rmi.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.Vector;

import a3.Physics.PhysicsController;
import a3.actions.*;
import a3.controller.*;
import a3.events.DamageEvent;
import a3.network.*;
import a3.npc.NPCController;
import a3.objects.Bullet;
import a3.objects.Tank;
import a3.objects.hud.CrosshairObject;
import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.app.BaseGame;
import sage.audio.AudioManagerFactory;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.IAudioManager;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.camera.ICamera;
import sage.display.IDisplaySystem;
import sage.event.EventManager;
import sage.event.IEventManager;
import sage.input.IInputManager;
import sage.input.InputManager;
import sage.input.action.IAction;
import sage.scene.Group;
import sage.scene.HUDImage;
import sage.scene.HUDString;
import sage.scene.Leaf;
import sage.scene.SceneNode;
import sage.scene.SkyBox;
import sage.scene.TriMesh;
import sage.scene.shape.Line;
import sage.scene.state.RenderState;
import sage.scene.state.TextureState;
import sage.terrain.AbstractHeightMap;
import sage.terrain.ImageBasedHeightMap;
import sage.terrain.TerrainBlock;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class TankBattlesGame extends BaseGame{
	private String serverAddress;
	private int serverPort;
	private boolean isConnected;
	private GameClient thisClient;
	private MyDisplaySystem display;
	private MouseController mouseController;
	private IInputManager im;
	private ICamera camera;
	private String kbName;
	private CameraController cc;
	private Vector<Object> deadArray;
	private Vector<Bullet> bulletCreate;
	private SkyBox skybox;
	private Texture skyboxDown, skyboxUp, skyboxNorth, skyboxSouth, skyboxEast, skyboxWest;
	private Vector<Tank> ghostAvatars;
	private Vector<Tank> ghostCreate;
	private Tank player, firingTank;
	private long timer, soundTimer;
	private HUDString scoreString, reloadString;
	private int score;
	private TerrainBlock terrain;
	private PhysicsController physicsController;
	private KeyController keyController;
	private IAudioManager audioMgr;
	private NPCController npcController;
	private float[] emptyArray={0,0,0};
	private Vector<float[]> spawnPoints;
	private boolean respawn = false;
	private int spawnIndice;
	private UUID uuid;
	private HUDImage HPObjectRed, HPObjectGreen, spawnScreen, winScreen, lostScreen;
	private CrosshairObject crosshair;
	private DecimalFormat df = new DecimalFormat("#.##");
	private String reload;
	private Sound gunfight;
	private int modelNumber;
	private boolean respawnScreenOpen, isFullScreen, lost=false;
	
	public TankBattlesGame(String serverAddr, int sPort, boolean isFS){
		isFullScreen=isFS;
		serverAddress = serverAddr;
		serverPort = sPort;
		isConnected=false;
		deadArray = new Vector<Object>();
		bulletCreate = new Vector<Bullet>();
		ghostAvatars = new Vector<Tank>();
		ghostCreate = new Vector<Tank>();
		timer=0;
		//setup spawn points
		float[] spawn1 = {20,28};
		float[] spawn2 = {223, 222};
		float[] spawn3 = {24, 222};
		float[] spawn4 = {23, 220};
		float[] spawn5 = {154, 164};
		float[] spawn6 = {91, 81};
		spawnPoints = new Vector<float[]>();
		spawnPoints.addElement(spawn1);
		spawnPoints.addElement(spawn2);
		spawnPoints.addElement(spawn3);
		spawnPoints.addElement(spawn4);
		spawnPoints.addElement(spawn5);
		spawnPoints.addElement(spawn6);
	}
	
	protected void initGame(){
		//network
		try{
			thisClient = new GameClient(InetAddress.getByName(serverAddress),serverPort,this);
		}
		catch(UnknownHostException e){
			e.printStackTrace();
		}
		catch(IOException e){
			e.printStackTrace();
		}
		if(thisClient != null){
			thisClient.sendJoinMessage();
		}	
		audioMgr = AudioManagerFactory.createAudioManager("sage.audio.joal.JOALAudioManager");
		if(!audioMgr.initialize()){
			System.out.println("Audio Manager failed to initialize");
			return;
		}
		//NPC controller
		npcController = new NPCController(this);
		initScene();
		initGameObjects();	//creates new objects
		initInputManager();
		initAudio();
		//Physics controller
		physicsController = new PhysicsController();
		physicsController.createInitWorld(terrain);
		physicsController.addTank(player);
		player.getLeftTracks().startAnimation("idleAction");
		player.getRightTracks().startAnimation("idleAction");
		
		keyController = new KeyController(player);
		display.addKeyListener(keyController);	
	}
	private void initGameObjects(){
		display.setTitle("Tank Combat");
		camera = display.getRenderer().getCamera();
		camera.setPerspectiveFrustum(90, 1, 0.01, 1000);
		camera.setLocation(new Point3D(0,0,10));	//Will change once orbit controller added
		//creates player
		player = new Tank(0,0.5f,0,1, false, this);
		player.setUUID(uuid);
		player.updateGeometricState(0, true);
		addGameWorldObject(player);
		cc = new CameraController(camera, player, im, kbName);
		//mouse controller
		mouseController = new MouseController(this, cc, player);
		display.addMouseListener(mouseController);
		display.addMouseMotionListener(mouseController);
		/*
		Point3D origin = new Point3D(0,0,0);
		Point3D xEnd = new Point3D(100,0,0);
		Point3D yEnd = new Point3D(0,100,0);
		Point3D zEnd = new Point3D(0,0,100);
		Line xAxis = new Line(origin,xEnd,Color.RED,2);
		Line yAxis = new Line(origin,yEnd,Color.GREEN,2);
		Line zAxis = new Line(origin,zEnd,Color.BLUE,2);
		addGameWorldObject(xAxis);
		addGameWorldObject(yAxis);
		addGameWorldObject(zAxis);
		*/
		//create HUD
		score = 0;
		scoreString = new HUDString("Score: "+score);
		scoreString.setColor(Color.RED);
		scoreString.setLocation(0,0.05);
		camera.addToHUD(scoreString);
		crosshair = new CrosshairObject();
		crosshair.setColor(Color.WHITE);
		camera.addToHUD(crosshair);
		HPObjectRed = new HUDImage("textures/hpRed.png");
		HPObjectRed.setLocation(0, 0.9);
		HPObjectRed.scale(1.01f, 0.1f, 0);
		camera.addToHUD(HPObjectRed);
		HPObjectGreen = new HUDImage("textures/hpGreen.png");
		HPObjectGreen.setLocation(0,0.9);
		HPObjectGreen.scale(0.99f, 0.09f, 0);
		camera.addToHUD(HPObjectGreen);
		reloadString = new HUDString(String.valueOf(player.getReloadTimer()));
		reloadString.setLocation(0.51,0.47);
		reloadString.setColor(Color.GREEN);
		camera.addToHUD(reloadString);
		spawnScreen = new HUDImage("textures/respawn.png");
		spawnScreen.scale(2f, 2f, 2f);
		spawnScreen.rotateImage(180);
		camera.addToHUD(spawnScreen);
		respawnScreenOpen=true;
		//create won and lost hud images
		winScreen = new HUDImage("textures/won.png");
		winScreen.scale(2f, 2f, 2f);
		winScreen.rotateImage(180);
		lostScreen = new HUDImage("textures/lost.png");
		lostScreen.scale(2f, 2f, 2f);
		lostScreen.rotateImage(180);
		firingTank = new Tank(0f,0f,0f,1, false, this);
	}
	private void initScene(){
		//skybox generation
		skybox = new SkyBox("Skybox",20f,20f,20f);
		skyboxDown = TextureManager.loadTexture2D("textures/skyboxDown.bmp");
		skyboxNorth = TextureManager.loadTexture2D("textures/skyboxNorth.bmp");
		skyboxEast = TextureManager.loadTexture2D("textures/skyboxEast.bmp");
		skyboxSouth = TextureManager.loadTexture2D("textures/skyboxSouth.bmp");
		skyboxWest = TextureManager.loadTexture2D("textures/skyboxWest.bmp");
		skyboxUp = TextureManager.loadTexture2D("textures/skyboxUp.bmp");
		skybox.setTexture(SkyBox.Face.Up, skyboxUp);
		skybox.setTexture(SkyBox.Face.North, skyboxNorth);
		skybox.setTexture(SkyBox.Face.East, skyboxEast);
		skybox.setTexture(SkyBox.Face.West, skyboxWest);
		skybox.setTexture(SkyBox.Face.South, skyboxSouth);
		skybox.setTexture(SkyBox.Face.Down, skyboxDown);
		addGameWorldObject(skybox);
		//terrain generation
		ImageBasedHeightMap heightMap = new ImageBasedHeightMap("textures/heightMap.png");
		terrain = createTerBlock(heightMap);
		//Texture state to skin terrain
		TextureState terrainState;
		Texture grassTexture = TextureManager.loadTexture2D("textures/grass.jpg");
		grassTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		terrainState = (TextureState) display.getRenderer().createRenderState(RenderState.RenderStateType.Texture);
		terrainState.setTexture(grassTexture,0);
		terrainState.setEnabled(true);
		//apply texture to terrain
		terrain.setRenderState(terrainState);
		addGameWorldObject(terrain);
	}
	
	//UPDATE
	protected void update(float time){
		super.update(time);
		cc.update(time);
		timer+=time;
		soundTimer+=time;
		//update HUD
		scoreString.setColor(Color.RED);
		scoreString.setText("Score: "+score);
		HPObjectGreen.scale(((float)player.getHP())/100, 0.09f, 0);
		reload = df.format(player.getReloadTimer()/1000);
		if(Float.valueOf(reload)<0.05){
			reload = "READY";
		}
		reloadString.setText(reload);
		//move skybox
		Point3D camLoc = camera.getLocation();
		Matrix3D camTranslation = new Matrix3D();
		camTranslation.translate(camLoc.getX(), camLoc.getY(), camLoc.getZ());
		skybox.setLocalTranslation(camTranslation);
		//updates world bound of everything. Add any new objects before this.
		for(SceneNode s : getGameWorld()){
			s.updateGeometricState(time, true);
			s.updateWorldBound();
		}
		//process packets
		if(thisClient!=null){
			thisClient.processPackets();
			if(timer>10){
				thisClient.sendWantsDetails();
				timer-=10;
			}
		}
		//check score
		if(score>=3){
			camera.addToHUD(winScreen);
			player.setDead(true);
			respawnScreenOpen=true;
			score=0;
			thisClient.sendWon();
		}
		if(lost){
			camera.removeFromHUD(spawnScreen);
			camera.addToHUD(lostScreen);
			player.setDead(true);
			respawnScreenOpen=true;
			score=0;
			lost=false;
		}
		if(soundTimer>7000){
			gunfight.play(10, false);
			soundTimer-=7000;
		}
		//update turret rotation
		updateTurretRotation(time);
		//checks bullet collision
		checkBulletCollision();
		//creates bullet new bullets since last update
		Iterator<Bullet> bulletIterator = bulletCreate.iterator();
		while(bulletIterator.hasNext()){
			Bullet b = bulletIterator.next();
			addGameWorldObject(b);
			getThisClient().sendBullet(player.getUUID());
			b.getBulletSound().play(100, true);
		}
		bulletCreate.clear();	//Clears array for next update. Prevents bullets continually being created.
		//creates new ghosts
		Iterator<Tank> ghostIterator = ghostCreate.iterator();
		while(ghostIterator.hasNext()){
			Tank g = ghostIterator.next();
			g.updateGeometricState(time, true);
			physicsController.addTank(g);
			addGameWorldObject(g);
			g.getLeftTracks().startAnimation("idleAction");
			g.getRightTracks().startAnimation("idleAction");
		}
		ghostCreate.clear();
		//Physics update
		physicsController.getPhysicsEngine().update(time);
		physicsController.updatePhysicsWorld();
		for(SceneNode s : getGameWorld()){
			//checks lifetime of bullet and puts it in dead array, also moves forward any bullets
			if(s instanceof Bullet){
				((Bullet) s).getBulletForward().performAction(time, null);
				((Bullet)s).checkLifeTime(time);
				((Bullet)s).getBulletSound().setLocation(new Point3D(s.getLocalTranslation().getCol(3)));	//move bullet sound
				//check if bullet hit terrain
				float bulletX=(float) ((Bullet)s).getLocalTranslation().getCol(3).getX();
				float bulletY=(float) ((Bullet)s).getLocalTranslation().getCol(3).getY();
				float bulletZ=(float) ((Bullet)s).getLocalTranslation().getCol(3).getZ();
				if(bulletY<=terrain.getHeight(bulletX, bulletZ)){
					((Bullet)s).setIsDead(true);
				}
				//add dead bullets to dead array
				if(((Bullet)s).getIsDead()){
					deadArray.add(s);
				}
			}
			//reduces reload timer for every tank multiplied by time
			else if(s instanceof Tank){
				//reduce reload timer
				if(!(((Tank) s).getReloadTimer()<1)){
					((Tank)s).reduceReloadTimer(time);
				}
				//move sound
				((Tank)s).getEngineSound().setLocation(new Point3D(s.getLocalTranslation().getCol(3)));
				((Tank)s).getHitSound().setLocation(new Point3D(s.getLocalTranslation().getCol(3)));
				((Tank)s).getDeadSound().setLocation(new Point3D(s.getLocalTranslation().getCol(3)));
				float x = (float) ((Tank)s).getLocalTranslation().getCol(3).getX();
				float y = (float) ((Tank)s).getLocalTranslation().getCol(3).getY();
				float z = (float) ((Tank)s).getLocalTranslation().getCol(3).getZ();
				float xoffset = 2;
				float zoffset = 3;
				//check if tank position is running into terrain
				if(y<=terrain.getHeight(x+xoffset, z+zoffset)||y<=terrain.getHeight(x+xoffset, z-zoffset)||y<=terrain.getHeight(x-xoffset, z+zoffset)||y<=terrain.getHeight(x-xoffset, z-zoffset)){
					((Tank)s).getPhysicsObject().setLinearVelocity(emptyArray);
				}
				//animation update
				((Tank)s).getLeftTracks().updateAnimation(time);
				((Tank)s).getRightTracks().updateAnimation(time);
				if(((Tank)s).isDead()){
					((Tank)s).getLeftTracks().startAnimation("idleAction");
					((Tank)s).getRightTracks().startAnimation("idleAction");
				}
			}
		}
		//check dead stuff
		Iterator<Object> deadIterator = deadArray.iterator();
		while(deadIterator.hasNext()){
			SceneNode b = (SceneNode) deadIterator.next();
			if(b instanceof Bullet){
				((Bullet)b).getBulletSound().stop();
			}
			if(b instanceof Tank){
				((Tank)b).getEngineSound().stop();
			}
			removeGameWorldObject(b);
		}
		deadArray.clear();  //Clears array for next update
		//NPC update
		npcController.update(time);
		//sound update
		setEarParameters();
		//System.out.println(player.getLocalTranslation().getCol(3).getX()+" "+player.getLocalTranslation().getCol(3).getZ());
		
		//respawn
		if(respawn){
			//player.setDead(true);
			deadArray.add(player);
			player = new Tank(spawnPoints.elementAt(spawnIndice)[0],0.5f, spawnPoints.elementAt(spawnIndice)[1],modelNumber, false, this);
			player.setUUID(thisClient.getID());
			addGameWorldObject(player);
			player.updateGeometricState(0, true);
			player.getEngineSound().play();
			cc = new CameraController(camera, player, im, kbName);
			physicsController.addTank(player);
			player.getLeftTracks().startAnimation("idleAction");
			player.getRightTracks().startAnimation("idleAction");
			mouseController = new MouseController(this, cc, player);
			initInputManager();
			display.addMouseListener(mouseController);
			display.addMouseMotionListener(mouseController);
			keyController = new KeyController(player);
			display.addKeyListener(keyController);
			thisClient.sendCreateMessage(player.getLocation(), player);
			camera.removeFromHUD(spawnScreen);
			camera.removeFromHUD(winScreen);
			camera.removeFromHUD(lostScreen);
			respawnScreenOpen=false;
			respawn=false;
		}
		if(player.isDead() && !respawnScreenOpen){
			camera.addToHUD(spawnScreen);
			respawnScreenOpen = true;
		}
	}
	public void initInputManager(){
		kbName = im.getKeyboardName();
		
		IAction moveForward = new MoveForwardAction(player);
		IAction moveBackward = new MoveBackwardAction(player);
		IAction rotateRight = new RotateRightAction(player, mouseController);
		IAction rotateLeft = new RotateLeftAction(player, mouseController);
		IAction createNPC = new CreateNPCAction(npcController);
		IAction respawn = new RespawnAction(this,player);
		IAction quitGame = new QuitAction(this);
		
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.W, moveForward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.S, moveBackward, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.D, rotateRight, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.A, rotateLeft, IInputManager.INPUT_ACTION_TYPE.REPEAT_WHILE_DOWN);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key._9, createNPC, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key._1, respawn, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);	//respawn associated with 1 and 2 keys
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key._2, respawn, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
		im.associateAction(kbName, net.java.games.input.Component.Identifier.Key.ESCAPE, quitGame, IInputManager.INPUT_ACTION_TYPE.ON_PRESS_ONLY);
	}
	public void initAudio(){
		//prepare background noise
		AudioResource fightResource;
		fightResource = audioMgr.createAudioResource("sounds/gunfight.wav", AudioResourceType.AUDIO_SAMPLE);
		gunfight = new Sound(fightResource, SoundType.SOUND_EFFECT,100,false);
		gunfight.initialize(audioMgr);
		gunfight.setMaxDistance(1000);
		gunfight.setMinDistance(0);
		gunfight.setRollOff(1);
		gunfight.setLocation(new Point3D(125,0,125));
		//only set engine location
		player.getEngineSound().setLocation(new Point3D(player.getWorldTranslation().getCol(3)));
		setEarParameters();
		player.getEngineSound().play();
	}
	public void setEarParameters(){
		Vector3D camDir = camera.getViewDirection();
		audioMgr.getEar().setLocation(camera.getLocation());
		audioMgr.getEar().setOrientation(camDir, new Vector3D(0,1,0));
	}
	//added so controller can access addGameWorldObject()
	public void addGameWorldObject(SceneNode s){
		super.addGameWorldObject(s);
	}
	//update turret rotation
	public void updateTurretRotation(double time){
		if(!player.isDead()){
			float offset = mouseController.getOffset();
			//System.out.println(offset);
			TriMesh head = player.getTankHead();
			Matrix3D headRot = (Matrix3D) player.getTankHead().getLocalRotation().clone();
			Matrix3D bodyRot = (Matrix3D) player.getLocalRotation().clone();
			headRot.concatenate(bodyRot.inverse());
			//System.out.println(headRot);
			float turnSpeed = (float) (.05*time);
			if(offset>0.01){
				head.rotate(turnSpeed, new Vector3D(0,1,0));
				mouseController.reduceOffset(turnSpeed);
			}
			else if(offset<-0.01){
				head.rotate(-turnSpeed, new Vector3D(0,1,0));
				mouseController.increaseOffset(turnSpeed);
			}
		}
	//	System.out.println(heading);
	//	System.out.println(azimuth);
	}
	public void createNewGhost(UUID id, float x, float y, float z, String isNPC, String modelNum){
		boolean boolIsNPC = Boolean.valueOf(isNPC);
		Tank ghost = new Tank(x, y, z,Integer.parseInt(modelNum), boolIsNPC, this);
		physicsController.addTank(ghost);
		ghost.setUUID(id);
		if(boolIsNPC){
			npcController.createBehaviorTree(ghost);
		}
		for(SceneNode t : getGameWorld()){
			if(t instanceof Tank){
				if(id.compareTo(((Tank) t).getUUID())==0){
					((Tank) t).setDead(true);
					deadArray.addElement(t);	//removes duplicate tanks during respawns
				}
			}
		}
		ghostAvatars.addElement(ghost);
		ghostCreate.addElement(ghost);
	}
	public void updateGhost(UUID id, float x, float y, float z, String bodyRotationString, String headRotationString, String isNPC, String modelNum){
		boolean createNew = true;
		Matrix3D bodyRotation = fromStringtoMatrix3D(bodyRotationString);
		Matrix3D headRotation = fromStringtoMatrix3D(headRotationString);
		for(Tank t : ghostAvatars){
			if(id.compareTo(t.getUUID())==0){
				createNew=false;
			}
		}
		if(createNew){
			createNewGhost(id,x,y,z, isNPC, modelNum);
		}
		if(!createNew){
			for(Tank t : ghostAvatars){
				if(id.compareTo(t.getUUID())==0){
					Matrix3D mat = new Matrix3D();
					mat.setCol(3, new Vector3D(x,y,z));
					t.getPhysicsObject().setTransform(mat.getValues());
					t.setBodyRotation(bodyRotation);
					t.setHeadRotation(headRotation);
				}
			}
		}
	}
	public Matrix3D fromStringtoMatrix3D(String matrixString){
		String[] items = matrixString.split(" ");
		int count=0;
		Matrix3D matrix = new Matrix3D();
		for(int i=0; i<=3;i++){
			for(int j=0; j<=3;j++){
				matrix.setElementAt(i, j, Double.parseDouble(items[count]));
				count++;
			}
		}
		//System.out.println(matrix);
		return matrix;
	}
	//Shutdown
	public void shutdown(){
		super.shutdown();
		display.close();
		audioMgr.shutdown();
		//client shutdown
		if(thisClient!=null){
			thisClient.sendByeMessage();
			try{
				thisClient.shutdown();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
	}
	public void createBullet(UUID tankID){
		Bullet bullet = new Bullet(this);
		
		for(SceneNode s : getGameWorld()){
			if(s instanceof Tank){
				Tank t = (Tank) s;
				if(t.getUUID().compareTo(tankID)==0){
					firingTank = t;
				}
			}
		}
		Group tankBody = firingTank;
		if(firingTank.getReloadTimer()<1){
			bullet.setUUID(tankID);	//Sets ID to prevent friendly fire
			Leaf tankHead = firingTank.getTankHead();
			bullet.translate((float)tankHead.getWorldTranslation().elementAt(0, 3), (float)tankHead.getWorldTranslation().elementAt(1, 3), (float)tankHead.getWorldTranslation().elementAt(2, 3));
			bullet.getLocalRotation().concatenate(tankBody.getLocalRotation());
			Vector3D dir = new Vector3D(1,0,0);
			dir = dir.mult(tankHead.getLocalRotation());
			bullet.rotate(90, dir);
			bulletCreate.add(bullet);
			firingTank.resetReloadTimer();
			firingTank.getFireSound().play(100, false);
		}
	}
	//Checks bullet collisions
	public void checkBulletCollision(){
		for(SceneNode s : getGameWorld()){
			if(s instanceof Bullet){
				Bullet b = (Bullet)s;
				if(b.getBulletBody().getWorldBound().intersects(player.getTankBody().getWorldBound())){
					if(b.getUUID().compareTo(player.getUUID())!=0){
						player.takeDamage();
						deadArray.add(b);
					}
				}
			}
		}
		for(SceneNode s : getGameWorld()){
			if(s instanceof Bullet){
				Iterator<Tank> ghostIterator = ghostAvatars.iterator();
				Bullet b = (Bullet) s;
				while(ghostIterator.hasNext()){
					Tank ghostTank = ghostIterator.next();
					if(b.getBulletBody().getWorldBound().intersects(ghostTank.getTankBody().getWorldBound())){
						if(b.getUUID().compareTo(ghostTank.getUUID())!=0){
							deadArray.add(b);
							ghostTank.takeDamage();
							//increase score
							if(ghostTank.getHP()<=0){
								score++;
								player.resetHP();
							}
						}
					}
				}
			}
		}
	}
	//Remove tank
	public void removeTank(UUID tankID){
		Iterator<Tank> ghostIterator = ghostAvatars.iterator();
		while(ghostIterator.hasNext()){
			Tank ghostTank = ghostIterator.next();
			if(ghostTank.getUUID().compareTo(tankID)==0){
				ghostTank.setDead(true);
				deadArray.add(ghostTank);
			}
		}
	}
	//Create Terrain Block
	private TerrainBlock createTerBlock(AbstractHeightMap heightMap){
		float heightScale = .1f;
		Vector3D terrainScale = new Vector3D(1,heightScale,1);
		int terrainSize = heightMap.getSize();
		float cornerHeight = heightMap.getTrueHeightAtPoint(0, 0)*heightScale;
		Point3D terrainOrigin = new Point3D(0,-cornerHeight,0);
		String name = "Terrain:"+heightMap.getClass().getSimpleName();
		TerrainBlock tb = new TerrainBlock(name, terrainSize, terrainScale, heightMap.getHeightData(), terrainOrigin);
		return tb;
	}
	//Respawn
	public void respawn(int modelNum){
		modelNumber=modelNum;
		respawn=true;
	}
	public void setSpawn(String spawn){
		spawnIndice=Integer.parseInt(spawn);
	}
	//initsystem
	public void initSystem(){
		//create display
		display=(MyDisplaySystem)createDisplaySystem();
		setDisplaySystem(display);
		
		im = new InputManager();
		setInputManager(im);
		
		ArrayList<SceneNode> gameWorld = new ArrayList<SceneNode>();
		setGameWorld(gameWorld);
	}
	//create display
	public IDisplaySystem createDisplaySystem(){
		IDisplaySystem display;
		if(isFullScreen){
			display = new MyDisplaySystem(1280,1060, 32, 30, isFullScreen, "sage.renderer.jogl.JOGLRenderer");	//display if fs
		}else{
			display = new MyDisplaySystem(1280,1060, 32, 60, isFullScreen, "sage.renderer.jogl.JOGLRenderer");	//display if not fs
		}
		System.out.print("\nWaiting for display creation...");
		int count =0;
		while(!display.isCreated()){
			try{
				Thread.sleep(10);
			}
			catch(InterruptedException e){
				throw new RuntimeException("Display creation interrupted");
			}
			count++;
			System.out.print("+");
			if(count%80==0){
				System.out.println();
			}
			if(count>2000){
				throw new RuntimeException("Unable to create display");
			}
		}
		System.out.println();
		return display;
	}
	public void setLost(){
		lost=true;
	}
	/*  UNUSED
	public void toggleFullScreen(){
		if(isFullScreen){
			display.switchFS();
			isFullScreen=false;
		}
		else{
			isFullScreen=true;
		}
	}
	*/
	//Getters and Setters
	public void setIsConnected(boolean yesNo){
		isConnected=yesNo;
	}
	public boolean getIsConnected(){
		return isConnected;
	}
	public Tank getPlayer(){
		return player;
	}
	public Vector<Tank> getGhostAvatars(){
		return ghostAvatars;
	}
	public NPCController getNPCController(){
		return npcController;
	}
	public GameClient getThisClient(){
		return thisClient;
	}
	public TerrainBlock getTerrainBlock(){
		return terrain;
	}
	public void setUUID(UUID id){
		uuid = id;
	}
	public IAudioManager getAudioManager(){
		return audioMgr;
	}
}
