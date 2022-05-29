package rebound.apps.pdfmonkey;

import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.annotations.semantic.allowedoperations.ReadonlyValue;
import rebound.annotations.semantic.allowedoperations.TreatAsImmutableValue;
import rebound.annotations.semantic.temporal.ConstantReturnValue;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.math.SmallFloatMathUtilities;
import rebound.math.geom2d.AffineTransformUtilities;
import rebound.math.geom2d.TranslationAndUniformScaleTransform2D;
import rebound.util.functional.FunctionInterfaces.UnaryFunction;

public interface BasicPDFAwtComponentWrapper
{
	@ConstantReturnValue
	@Nonnull
	public Component getAWTComponent();
	
	@Nullable
	public AwtPaintable getOverlay();
	
	@Nullable
	@ConstantReturnValue
	public void setOverlay(AwtPaintable overlay);
	
	
	
	
	
	
	public BasicPDFPageRenderableRaster getCurrentPage();
	public void setCurrentPage(BasicPDFPageRenderableRaster page) throws ClassCastException, IllegalArgumentException;
	
	
	
	public default Rectangle2D getCurrentPageBorderInDisplaySpace()
	{
		Rectangle2D pageBorderInPageSpace = getCurrentPage().getPageBoundaries();
		
		Point2D translation = getCurrentPageTranslation();
		double scale = getCurrentScale();
		
		return rect(pageBorderInPageSpace.getX() * scale + translation.getX(), pageBorderInPageSpace.getY() * scale + translation.getY(), pageBorderInPageSpace.getWidth() * scale, pageBorderInPageSpace.getHeight() * scale);
	}
	
	
	
	/**
	 * This is a vector of how many pixels the page is translated (in page units) "underneath" the component!
	 * It is the point in the viewer's coordinate space of the origin (top-left corner) of the page! ^_^
	 * (this is what you should work with for scrolling/panning/dragging in the UI! ^w~ )
	 */
	@ReadonlyValue
	@Nonnull
	public Point2D getCurrentPageTranslation();
	public void setCurrentPageTranslation(double deltaX, double deltaY);
	
	public default void setCurrentPageTranslation(Point2D translationVector)
	{
		setCurrentPageTranslation(translationVector.getX(), translationVector.getY());
	}
	
	public default void setCurrentPageTranslationX(double deltaX)
	{
		setCurrentPageTranslation(deltaX, getCurrentPageTranslation().getY());
	}
	
	public default void setCurrentPageTranslationY(double deltaY)
	{
		setCurrentPageTranslation(getCurrentPageTranslation().getX(), deltaY);
	}
	
	
	
	/**
	 * This is how much the page is zoomed to: it is given in pixels per page-unit!
	 * Increasing this value "zooms in" and decreasing it "zooms out" :>
	 */
	public double getCurrentScale();
	public void setCurrentScale(double scaleFactor);
	
	
	public void setCurrentPageTranslationAndScale(double deltaX, double deltaY, double scaleFactor);
	
	
	public default void setCurrentPageTranslationAndScaleInPageUnits(double displayOriginInPageSpaceX, double displayOriginInPageSpaceY, double normalScaleFactor)
	{
		setCurrentPageTranslationAndScaleInPageUnits(pointOrVector2D(displayOriginInPageSpaceX, displayOriginInPageSpaceY), normalScaleFactor);
	}
	
	public default void setCurrentPageTranslationAndScaleInPageUnits(Point2D originInPageSpace, double normalScaleFactor)
	{
		setCurrentPageTranslationAndScale(originInPageSpace.getX() * normalScaleFactor, originInPageSpace.getY() * normalScaleFactor, normalScaleFactor);
	}
	
	
	
	
	
	
	public default void translateToCentersInCoincidence()
	{
		Rectangle2D b = getCurrentPage().getPageBoundaries();
		Component ac = getAWTComponent();
		translateToPutIntoCoincidence(pointOrVector2D(b.getCenterX(), b.getCenterY()), pointOrVector2D(ac.getWidth() / 2d, ac.getHeight() / 2d));
	}
	
	public default void translateToTopMiddlesInCoincidence()
	{
		Rectangle2D b = getCurrentPage().getPageBoundaries();
		Component ac = getAWTComponent();
		translateToPutIntoCoincidence(pointOrVector2D(b.getCenterX(), 0), pointOrVector2D(ac.getWidth() / 2d, 0));
	}
	
	
	
	
	public default void setToFitToWidthButKeepCenterYsInCoincidence()
	{
		Rectangle2D b = getCurrentPage().getPageBoundaries();
		Component ac = getAWTComponent();
		setScaleButKeepCenterInCoincidence(ac.getWidth() / b.getWidth());
		setCurrentPageTranslation(0, getCurrentPageTranslation().getY());
	}
	
