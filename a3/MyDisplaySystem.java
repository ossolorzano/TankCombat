package a3;

import java.awt.Canvas;
import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import sage.display.DisplaySystem;
import sage.display.IDisplaySystem;
import sage.renderer.IRenderer;
import sage.renderer.RendererFactory;

public class MyDisplaySystem implements IDisplaySystem{
	private int w, h, depth, rate;
	private boolean isFS, isCreated;
	private IRenderer myRenderer;
	private Canvas rendererCanvas;
	private JFrame myFrame;
	private GraphicsDevice device;
	private DisplayMode displayMode;
	
	public MyDisplaySystem(int w, int h, int depth, int rate, boolean isFS, String rName){
		this.w=w;
		this.h=h;
		this.depth=depth;
		this.rate=rate;
		
		myRenderer = RendererFactory.createRenderer(rName);
		if(myRenderer == null){
			throw new RuntimeException("Unable to find renderer.");
		}
		rendererCanvas = myRenderer.getCanvas();
		myFrame = new JFrame("Tank Combat");
		myFrame.add(rendererCanvas);
		
		displayMode = new DisplayMode(w,h,depth,rate);
		initScreen(displayMode,isFS);
		DisplaySystem.setCurrentDisplaySystem(this);
		myFrame.setVisible(true);
		isCreated=true;
	}
	public void initScreen(DisplayMode displayMode, boolean FSRequested){
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		device = environment.getDefaultScreenDevice();
		if(device.isFullScreenSupported()&&FSRequested){
			myFrame.setUndecorated(true);
			myFrame.setResizable(false);
			myFrame.setIgnoreRepaint(true);
			
			device.setFullScreenWindow(myFrame);
			
			if(displayMode != null && device.isDisplayChangeSupported()){
				try{
					device.setDisplayMode(displayMode);
					myFrame.setSize(displayMode.getWidth(), displayMode.getHeight());
					isFS=true;
				}
				catch(Exception ex){
					System.err.println("Exception setting DisplayMode: "+ex);
				}
			}
			else{
				System.err.println("Cannot set display mode!");
			}
		}
		else{
			myFrame.setSize(displayMode.getWidth(), displayMode.getHeight());
			myFrame.setLocationRelativeTo(null);
			isFS=false;
		}
	}
	@Override
	public void addKeyListener(KeyListener kl) {
		myFrame.addKeyListener(kl);
		rendererCanvas.addKeyListener(kl);
	}

	@Override
	public void addMouseListener(MouseListener ml) {
		myFrame.addMouseListener(ml);
		rendererCanvas.addMouseListener(ml);
	}

	@Override
	public void addMouseMotionListener(MouseMotionListener mml) {
		myFrame.addMouseMotionListener(mml);
		rendererCanvas.addMouseMotionListener(mml);
	}

	@Override
	public void close() {
		if(device!=null){
			Window window = device.getFullScreenWindow();
			if(window != null){
				window.dispose();
			}
			device.setFullScreenWindow(null);
		}
	}

	@Override
	public void convertPointToScreen(Point arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getBitDepth() {
		return depth;
	}

	@Override
	public int getHeight() {
		return h;
	}

	@Override
	public int getRefreshRate() {
		return rate;
	}

	@Override
	public IRenderer getRenderer() {
		return myRenderer;
	}

	@Override
	public int getWidth() {
		return w;
	}

	@Override
	public boolean isCreated() {
		return isCreated;
	}

	@Override
	public boolean isFullScreen() {
		return isFS;
	}

	@Override
	public boolean isShowing() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setBitDepth(int depth) {
		this.depth=depth;
	}

	@Override
	public void setCustomCursor(String c) {
		Image image = new ImageIcon(c).getImage();
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0,0), "");
		myRenderer.getCanvas().setCursor(cursor);
	}

	@Override
	public void setHeight(int h) {
		this.h=h;
	}

	@Override
	public void setPredefinedCursor(int c) {
		
	}

	@Override
	public void setRefreshRate(int rate) {
		this.rate=rate;
	}

	@Override
	public void setTitle(String title) {
		myFrame.setTitle(title);
	}

	@Override
	public void setWidth(int w) {
		this.w=w;
	}
	public DisplayMode getDisplayMode(){
		return displayMode;
	}
}
