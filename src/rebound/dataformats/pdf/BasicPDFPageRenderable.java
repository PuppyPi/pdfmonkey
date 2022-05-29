package rebound.dataformats.pdf;

import java.awt.Image;
import java.awt.geom.Rectangle2D;

public interface BasicPDFPageRenderable
extends BasicPDFPage
{
	public interface BasicPDFPageRenderableJava2D
	extends BasicPDFPageRenderable
	{
		
	}
	
	public interface BasicPDFPageRenderableRaster
	extends BasicPDFPageRenderable
	{
		public Image getRenderedImage(int imageWidth, int imageHeight, Rectangle2D clipInPageSpace);
	}
}
