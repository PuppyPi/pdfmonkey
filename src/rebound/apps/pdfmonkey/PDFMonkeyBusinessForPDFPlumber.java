package rebound.apps.pdfmonkey;

import static rebound.util.collections.CollectionUtilities.*;
import static rebound.util.objectutil.BasicObjectUtilities.*;
import java.awt.geom.Rectangle2D;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.tieins.pdfplumberpuppetteer.PDFPlumberBoundingBox;
import rebound.tieins.pdfplumberpuppetteer.PDFPlumberPuppeteer;
import rebound.tieins.pdfplumberpuppetteer.PDFPlumberTableMetadata;
import rebound.util.collections.PairOrdered;
import rebound.util.collections.SimpleTable;

/**
 * Note that this is not a static class like tabula!
 * (mostly because we can't embed the PDFPlumber library directly in us as it turns out <i>Jython never got around to supporting Python 3!!</i>  x"D )
 */
public class PDFMonkeyBusinessForPDFPlumber
implements Closeable
{
	protected PDFPlumberPuppeteer puppeteerConnection;
	
	
	/**
	 * @throws UnsupportedOperationException  If PDFPlumber or Python3 or required libraries couldn't be found.
	 */
	public PDFMonkeyBusinessForPDFPlumber() throws UnsupportedOperationException
	{
		//TODO softcode
		String python3Interpreter = "python3";
		File puppet = new File("/fix/eclipse/workspace/PDFMonkey/src-python/pdfplumberpuppeteer.py");
		File libDir = new File("/fix/eclipse/workspace/PDFMonkey/src-python/lib");
		
		String d = libDir.getAbsolutePath();
		if (d.indexOf(':') != -1)
			throw new IllegalStateException("The path to the Python libraries contains a colon in it!  And afaik they never invented an escape syntax for that!");
		
		try
		{
			this.puppeteerConnection = PDFPlumberPuppeteer.openLocal(python3Interpreter, puppet, d);
		}
		catch (IOException exc)
		{
			throw new UnsupportedOperationException(exc);
		}
	}
	
	
	
	public static class TableData
	{
		public final Rectangle2D box;
		public final List<Rectangle2D> cells;
		public final SimpleTable<String> contents;
		
		public TableData(Rectangle2D box, List<Rectangle2D> cells, SimpleTable<String> contents)
		{
			this.box = box;
			this.cells = cells;
			this.contents = contents;
		}
	}
	
	
	
	
	
	protected File currentOpenFile = null;
	
	public List<TableData> extractEntirePageAsTables(BasicPDFPage page) throws Exception
	{
		return extractRegionOfPageAsTables(page, null);
	}
	
	public List<TableData> extractRegionOfPageAsTables(BasicPDFPage page, @Nullable Rectangle2D regionInPageSpace) throws Exception
	{
		File thisFile = page.getContainingFile().getLocalFileIfApplicable();
		
		if (thisFile == null)
			throw new UnsupportedOperationException();
		
		if (!eq(thisFile, currentOpenFile))
		{
			puppeteerConnection.openPDFFile(thisFile.getAbsolutePath());
			currentOpenFile = thisFile;
		}
		
		List<PairOrdered<PDFPlumberTableMetadata, SimpleTable<String>>> r = puppeteerConnection.findAndExtractTables(page.getPageIndex(), regionInPageSpace == null ? null : ours2theirsBBox(regionInPageSpace, page));
		
		return mapToList(t -> new TableData(theirs2oursBBox(t.getA().getBoundingBox(), page), mapToList(b -> theirs2oursBBox(b, page), t.getA().getCellBoundingBoxen()), mapTableOP(c -> c == null ? "" : c, t.getB())), r);
	}
	
	protected PDFPlumberBoundingBox ours2theirsBBox(Rectangle2D r, BasicPDFPage page)
	{
		double y = page.getPageBoundaries().getHeight() - r.getY() - r.getHeight();
		return new PDFPlumberBoundingBox(r.getMinX(), y, r.getMaxX(), y + r.getHeight());
	}
	
	protected Rectangle2D theirs2oursBBox(PDFPlumberBoundingBox r, BasicPDFPage page)
	{
		double y = page.getPageBoundaries().getHeight() - r.getBottom();
		return new Rectangle2D.Double(r.getLeft(), y, r.getRight() - r.getLeft(), r.getBottom() - r.getTop());
	}
	
	
	
	
	
	
	
	
	
	@Override
	public void close() throws IOException
	{
		puppeteerConnection.close();
	}
}
