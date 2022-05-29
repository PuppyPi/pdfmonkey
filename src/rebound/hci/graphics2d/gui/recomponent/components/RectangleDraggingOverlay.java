package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.hci.graphics2d.ColorUtilities.*;
import static rebound.math.geom2d.SmallIntegerBasicGeometry2D.*;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.annotation.Nullable;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.simpletrackers.SimpleBroadcaster;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.DragReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.SimpleDragManager;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.broadcasters.DragReceiverBroadcaster;
import rebound.hci.graphics2d.java2d.Java2DUtilities;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;
import rebound.math.geom.ints.analogoustojavaawt.IntRectangle;

public class RectangleDraggingOverlay
extends ReComponentPane
{
	protected SimpleDragManager dragger;
	protected DragReceiver draggerOurReceiver;
	protected DragReceiverBroadcaster dragBroadcaster;
	
	protected @Nullable Color dragBoxColor = null;  //null makes it invisible :3
	
	
	
	
	
	public RectangleDraggingOverlay()
	{
		initTrackers();
	}
	
	public RectangleDraggingOverlay(ReComponent underlyingComponent, DragReceiver dragReceiver)
	{
		initTrackers();
		dragBroadcaster.addReceiver(dragReceiver);
		setContainedComponent(underlyingComponent);
	}
	
	protected void initTrackers()
	{
		draggerOurReceiver = new DragReceiver()
		{
			@Override
			public void dragUpdated(int startX, int startY, int currentX, int currentY)
			{
				repaint();
			}
			
			@Override
			public void dragStarted(int startX, int startY)
			{
				repaint();
			}
			
			@Override
			public void dragCompleted(int startX, int startY, int endX, int endY)
			{
				repaint();
			}
			
			@Override
			public void dragCancelled()
			{
				repaint();
			}
		};
		
		dragger = setupDragging();
		dragBroadcaster = new DragReceiverBroadcaster();
		dragBroadcaster.addReceiver(draggerOurReceiver);
		dragger.setDragReceiver(dragBroadcaster);
	}
	
	public SimpleBroadcaster<DragReceiver> getDraggingEventBroadcaster()
	{
		return dragBroadcaster;
	}
	
	
	
	
	
	
	
	
	@Override
	public void paint(Graphics2D g)
	{
		Java2DUtilities.drawWithCopy(g, super::paint);
		paintRectangle(g);
	}
	
	
	
	protected void paintRectangle(Graphics2D g)
	{
		if (isInDrag())
		{
			Color dragBoxColor = this.dragBoxColor;
			
			if (dragBoxColor != null && !isTransparent(dragBoxColor))
			{
				//Paint the current drag box rectangle!! :DDD
				
				IntPoint now = mouseMotionTracker.getCurrentCursorPosition();
				
				if (now != null)
				{
					IntRectangle dragBox = irectTwoPoints(ipoint(dragger.getDragStartX(), dragger.getDragStartY()), now);
					
					g.setColor(dragBoxColor);
					g.drawRect(dragBox.x - 1, dragBox.y - 1, dragBox.width + 1, dragBox.height + 1);
				}
			}
		}
	}
	
	
	
	
	
	
	
	public boolean isInDrag()
	{
		return dragger.isInDrag();
	}
	
	public void cancelDrag()
	{
		dragger.cancelDrag();
	}
	
	
	public @Nullable IntPoint getCurrentMousePosition()
	{
		return mouseMotionTracker.getCurrentCursorPosition();
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public Color getDragBoxColor()
	{
		return dragBoxColor;
	}
	
	public void setDragBoxColor(Color dragBoxColor)
	{
		if (dragBoxColor != this.dragBoxColor)
		{
			this.dragBoxColor = dragBoxColor;
			
			if (isInDrag())
				repaint();
		}
	}
	
	
	
	
	public boolean isDraggingEnabled()
	{
		return dragger.isDraggingEnabled();
	}
	
	public void setDraggingEnabled(boolean draggingEnabled)
	{
		dragger.setDraggingEnabled(draggingEnabled);
	}
}
