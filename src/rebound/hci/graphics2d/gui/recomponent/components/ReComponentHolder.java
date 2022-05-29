package rebound.hci.graphics2d.gui.recomponent.components;

import rebound.hci.graphics2d.gui.recomponent.ReComponent;

public interface ReComponentHolder
{
	public ReComponent getContainedComponent();
	public void setContainedComponent(ReComponent containedComponent);
}
