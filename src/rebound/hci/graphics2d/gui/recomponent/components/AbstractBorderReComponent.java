package rebound.hci.graphics2d.gui.recomponent.components;

import static rebound.math.SmallIntegerMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import rebound.exceptions.UnsupportedOptionException;
import rebound.hci.graphics2d.java2d.Java2DUtilities;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;

public abstract class AbstractBorderReComponent
extends AbstractReComponentHolder
{
	public abstract Insets getInsets();
	public abstract void paintBorder(Graphics2D g);
	
	
	
	@Override
	public void paint(Graphics2D g)
	{
		if (containedComponent != null)
		{
			Integer w = containedComponent.getWidth();
			Integer h = containedComponent.getHeight();
			
			if (w == null)  throw new UnsupportedOptionException("Infinite sizes not supported");
			if (h == null)  throw new UnsupportedOptionException("Infinite sizes not supported");
			
			Insets i = getInsets();
			Rectangle2D subclip = rect(i.left, i.top, w, h);
			
			Java2DUtilities.drawWithCopyAndSubclip(g, subclip, containedComponent::paint);
			paintBorder(g);
		}
	}
	
	
	
	@Override
	public Integer getWidth()
	{
		if (containedComponent == null)
			throw new IllegalStateException();
		
		Integer w = containedComponent.getWidth();
		if (w == null)  throw new UnsupportedOptionException("Infinite sizes not supported");
		
		Insets i = getInsets();
		
		return w + i.left + i.right;
	}
	
	@Override
	public Integer getHeight()
	{
		if (containedComponent == null)
			throw new IllegalStateException();
		
		Integer h = containedComponent.getHeight();
		if (h == null)  throw new UnsupportedOptionException("Infinite sizes not supported");
		
		Insets i = getInsets();
		
		return h + i.top + i.bottom;
	}
	
	
	
	@Override
	public void setSize(Integer width, Integer height) throws UnsupportedOperationException
	{
		if (containedComponent == null)
			throw new IllegalStateException();
		
		Insets i = getInsets();
		
		containedComponent.setSize(
		greatest(0, width - (i.left + i.right)),
		greatest(0, height - (i.top + i.bottom))
		);
	}
	
	
	
	
	
	
	
	
	
	public Rectangle getContainedComponentBounds()
	{
		Insets i = getInsets();
		
		return new Rectangle(i.left, i.top, containedComponent.getWidth(), containedComponent.getHeight());
	}
	
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(IntPoint p)
	{
		Insets i = getInsets();
		
		Rectangle r = getContainedComponentBounds();
		
		if (r.contains(p.getX(), p.getY()))
		{
			return new IntPoint(p.getX() + i.left, p.getY() + i.right);
		}
		else
		{
			return null;
		}
	}
	
	
	@Override
	public IntPoint transformFromOurSpaceToContainedSpace(IntPoint p)
	{
		Insets i = getInsets();
		
		return new IntPoint(p.getX() + i.left, p.getY() + i.right);
	}
	
	
	public Point2D transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(Point2D p)
	{
		Insets i = getInsets();
		
		Rectangle r = getContainedComponentBounds();
		
		if (r.contains(p.getX(), p.getY()))
		{
			return new Point2D.Double(p.getX() + i.left, p.getY() + i.right);
		}
		else
		{
			return null;
		}
	}
	
	
	public Rectangle2D transformRectangleFloatingFromOurSpaceToContainedSpace(Rectangle2D r)
	{
		Insets i = getInsets();
		return rect(r.getX() - i.left, r.getY() - i.right, r.getWidth(), r.getHeight());
	}
}
