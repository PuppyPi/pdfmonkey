package rebound.hci.graphics2d.gui.recomponent.components;

import javax.annotation.Nullable;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.simpletrackers.SimpleBroadcaster;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.FullButtonInputReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.SimpleButtonStateTracker;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.broadcasters.FullButtonInputReceiverBroadcaster;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.SimpleAbsoluteCursorPositionTracker;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.SimpleDragManager;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.broadcasters.AbsolutePointingMotionInputReceiverBroadcaster;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;

public abstract class AbstractReComponent
implements ReComponent
{
	protected @Nullable Runnable repaintParent;
	
	@Override
	public Runnable getParentForRepainting()
	{
		return repaintParent;
	}
	
	@Override
	public void setParentForRepainting(Runnable r)
	{
		this.repaintParent = r;
	}
	
	
	
	
	
	
	
	
	
	
	protected AbsolutePointingMotionInputReceiverBroadcaster mouseMotionBroadcaster = new AbsolutePointingMotionInputReceiverBroadcaster();
	protected FullButtonInputReceiverBroadcaster mouseButtonBroadcaster = new FullButtonInputReceiverBroadcaster();
	
	protected SimpleAbsoluteCursorPositionTracker mouseMotionTracker = new SimpleAbsoluteCursorPositionTracker(mouseMotionBroadcaster);
	protected SimpleButtonStateTracker mouseButtonTracker = new SimpleButtonStateTracker(mouseButtonBroadcaster);
	
	@Override
	public IntPoint getCurrentCursorPosition()
	{
		return mouseMotionTracker.getCurrentCursorPosition();
	}
	
	@Override
	public boolean getCurrentMouseButtonState(int buttonIndex)
	{
		return mouseButtonTracker.getButtonState(buttonIndex);
	}
	
	@Override
	public SimpleBroadcaster<AbsolutePointingMotionInputReceiver> getMouseMotionBroadcaster()
	{
		return mouseMotionBroadcaster;
	}
	
	@Override
	public SimpleBroadcaster<FullButtonInputReceiver> getMouseButtonBroadcaster()
	{
		return mouseButtonBroadcaster;
	}
	
	
	
	@Override
	public void pointingAbsoluteMotion(int newX, int newY)
	{
		mouseMotionTracker.pointingAbsoluteMotion(newX, newY);
	}
	
	public void pointingLost()
	{
		mouseMotionTracker.pointingLost();
		pointingButtonsAllReleased();
	}
	
	@Override
	public void pointingButtonStateChange(int buttonIndex, boolean newState)
	{
		mouseButtonTracker.setButtonState(buttonIndex, newState);
	}
	
	@Override
	public void pointingButtonsAllReleased()
	{
		mouseButtonTracker.setAllButtonsToReleasedSimultaneously();
	}
	
	
	
	
	
	
	
	public static final int DefaultButtonIndexForDragging = 0;  //whatever the "first" button is XD
	
	protected SimpleDragManager setupDragging()
	{
		return SimpleDragManager.setup(DefaultButtonIndexForDragging, mouseMotionTracker, mouseMotionBroadcaster, mouseButtonBroadcaster);
	}
}
