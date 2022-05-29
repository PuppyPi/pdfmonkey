package rebound.apps.pdfmonkey.worlds;

import static java.util.Objects.*;
import static rebound.math.SmallIntegerMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import rebound.annotations.semantic.allowedoperations.WritableValue;
import rebound.apps.pdfmonkey.BorderMaker;
import rebound.apps.pdfmonkey.PDFPageComponentWrapper;
import rebound.apps.pdfmonkey.PDFWorldReComponentWrapper;
import rebound.apps.pdfmonkey.actualpdfrendering.CachingRendererOnPDF;
import rebound.apps.pdfmonkey.actualpdfrendering.RendererOnPDF;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.AbstractBorderReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentZoomer;

public class SinglePageWorld
implements PDFWorldReComponentWrapper
{
	protected final CachingRendererOnPDF pageComponentCache;
	protected final @WritableValue AbstractBorderReComponent border;
	
	protected int pageIndexZerobased;
	protected PDFPageComponentWrapper currentPage;
	
	
	public SinglePageWorld(@Nonnull RendererOnPDF pdf, BorderMaker borderMaker)
	{
		if (pdf.getNumberOfPages() <= 0)
			throw new IllegalArgumentException();
		
		pageComponentCache = new CachingRendererOnPDF(pdf);
		border = borderMaker.newBorder();
		
		actuallySetCurrentPageByIndex(0);
	}
	
	@Override
	public ReComponent getComponent()
	{
		return border;
	}
	
	public RendererOnPDF getPDF()
	{
		return pageComponentCache.getUnderlying();
	}
	
	public PDFPageComponentWrapper getCurrentPage()
	{
		return currentPage;
	}
	
	
	
	
	
	
	
	@Override
	public Integer getArbitraryPageInRectangle(Rectangle2D regionInComponentSpace)
	{
		Rectangle pageComponentBoundaries = border.getContainedComponentBounds();
		
		boolean intersects = pageComponentBoundaries.intersects(regionInComponentSpace);
		
		return intersects ? getCurrentPageIndex() : null;
	}
	
	
	
	
	@Override
	public PointLocationResult findPointInPageSpace(double pointInComponentSpaceX, double pointInComponentSpaceY)
	{
		PDFPageComponentWrapper w = getCurrentPage();
		requireNonNull(w);
		
		Point2D p = pointOrVector2D(pointInComponentSpaceX, pointInComponentSpaceY);
		
		Insets i = border.getInsets();
		p = subtractPoints(p, pointOrVector2D(i.left, i.top));
		
		ReComponent c = w.getComponent();
		
		if (rect(0, 0, c.getWidth(), c.getHeight()).contains(p))
		{
			Point2D pp = w.transformFromComponentSpaceToPageSpace(p);
			return new PointLocationResult(getCurrentPageIndex(), pp.getX(), pp.getY());
		}
		else
		{
			return null;
		}
	}
	
	
	
	@Override
	public RectangleSinglePageLocationResult findRectangleInPageSpace(Rectangle2D rectangleInComponentSpace)
	{
		Rectangle pageComponentBoundaries = border.getContainedComponentBounds();
		
		Rectangle2D intersection = intersectionOfRectanglesOPC(rectangleInComponentSpace, pageComponentBoundaries);
		
		if (intersection.isEmpty())
		{
			return null;
		}
		else
		{
			Rectangle2D intersectionInPageComponentSpace = border.transformRectangleFloatingFromOurSpaceToContainedSpace(intersection);
			
			Rectangle2D intersectionInPageSpace = getCurrentPage().transformRectangleFromComponentSpaceToPageSpace(intersectionInPageComponentSpace);
			
			return new RectangleSinglePageLocationResult(getCurrentPageIndex(), intersectionInPageSpace);
		}
	}
	
	
	
	
	
	
	
	public void setCurrentPageByIndex(int pageIndexZerobased)
	{
		if (pageIndexZerobased != this.pageIndexZerobased)
		{
			actuallySetCurrentPageByIndex(pageIndexZerobased);
		}
	}
	
	protected void actuallySetCurrentPageByIndex(int pageIndexZerobased)
	{
		PDFPageComponentWrapper w = requireNonNull(pageComponentCache.getForPage(pageIndexZerobased));
		this.currentPage = w;
		this.border.setContainedComponent(w.getComponent());
		this.pageIndexZerobased = pageIndexZerobased;
	}
	
	public int getCurrentPageIndex()
	{
		return pageIndexZerobased;
	}
	
	public void switchPageRelative(int numberOfPages)
	{
		setCurrentPageByIndex(progmod(getCurrentPageIndex() + numberOfPages, getPDF().getNumberOfPages()));
	}
	
	
	
	
	
	
	
	
	@Override
	public void setDisplaySettingsFromOtherOfSameRuntimeTypeOfIgnoreIfImmutable(PDFWorldReComponentWrapper other)
	{
		SinglePageWorld otherSP = (SinglePageWorld)other;
		this.setCurrentPageByIndex(otherSP.getCurrentPageIndex());
	}
	
	
	
	
	
	
	@Override
	public Point2D getTopCenterPointOfPageInWorldSpace(Integer childIdentifier)
	{
		return pointOrVector2D(getComponent().getWidth() / 2d, 0);
	}
	
	
	@Override
	public void gotoPage(int pageIndexZerobased, ReComponentZoomer zoomer)
	{
		pageIndexZerobased = progmod(pageIndexZerobased, getPDF().getNumberOfPages());
		
		setCurrentPageByIndex(pageIndexZerobased);
	}
}
