package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.hci.graphics2d.gui.GUIGeometryUtilities.*;
import static rebound.math.geom2d.AffineTransformUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.annotations.semantic.allowedoperations.ReadonlyValue;
import rebound.annotations.semantic.allowedoperations.TreatAsImmutableValue;
import rebound.math.SmallFloatMathUtilities;
import rebound.math.geom2d.AffineTransformUtilities;
import rebound.math.geom2d.TranslationAndUniformScaleTransform2D;

public interface BasicZoomer
{
	/**
	 * This is a vector of how many pixels the canvas is translated (in canvas units) "underneath" the component!
	 * It is the point in the viewer's coordinate space of the origin (top-left corner) of the canvas! ^_^
	 * (this is what you should work with for scrolling/panning/dragging in the UI! ^w~ )
	 */
	@ReadonlyValue
	@Nonnull
	public default Point2D getCurrentCanvasTranslation()
	{
		return pointOrVector2D(getCurrentCanvasTranslationX(), getCurrentCanvasTranslationY());
	}
	
	
	public double getCurrentCanvasTranslationX();
	public double getCurrentCanvasTranslationY();
	
	
	
	
	/**
	 * This is how much the canvas is zoomed to: it is given in pixels per canvas-unit!
	 * Increasing this value "zooms in" and decreasing it "zooms out" :>
	 */
	public double getCurrentScale();
	
	
	public int getWindowWidth();
	public int getWindowHeight();
	
	/**
	 * @return null means infinite, just like ReComponent
	 */
	public @Nonnegative @Nullable Integer getCanvasWidth();
	
	/**
	 * @return null means infinite, just like ReComponent
	 */
	public @Nonnegative @Nullable Integer getCanvasHeight();
	
	
	
	
	
	/**
	 * Only the one setter for setting the whole transform at once is given because it may alter the provided parameters (for example, to keep the canvas in bounds!  see {@link #adjustToKeepInBounds(TranslationAndUniformScaleTransform2D, BasicZoomer, double, double)})
	 */
	public void setCurrentCanvasTranslationAndScale(double deltaX, double deltaY, double scaleFactor);
	
	
	
	
	
	public default TranslationAndUniformScaleTransform2D getCurrentConstrainedTransform()
	{
		return new TranslationAndUniformScaleTransform2D(getCurrentCanvasTranslation(), getCurrentScale());
	}
	
	public default void setCurrentConstrainedTransform(TranslationAndUniformScaleTransform2D newTransform)
	{
		setCurrentCanvasTranslationAndScale(newTransform.getX(), newTransform.getY(), newTransform.getScale());
	}
	
	
	
	
	
	
	
	
	
	
	
