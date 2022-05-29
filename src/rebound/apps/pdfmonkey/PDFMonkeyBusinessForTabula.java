package rebound.apps.pdfmonkey;

import static java.util.Objects.*;
import static rebound.text.StringUtilities.*;
import static rebound.util.collections.BasicCollectionUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.List;
import rebound.annotations.semantic.allowedoperations.ReadonlyValue;
import rebound.annotations.semantic.allowedoperations.WritableValue;
import rebound.annotations.semantic.reachability.ThrowAwayValue;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.texttable.util.TextTableUtilities;
import rebound.exceptions.NotSingletonException;
import rebound.util.collections.SimpleTable;
import technology.tabula.Pair;
import technology.tabula.RectangularTextContainer;
import technology.tabula.Table;
import technology.tabula.TabulaRectangle;
import technology.tabula.app.AreaCalculationMode;
import technology.tabula.app.ExtractionMethod;
import technology.tabula.app.TableExtractor;
import technology.tabula.app.Tabula;

public class PDFMonkeyBusinessForTabula
{
	public static SimpleTable<String> extractEntirePageAsTable(BasicPDFPage page) throws Exception
	{
		SimpleTable<String> t = _extractEntirePageAsTableCSV(page);
		postProcessTabulaCSV(t);
		return t;
	}
	
	public static SimpleTable<String> extractRegionOfPageAsTable(BasicPDFPage page, Rectangle2D regionInPageSpace) throws Exception
	{
		SimpleTable<String> t = _extractRegionOfPageAsTableCSV(page, regionInPageSpace);
		postProcessTabulaCSV(t);
		return t;
	}
	
	protected static void postProcessTabulaCSV(@WritableValue SimpleTable<String> table)
	{
		TextTableUtilities.trimInPlace(table);
	}
	
	
	
	
	
	
	
	
	public static String extractEntirePageAsText(BasicPDFPage page) throws Exception
	{
		requireNonNull(page);
		
		//Todo something else more proper than converting to CSV then turning that into text ^^' X'D
		return convertTabulaCSVOutputToText(_extractEntirePageAsTableCSV(page));
	}
	
	public static String extractRegionOfPageAsText(BasicPDFPage page, Rectangle2D regionInPageSpace) throws Exception
	{
		requireNonNull(page);
		requireNonNull(regionInPageSpace);
		
		//Todo something else more proper than converting to CSV then turning that into text ^^' X'D
		return convertTabulaCSVOutputToText(_extractRegionOfPageAsTableCSV(page, regionInPageSpace));
	}
	
	protected static String convertTabulaCSVOutputToText(@ReadonlyValue SimpleTable<String> table)
	{
		requireNonNull(table);
		
		StringBuilder buff = new StringBuilder();
		
		int w = table.getNumberOfColumns();
		int h = table.getNumberOfRows();
		
		for (int r = 0; r < h; r++)
		{
			for (int c = 0; c < w; c++)
			{
				String cell = table.getCellContents(c, r);
				
				boolean lastColumn = c == w - 1;
				
				if (lastColumn)
				{
					cell = rtrim(cell);
					buff.append(cell);
				}
				else
				{
					cell = rtrim(cell);
					buff.append(cell);
					buff.append(' ');
				}
			}
			
			buff.append('\n');
		}
		
		return buff.toString();
	}
	
	
	
	
	
	
	
	
	
	
	
	@ThrowAwayValue
	protected static SimpleTable<String> _extractEntirePageAsTableCSV(BasicPDFPage page) throws Exception
	{
		requireNonNull(page);
		
		File file = page.getContainingFile().getLocalFileIfApplicable();
		int pageIndex = page.getPageIndex();
		
		TableExtractor tableExtractor = new TableExtractor();
		tableExtractor.setGuess(false);
		tableExtractor.setMethod(ExtractionMethod.DECIDE);
		
		Tabula t = new Tabula(null, listof(pageIndex + 1), null, tableExtractor);
		
		return convertTabulaTableToRebound(t.extract(file));
	}
	
