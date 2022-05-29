package rebound.hci.graphics2d.gui.recomponent;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;
import rebound.annotations.hints.IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser;
import rebound.hci.graphics2d.gui.simpletrackers.SimpleBroadcaster;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.FullButtonInputReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.CursorPositionTracker;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.PointingMotionInputReceiver.AbsolutePointingMotionInputReceiver;
import rebound.hci.graphics2d.java2d.BasicPaintable;

public interface ReComponent
extends BasicPaintable, AbsolutePointingMotionInputReceiver, CursorPositionTracker
{
	/**
	 * @return null for infinity (eg, canvases to be put inside scrollpanes)
	 */
	public @Nonnegative @Nullable Integer getWidth();
	
	/**
	 * @return null for infinity (eg, canvases to be put inside scrollpanes)
	 */
	public @Nonnegative @Nullable Integer getHeight();
	
	
	public default boolean containsPoint(int x, int y)
	{
		Integer w = this.getWidth();
		Integer h = this.getHeight();
		
		return
		(w == null || (x >= 0 && x < w)) &&
		(h == null || (y >= 0 && y < h));
	}
	
	public default boolean containsPoint(double x, double y)
	{
		Integer w = this.getWidth();
		Integer h = this.getHeight();
		
		return
		(w == null || (x >= 0 && x < w)) &&
		(h == null || (y >= 0 && y < h));
	}
	
	
	public void setSize(@Nonnegative @Nullable Integer width, @Nonnegative @Nullable Integer height) throws UnsupportedOperationException;
	
	
	
	public @Nullable Runnable getParentForRepainting();
	public void setParentForRepainting(@Nullable Runnable r);
	
	
	public default void repaint()
	{
		Runnable p = getParentForRepainting();
		
		if (p != null)
		{
			try
			{
				p.run();
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
			}
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * Note that this may be called even if it's out of bounds!
	 *  (Eg, on a canvas component by a scrollpane)
	 */
	@Override
	public void pointingAbsoluteMotion(int newX, int newY);
	
	public boolean getCurrentMouseButtonState(int buttonIndex);
	public void pointingButtonStateChange(int buttonIndex, boolean newState);
	public void pointingButtonsAllReleased();
	
	/**
	 * Eg, mouse wheel :3
	 */
	@IntendedToOptionallyBeSubclassedImplementedOrOverriddenByApiUser
	public default void pointingExtraEvent(int x, int y, Object event) {}
	
	public SimpleBroadcaster<AbsolutePointingMotionInputReceiver> getMouseMotionBroadcaster();
	public SimpleBroadcaster<FullButtonInputReceiver> getMouseButtonBroadcaster();
	
	
	
	public default FullButtonInputReceiver asMouseButtonReceiver()
	{
		return new FullButtonInputReceiver()
		{
			@Override
			public void setButtonState(int buttonIndex, boolean newState)
			{
				pointingButtonStateChange(buttonIndex, newState);
			}
			
			@Override
			public void setAllButtonsToReleasedSimultaneously()
			{
				pointingButtonsAllReleased();
			}
		};
	}
}
