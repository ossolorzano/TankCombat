package a3.controller;

import graphicslib3D.Point3D;
import graphicslib3D.Vector3D;
import sage.camera.ICamera;
import sage.input.IInputManager;
import sage.scene.SceneNode;
import sage.util.MathUtils;

public class CameraController {
	private ICamera camera;
	private SceneNode target;
	private float azimuth;
	private float elevation;
	private float distance;
	private float cameraOffset;
	private Point3D targetPos;
	private Vector3D worldUpVec;
	
	public CameraController(ICamera camera, SceneNode target, IInputManager inputMgr, String controllerName){
		this.camera = camera;
		this.target = target;
		worldUpVec = new Vector3D(0.0f,1.0f,0.0f);
		distance = 8;
		azimuth=180;
		elevation = 20;
		cameraOffset = 1;
		update(0.0f);
		setupInput(inputMgr, controllerName);	//unused
	}
	public void update(float time){
		targetPos = new Point3D(target.getWorldTranslation().getCol(3));
		updateCameraPosition();
		Point3D pointOffset = new Point3D(0.0f,cameraOffset,0.0f);
		camera.lookAt(targetPos.add(pointOffset), worldUpVec);
	}
	public void updateCameraPosition(){
		if(azimuth>360){
			azimuth-=360;
		}
		if(azimuth<0){
			azimuth+=360;
		}
		Point3D relativePosition = MathUtils.sphericalToCartesian(azimuth,elevation,distance);
		Point3D desiredCameraLoc = relativePosition.add(targetPos);
		camera.setLocation(desiredCameraLoc);
	}
	public void setupInput(IInputManager im, String kb){
		
	}
	//getters and setters
	public float getCameraOffset(){
		return cameraOffset;
	}
	public void setCameraOffset(float newCameraOffset){
		cameraOffset=newCameraOffset;
	}
	public float getAzimuth(){
		return azimuth;
	}
	public void setAzimuth(float newAzimuth){
		azimuth = newAzimuth;
	}
}
