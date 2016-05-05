package a3.objects.hud;

import sage.renderer.IRenderer;
import sage.scene.HUDObject;
import sage.scene.shape.Rectangle;

public class CrosshairObject extends HUDObject{
	private Rectangle leftRect, rightRect;
	public CrosshairObject(){
		leftRect = new Rectangle(0.05f,0.005f);
		rightRect= new Rectangle(0.005f,0.05f);
	}
	@Override
	public void draw(IRenderer renderer) {
		renderer.draw(leftRect);
		renderer.draw(rightRect);
	}

}
