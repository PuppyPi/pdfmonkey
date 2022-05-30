package rebound.hci.graphics2d.gui;

import static java.util.Objects.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.annotations.semantic.simpledata.Emptyable;

/**
 * You've got a bunch of things (graphically) inside a big thing (eg, a set of pages in a pdf file), possibly one next to another or in a grid or etc.
 * How do you find, given a point or shape in the big thing's coordinate space, which smaller thing it overlaps with?
 * This api helps with that :>
 */
public interface TraceableCollage<I>
{
	/**
	 * This is for eg, operations that apply to the "currently viewed page".
	 * So the user probably will expect that if one page takes up the vast majority of the area specified (the viewing window), that that page is the one this returns (and the operation applies to)  XD
	 * 
	 * @return null ifF there are *no* pages in the region!  Unlike {@link #findRectangleInPageSpace(Rectangle2D)} which returns null if there are none *or multiple*!
	 */
	public @Nullable I getArbitraryPageInRectangle(Rectangle2D regionInComponentSpace);
	
	
	
	
	
	
	
	
	/**
	 * @return null ifF it's not inside any child or it's inside multiple!
	 */
	public @Nullable PointLocationResult<I> findPointInPageSpace(double pointInComponentSpaceX, double pointInComponentSpaceY);
	
	
	
	public static class PointLocationResult<I>
	{
		protected final I childIdentifier;
		protected final double pointInPageSpaceX;
		protected final double pointInPageSpaceY;
		
		public PointLocationResult(@Nonnull I childIdentifier, double pointInPageSpaceX, double pointInPageSpaceY)
		{
			this.childIdentifier = requireNonNull(childIdentifier);
			this.pointInPageSpaceX = pointInPageSpaceX;
			this.pointInPageSpaceY = pointInPageSpaceY;
		}
		
		public I getChildIdentifier()
		{
			return childIdentifier;
		}
		
		public double getPointInPageSpaceX()
		{
			return pointInPageSpaceX;
		}
		
		public double getPointInPageSpaceY()
		{
			return pointInPageSpaceY;
		}
	}	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @return null ifF it's not inside any page OR it covers multiple pages!
	 */
	public @Nullable RectangleSinglePageLocationResult<I> findRectangleInPageSpace(Rectangle2D rectangleInComponentSpace);
	
	public @Nonnull @Emptyable List<RectangleSinglePageLocationResult<I>> findRectanglesInPageSpace(Rectangle2D rectangleInComponentSpace);
	
	
	public static class RectangleSinglePageLocationResult<I>
	{
		protected final I childIdentifier;
		protected final Rectangle2D rectangleInPageSpace;
		
		public RectangleSinglePageLocationResult(I childIdentifier, Rectangle2D rectangleInPageSpace)
		{
			this.childIdentifier = childIdentifier;
			this.rectangleInPageSpace = rectangleInPageSpace;
		}
		
		public I getChildIdentifier()
		{
			return childIdentifier;
		}
		
		public Rectangle2D getRectangleInPageSpace()
		{
			return rectangleInPageSpace;
		}
	}
	
	
	
	
	
	
	
	
	/**
	 * In World Space not Page Space!
	 */
	public default Point2D getTopCenterPointOfPageInWorldSpace(I childIdentifier)  //Todo remove this!
	{
		Rectangle2D r = getPageBoundsInWorldSpace(childIdentifier);
		return pointOrVector2D(r.getCenterX(), r.getMinY());
	}
	
	
	/**
	 * In World Space not Page Space!
	 */
	public Rectangle2D getPageBoundsInWorldSpace(I childIdentifier);
}
