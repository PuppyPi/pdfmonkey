package rebound.hci.graphics2d.gui.awt.components;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import rebound.hci.graphics2d.gui.awt.AwtAndSimpleTrackersUtilities;
import rebound.hci.graphics2d.gui.awt.AwtAndSimpleTrackersUtilities.MouseSuite;
import rebound.hci.graphics2d.gui.recomponent.MouseWheelExtraPointingEvent;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentHolder;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.FullButtonInputReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.PointingMotionInputReceiver.AbsolutePointingMotionInputReceiver;

/**
 * An adapter from AWT to {@link ReComponent}!  :D
 * 
 * See {@link AwtAndSimpleTrackersUtilities} for mouse button index meanings!
 */
public class AwtPaneForReComponent
extends BasicReComponentHoldingAwtComponent
implements ReComponentHolder
{
	private static final long serialVersionUID = 1L;
	
	protected MouseSuite trackers;
	
	
	public AwtPaneForReComponent()
	{
		trackers = AwtAndSimpleTrackersUtilities.setupMouseSuite(this, new AbsolutePointingMotionInputReceiver()
		{
			@Override
			public void pointingLost()
			{
				if (containedComponent != null)
				{
					containedComponent.pointingLost();
					containedComponent.pointingButtonsAllReleased();
				}
			}
			
			@Override
			public void pointingAbsoluteMotion(int newX, int newY)
			{
				if (containedComponent != null)
					containedComponent.pointingAbsoluteMotion(newX, newY);
			}
		},
		
		new FullButtonInputReceiver()
		{
			@Override
			public void setButtonState(int buttonIndex, boolean newState)
			{
				if (containedComponent != null)
					containedComponent.pointingButtonStateChange(buttonIndex, newState);
			}
			
			@Override
			public void setAllButtonsToReleasedSimultaneously()
			{
				if (containedComponent != null)
					containedComponent.pointingButtonsAllReleased();
			}
		});
		
		
		
		this.addMouseWheelListener(new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e)
			{
				if (containedComponent != null)
					containedComponent.pointingExtraEvent(e.getX(), e.getY(), new MouseWheelExtraPointingEvent(e.getPreciseWheelRotation()));
			}
		});
	}
	
	
	
	
	
	
	@Override
	public void setSize(int width, int height)
	{
		super.setSize(width, height);
		
		if (containedComponent != null)
			containedComponent.setSize(width, height);
	}
	
	@Override
	protected void initNewContained(ReComponent containedComponent)
	{
		containedComponent.setSize(this.getWidth(), this.getHeight());
	}
	
	
	
	
	@Override
	public void paint(Graphics g)
	{
		if (containedComponent != null)
			containedComponent.paint((Graphics2D) g);
	}
}
