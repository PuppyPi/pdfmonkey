package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.util.Primitives.*;
import java.awt.Graphics2D;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;

/**
 * This *does* make the sizes of the components match up!  (And more formally, their coordinate spaces!)
 * That makes it *not* useful for zoomers/scrollpanes or borders or etc. XD
 * But perfect for overlays! :D
 */
public class ReComponentPane
extends AbstractReComponentHolder
{
	//Defaulting to Infinite causes problems with contained components that don't support that!  (But everything supports finiteness..hopefully XD'   If not, just don't allow the containedComponent to be provided in the constructor but only through setContainedComponent(), and ask them nicely to only set it after calling setSize()  :3 )
	protected Integer width = 0;
	protected Integer height = 0;
	
	
	public ReComponentPane()
	{
		super();
	}
	
	public ReComponentPane(ReComponent containedComponent)
	{
		super(containedComponent);
	}
	
	
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
		if (!eq(width, this.width) || !eq(height, this.height))
		{
			this.width = width;
			this.height = height;
			
			if (containedComponent != null)
				containedComponent.setSize(width, height);
		}
	}
	
	@Override
	protected void installChild(ReComponent containedComponent)
	{
		super.installChild(containedComponent);
		containedComponent.setSize(this.getWidth(), this.getHeight());
	}
	
	
	@Override
	public void paint(Graphics2D g)
	{
		if (containedComponent != null)
			containedComponent.paint(g);
	}
	
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(IntPoint p)
	{
		//Identity transform! :D
		return containedComponent.containsPoint(p.getX(), p.getY()) ? p : null;
	}
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpace(IntPoint p)
	{
		//Identity transform! :D
		return p;
	}
}
