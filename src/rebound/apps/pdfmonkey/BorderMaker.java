package rebound.apps.pdfmonkey;

import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.AbstractBorderReComponent;

public interface BorderMaker
{
	public AbstractBorderReComponent newBorder();
	
	public default AbstractBorderReComponent newBorder(ReComponent child)
	{
		AbstractBorderReComponent b = newBorder();
		b.setContainedComponent(child);
		return b;
	}
}
