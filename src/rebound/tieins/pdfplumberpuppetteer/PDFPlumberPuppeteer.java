package rebound.tieins.pdfplumberpuppetteer;

import static rebound.text.StringUtilities.*;
import static rebound.util.collections.BasicCollectionUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import static rebound.util.gds.util.GDSUtilities.*;
import static rebound.util.objectutil.BasicObjectUtilities.*;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.dataformats.json.JSONUtilities;
import rebound.exceptions.GenericDataStructuresFormatException;
import rebound.exceptions.ImpossibleException;
import rebound.io.util.AbstractPuppeteer;
import rebound.util.collections.NestedListsSimpleTable;
import rebound.util.collections.PairOrdered;
import rebound.util.collections.SimpleTable;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

/**
 * PDFPlumber table contents is given here as a {@link SimpleTable} where the {@link String}s are {@link Nullable}.
 * (I think PDFPlumber uses nulls when the cell is absent not empty, eg from cell merging!)
 */
public class PDFPlumberPuppeteer
extends AbstractPuppeteer
implements Closeable
{
	protected @Nonnull BufferedReader fromPuppetText;
	protected @Nonnull Writer toPuppetText;
	
	
	public PDFPlumberPuppeteer(InputStream fromPuppet, OutputStream toPuppet, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		super(fromPuppet, toPuppet, cleanlyWaitFor, forciblyCleanup);
	}
	
	public PDFPlumberPuppeteer(Process p)
	{
		super(p);
	}
	
	@Override
	protected void init(InputStream fromPuppet, OutputStream toPuppet, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		super.init(fromPuppet, toPuppet, cleanlyWaitFor, forciblyCleanup);
		
		this.fromPuppetText = new BufferedReader(new InputStreamReader(fromPuppet, StandardCharsets.UTF_8));
		this.toPuppetText = new OutputStreamWriter(toPuppet, StandardCharsets.UTF_8);
	}
	
	
	
	
	public static PDFPlumberPuppeteer openLocal(String python3Interpreter, File pdfplumberpuppeteerFile, @Nullable String pythonpath) throws IOException
	{
		ProcessBuilder b = new ProcessBuilder(listof(python3Interpreter, pdfplumberpuppeteerFile.getAbsolutePath()));
		
		if (pythonpath != null)
			b.environment().put("PYTHONPATH", pythonpath);
		
		b.redirectInput(Redirect.PIPE);
		b.redirectOutput(Redirect.PIPE);
		b.redirectError(Redirect.INHERIT);
		
		Process p = b.start();
		return new PDFPlumberPuppeteer(p);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * @return the number of pages in the pdf file as returned by PDFPlumber!  :D
	 */
	public int openPDFFile(String pathname) throws IOException
	{
		write(listof("open", pathname));
		return toIntNonnull(readResponseWhenSuccessIsUnary());
	}
	
	public void closePDFFile() throws IOException
	{
		write(listof("close"));
		readResponseWhenSuccessIsNullary();
	}
	
	@Override
	protected void sendShutdownCleanlyButDontWait() throws IOException
	{
		write(listof("quit"));
	}
	
	@Override
	protected void waitForCleanShutDownResponse() throws IOException
	{
		readResponseWhenSuccessIsNullary();
	}
	
	
	
	
	public List<PDFPlumberTableMetadata> findTables(int pageIndex, @Nullable PDFPlumberBoundingBox region) throws IOException
	{
		if (region == null)
			write(listof("find_tables", pageIndex));
		else
			write(listof("find_tables", pageIndex, region.getLeft(), region.getTop(), region.getRight(), region.getBottom()));
		
		return mapToList(g -> convertTableMetadata(g), (List)readResponseWhenSuccessIsUnary());
	}
	
	public List<PairOrdered<PDFPlumberTableMetadata, SimpleTable<String>>> findAndExtractTables(int pageIndex, @Nullable PDFPlumberBoundingBox region) throws IOException
	{
		if (region == null)
			write(listof("find_tables,extract_tables", pageIndex));
		else
			write(listof("find_tables,extract_tables", pageIndex, region.getLeft(), region.getTop(), region.getRight(), region.getBottom()));
		
		List r = readResponseWhenSuccessIsNary(2);
		List metadataGDSs = (List) r.get(0);
		List extractionGDSs = (List) r.get(1);
		
		int n = metadataGDSs.size();
		if (n != extractionGDSs.size())
			throw new GenericDataStructuresFormatException();
		
		return mapToList(i -> pair(convertTableMetadata(metadataGDSs.get(i)), convertExtractedTable(extractionGDSs.get(i))), intervalIntegersList(0, n));
	}
	
	public List<SimpleTable<String>> extractTables(int pageIndex, @Nullable PDFPlumberBoundingBox region) throws IOException
	{
		if (region == null)
			write(listof("extract_tables", pageIndex));
		else
			write(listof("extract_tables", pageIndex, region.getLeft(), region.getTop(), region.getRight(), region.getBottom()));
		
		return mapToList(g -> convertExtractedTable(g), (List)readResponseWhenSuccessIsUnary());
	}
	
	
	/**
	 * Call after {@link #findTables(int, PDFPlumberBoundingBox)} to operate on its output.
	 */
	public SimpleTable<String> extractCurrentTable(int tableIndex) throws IOException
	{
		write(listof("find_tables[i].extract", tableIndex));
		return convertExtractedTable(readResponseWhenSuccessIsUnary());
	}
	
	/**
	 * Call after {@link #findTables(int, PDFPlumberBoundingBox)} to operate on its output.
	 */
	public List<SimpleTable<String>> extractCurrentTables() throws IOException
	{
		write(listof("find_tables.extract"));
		return mapToList(g -> convertExtractedTable(g), (List)readResponseWhenSuccessIsUnary());
	}
	
	
	
	
	
	
	
	protected PDFPlumberTableMetadata convertTableMetadata(Object gds)
	{
		Map<String, Object> m = (Map<String, Object>) gds;
		List<Object> bbox = (List<Object>) getMandatory(m, "bbox");
		List<List<Object>> cells = (List<List<Object>>) getMandatory(m, "cells");
		
		return new PDFPlumberTableMetadata(convertBoundingBox(bbox), mapToList(c -> convertBoundingBox(c), cells));
	}
	
	
	protected PDFPlumberBoundingBox convertBoundingBox(Object gds)
	{
		List<Object> l = (List<Object>) gds;
		if (l.size() != 4)
			throw new GenericDataStructuresFormatException();
		return new PDFPlumberBoundingBox(toDoubleNonnull(l.get(0)), toDoubleNonnull(l.get(1)), toDoubleNonnull(l.get(2)), toDoubleNonnull(l.get(3)));
	}
	
	
	protected SimpleTable<String> convertExtractedTable(Object gds)
	{
		return new NestedListsSimpleTable<>((List<List<String>>)gds);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	protected void readResponseWhenSuccessIsNullary() throws IOException
	{
		readResponseWhenSuccessIsNary(0);
	}
	
	protected Object readResponseWhenSuccessIsUnary() throws IOException
	{
		return readResponseWhenSuccessIsNary(1).get(0);
	}
	
	protected List readResponseWhenSuccessIsNary(int expectedSize) throws IOException
	{
		List r = readResponse();
		if (r.size() != expectedSize)
			throw new GenericDataStructuresFormatException("Got response with "+r.size()+" elements instead of "+expectedSize);
		return r;
	}
	
	
	protected List readResponse() throws IOException
	{
		List r = (List) read();
		
		String c = (String) r.get(0);
		
		if (eq(c, "okay"))
			return r.subList(1, r.size());
		else if (eq(c, "error"))
			throw new PDFPlumberPuppeteerException((String)r.get(1));
		else
			throw new GenericDataStructuresFormatException("Unknown response code: "+repr(c));
	}
	
	
	
	
	
	protected Object read() throws IOException
	{
		String line = fromPuppetText.readLine();
		return JSONUtilities.parseJSON(line);
	}
	
	protected void write(Object x) throws IOException
	{
		String s = JSONUtilities.serializeJSONEfficiently(x);
		
		if (s.indexOf('\n') != -1)
			throw new ImpossibleException();
		
		if (s.indexOf('\r') != -1)
			throw new ImpossibleException();
		
		toPuppetText.write(s);
		toPuppetText.flush();
	}
}
