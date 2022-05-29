package rebound.hci.graphics2d.gui.recomponent.components;

import rebound.hci.graphics2d.gui.recomponent.ReComponent;

public abstract class AbstractReComponentContainer
extends AbstractReComponent
{
	protected void installChild(ReComponent c)
	{
		c.setParentForRepainting(this::repaint);
	}
	
	protected void deinstallChild(ReComponent c)
	{
		c.setParentForRepainting(null);
	}
}
