package rebound.dataformats.pdf.util;

import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.exceptions.NotYetImplementedException;

public class BasicPDFPageRenderableRasterAdapter
//extends BasicPDFPageRenderableDecorator<BasicPDFPageRenderableJava2D>
//implements BasicPDFPageRenderableRaster
{
	public static BasicPDFPageRenderableRaster toRasterRendering(BasicPDFPage page)
	{
		if (page instanceof BasicPDFPageRenderableRaster)
		{
			return (BasicPDFPageRenderableRaster) page;
		}
		else
		{
			//Todo ^^''''
			throw new NotYetImplementedException();
		}
	}
}
