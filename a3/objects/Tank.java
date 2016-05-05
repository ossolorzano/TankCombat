package a3.objects;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import graphicslib3D.Matrix3D;
import graphicslib3D.Point3D;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.event.EventManager;
import sage.event.IEventListener;
import sage.event.IEventManager;
import sage.event.IGameEvent;
import sage.model.loader.OBJLoader;
import sage.model.loader.ogreXML.OgreXMLParser;
import sage.scene.Group;
import sage.scene.Model3DTriMesh;
import sage.scene.SceneNode;
import sage.scene.TriMesh;
import sage.scene.bounding.BoundingSphere;
import sage.texture.Texture;
import sage.texture.TextureManager;
import javax.script.*;

import a3.TankBattlesGame;
import a3.events.DamageEvent;

public class Tank extends Group implements IMovable, IEventListener{
	private OBJLoader objLoader;
	private OgreXMLParser ogreLoader;
	private Group trackGroup;
	private Group trackRotateGroup;
	private Model3DTriMesh leftTracks, rightTracks;
	private TriMesh tankBody;
	private TriMesh tankHead;
	private Texture tankBodyTexture, tankHeadTexture, rightTrackTexture, leftTrackTexture;
	private Texture tankBodyTarnish1, tankBodyTarnish2, tankHeadTarnish1, tankHeadTarnish2;
	private float movementSpeed;
	private float rotateSpeed;
	private UUID id;
	private int HP, modelNum;
	private ScriptEngineManager factory;
	private String scriptFileName;
	private ScriptEngine jsEngine;
	private double tmp;
	private boolean isNPC;
	private boolean isDead=false;
	private float reloadTimer;
	private Sound engineSound, fireSound, hitSound, deadSound;
	private TankBattlesGame game;
	private IEventManager eventMgr;
	
