package rebound.dataformats.pdf;

import java.awt.Image;

public interface BasicPDFPageRenderable
extends BasicPDFPage
{
	public interface BasicPDFPageRenderableJava2D
	extends BasicPDFPageRenderable
	{
		//TODO XD''
	}
	
	public interface BasicPDFPageRenderableRaster
	extends BasicPDFPageRenderable
	{
		/**
		 * • Right-Handed Coordinate system!!  Use {@link #getPageBoundaries()} to transform :3
		 * 
		 * @param scale Display/Page, so scale > 1 means the display space rectangle's width is a bigger number than the page space rectangle's width :>
		 */
		public Image getRenderedImage(double xInDisplaySpace, double yInDisplaySpace, int imageWidth, int imageHeight, double scale);
	}
}
