package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering;

import java.awt.Image;

public class ImageAndActualRegion
{
	protected final Image image;
	protected final double actualXInDisplaySpace;
	protected final double actualYInDisplaySpace;
	
	public ImageAndActualRegion(Image image, double actualXInDisplaySpace, double actualYInDisplaySpace)
	{
		this.image = image;
		this.actualXInDisplaySpace = actualXInDisplaySpace;
		this.actualYInDisplaySpace = actualYInDisplaySpace;
	}
	
	public Image getImage()
	{
		return image;
	}
	
	public double getActualXInDisplaySpace()
	{
		return actualXInDisplaySpace;
	}
	
	public double getActualYInDisplaySpace()
	{
		return actualYInDisplaySpace;
	}
}
