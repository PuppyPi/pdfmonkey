package rebound.apps.pdfmonkey.actualpdfrendering;

import static java.util.Objects.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import rebound.apps.pdfmonkey.PDFPageComponentWrapper;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.BreakthroughRenderingAdapterSpi;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.BreakthroughRenderingUtilities;

public class RasterBasedRendererOnPDF
implements RendererOnPDF
{
	protected final BasicPDFFile basicPDFFile;
	protected BreakthroughRenderingAdapterSpi breakthroughRenderingAdapter = BreakthroughRenderingUtilities.AdapterDefault;
	
	public RasterBasedRendererOnPDF(@Nonnull BasicPDFFile basicPDFFile)
	{
		this.basicPDFFile = requireNonNull(basicPDFFile);
	}
	
	
	public BasicPDFFile getBasicPDFFile()
	{
		return basicPDFFile;
	}
	
	
	@Override
	public int getNumberOfPages()
	{
		return basicPDFFile.getNumberOfPages();
	}
	
	
	
	
	
	
	
	@Override
	public PDFPageComponentWrapper newForPage(int pageIndexZerobased)
	{
		BasicPDFPage page = basicPDFFile.getPages().get(pageIndexZerobased);
		BasicPDFPageRenderableRaster renderablePage = (BasicPDFPageRenderableRaster) page;
		BasicPDFPageBreakthroughRenderable breakthroughRenderable = new BasicPDFPageBreakthroughRenderable(renderablePage);
		ReComponent component = breakthroughRenderingAdapter.newInstance(breakthroughRenderable);
		
		return new PDFPageComponentWrapper()
		{
			@Override
			public ReComponent getComponent()
			{
				return component;
			}
			
			@Override
			public Point2D transformFromComponentSpaceToPageSpace(Point2D inComponentSpace)
			{
				return breakthroughRenderable.transformFromComponentSpaceToPageSpace(inComponentSpace);
			}
			
			@Override
			public Point2D transformFromPageSpaceToComponentSpace(Point2D inPageSpace)
			{
				return breakthroughRenderable.transformFromPageSpaceToComponentSpace(inPageSpace);
			}
			
			@Override
			public Rectangle2D transformRectangleFromComponentSpaceToPageSpace(Rectangle2D inComponentSpace)
			{
				return breakthroughRenderable.transformRectangleFromComponentSpaceToPageSpace(inComponentSpace);
			}
			
			@Override
			public Rectangle2D transformRectangleFromPageSpaceToComponentSpace(Rectangle2D inPageSpace)
			{
				return breakthroughRenderable.transformRectangleFromPageSpaceToComponentSpace(inPageSpace);
			}
		};
	}
}
