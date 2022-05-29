package rebound.tieins.pdfplumberpuppetteer;

/**
 * PDFPlumber uses left-handed y top-left origin coordinates like AWT and raster graphics usually does, not right-handed left-bottom coordinates like science/math/SVG/PDF does (confusingly X'D ).
 */
public class PDFPlumberBoundingBox
{
	protected final double left, top, right, bottom;
	
	public PDFPlumberBoundingBox(double left, double top, double right, double bottom)
	{
		this.left = left;
		this.top = top;
		this.right = right;
		this.bottom = bottom;
	}
	
	public double getLeft()
	{
		return left;
	}
	
	public double getTop()
	{
		return top;
	}
	
	public double getRight()
	{
		return right;
	}
	
	public double getBottom()
	{
		return bottom;
	}
	
	
	@Override
	public String toString()
	{
		return "(" + left + ", " + top + ", " + right + ", " + bottom + ")";  //This is what it would be in Python :3
	}
}