	public default void setToFitToHeightButKeepCenterXsInCoincidence()
	{
		Rectangle2D b = getCurrentPage().getPageBoundaries();
		Component ac = getAWTComponent();
		setScaleButKeepCenterInCoincidence(ac.getHeight() / b.getHeight());
		setCurrentPageTranslation(getCurrentPageTranslation().getX(), 0);
	}
	
	public default void setToFitToPage()
	{
		Rectangle2D b = getCurrentPage().getPageBoundaries();
		Component ac = getAWTComponent();
		double zoomForWidth = ac.getWidth() / b.getWidth();
		double zoomForHeight = ac.getHeight() / b.getHeight();
		setCurrentScale(SmallFloatMathUtilities.least(zoomForWidth, zoomForHeight));
		translateToCentersInCoincidence();
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
		Component ac = getAWTComponent();
		setScaleButKeepPointInDisplaySpaceCoincident(newScale, pointOrVector2D(ac.getWidth() / 2d, ac.getHeight() / 2d));
	}
	
	
	
	public default void setScaleButKeepPointInDisplaySpaceCoincident(double newScale, Point2D pointInDisplaySpace)
	{
		//Backtransform it *before* we alter the scale XDD'
		Point2D pointInPageSpace = transformPointFromDisplaySpaceToPageSpace(pointInDisplaySpace);
		
		setCurrentScale(newScale);
		translateToPutIntoCoincidence(pointInPageSpace, pointInDisplaySpace);
	}
	
	
	
	
	
	
	public default void translateToPutIntoCoincidence(Point2D pointInPageSpace, Point2D pointInDisplaySpace)
	{
		setCurrentPageTranslation(pointInDisplaySpace.getX() - pointInPageSpace.getX() * getCurrentScale(), pointInDisplaySpace.getY() - pointInPageSpace.getY() * getCurrentScale());
	}
	
	public default void translateToPutXIntoCoincidence(double pointInPageSpaceX, double pointInDisplaySpaceX)
	{
		setCurrentPageTranslation(pointInDisplaySpaceX - pointInPageSpaceX * getCurrentScale(), getCurrentPageTranslation().getY());
	}
	
	public default void translateToPutYIntoCoincidence(double pointInPageSpaceY, double pointInDisplaySpaceY)
	{
		setCurrentPageTranslation(getCurrentPageTranslation().getX(), pointInDisplaySpaceY - pointInPageSpaceY * getCurrentScale());
	}
	
	
	
	
	
	
	
	@ReadonlyValue
	public default AffineTransform getCurrentTransformPageSpaceToDisplaySpace()
	{
		Point2D t = getCurrentPageTranslation();
		return AffineTransformUtilities.newScaleAndTranslateTransform(getCurrentScale(), t.getX(), t.getY());
	}
	
	@ReadonlyValue
	public default AffineTransform getCurrentTransformDisplaySpaceToPageSpace()
	{
		return AffineTransformUtilities.getInverseTransformOPC(getCurrentTransformPageSpaceToDisplaySpace());
	}
	
	
	@TreatAsImmutableValue
	public default Point2D transformPointFromPageSpaceToDisplaySpace(Point2D pointInPageSpace)
	{
		return AffineTransformUtilities.transformPointOPC(getCurrentTransformPageSpaceToDisplaySpace(), pointInPageSpace);
	}
	
	@TreatAsImmutableValue
	public default Point2D transformPointFromDisplaySpaceToPageSpace(Point2D pointInDisplaySpace)
	{
		return AffineTransformUtilities.transformPointOPC(getCurrentTransformDisplaySpaceToPageSpace(), pointInDisplaySpace);
	}
	
	
	
	
	public default TranslationAndUniformScaleTransform2D getCurrentConstrainedTransform()
	{
		return new TranslationAndUniformScaleTransform2D(getCurrentPageTranslation(), getCurrentScale());
	}
	
	public default void setCurrentConstrainedTransform(TranslationAndUniformScaleTransform2D newTransform)
	{
		setCurrentPageTranslationAndScale(newTransform.getX(), newTransform.getY(), newTransform.getScale());
	}
	
	
	
	
	
	@Nullable
	public UnaryFunction<TranslationAndUniformScaleTransform2D, TranslationAndUniformScaleTransform2D> getTransformFilter();
	
	public void setTransformFilter(@Nullable UnaryFunction<TranslationAndUniformScaleTransform2D, TranslationAndUniformScaleTransform2D> transformFilter);
	
	public void rerunTransformFilter();
	
	
	
	
	
	public static interface AwtPaintable
	{
		public void paint(Graphics2D g);
	}
}
