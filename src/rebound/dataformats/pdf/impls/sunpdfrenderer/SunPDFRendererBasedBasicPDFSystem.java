package rebound.dataformats.pdf.impls.sunpdfrenderer;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.dataformats.pdf.BasicPDFSystem;
import rebound.dataformats.pdf.impls.AbstractBasicPDFFile;
import rebound.dataformats.pdf.impls.AbstractBasicPDFPage;
import rebound.dataformats.pdf.impls.sunpdfrenderer.SunPDFRendererBasedBasicPDFSystem.SunPDFRendererBasedBasicPDFFile.SunPDFRendererBasedBasicPDFPage;
import rebound.exceptions.BinarySyntaxException;
import rebound.io.util.FSIOUtilities;
import com.totallynotsun.pdfview.PDFFile;
import com.totallynotsun.pdfview.PDFPage;

public class SunPDFRendererBasedBasicPDFSystem
implements BasicPDFSystem
{
	@Override
	public BasicPDFFile load(File file) throws IOException, BinarySyntaxException
	{
		PDFFile implFile = new PDFFile(FSIOUtilities.fileAsByteBufferReadonly(file));
		
		int numberOfPages = implFile.getNumPages();
		
		List<SunPDFRendererBasedBasicPDFPage> pages = new ArrayList<>();
		
		SunPDFRendererBasedBasicPDFFile ourFile = new SunPDFRendererBasedBasicPDFFile(implFile, pages, file);
		
		for (int pageIndex = 0; pageIndex < numberOfPages; pageIndex++)
		{
			PDFPage implPage = implFile.getPage(pageIndex+1);
			
			Rectangle2D pageBoundaries = implPage.getPageBox();
			
			SunPDFRendererBasedBasicPDFPage ourPage = new SunPDFRendererBasedBasicPDFPage(ourFile, implPage, pageIndex, pageBoundaries);
			
			pages.add(ourPage);
		}
		
		
		return ourFile;
	}
	
	
	
	
	
	
	
	public static class SunPDFRendererBasedBasicPDFFile
	extends AbstractBasicPDFFile
	implements BasicPDFFile
	{
		protected final PDFFile implFile;
		
		public SunPDFRendererBasedBasicPDFFile(PDFFile implFile, List<SunPDFRendererBasedBasicPDFPage> pages, File localFileIfApplicable)
		{
			super(pages, localFileIfApplicable);
			this.implFile = implFile;
		}
		
		public PDFFile getImplFile()
		{
			return implFile;
		}
		
		
		
		
		
		public static class SunPDFRendererBasedBasicPDFPage
		extends AbstractBasicPDFPage
		implements BasicPDFPageRenderableRaster
		{
			protected final PDFPage implPage;
			
			public SunPDFRendererBasedBasicPDFPage(SunPDFRendererBasedBasicPDFFile containingFile, PDFPage implPage, int pageIndex, Rectangle2D pageBoundaries)
			{
				super(containingFile, pageIndex, pageBoundaries);
				this.implPage = implPage;
			}
			
			@Override
			public SunPDFRendererBasedBasicPDFFile getContainingFile()
			{
				return (SunPDFRendererBasedBasicPDFFile)super.getContainingFile();
			}
			
			public PDFPage getImplPage()
			{
				return implPage;
			}

			
			
			@Override
			public Image getRenderedImage(int imageWidth, int imageHeight, Rectangle2D clipInPageSpace)
			{
				// get the new image
				return implPage.getImage(imageWidth, imageHeight, clipInPageSpace, null, true, true);
			}
		}
	}
}