	@ThrowAwayValue
	protected static SimpleTable<String> _extractRegionOfPageAsTableCSV(BasicPDFPage page, Rectangle2D regionInPageSpace) throws Exception
	{
		requireNonNull(page);
		requireNonNull(regionInPageSpace);
		
		File file = page.getContainingFile().getLocalFileIfApplicable();
		
		if (file == null)
			throw new IllegalArgumentException("Currently requires an actual file to use, sorry ^^'");
			
		int pageIndex = page.getPageIndex();
		
		TableExtractor tableExtractor = new TableExtractor();
		tableExtractor.setGuess(false);
		tableExtractor.setMethod(ExtractionMethod.DECIDE);
		
		
		//Uhhhhhh Tabula you don't use actual PDF coordinates?! X'D
		//    TODO Debug this for PDFs with a page origin (ie, page.getPageBoundaries().getMinX/Y()) which isn't [0, 0]   X'3
		//double y = regionInPageSpace.getY();
		double y = page.getPageBoundaries().getHeight() - regionInPageSpace.getY() - regionInPageSpace.getHeight();
		
		//NOTE THE FLIPPED X AND Y RELATIVE TO WIDTH/HEIGHT
		//TABULA WHYYYYYYYYYYY? XD
		TabulaRectangle tr = new TabulaRectangle((float)y, (float)regionInPageSpace.getX(), (float)regionInPageSpace.getWidth(), (float)regionInPageSpace.getHeight());
		
		List<Pair<AreaCalculationMode, TabulaRectangle>> pageAreas = listof(new Pair<>(AreaCalculationMode.ABSOLUTE_AREA_CALCULATION_MODE, tr));
		Tabula t = new Tabula(pageAreas, listof(pageIndex + 1), null, tableExtractor);
		
		return convertTabulaTableToRebound(t.extract(file));
	}
	
	
	
	@ThrowAwayValue
	protected static SimpleTable<String> convertTabulaTableToRebound(List<Table> tabulaTables) throws NotSingletonException
	{
		int n = tabulaTables.size();
		
		if (n == 0)
		{
			return newTable();
		}
		else if (n == 1)
		{
			Table tabulaTable = getSingleElement(tabulaTables);
			
			SimpleTable<String> reboundTable = null;
			
			int r = 0;
			
			for (List<RectangularTextContainer> row : tabulaTable.getRows())
			{
				if (reboundTable == null)
					reboundTable = newTableNullfilled(row.size(), tabulaTable.getRowCount());
				
				int c = 0;
				
				for (RectangularTextContainer<?> tc : row)
				{
					reboundTable.setCellContents(c, r, tc.getText());
					c++;
				}
				
				r++;
			}
			
			return reboundTable == null ? newTable() : reboundTable;
		}
		else
		{
			throw new NotSingletonException("Tabula found multiple ("+tabulaTables.size()+") tables!!");
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	//	protected static String _extractEntirePageAsTableCSV(BasicPDFPage page) throws Exception
	//	{
	//		File file = page.getContainingFile().getLocalFileIfApplicable();
	//		int pageIndex = page.getPageIndex();
	//		
	//		if (file == null)
	//			throw new UnsupportedOptionException("Currently we can only work on local PDF files, Sorry!! ^^\"");
	//		
	//		String rawCSV = runAndCaptureOutput("tabula", "--format", "CSV", "--pages", Integer.toString(pageIndex + 1), file.getAbsolutePath());
	//		
	//		return rawCSV;
	//	}
	//	
	//	
	//	protected static String _extractRegionOfPageAsTableCSV(BasicPDFPage page, Rectangle2D regionInPageSpace) throws Exception
	//	{
	//		File file = page.getContainingFile().getLocalFileIfApplicable();
	//		int pageIndex = page.getPageIndex();
	//		
	//		if (file == null)
	//			throw new UnsupportedOptionException("Currently we can only work on local PDF files, Sorry!! ^^\"");
	//		
	//		String rawCSV = runAndCaptureOutput("tabula",
	//		"--area",
	//		Double.toString(regionInPageSpace.getMinY())+","+
	//		Double.toString(regionInPageSpace.getMinX())+","+
	//		Double.toString(regionInPageSpace.getMaxY())+","+
	//		Double.toString(regionInPageSpace.getMaxX()),
	//		
	//		"--format", "CSV",
	//		"--pages", Integer.toString(pageIndex + 1),
	//		file.getAbsolutePath()
	//		);
	//		
	//		return rawCSV;
	//	}
	//	
	//	
	//	
	//	
	//	
	//	
	//	protected static String runAndCaptureOutput(String... commandAndArgs) throws Exception
	//	{
	//		ProcessBuilder pb = new ProcessBuilder(Arrays.asList(commandAndArgs));
	//		pb.redirectOutput(Redirect.PIPE);
	//		pb.redirectError(Redirect.INHERIT);
	//		Process p = pb.start();
	//		
	//		byte[] raw = JRECompatIOUtilities.readAll(p.getInputStream());
	//		
	//		int ec = p.waitFor();
	//		
	//		if (ec != 0)
	//			throw new IOException("Non-zero exit code! ("+ec+") -- The "+repr(commandAndArgs[0])+" command failed!!");
	//		
	//		return new String(raw, StandardCharsets.UTF_8);
	//	}
}
