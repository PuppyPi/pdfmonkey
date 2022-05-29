package rebound.hci.graphics2d.gui.awt.components;

import java.awt.Component;
import javax.annotation.Nullable;
import rebound.annotations.hints.IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentHolder;

/**
 * This does *not* make the sizes of the components match up!
 * That makes it useful for zoomers/scrollpanes or borders or etc. :>
 */
public class BasicReComponentHoldingAwtComponent
extends Component
implements ReComponentHolder
{
	private static final long serialVersionUID = 1L;
	
	
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
				this.containedComponent.setParentForRepainting(null);
			
			if (containedComponent != null)
			{
				containedComponent.setParentForRepainting(this::repaint);
				initNewContained(containedComponent);
			}
			
			this.containedComponent = containedComponent;
			
			repaint();
		}
	}
	
	@IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser
	protected void initNewContained(ReComponent containedComponent)
	{
	}
}
