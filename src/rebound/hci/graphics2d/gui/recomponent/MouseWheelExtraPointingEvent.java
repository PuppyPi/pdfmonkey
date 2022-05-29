package rebound.hci.graphics2d.gui.recomponent;

/**
 * @see ReComponent#pointingExtraEvent(int, int, Object)
 */
public class MouseWheelExtraPointingEvent
{
	protected final double rotationAmount;
	
	public MouseWheelExtraPointingEvent(double rotationAmount)
	{
		this.rotationAmount = rotationAmount;
	}
	
	public double getRotationAmount()
	{
		return rotationAmount;
	}
}
