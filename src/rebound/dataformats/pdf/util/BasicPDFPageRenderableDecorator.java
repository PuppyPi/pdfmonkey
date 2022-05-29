package rebound.dataformats.pdf.util;

import java.awt.geom.Rectangle2D;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPageRenderable;

public class BasicPDFPageRenderableDecorator<U extends BasicPDFPageRenderable>
implements BasicPDFPageRenderable
{
	protected U underlying;
	
	
	public BasicPDFFile getContainingFile()
	{
		return underlying.getContainingFile();
	}
	
	public int getPageIndex()
	{
		return underlying.getPageIndex();
	}
	
	public Rectangle2D getPageBoundaries()
	{
		return underlying.getPageBoundaries();
	}
}