	public Tank(float x, float y, float z, int model,boolean isNPC, TankBattlesGame game){
		eventMgr=EventManager.getInstance();
		this.game=game;
		modelNum=model;
		//set reload timer
		reloadTimer =0;
		//set NPC
		this.isNPC = isNPC;
		//Do script functions
		factory = new ScriptEngineManager();
		scriptFileName = "javascripts/tankAttributes.js";
		jsEngine = factory.getEngineByName("js");
		runScript(jsEngine,scriptFileName);
		
		HP=(int) jsEngine.get("HP");
		tmp = (double) jsEngine.get("movementSpeed");
		movementSpeed=  (float)tmp;
		tmp = (double)jsEngine.get("rotateSpeed");
		rotateSpeed =  (float) tmp;
		//Create model
		objLoader = new OBJLoader();
		objLoader.setShowWarnings(false);
		createTracks();
		createModel(modelNum);
				
		tankBody.setTexture(tankBodyTexture);
		tankHead.setTexture(tankHeadTexture);
		
		addChild(tankBody);
		addChild(tankHead);
		
		((BoundingSphere)tankBody.getLocalBound()).setRadius(1.5f);
		((BoundingSphere)tankHead.getLocalBound()).setRadius(0);
		//tankBody.setShowBound(true);
		//tankHead.setShowBound(true);
		translate(0.0f, 1.10f, 0.0f); //translates everything to be above y=0		
		//translate to new initial location
		translate(x,y,z);
		initAudio();
	}
	public void createModel(int model){
		if(model==1){
			tankBody = new TriMesh();
			tankBody=objLoader.loadModel("models/tank1.obj");
			tankBody.scale(1, 1, 1);
			tankHead = new TriMesh();
			tankHead=objLoader.loadModel("models/tank1Head.obj");
			tankHead.scale(0.9f, 0.9f, 0.9f);
			tankHead.translate(0.0f, 0.95f, 0.0f); //raise head to sit on top of body
			
			leftTracks.scale(1,1,1);
			rightTracks.scale(1,1,1);
			//Texture Model
			tankBodyTexture = TextureManager.loadTexture2D("textures/tank1.png");
			tankBodyTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTexture = TextureManager.loadTexture2D("textures/tank1Head.png");
			tankHeadTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			
			tankBodyTarnish1 = TextureManager.loadTexture2D("textures/tank1Tarnish1.png");
			tankBodyTarnish1.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankBodyTarnish2 = TextureManager.loadTexture2D("textures/tank1Tarnish2.png");
			tankBodyTarnish2.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTarnish1 = TextureManager.loadTexture2D("textures/tank1HeadTarnish1.png");
			tankHeadTarnish1.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTarnish2 = TextureManager.loadTexture2D("textures/tank1HeadTarnish2.png");
			tankHeadTarnish2.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		}
		if(model==2){
			tankBody = new TriMesh();
			tankBody=objLoader.loadModel("models/tank2.obj");
			tankBody.scale(1, 1.1f, 0.9f);
			tankBody.translate(0, 0.1f, 0);
			tankHead = new TriMesh();
			tankHead=objLoader.loadModel("models/tank2Head.obj");
			tankHead.scale(1f, 1f, 1f);
			tankHead.translate(0.0f, 0.95f, 0.0f); //raise head to sit on top of body
			
			leftTracks.scale(1,1.0f, 0.9f);
			rightTracks.scale(1,1.0f,0.9f);
			leftTracks.translate(0, 0.05f, 0);
			rightTracks.translate(0, 0.05f, 0);
			//Texture Model
			tankBodyTexture = TextureManager.loadTexture2D("textures/tank2.png");
			tankBodyTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTexture = TextureManager.loadTexture2D("textures/tank2Head.png");
			tankHeadTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			
			tankBodyTarnish1 = TextureManager.loadTexture2D("textures/tank2Tarnish1.png");
			tankBodyTarnish1.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankBodyTarnish2 = TextureManager.loadTexture2D("textures/tank2Tarnish2.png");
			tankBodyTarnish2.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTarnish1 = TextureManager.loadTexture2D("textures/tank2HeadTarnish1.png");
			tankHeadTarnish1.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
			tankHeadTarnish2 = TextureManager.loadTexture2D("textures/tank2HeadTarnish2.png");
			tankHeadTarnish2.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		}
	}
	public void createTracks(){
		//create animated model part
				ogreLoader = new OgreXMLParser();
				try{
					//load left tracks
					trackGroup = ogreLoader.loadModel("models/Cube.mesh.xml",
												  "materials/tracks.material",
												  "models/Cube.skeleton.xml");
					trackGroup.updateGeometricState(0,true);
					java.util.Iterator <SceneNode> modelIterator = trackGroup.iterator();
					leftTracks = (Model3DTriMesh) modelIterator.next();
					//load right tracks
					trackGroup = ogreLoader.loadModel("models/Cube.mesh.xml",
							  "materials/tracks.material",
							  "models/Cube.skeleton.xml");
					trackGroup.updateGeometricState(0,true);
					modelIterator = trackGroup.iterator();
					rightTracks = (Model3DTriMesh) modelIterator.next();
				}
				catch(Exception e){
					e.printStackTrace();
					System.exit(1);
				}
				//group up tracks to fix rotation
				trackRotateGroup = new Group();
				createTrackTexture();
				leftTracks.setTexture(leftTrackTexture);
				leftTracks.updateGeometricState(0, true);
				leftTracks.updateRenderStates();
				rightTracks.setTexture(rightTrackTexture);
				rightTracks.updateRenderStates();
				rightTracks.updateGeometricState(0, true);
				
				trackRotateGroup.addChild(leftTracks);
				leftTracks.translate(0.85f, -0.7f, -0.3f);
				trackRotateGroup.addChild(rightTracks);
				rightTracks.translate(-0.85f, -0.7f, -0.3f);
				addChild(trackRotateGroup);
				trackRotateGroup.setIsTransformSpaceParent(true);
				leftTracks.setIsTransformSpaceParent(true);
				rightTracks.setIsTransformSpaceParent(true);
	}
	public void runScript(ScriptEngine engine, String scriptFileName){
		try{
			FileReader fileReader = new FileReader(scriptFileName);
			engine.eval(fileReader);
			fileReader.close();
		}
		catch(FileNotFoundException e1){
			System.out.println(scriptFileName+" not found " +e1);
		}
		catch(IOException e2){
			System.out.println("IO problem with "+scriptFileName+" "+e2);
		}
		catch(ScriptException e3){
			System.out.println("Script Exception in "+scriptFileName+" "+e3);
		}
		catch(NullPointerException e4){
			System.out.println("Null pointer exception in "+scriptFileName+" "+e4);
		}
	}
	public void createTrackTexture(){
		leftTrackTexture = TextureManager.loadTexture2D("textures/tracks.png");
		leftTrackTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		rightTrackTexture = TextureManager.loadTexture2D("textures/tracks.png");
		rightTrackTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
	}
	public void setBodyRotation(Matrix3D rotation){
		setLocalRotation(rotation);
	}
	public void setHeadRotation(Matrix3D rotation){
		tankHead.setLocalRotation(rotation);
	}
	//audio
	public void initAudio(){
		AudioResource resource1, resource2, resource3, resource4;
		resource1 = game.getAudioManager().createAudioResource("sounds/fire.wav", AudioResourceType.AUDIO_SAMPLE);
		resource2 = game.getAudioManager().createAudioResource("sounds/engine.wav", AudioResourceType.AUDIO_SAMPLE);
		resource3 = game.getAudioManager().createAudioResource("sounds/hit.wav", AudioResourceType.AUDIO_SAMPLE);
		resource4 = game.getAudioManager().createAudioResource("sounds/death.wav", AudioResourceType.AUDIO_SAMPLE);
		
		fireSound = new Sound(resource1, SoundType.SOUND_EFFECT,100,false);
		engineSound = new Sound(resource2, SoundType.SOUND_EFFECT,100,true);
		hitSound = new Sound(resource3, SoundType.SOUND_EFFECT,100,false);
		deadSound = new Sound(resource4, SoundType.SOUND_EFFECT,100,false);
		fireSound.initialize(game.getAudioManager());
		engineSound.initialize(game.getAudioManager());
		hitSound.initialize(game.getAudioManager());
		deadSound.initialize(game.getAudioManager());
		
		fireSound.setMaxDistance(1000f);
		fireSound.setMinDistance(0f);
		fireSound.setRollOff(0.5f);
		engineSound.setMaxDistance(1000f);
		engineSound.setMinDistance(5f);
		engineSound.setRollOff(5f);
		hitSound.setMaxDistance(1000f);
		hitSound.setMinDistance(0f);
		hitSound.setRollOff(5);
		deadSound.setMaxDistance(1000f);
		deadSound.setMinDistance(0f);
		deadSound.setRollOff(5);
	}
	//sets UUID
	public void setUUID(UUID id){
		this.id=id;
	}
	public UUID getUUID(){
		return id;
	}
	public Point3D getLocation(){
		return new Point3D(getWorldTranslation().elementAt(0, 3),getWorldTranslation().elementAt(1,3),getWorldTranslation().elementAt(2, 3));
	}
	public void setLocation(float x, float y, float z){
		Matrix3D newLocation = new Matrix3D();
		newLocation.setElementAt(0, 3, x);
		newLocation.setElementAt(1, 3, y);
		newLocation.setElementAt(2, 3, z);
		setLocalTranslation(newLocation);
	}
	public Matrix3D getMatrix(){
		return getWorldTranslation();
	}
	//returns whole tank
	public TriMesh getTankBody(){
		return tankBody;
	}
	public TriMesh getTankHead(){
		return tankHead;
	}
	public Model3DTriMesh getLeftTracks(){
		return leftTracks;
	}
	public Model3DTriMesh getRightTracks(){
		return rightTracks;
	}
	public float getMovementSpeed(){
		return movementSpeed;
	}
	public float getRotateSpeed(){
		return rotateSpeed;
	}
	public int getHP(){
		return HP;
	}
	public void takeDamage(){
		HP-=10;
		DamageEvent de = new DamageEvent();
		eventMgr.triggerEvent(de);
		eventMgr.addListener(this, DamageEvent.class);
		System.out.println(HP);
	}
	public void setDead(boolean yesNo){
		isDead=yesNo;
	}
	public Boolean isDead(){
		return isDead;
	}
	public boolean isNPC(){
		return isNPC;
	}
	public float getReloadTimer(){
		return reloadTimer;
	}
	public void resetReloadTimer(){
		reloadTimer=3000;
	}
	public void reduceReloadTimer(float time){
		reloadTimer-=time;
	}
	public Sound getEngineSound(){
		return engineSound;
	}
	public Sound getFireSound(){
		return fireSound;
	}
	public Sound getHitSound(){
		return hitSound;
	}
	public Sound getDeadSound(){
		return deadSound;
	}
	public int getModelNumber(){
		return modelNum;
	}
	public void resetHP(){
		HP=100;
		tankBody.setTexture(tankBodyTexture);
		tankHead.setTexture(tankHeadTexture);
	}
	@Override
	public boolean handleEvent(IGameEvent e) {
		if(HP<=60){
			tankBody.setTexture(tankBodyTarnish1);
			tankHead.setTexture(tankHeadTarnish1);
		}
		if(HP<=30){
			tankBody.setTexture(tankBodyTarnish2);
			tankHead.setTexture(tankHeadTarnish2);
		}
		if(HP>0){
			hitSound.play();
		}
		if(HP<=0){
			isDead=true;
			deadSound.play();
		}
		return true;
	}
}
