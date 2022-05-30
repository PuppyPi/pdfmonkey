package rebound.hci.graphics2d.gui.recomponent.components;

import java.awt.Graphics2D;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;

/**
 * This *does* make the sizes of *this* match up to the *child component*!  (And more formally, the coordinate spaces!)
 * That makes it *not* useful for zoomers/scrollpanes or borders or etc. XD
 * But perfect for overlays! :D
 * (Namely, overlays of components that control their own size, like canvases inside zoomers!)
 * (Contrast with {@link ReComponentPane}, which controls its child's size instead of being controlled by its child size; which makes it useful for normal things inside a window)
 */
public class ReComponentCanvasPane
extends AbstractReComponentHolder
{
	
	public ReComponentCanvasPane()
	{
		super();
	}
	
	public ReComponentCanvasPane(ReComponent containedComponent)
	{
		super(containedComponent);
	}
	
	
	@Override
	public Integer getWidth()
	{
		ReComponent c = getContainedComponent();
		return c == null ? 0 : c.getWidth();
	}
	
	@Override
	public Integer getHeight()
	{
		ReComponent c = getContainedComponent();
		return c == null ? 0 : c.getHeight();
	}
	
	@Override
	public void setSize(Integer width, Integer height) throws UnsupportedOperationException
	{
		if (containedComponent != null)
			containedComponent.setSize(width, height);
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
