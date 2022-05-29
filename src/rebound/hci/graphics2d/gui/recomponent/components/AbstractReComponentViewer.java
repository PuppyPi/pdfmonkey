package rebound.hci.graphics2d.gui.recomponent.components;

/**
 * This does *not* make the sizes of the components match up!  (And more formally, their coordinate spaces!)
 * And it keeps its own size/shape/coordinate-system that generally is independent of what's inside it :3
 * That makes it useful for zoomers/scrollpanes or borders or etc. :>
 */
public abstract class AbstractReComponentViewer
extends AbstractReComponentHolder
{
	protected Integer width, height;
	
	@Override
	public Integer getWidth()
	{
		return width;
	}
	
	@Override
	public Integer getHeight()
	{
		return height;
	}
	
	@Override
	public void setSize(Integer width, Integer height) throws UnsupportedOperationException
	{
		this.width = width;
		this.height = height;
	}
}
