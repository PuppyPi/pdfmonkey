package rebound.hci.graphics2d.gui.recomponent.components;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;

/**
 * This does *not* make the sizes of the components match up!
 * That makes it useful for zoomers/scrollpanes or borders or etc. :>
 */
public abstract class AbstractReComponentHolder
extends AbstractReComponentContainer
implements ReComponent, ReComponentHolder
{
	protected @Nullable ReComponent containedComponent = null;
	
	@Override
	public ReComponent getContainedComponent()
	{
		return containedComponent;
	}
	
	@Override
	public void setContainedComponent(ReComponent containedComponent)
	{
		if (this.containedComponent != containedComponent)
		{
			if (this.containedComponent != null)
				deinstallChild(this.containedComponent);
			
			if (containedComponent != null)
				installChild(containedComponent);
			
			this.containedComponent = containedComponent;
			
			repaint();
		}
	}
	
	
	
	
	
	
	
	
	
	public abstract @Nonnull IntPoint transformFromOurSpaceToContainedSpace(IntPoint p);
	public abstract @Nullable IntPoint transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(IntPoint p);
	//Todo public abstract @Nonnull IntPoint transformFromContainedSpaceToOurSpace(IntPoint p);
	//Todo public abstract @Nullable IntPoint transformFromContainedSpaceToOurSpaceOrNullIfOutOfBounds(IntPoint p);
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Todo dragging isn't properly dealt with here so we just enable all pointing events to pass through X'D
	//			+ We really should've just used Y shouldn't we've?  X'D
	
	
	@Override
	public void pointingAbsoluteMotion(int newX, int newY)
	{
		super.pointingAbsoluteMotion(newX, newY);
		
		if (containedComponent != null)
		{
			IntPoint p = transformFromOurSpaceToContainedSpace(new IntPoint(newX, newY));
			
			containedComponent.pointingAbsoluteMotion(p.getX(), p.getY());
		}
	}
	
	@Override
	public void pointingLost()
	{
		super.pointingLost();
		
		if (containedComponent != null)
			containedComponent.pointingLost();
	}
	
	
	/**
	 * Override this if/when/how you want to stop mouse button events from propagating to the contained component :3
	 */
	@Override
	public void pointingButtonStateChange(int buttonIndex, boolean newState)
	{
		super.pointingButtonStateChange(buttonIndex, newState);
		
		if (containedComponent != null)
			containedComponent.pointingButtonStateChange(buttonIndex, newState);
	}
	
	
	/**
	 * Override this if/when/how you want to stop mouse button events from propagating to the contained component :3
	 */
	@Override
	public void pointingButtonsAllReleased()
	{
		super.pointingButtonsAllReleased();
		
		if (containedComponent != null)
			containedComponent.pointingButtonsAllReleased();
	}
	
	
	public void pointingExtraEvent(int x, int y, Object event)
	{
		super.pointingExtraEvent(x, y, event);
		
		if (containedComponent != null)
		{
			IntPoint p = transformFromOurSpaceToContainedSpace(new IntPoint(x, y));
			
			containedComponent.pointingExtraEvent(p.getX(), p.getY(), event);
		}
	}
}
