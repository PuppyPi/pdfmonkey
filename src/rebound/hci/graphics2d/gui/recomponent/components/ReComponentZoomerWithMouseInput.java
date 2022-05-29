package rebound.hci.graphics2d.gui.recomponent.components;

import static java.util.Objects.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import static rebound.util.BasicExceptionUtilities.*;
import java.awt.geom.Point2D;
import javax.annotation.Nullable;
import rebound.hci.graphics2d.gui.recomponent.MouseWheelExtraPointingEvent;
import rebound.hci.graphics2d.gui.simpletrackers.SimpleBroadcaster;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.DragReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.SimpleDragManager;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.broadcasters.DragReceiverBroadcaster;

public class ReComponentZoomerWithMouseInput
extends ReComponentZoomer
{
	protected double relativeZoomFactor = 1.5;
	protected double wheelScrollModeFactor = 50;  //pixels per wheel-click-amount ^wwww^
	protected @Nullable Runnable nullaryUpdateListenerForZoomChanging;
	
	protected Point2D translationAtDragStart;
	protected WheelMode wheelMode = WheelMode.Y;
	
	
	
	
	public static enum WheelMode
	{
		X,
		Y,
		Zoom,
	}
	
	
	
	
	
	
	
	
	protected SimpleDragManager dragger;
	protected DragReceiver draggerOurReceiver;
	protected DragReceiverBroadcaster dragBroadcaster;
	
	
	public ReComponentZoomerWithMouseInput()
	{
		initTrackers();
	}
	
	
	protected void initTrackers()
	{
		draggerOurReceiver = new DragReceiver()
		{
			@Override
			public void dragUpdated(int startX, int startY, int currentX, int currentY)
			{
				//newContentTranslation = pointNow - pointAtDragStart + originalValue;
				
				Point2D newContentTranslation = addVector(pointOrVector2D(currentX - startX, currentY - startY), translationAtDragStart);
				setCurrentCanvasTranslationAndScale(newContentTranslation.getX(), newContentTranslation.getY(), getCurrentScale());
				
				repaint();
			}
			
			@Override
			public void dragStarted(int startX, int startY)
			{
				translationAtDragStart = getCurrentCanvasTranslation();
				repaint();
			}
			
			@Override
			public void dragCompleted(int startX, int startY, int endX, int endY)
			{
			}
			
			@Override
			public void dragCancelled()
			{
				setCurrentCanvasTranslationAndScale(translationAtDragStart.getX(), translationAtDragStart.getY(), getCurrentScale());
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
	public void pointingExtraEvent(int x, int y, Object event)
	{
		if (event instanceof MouseWheelExtraPointingEvent)
		{
			double r = ((MouseWheelExtraPointingEvent)event).getRotationAmount();
			
			if (r != 0)
			{
				realWheelDeal(r);
				
				if (nullaryUpdateListenerForZoomChanging != null)
					this.nullaryUpdateListenerForZoomChanging.run();
				
				return;  //gobble it up :3
			}
		}
		
		super.pointingExtraEvent(x, y, event);
	}
	
	
	
	
	
	
	
	public void realWheelDeal(double wheelRotationAmount)   //^wwwwwwwwwwwwwww^
	{
		wheelRotationAmount = -wheelRotationAmount;
		
		WheelMode wheelMode = getWheelMode();
		
		if (wheelMode == WheelMode.Zoom)
			this.setScaleButKeepPointInDisplaySpaceCoincident(this.getCurrentScale() * Math.pow(relativeZoomFactor, wheelRotationAmount), intPointToFloat(mouseMotionTracker.getCurrentCursorPosition()));
		else if (wheelMode == WheelMode.X)
			this.setCurrentCanvasTranslationAndScale(this.getCurrentCanvasTranslationX() + wheelRotationAmount * wheelScrollModeFactor, getCurrentCanvasTranslationY(), getCurrentScale());
		else if (wheelMode == WheelMode.Y)
			this.setCurrentCanvasTranslationAndScale(getCurrentCanvasTranslationX(), this.getCurrentCanvasTranslationY() + wheelRotationAmount * wheelScrollModeFactor, getCurrentScale());
		else
			throw newUnexpectedHardcodedEnumValueExceptionOrNullPointerException(wheelMode);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public double getRelativeZoomFactor()
	{
		return relativeZoomFactor;
	}
	
	public void setRelativeZoomFactor(double relativeZoomFactor)
	{
		this.relativeZoomFactor = relativeZoomFactor;
	}
	
	public Runnable getNullaryUpdateListenerForZoomChanging()
	{
		return nullaryUpdateListenerForZoomChanging;
	}
	
	public void setNullaryUpdateListenerForZoomChanging(Runnable nullaryUpdateListenerForZoomChanging)
	{
		this.nullaryUpdateListenerForZoomChanging = nullaryUpdateListenerForZoomChanging;
	}
	
	public WheelMode getWheelMode()
	{
		return wheelMode;
	}
	
	public void setWheelMode(WheelMode wheelMode)
	{
		this.wheelMode = requireNonNull(wheelMode);
	}
	
	public boolean isDraggingEnabled()
	{
		return dragger.isDraggingEnabled();
	}
	
	public void setDraggingEnabled(boolean draggingEnabled)
	{
		dragger.setDraggingEnabled(draggingEnabled);
	}
	
	public double getWheelScrollModeFactor()
	{
		return wheelScrollModeFactor;
	}
	
	public void setWheelScrollModeFactor(double wheelScrollModeFactor)
	{
		this.wheelScrollModeFactor = wheelScrollModeFactor;
	}
}
