package rebound.formats.pdf.pdfbox.rputils._affiliated;

import static rebound.text.StringUtilities.*;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.graphics.image.PDImage;
import rebound.formats.pdf.pdfbox.rputils.PDFGraphicsStreamEngine2;

public class PDFBoxSandbox
{
	public static void main(String[] args) throws InvalidPasswordException, IOException
	{
		PDDocument doc = PDDocument.load(new File("/data/Immediate — Beta/Knowledge!!  :DD/Computerey :3/Specifications!/Microcontrollers/PIC!! :DDD/Datasheets for ones I have XDD/PIC16F1454, PIC16F1455, PIC16F1459, PIC16LF1454, PIC16LF1455, PIC16LF1459.pdf"));
		//PDDocument doc = PDDocument.load(new File("/data/Immediate — Beta/Knowledge!!  :DD/Computerey :3/Specifications!/USB/UVC Class specification.pdf"));
		
		
		int n = doc.getNumberOfPages();
		
		for (int i = 0; i < n; i++)
		{
			PDPage page = doc.getPage(i);
			
			System.out.println("Page: "+i);
			
			PDFStreamEngine acceptor = new PDFGraphicsStreamEngine2(page)
			{
				@Override
				public void lineTo(float x, float y) throws IOException
				{
					System.out.println("\tL: "+x+", "+y);
				}
				
				@Override
				public void moveTo(float x, float y) throws IOException
				{
					System.out.println("\tM: "+x+", "+y);
				}
				
				@Override
				public void appendRectangle(Point2D p0, Point2D p1, Point2D p2, Point2D p3) throws IOException
				{
					System.out.println("\tRectangle: ("+p0.getX()+", "+p0.getY()+"), ("+p1.getX()+", "+p1.getY()+"), ("+p2.getX()+", "+p2.getY()+"), ("+p3.getX()+", "+p3.getY()+")");
				}
				
				@Override
				public void drawImage(PDImage pdImage) throws IOException
				{
					System.out.println("\tRASTER IMAGE!!");
				}
				
				@Override
				public void clip(int windingRule) throws IOException
				{
					System.out.println("\tClip: "+windingRuleToString(windingRule));
				}
				
				@Override
				public void curveTo(float x1, float y1, float x2, float y2, float x3, float y3) throws IOException
				{
					System.out.println("\tC: ("+x1+", "+y1+"),  ("+x2+", "+y2+"),  ("+x3+", "+y3+")");
				}
				
				@Override
				public void closePath() throws IOException
				{
					System.out.println("\tClose");
				}
				
				@Override
				public void endPath() throws IOException
				{
					System.out.println("\tEnd");
				}
				
				@Override
				public void strokePath() throws IOException
				{
					System.out.println("\tStroke!");
				}
				
				@Override
				public void fillPath(int windingRule) throws IOException
				{
					System.out.println("\tFill: "+windingRuleToString(windingRule));
				}
				
				@Override
				public void fillAndStrokePath(int windingRule) throws IOException
				{
					System.out.println("\tFill and Stroke: "+windingRuleToString(windingRule));
				}
				
				@Override
				public void shadingFill(COSName shadingName) throws IOException
				{
					System.out.println("\tShading Fill with: "+repr(shadingName.getName()));
				}
			};
			acceptor.processPage(page);
		}
	}
	
	
	
	
	public static final String windingRuleToString(int windingRule)
	{
		if (windingRule == PathIterator.WIND_EVEN_ODD)
			return "Even-Odd";
		else if (windingRule == PathIterator.WIND_NON_ZERO)
			return "Nonzero";
		else
			throw new IllegalArgumentException(String.valueOf(windingRule));
	}
}
