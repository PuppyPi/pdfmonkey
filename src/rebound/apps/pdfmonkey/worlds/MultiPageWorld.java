package rebound.apps.pdfmonkey.worlds;

import static rebound.math.SmallIntegerMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.annotation.Nonnull;
import rebound.apps.pdfmonkey.BorderMaker;
import rebound.apps.pdfmonkey.PDFPageComponentWrapper;
import rebound.apps.pdfmonkey.PDFWorldReComponentWrapper;
import rebound.apps.pdfmonkey.actualpdfrendering.RendererOnPDF;
import rebound.hci.graphics2d.gui.TraceableCollage;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.AbstractBorderReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentZoomer;
import rebound.hci.graphics2d.gui.recomponent.components.VerticalInternalLayoutReComponent;
import rebound.util.functional.FunctionInterfaces.UnaryFunction;

public class MultiPageWorld
implements PDFWorldReComponentWrapper
{
	protected final ReComponent worldComponent;
	protected final RendererOnPDF pdf;
	protected final UnaryFunction<Integer, AbstractBorderReComponent> childIdentifierToComponent;
	protected final UnaryFunction<Integer, PDFPageComponentWrapper> childIdentifierToPageComponentWrapper;
	protected final TraceableCollage<Integer> collageInWorldSpace;
	
	public MultiPageWorld(ReComponent worldComponent, RendererOnPDF pdf, UnaryFunction<Integer, AbstractBorderReComponent> childIdentifierToComponent, UnaryFunction<Integer, PDFPageComponentWrapper> childIdentifierToPageComponentWrapper, TraceableCollage<Integer> collageInWorldSpace)
	{
		this.worldComponent = worldComponent;
		this.pdf = pdf;
		this.childIdentifierToComponent = childIdentifierToComponent;
		this.childIdentifierToPageComponentWrapper = childIdentifierToPageComponentWrapper;
		this.collageInWorldSpace = collageInWorldSpace;
	}
	
	
	@Override
	public ReComponent getComponent()
	{
		return worldComponent;
	}
	
	public RendererOnPDF getPDF()
	{
		return pdf;
	}
	
	
	
	
	
	@Override
	public PointLocationResult<Integer> findPointInPageSpace(double pointInComponentSpaceX, double pointInComponentSpaceY)
	{
		PointLocationResult<Integer> r = collageInWorldSpace.findPointInPageSpace(pointInComponentSpaceX, pointInComponentSpaceY);
		
		if (r == null)
		{
			return null;
		}
		else
		{
			int i = r.getChildIdentifier();
			
			AbstractBorderReComponent c = childIdentifierToComponent.f(i);
			
			Point2D pointInPageComponentSpace = c.transformFromOurSpaceToContainedSpaceOrNullIfOutOfBounds(pointOrVector2D(r.getPointInPageSpaceX(), r.getPointInPageSpaceY()));
			
			if (pointInPageComponentSpace == null)
			{
				return null;
			}
			else
			{
				PDFPageComponentWrapper pageComponentWrapper = childIdentifierToPageComponentWrapper.f(i);
				
				Point2D pointInPageSpace = pageComponentWrapper.transformFromComponentSpaceToPageSpace(pointInPageComponentSpace);
				
				return new PointLocationResult<Integer>(i, pointInPageSpace.getX(), pointInPageSpace.getY());
			}
		}
	}
	
	
	@Override
	public RectangleSinglePageLocationResult<Integer> findRectangleInPageSpace(Rectangle2D rectangleInComponentSpace)
	{
		RectangleSinglePageLocationResult<Integer> r = collageInWorldSpace.findRectangleInPageSpace(rectangleInComponentSpace);
		
		if (r == null)
		{
			return null;
		}
		else
		{
			int i = r.getChildIdentifier();
			
			AbstractBorderReComponent c = childIdentifierToComponent.f(i);
			
			Rectangle2D rectangleInPageComponentSpace = c.transformRectangleFloatingFromOurSpaceToContainedSpace(r.getRectangleInPageSpace());
			
			PDFPageComponentWrapper pageComponentWrapper = childIdentifierToPageComponentWrapper.f(i);
			
			Rectangle2D rectangleInPageSpace = pageComponentWrapper.transformRectangleFromComponentSpaceToPageSpace(rectangleInPageComponentSpace);
			
			return new RectangleSinglePageLocationResult<Integer>(i, rectangleInPageSpace);
		}
	}
	
	
	@Override
	public Integer getArbitraryPageInRectangle(Rectangle2D regionInComponentSpace)
	{
		Integer i = collageInWorldSpace.getArbitraryPageInRectangle(regionInComponentSpace);
		
		if (i == null)
		{
			return null;
		}
		else
		{
			return i;
		}
	}
	
	
	@Override
	public void setDisplaySettingsFromOtherOfSameRuntimeTypeOfIgnoreIfImmutable(PDFWorldReComponentWrapper other)
	{
		//Nothing to do since we're immutable! :D
	}
	
	
	
	
	@Override
	public Point2D getTopCenterPointOfPageInWorldSpace(Integer childIdentifier)
	{
		return collageInWorldSpace.getTopCenterPointOfPageInWorldSpace(childIdentifier);
	}
	
	
	
	@Override
	public void gotoPage(int pageIndexZerobased, ReComponentZoomer zoomer)
	{
		pageIndexZerobased = progmod(pageIndexZerobased, getPDF().getNumberOfPages());
		
		Point2D topMiddleInCanvasSpace = this.getTopCenterPointOfPageInWorldSpace(pageIndexZerobased);
		
		Point2D topMiddleInViewSpace = pointOrVector2D(zoomer.getWidth() / 2d, 0);
		
		zoomer.translateToPutIntoCoincidence(topMiddleInCanvasSpace, topMiddleInViewSpace);
	}
	
	
	
	
	
	
	
	
	
	
	public static MultiPageWorld newMultiPageWorld_VerticalLayout(@Nonnull RendererOnPDF pdf, BorderMaker borderMaker)
	{
		if (pdf.getNumberOfPages() <= 0)
			throw new IllegalArgumentException();
		
		
		List<PDFPageComponentWrapper> pages = mapToList(i -> pdf.newForPage(i), intervalIntegersList(0, pdf.getNumberOfPages()));
		
		
		VerticalInternalLayoutReComponent worldComponent = new VerticalInternalLayoutReComponent();
		worldComponent.setMargin(10);  //Todo softcode ^^'
		
		List<AbstractBorderReComponent> components = mapToList(w -> borderMaker.newBorder(w.getComponent()), pages);
		
		worldComponent.setChildren(components);
		
		return new MultiPageWorld(worldComponent, pdf, components::get, pages::get, worldComponent);
	}
}
