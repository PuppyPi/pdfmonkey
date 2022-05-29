package rebound.tieins.pdfplumberpuppetteer;

import static rebound.util.collections.CollectionUtilities.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

public class PDFPlumberPuppeteer
extends AbstractPuppeteer
implements Closeable
{
	protected @Nonnull Reader fromPuppetText;
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
		
		this.fromPuppetText = new InputStreamReader(fromPuppet, StandardCharsets.UTF_8);
		this.toPuppetText = new OutputStreamWriter(toPuppet, StandardCharsets.UTF_8);
	}
	
	
	
	
	public static PDFPlumberPuppeteer openLocal(String python3Interpreter, File pdfplumberpuppeteerFile, @Nullable String pythonpath) throws IOException
	{
		ProcessBuilder b = new ProcessBuilder(listof(python3Interpreter, pdfplumberpuppeteerFile.getAbsolutePath()));
		
		if (pythonpath != null)
			b.environment().put("PYTHONPATH", pythonpath);
		
		Process p = b.start();
		return new PDFPlumberPuppeteer(p);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
