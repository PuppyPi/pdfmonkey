package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.math.MathUtilities.*;
import static rebound.math.SmallIntegerMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import rebound.annotations.semantic.reachability.LiveValue;
import rebound.exceptions.UnsupportedOptionException;
import rebound.hci.graphics2d.gui.TraceableCollage;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.java2d.Java2DUtilities;
import rebound.util.container.ContainerInterfaces.BooleanContainer;
import rebound.util.container.ContainerInterfaces.IntegerContainer;
import rebound.util.container.ContainerInterfaces.ObjectContainer;
import rebound.util.container.SimpleContainers.SimpleBooleanContainer;
import rebound.util.container.SimpleContainers.SimpleIntegerContainer;
import rebound.util.container.SimpleContainers.SimpleObjectContainer;


/**
 * Internal layout meaning you can't setSize(), its size is determined its contents :3
 * 
 * + You must call {@link #setChildren(List)} before anything else is called or a {@link NullPointerException} will happen!
 */
public class VerticalInternalLayoutReComponent
extends AbstractReComponentContainer
implements TraceableCollage<Integer>
{
	protected int margin;
	
	protected List<? extends ReComponent> children;
	
	protected boolean sizeCacheValid;
	protected Integer width;
	protected Integer height;
	
	
	
	
	
	//Todo heheh, make a better way than this needing to be public ^^''    (we should probably just use Y though, shouldn't we at this point? XD'')
	public void invalidateSizeCache()
	{
		sizeCacheValid = false;
	}
	
	protected void validateSizeCache()
	{
		if (!sizeCacheValid)
		{
			//Width :3
			{
				if (forAny(c -> c.getWidth() == null, children))
					width = null;
				else
					width = greatestMap(c -> c.getWidth(), children);
			}
			
			//Height :3
			{
				for (ReComponent c : children)
					if (c.getHeight() == null)
						throw new UnsupportedOptionException("Infinite child height not supported!");
				
				height = safeCastAnythingToS32(sumMapping(c -> requirePositive(c.getHeight()), children)) + (margin * (children.size() - 1));
			}
			
			sizeCacheValid = true;
		}
	}
	
	
	
	
	@Override
	public Integer getWidth()
	{
		validateSizeCache();
		return width;
	}
	
	@Override
	public Integer getHeight()
	{
		validateSizeCache();
		return height;
	}
	
	@Override
	public void setSize(Integer width, Integer height) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException();
	}
	
	
	
	
	@Override
	public void paint(Graphics2D g)
	{
		final int x = 0;  //Todo center them ^^'
		
		
		int y = 0;
		
		for (ReComponent c : children)
		{
			//Note: Java2D doesn't support semi-infinite clips (afaik) so it'll fail here if that's used, sorry ^^''
			
			int h = c.getHeight();
			
			Java2DUtilities.drawWithCopyAndSubclip(g, rect(x, y, c.getWidth(), h), c::paint);
			
			y += h;
			y += margin;
		}
	}
	
	
	
	
	
	
	
	public int getMargin()
	{
		return margin;
	}
	
	public void setMargin(int margin)
	{
		if (margin != this.margin)
		{
			this.margin = margin;
			invalidateSizeCache();
		}
	}
	
	@LiveValue
	public List<? extends ReComponent> getChildren()
	{
		return children;
	}
	
	public void setChildren(@LiveValue List<? extends ReComponent> children)
	{
		this.children = children;
	}
	
	
	
	
	
	protected void forEachChildLayout(ChildLayoutObserver o)
	{
		final int x = 0;  //Todo center them ^^'
		
		
		final int n = children.size();
		
		int y = 0;
		
		for (int i = 0; i < n; i++)
		{
			ReComponent c = children.get(i);
			int w = c.getWidth();
			int h = c.getHeight();
			
			Rectangle2D b = rect(x, y, w, h);
			
			o.f(i, b);
			
			y += h;
			y += margin;
		}
	}
	
	public static interface ChildLayoutObserver
	{
		public void f(int index, Rectangle2D childBoundsInParentSpace);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Todo cache the positions to make these faster ^^'
	
	@Override
	public PointLocationResult<Integer> findPointInPageSpace(double pointInComponentSpaceX, double pointInComponentSpaceY)
	{
		IntegerContainer matches_C = new SimpleIntegerContainer(0);
		IntegerContainer matchingIndex_C = new SimpleIntegerContainer(0);
		ObjectContainer<Point2D> matchingPointInChildSpace_C = new SimpleObjectContainer<>(null);
		
		forEachChildLayout((index, childBoundsInParentSpace) ->
		{
			if (childBoundsInParentSpace.contains(pointInComponentSpaceX, pointInComponentSpaceY))
			{
				int m = matches_C.get();
				
				if (m == 0)
				{
					matchingIndex_C.set(index);
					matchingPointInChildSpace_C.set(pointOrVector2D(pointInComponentSpaceX - childBoundsInParentSpace.getX(), pointInComponentSpaceY - childBoundsInParentSpace.getY()));
					matches_C.set(1);
				}
				else if (m == 1)
				{
					matches_C.set(2);
				}
			}
		});
		
		
		if (matches_C.get() == 1)
		{
			Point2D p = matchingPointInChildSpace_C.get();
			return new PointLocationResult<Integer>(matchingIndex_C.get(), p.getX(), p.getY());
		}
		else
		{
			return null;
		}
	}
	
	
	@Override
	public RectangleSinglePageLocationResult<Integer> findRectangleInPageSpace(Rectangle2D rectangleInComponentSpace)
	{
		IntegerContainer matches_C = new SimpleIntegerContainer(0);
		IntegerContainer matchingIndex_C = new SimpleIntegerContainer(0);
		ObjectContainer<Rectangle2D> matchingClippedRectangleInChildSpace_C = new SimpleObjectContainer<>(null);
		
		forEachChildLayout((index, childBoundsInParentSpace) ->
		{
			if (childBoundsInParentSpace.intersects(rectangleInComponentSpace))
			{
				int m = matches_C.get();
				
				if (m == 0)
				{
					Rectangle2D clippedInParentSpace = intersectionOfRectanglesOPC(rectangleInComponentSpace, childBoundsInParentSpace);
					
					matchingIndex_C.set(index);
					matchingClippedRectangleInChildSpace_C.set(translateOPC(clippedInParentSpace, pointOrVector2D(-childBoundsInParentSpace.getX(), -childBoundsInParentSpace.getY())));
					matches_C.set(1);
				}
				else if (m == 1)
				{
					matches_C.set(2);
				}
			}
		});
		
		
		if (matches_C.get() == 1)
		{
			Rectangle2D p = matchingClippedRectangleInChildSpace_C.get();
			return new RectangleSinglePageLocationResult<Integer>(matchingIndex_C.get(), p);
		}
		else
		{
			return null;
		}
	}
	
	
	@Override
	public Integer getArbitraryPageInRectangle(Rectangle2D rectangleInComponentSpace)
	{
		//Todo do this right, according to greatest-area calculations XD'
		
		PointLocationResult<Integer> r = findPointInPageSpace(rectangleInComponentSpace.getCenterX(), rectangleInComponentSpace.getCenterY());
		
		if (r != null)
		{
			return r.getChildIdentifier();
		}
		else
		{
			//Todo especially down here XD'
			
			
			BooleanContainer matches_C = new SimpleBooleanContainer(false);
			IntegerContainer matchingIndex_C = new SimpleIntegerContainer(0);
			
			forEachChildLayout((index, childBoundsInParentSpace) ->
			{
				if (childBoundsInParentSpace.intersects(rectangleInComponentSpace))
				{
					matchingIndex_C.set(index);
					matches_C.set(true);
				}
			});
			
			
			if (matches_C.get())
			{
				return matchingIndex_C.get();
			}
			else
			{
				return null;
			}
		}
	}
	
	
	
	
	
	@Override
	public Point2D getTopCenterPointOfPageInWorldSpace(Integer childIdentifier)
	{
		int y = 0;
		int w = 0;
		
		for (int i = 0; i < childIdentifier; i++)
		{
			ReComponent c = children.get(i);
			w = c.getWidth();
			int h = c.getHeight();
			
			y += h;
			y += margin;
		}
		
		
		return pointOrVector2D(w / 2d, y);
	}
}