	public static TranslationAndUniformScaleTransform2D adjustToKeepInBounds(TranslationAndUniformScaleTransform2D requestedTransform, BasicZoomer zoomer, double xPositioningFactorIfCanvasIsTooSmall, double yPositioningFactorIfCanvasIsTooSmall)
	{
		//Todo Support singly-infinite canvases ^^'
		
		Integer canvasWidth = zoomer.getCanvasWidth();
		Integer canvasHeight = zoomer.getCanvasHeight();
		boolean finiteWidth = canvasWidth != null;
		boolean finiteHeight = canvasHeight != null;
		
		if (!finiteWidth && !finiteHeight)
			return requestedTransform;  //nothing to do XD
		
		
		
		//Retranslate to keep in bounds!
		{
			Rectangle2D canvasBoundsInOwnSpace = rect(0, 0, zoomer.getCanvasWidth(), zoomer.getCanvasHeight());
			Rectangle2D viewportBoundsInOwnSpace = rect(0, 0, zoomer.getWindowWidth(), zoomer.getWindowHeight());
			Rectangle2D viewportBoundsInCanvasSpace = zoomer.transformRectangleFromDisplaySpaceToCanvasSpace(viewportBoundsInOwnSpace);
			
			
			
			Point2D currentTranslationOfCanvasInViewportSpace = zoomer.getCurrentCanvasTranslation();
			
			Point2D desiredTranslationVectorOfCanvasInViewportSpace = requestedTransform.getTranslation();
			
			
			
			AffineTransform t = zoomer.getCurrentTransformCanvasSpaceToDisplaySpace();
			
			Point2D currentTranslationOfViewportInCanvasSpace = negateVector(inverseTransformVectorOPC(t, currentTranslationOfCanvasInViewportSpace));
			
			Point2D desiredTranslationOfViewportInCanvasSpace = negateVector(inverseTransformVectorOPC(t, desiredTranslationVectorOfCanvasInViewportSpace));
			
			Point2D actualTranslationOfViewportInCanvasSpace = addVector(calculateRelativeTranslationVectorKeepingInBounds(viewportBoundsInCanvasSpace, canvasBoundsInOwnSpace, subtractPoints(desiredTranslationOfViewportInCanvasSpace, currentTranslationOfViewportInCanvasSpace), xPositioningFactorIfCanvasIsTooSmall, yPositioningFactorIfCanvasIsTooSmall), currentTranslationOfViewportInCanvasSpace);
			
			Point2D actualTranslationOfCanvasInViewportSpace = negateVector(transformVectorOPC(t, actualTranslationOfViewportInCanvasSpace));
			
			
			return new TranslationAndUniformScaleTransform2D(actualTranslationOfCanvasInViewportSpace, requestedTransform.getScale());
		}
	}
	
	
	public static Point2D getPossiblyTruncatedCanvasOriginAkaTranslationKeepingInBounds(Rectangle2D canvasBoundsInOwnSpace, Rectangle2D viewportBoundsInOwnSpace, AffineTransform transformFromCanvasSpaceToViewportSpace, double xPositioningFactorIfCanvasIsTooSmall, double yPositioningFactorIfCanvasIsTooSmall)
	{
		Rectangle2D canvasBoundsInViewportSpace = transformAxisAlignedRectangleOPC(canvasBoundsInOwnSpace, transformFromCanvasSpaceToViewportSpace);
		
		double oldMinX = canvasBoundsInViewportSpace.getMinX();
		double oldMinY = canvasBoundsInViewportSpace.getMinY();
		
		double newMinX = oldMinX;
		double newMinY = oldMinY;
		
		if (canvasBoundsInViewportSpace.getWidth() < viewportBoundsInOwnSpace.getWidth())
		{
			newMinX = (viewportBoundsInOwnSpace.getWidth() - canvasBoundsInViewportSpace.getWidth()) * xPositioningFactorIfCanvasIsTooSmall;
		}
		else
		{
			if (canvasBoundsInViewportSpace.getMinX() > viewportBoundsInOwnSpace.getMinX())
				newMinX = viewportBoundsInOwnSpace.getMinX();
			else if (canvasBoundsInViewportSpace.getMaxX() < viewportBoundsInOwnSpace.getMaxX())
				//newMaxX = viewportBoundsInOwnSpace.getMaxX();
				//maxX - minX = width
				//-minX = width - maxX
				//minX = maxX - width
				newMinX = viewportBoundsInOwnSpace.getMaxX() - canvasBoundsInViewportSpace.getWidth();
		}
		
		
		if (canvasBoundsInViewportSpace.getHeight() < viewportBoundsInOwnSpace.getHeight())
		{
			newMinY = (viewportBoundsInOwnSpace.getHeight() - canvasBoundsInViewportSpace.getHeight()) * yPositioningFactorIfCanvasIsTooSmall;
		}
		else
		{
			if (canvasBoundsInViewportSpace.getMinY() > viewportBoundsInOwnSpace.getMinY())
				newMinY = viewportBoundsInOwnSpace.getMinY();
			else if (canvasBoundsInViewportSpace.getMaxY() < viewportBoundsInOwnSpace.getMaxY())
				//newMaxY = viewportBoundsInOwnSpace.getMaxY();
				//maxY - minY = height
				//-minY = height - maxY
				//minY = maxY - height
				newMinY = viewportBoundsInOwnSpace.getMaxY() - canvasBoundsInViewportSpace.getHeight();
		}
		
		
		return pointOrVector2D(newMinX, newMinY);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	public default void setCurrentCanvasTranslationAndScaleInCanvasUnits(double displayOriginInCanvasSpaceX, double displayOriginInCanvasSpaceY, double normalScaleFactor)
	{
		setCurrentCanvasTranslationAndScaleInCanvasUnits(pointOrVector2D(displayOriginInCanvasSpaceX, displayOriginInCanvasSpaceY), normalScaleFactor);
	}
	
	public default void setCurrentCanvasTranslationAndScaleInCanvasUnits(Point2D originInCanvasSpace, double normalScaleFactor)
	{
		setCurrentCanvasTranslationAndScale(originInCanvasSpace.getX() * normalScaleFactor, originInCanvasSpace.getY() * normalScaleFactor, normalScaleFactor);
	}
	
	
	
	
	
	
	public default void translateToCentersInCoincidence()
	{
		Integer canvasWidth = getCanvasWidth();
		Integer canvasHeight = getCanvasHeight();
		boolean finiteWidth = canvasWidth != null;
		boolean finiteHeight = canvasHeight != null;
		
		
		translateToPutIntoCoincidence(
		
		pointOrVector2D(
		finiteWidth ? canvasWidth / 2d : 0,
		finiteHeight ? canvasHeight / 2d : 0
		),
		
		pointOrVector2D(
		finiteWidth ? canvasWidth / 2d : 0,
		finiteHeight ? canvasHeight / 2d : 0
		)
		
		);
	}
	
	public default void translateToTopMiddlesInCoincidence()
	{
		Integer canvasWidth = getCanvasWidth();
		//Integer canvasHeight = getCanvasHeight();
		boolean finiteWidth = canvasWidth != null;
		//boolean finiteHeight = canvasHeight != null;
		
		
		translateToPutIntoCoincidence(
		
		pointOrVector2D(
		finiteWidth ? canvasWidth / 2d : 0,
		0
		),
		
		pointOrVector2D(
		finiteWidth ? canvasWidth / 2d : 0,
		0
		)
		
		);
	}
	
	public default void translateToTopLeftsInCoincidence()
	{
		translateToPutIntoCoincidence(
		pointOrVector2D(0, 0),
		pointOrVector2D(0, 0)
		);
	}
	
	
	
	
	public default void setToFitToWidthButKeepCenterYsInCoincidence()
	{
		Integer canvasWidth = getCanvasWidth();
		//Integer canvasHeight = getCanvasHeight();
		boolean finiteWidth = canvasWidth != null;
		//boolean finiteHeight = canvasHeight != null;
		
		if (finiteWidth)
		{
			//Todo can we set them all at once?
			setScaleButKeepCenterInCoincidence((double)this.getWindowWidth() / (double)canvasWidth);
			setCurrentCanvasTranslationAndScale(0, getCurrentCanvasTranslation().getY(), getCurrentScale());
		}
	}
	
	public default void setToFitToHeightButKeepCenterXsInCoincidence()
	{
		//Integer canvasWidth = getCanvasWidth();
		Integer canvasHeight = getCanvasHeight();
		//boolean finiteWidth = canvasWidth != null;
		boolean finiteHeight = canvasHeight != null;
		
		if (finiteHeight)
		{
			//Todo can we set them all at once?
			setScaleButKeepCenterInCoincidence((double)this.getWindowHeight() / (double)canvasHeight);
			setCurrentCanvasTranslationAndScale(getCurrentCanvasTranslation().getX(), 0, getCurrentScale());
		}
	}
	
	public default void setToFitToCanvas()
	{
		Integer canvasWidth = getCanvasWidth();
		Integer canvasHeight = getCanvasHeight();
		boolean finiteWidth = canvasWidth != null;
		boolean finiteHeight = canvasHeight != null;
		
		double scale;
		{
			if (finiteWidth)
			{
				if (finiteHeight)
				{
					double zoomForWidth = (double)this.getWindowWidth() / (double)canvasWidth;
					double zoomForHeight = (double)this.getWindowHeight() / (double)canvasHeight;
					scale = SmallFloatMathUtilities.least(zoomForWidth, zoomForHeight);
				}
				else
				{
					double zoomForWidth = (double)this.getWindowWidth() / (double)canvasWidth;
					scale = zoomForWidth;
				}
			}
			else
			{
				if (finiteHeight)
				{
					double zoomForHeight = (double)this.getWindowHeight() / (double)canvasHeight;
					scale = zoomForHeight;
				}
				else
				{
					//As good as anything XD'
					scale = 1;
				}
			}
		}
		
		setScaleButKeepCenterInCoincidence(scale);
	}
	
	
	
	public default void setToNaturalScaleButKeepCenterInCoincidence()
	{
		setScaleButKeepCenterInCoincidence(1);
	}
	
	public default void multiplyScaleButKeepCenterInCoincidence(double relativeFactor)
	{
		setScaleButKeepCenterInCoincidence(getCurrentScale() * relativeFactor);
	}
	
	public default void setScaleButKeepCenterInCoincidence(double newScale)
	{
		setScaleButKeepPointInDisplaySpaceCoincident(newScale, pointOrVector2D(getWindowWidth() / 2d, getWindowHeight() / 2d));
	}
	
	
	
	public default void setScaleButKeepPointInDisplaySpaceCoincident(double newScale, Point2D pointInDisplaySpace)
	{
		//Backtransform it *before* we alter the scale XDD'
		Point2D pointInCanvasSpace = transformPointFromDisplaySpaceToCanvasSpace(pointInDisplaySpace);
		
		setCurrentCanvasTranslationAndScale(pointInDisplaySpace.getX() - pointInCanvasSpace.getX() * newScale, pointInDisplaySpace.getY() - pointInCanvasSpace.getY() * newScale, newScale);
		
		setCurrentConstrainedTransform(getCurrentConstrainedTransform());  //TODO Why is this necessary!?  X'D
	}
	
	
	
	
	
	
	public default void translateToPutIntoCoincidence(Point2D pointInCanvasSpace, Point2D pointInDisplaySpace)
	{
		setCurrentCanvasTranslationAndScale(pointInDisplaySpace.getX() - pointInCanvasSpace.getX() * getCurrentScale(), pointInDisplaySpace.getY() - pointInCanvasSpace.getY() * getCurrentScale(), getCurrentScale());
	}
	
	public default void translateToPutXIntoCoincidence(double pointInCanvasSpaceX, double pointInDisplaySpaceX)
	{
		setCurrentCanvasTranslationAndScale(pointInDisplaySpaceX - pointInCanvasSpaceX * getCurrentScale(), getCurrentCanvasTranslation().getY(), getCurrentScale());
	}
	
	public default void translateToPutYIntoCoincidence(double pointInCanvasSpaceY, double pointInDisplaySpaceY)
	{
		setCurrentCanvasTranslationAndScale(getCurrentCanvasTranslation().getX(), pointInDisplaySpaceY - pointInCanvasSpaceY * getCurrentScale(), getCurrentScale());
	}
	
	
	
	
	
	
	
	@ReadonlyValue
	public default AffineTransform getCurrentTransformCanvasSpaceToDisplaySpace()
	{
		Point2D t = getCurrentCanvasTranslation();
		return AffineTransformUtilities.newScaleAndTranslateTransform(getCurrentScale(), t.getX(), t.getY());
	}
	
	@ReadonlyValue
	public default AffineTransform getCurrentTransformDisplaySpaceToCanvasSpace()
	{
		return AffineTransformUtilities.getInverseTransformOPC(getCurrentTransformCanvasSpaceToDisplaySpace());
	}
	
	
	@TreatAsImmutableValue
	public default Point2D transformPointFromCanvasSpaceToDisplaySpace(Point2D pointInCanvasSpace)
	{
		//Todo getCurrentConstrainedTransform().transformPoint(..)
		return AffineTransformUtilities.transformPointOPC(getCurrentTransformCanvasSpaceToDisplaySpace(), pointInCanvasSpace);
	}
	
	@TreatAsImmutableValue
	public default Point2D transformPointFromDisplaySpaceToCanvasSpace(Point2D pointInDisplaySpace)
	{
		//Todo getCurrentConstrainedTransform().inverseTransformPoint(..)
		return AffineTransformUtilities.transformPointOPC(getCurrentTransformDisplaySpaceToCanvasSpace(), pointInDisplaySpace);
	}
	
	
	@TreatAsImmutableValue
	public default Rectangle2D transformRectangleFromDisplaySpaceToCanvasSpace(Rectangle2D rectangleInDisplaySpace)
	{
		//Todo getCurrentConstrainedTransform().transformRectangle(..)
		
		Point2D p0d = pointOrVector2D(rectangleInDisplaySpace.getMinX(), rectangleInDisplaySpace.getMinY());
		Point2D p1d = pointOrVector2D(rectangleInDisplaySpace.getMaxX(), rectangleInDisplaySpace.getMinY());
		Point2D p2d = pointOrVector2D(rectangleInDisplaySpace.getMinX(), rectangleInDisplaySpace.getMaxY());
		Point2D p3d = pointOrVector2D(rectangleInDisplaySpace.getMaxX(), rectangleInDisplaySpace.getMaxY());
		
		Point2D p0c = transformPointFromDisplaySpaceToCanvasSpace(p0d);
		Point2D p1c = transformPointFromDisplaySpaceToCanvasSpace(p1d);
		Point2D p2c = transformPointFromDisplaySpaceToCanvasSpace(p2d);
		Point2D p3c = transformPointFromDisplaySpaceToCanvasSpace(p3d);
		
		return boundsOPC(p0c, p1c, p2c, p3c);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public default void setZoomSettingsFromOther(BasicZoomer other)
	{
		this.setCurrentConstrainedTransform(other.getCurrentConstrainedTransform());
	}
}
