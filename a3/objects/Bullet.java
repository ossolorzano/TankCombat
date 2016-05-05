package a3.objects;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.UUID;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import a3.TankBattlesGame;
import a3.actions.MoveUpwardAction;
import graphicslib3D.Point3D;
import sage.audio.AudioResource;
import sage.audio.AudioResourceType;
import sage.audio.Sound;
import sage.audio.SoundType;
import sage.input.action.IAction;
import sage.model.loader.OBJLoader;
import sage.scene.Group;
import sage.scene.TriMesh;
import sage.scene.bounding.BoundingSphere;
import sage.texture.Texture;
import sage.texture.TextureManager;

public class Bullet extends Group implements IMovable{
	private OBJLoader objLoader;
	private TriMesh fullBullet;
	private Texture bulletTexture;
	private float scaleFactor;
	private float movementSpeed;
	private IAction bulletForward;
	private double lifeTime;
	private boolean isDead;
	private UUID tankID;
	private ScriptEngineManager factory;
	private String scriptFileName;
	private ScriptEngine jsEngine;
	private double tmp;
	private TankBattlesGame game;
	private Sound bulletSound;
	
	public Bullet(TankBattlesGame game){
		this.game=game;
		//Do script functions
		factory = new ScriptEngineManager();
		scriptFileName = "javascripts/bulletAttributes.js";
		jsEngine = factory.getEngineByName("js");
		runScript(jsEngine,scriptFileName);
		
		lifeTime=(double) jsEngine.get("lifeTime");
		isDead=false;
		tmp = (double) jsEngine.get("movementSpeed");
		movementSpeed = (float) tmp;
		tmp = (double) jsEngine.get("scaleFactor");
		scaleFactor = (float) tmp;
		//Create Model
		objLoader = new OBJLoader();
		objLoader.setShowWarnings(false);
		
		fullBullet = new TriMesh();
		fullBullet=objLoader.loadModel("models/bullet.obj");
		//Texture model
		bulletTexture = TextureManager.loadTexture2D("textures/bullet.png");
		bulletTexture.setApplyMode(sage.texture.Texture.ApplyMode.Replace);
		
		fullBullet.setTexture(bulletTexture);
		//((BoundingSphere)fullBullet.getLocalBound()).setRadius(0.05f);
		//fullBullet.setShowBound(true);
		addChild(fullBullet);
		
		scale(scaleFactor,scaleFactor, scaleFactor);
		bulletForward = new MoveUpwardAction(this);
		initAudio();
	}
	public void checkLifeTime(double newTime){
		lifeTime-=newTime;
		if(lifeTime<0){
			isDead=true;
		}
	//	System.out.println(lifeTime);
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
	public void initAudio(){
		AudioResource resource1;
		resource1 = game.getAudioManager().createAudioResource("sounds/bullet.wav", AudioResourceType.AUDIO_SAMPLE);
		
		bulletSound = new Sound(resource1, SoundType.SOUND_EFFECT,100,true);
		bulletSound.initialize(game.getAudioManager());
		
		bulletSound.setMaxDistance(1000f);
		bulletSound.setMinDistance(1f);
		bulletSound.setRollOff(1f);
	}
	public TriMesh getBulletBody(){
		return fullBullet;
	}
	public Sound getBulletSound(){
		return bulletSound;
	}
	public void setUUID(UUID tankID){
		this.tankID = tankID;
	}
	public UUID getUUID(){
		return tankID;
	}
	public boolean getIsDead(){
		return isDead;
	}
	public void setIsDead(boolean yesNo){
		isDead=yesNo;
	}
	public float getMovementSpeed(){
		return movementSpeed;
	}
	public IAction getBulletForward(){
		return bulletForward;
	}
}
