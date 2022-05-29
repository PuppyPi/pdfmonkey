package rebound.dataformats.pdf.impls;

import java.awt.geom.Rectangle2D;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.math.geom2d.ImmutableRectangle2DDouble;

public abstract class AbstractBasicPDFPage
implements BasicPDFPage
{
	protected final BasicPDFFile containingFile;
	protected final int pageIndex;
	protected final Rectangle2D pageBoundaries;
	
	public AbstractBasicPDFPage(BasicPDFFile containingFile, int pageIndex, Rectangle2D pageBoundaries)
	{
		this.containingFile = containingFile;
		this.pageIndex = pageIndex;
		this.pageBoundaries = ImmutableRectangle2DDouble.immutableCopy(pageBoundaries);
	}
	
	
	@Override
	public BasicPDFFile getContainingFile()
	{
		return containingFile;
	}
	
	@Override
	public int getPageIndex()
	{
		return pageIndex;
	}
	
	@Override
	public Rectangle2D getPageBoundaries()
	{
		return pageBoundaries;
	}
}
