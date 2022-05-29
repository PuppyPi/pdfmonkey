package rebound.apps.pdfmonkey.actualpdfrendering;

import static java.util.Objects.*;
import static rebound.math.SmallFloatMathUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Image;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

/**
 * This exists because {@link BasicPDFPage PDF pages} can have an origin other than (0, 0) and because they use the right-handed (x, -y) coordinate system.
 */
public class BasicPDFPageBreakthroughRenderable
implements ReRenderableImage
{
	protected final BasicPDFPageRenderableRaster page;
	protected final double translationX, translationYRH;
	protected final int width, height;
	
	public BasicPDFPageBreakthroughRenderable(BasicPDFPageRenderableRaster page)
	{
		this.page = requireNonNull(page);
		
		Rectangle2D r = page.getPageBoundaries();
		
		width = roundCeilS32(r.getWidth());
		height = roundCeilS32(r.getHeight());
		translationX = r.getX();
		translationYRH = r.getY();
	}
	
	
	
	@Override
	public int getWidthInNaturalSpace()
	{
		return width;
	}
	
	@Override
	public int getHeightInNaturalSpace()
	{
		return height;
	}
	
	
	
	@Override
	public ImageAndActualRegion render(double xInDisplaySpace, double yInDisplaySpaceLH, int widthInDisplaySpace, int heightInDisplaySpace, double scale)
	{
		double translationInDisplaySpaceX = scale * translationX;
		double translationInDisplaySpaceYRH = scale * translationYRH;
		
		Rectangle2D pb = page.getPageBoundaries();
		
		double yInDisplaySpaceRH = (pb.getHeight() * scale - heightInDisplaySpace) + -yInDisplaySpaceLH;
		
		Image i = page.getRenderedImage(xInDisplaySpace + translationInDisplaySpaceX, yInDisplaySpaceRH + translationInDisplaySpaceYRH, widthInDisplaySpace, heightInDisplaySpace, scale);
		
		return new ImageAndActualRegion(i, xInDisplaySpace, yInDisplaySpaceLH);
	}
	
	
	
	
	public double getTranslationX()
	{
		return translationX;
	}
	
	public double getTranslationY()
	{
		return translationYRH;
	}
	
	
	
	
	
	
	public Point2D transformFromComponentSpaceToPageSpace(Point2D inComponentSpace)
	{
		requireNonNull(inComponentSpace);
		return pointOrVector2D(inComponentSpace.getX() - translationX, page.getPageBoundaries().getHeight() - inComponentSpace.getY() - translationYRH);
	}
	
	public Point2D transformFromPageSpaceToComponentSpace(Point2D inPageSpace)
	{
		requireNonNull(inPageSpace);
		return pointOrVector2D(inPageSpace.getX() + translationX, page.getPageBoundaries().getHeight() - inPageSpace.getY() + translationYRH);
	}
	
	
	public Rectangle2D transformRectangleFromComponentSpaceToPageSpace(Rectangle2D inComponentSpace)
	{
		Point2D min = transformFromComponentSpaceToPageSpace(rectmin(inComponentSpace));
		Point2D max = transformFromComponentSpaceToPageSpace(rectmax(inComponentSpace));
		
		return rect(min, max);
	}
	
	public Rectangle2D transformRectangleFromPageSpaceToComponentSpace(Rectangle2D inPageSpace)
	{
		Point2D min = transformFromPageSpaceToComponentSpace(rectmin(inPageSpace));
		Point2D max = transformFromPageSpaceToComponentSpace(rectmax(inPageSpace));
		
		return rect(min, max);
	}
}
