package rebound.formats.pdf.pdfbox.rputils;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.io.IOException;
import org.apache.pdfbox.contentstream.PDFGraphicsStreamEngine;
import org.apache.pdfbox.pdmodel.PDPage;

public abstract class PDFGraphicsStreamEngine2
extends PDFGraphicsStreamEngine
{
	private Path2D linePath = new Path2D.Float();
	
	public PDFGraphicsStreamEngine2(PDPage page)
	{
		super(page);
	}
	
	
	//This method basically requires us to implement complete remembrance of the path to implement this method properly X'D
	@Override
	public Point2D getCurrentPoint() throws IOException
	{
		return linePath.getCurrentPoint();
	}
	
	
	
	
	
	
	
	
	
	
	
	@Override
	public void moveTo(float x, float y) throws IOException
	{
		linePath.moveTo(x, y);
	}
	
	@Override
	public void lineTo(float x, float y) throws IOException
	{
		linePath.lineTo(x, y);
	}
	
	@Override
	public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
	{
		linePath.curveTo(x1, y1, x2, y2, x3, y3);
	}
	
	@Override
	public void closePath() throws IOException
	{
		linePath.closePath();
	}
	
	@Override
	public void endPath() throws IOException
	{
		linePath.reset();
	}
	
	
	@Override
	public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
	{
		// to ensure that the path is created in the right direction, we have to create
		// it by combining single lines instead of creating a simple rectangle
		linePath.moveTo((float) p0.getX(), (float) p0.getY());
		linePath.lineTo((float) p1.getX(), (float) p1.getY());
		linePath.lineTo((float) p2.getX(), (float) p2.getY());
		linePath.lineTo((float) p3.getX(), (float) p3.getY());
		
		// close the subpath instead of adding the last line so that a possible set line
		// cap style isn't taken into account at the "beginning" of the rectangle
		linePath.closePath();
	}
}
