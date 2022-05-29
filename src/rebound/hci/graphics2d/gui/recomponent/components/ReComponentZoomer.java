package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.math.SmallFloatMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import rebound.exceptions.UnsupportedOptionException;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;
import rebound.math.geom2d.TranslationAndUniformScaleTransform2D;

public class ReComponentZoomer
extends AbstractReComponentViewer
implements BasicZoomer
{
	protected boolean lockMode = true;
	
	protected double xPositioningFactorIfCanvasIsTooSmall = 0.5;
	protected double yPositioningFactorIfCanvasIsTooSmall = 0.5;
	
	protected double x = 0;
	protected double y = 0;
	protected double scale = 1;
	
	
	
	
	@Override
	public void paint(Graphics2D g)
	{
		if (containedComponent != null)
		{
			Graphics2D g2d = (Graphics2D)g;
			g2d.transform(getCurrentTransformCanvasSpaceToDisplaySpace());
			containedComponent.paint(g2d);
		}
	}
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(IntPoint p)
	{
		Point2D pp = transformPointFromDisplaySpaceToCanvasSpace(pointOrVector2D(p.getX(), p.getY()));
		
		if (getContainedComponent().containsPoint(pp.getX(), pp.getY()))
			return new IntPoint(roundClosestArbtiesS32(pp.getX()), roundClosestArbtiesS32(pp.getY()));
		else
			return null;
	}
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpace(IntPoint p)
	{
		Point2D pp = transformPointFromDisplaySpaceToCanvasSpace(pointOrVector2D(p.getX(), p.getY()));
		return new IntPoint(roundClosestArbtiesS32(pp.getX()), roundClosestArbtiesS32(pp.getY()));
	}
	
	
	@Override
	public int getWindowWidth()
	{
		return getWidth();
	}
	
	@Override
	public int getWindowHeight()
	{
		return getHeight();
	}
	
	@Override
	public void setSize(Integer width, Integer height)
	{
		if (width == null || height == null)
			throw new UnsupportedOptionException("Infinite viewers not supported by this class");
		
		super.setSize(width, height);
		
		recaltulateForLockMode();
	}
	
	
	
	
	
	
	@Override
	public double getCurrentCanvasTranslationX()
	{
		return x;
	}
	
	@Override
	public double getCurrentCanvasTranslationY()
	{
		return y;
	}
	
	@Override
	public double getCurrentScale()
	{
		return scale;
	}
	
	@Override
	public void setCurrentCanvasTranslationAndScale(double deltaX, double deltaY, double scaleFactor)
	{
		if (lockMode && containedComponent != null)
		{
			TranslationAndUniformScaleTransform2D requestedTransform = new TranslationAndUniformScaleTransform2D(deltaX, deltaY, scaleFactor);
			TranslationAndUniformScaleTransform2D transformWereGoingWith = BasicZoomer.adjustToKeepInBounds(requestedTransform, this, xPositioningFactorIfCanvasIsTooSmall, yPositioningFactorIfCanvasIsTooSmall);
			deltaX = transformWereGoingWith.getX();
			deltaY = transformWereGoingWith.getY();
			scaleFactor = transformWereGoingWith.getScale();
		}
		
		
		if (deltaX != this.x || deltaY != this.y || scaleFactor != this.scale)
		{
			this.x = deltaX;
			this.y = deltaY;
			this.scale = scaleFactor;
			
			repaint();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public Integer getCanvasWidth()
	{
		return getContainedComponent().getWidth();
	}
	
	@Override
	public Integer getCanvasHeight()
	{
		return getContainedComponent().getHeight();
	}
	
	
	
	
	
	public boolean isLockMode()
	{
		return lockMode;
	}
	
	public void setLockMode(boolean lockMode)
	{
		if (lockMode != this.lockMode)
		{
			this.lockMode = lockMode;
			recaltulateForLockMode();
		}
	}
	
	public double getxPositioningFactorIfCanvasIsTooSmall()
	{
		return xPositioningFactorIfCanvasIsTooSmall;
	}
	
	public double getYPositioningFactorIfCanvasIsTooSmall()
	{
		return yPositioningFactorIfCanvasIsTooSmall;
	}
	
	public void setPositioningFactorsIfCanvasIsTooSmall(double xPositioningFactorIfCanvasIsTooSmall, double yPositioningFactorIfCanvasIsTooSmall)
	{
		if (xPositioningFactorIfCanvasIsTooSmall != this.xPositioningFactorIfCanvasIsTooSmall || yPositioningFactorIfCanvasIsTooSmall != this.yPositioningFactorIfCanvasIsTooSmall)
		{
			this.xPositioningFactorIfCanvasIsTooSmall = xPositioningFactorIfCanvasIsTooSmall;
			this.yPositioningFactorIfCanvasIsTooSmall = yPositioningFactorIfCanvasIsTooSmall;
			recaltulateForLockMode();
		}
	}
	
	
	
	protected void recaltulateForLockMode()
	{
		if (lockMode && containedComponent != null)
			setCurrentConstrainedTransform(BasicZoomer.adjustToKeepInBounds(this.getCurrentConstrainedTransform(), this, xPositioningFactorIfCanvasIsTooSmall, yPositioningFactorIfCanvasIsTooSmall));
	}
}
