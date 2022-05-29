package rebound.tieins.pdfplumberpuppetteer;

import static rebound.util.collections.CollectionUtilities.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nullable;
import rebound.osint.OSUtilities;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

public class PDFPlumberPuppeteerEmissary
implements Closeable
{
	protected final Reader fromPuppeteer;
	protected final Writer toPuppeteer;
	protected final RunnableThrowingIOException cleanlyWaitFor;
	protected final RunnableThrowingIOException forciblyCleanup;
	
	public PDFPlumberPuppeteerEmissary(InputStream fromPuppeteer, OutputStream toPuppeteer, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		this.fromPuppeteer = new InputStreamReader(fromPuppeteer, StandardCharsets.UTF_8);
		this.toPuppeteer = new OutputStreamWriter(toPuppeteer, StandardCharsets.UTF_8);
		this.cleanlyWaitFor = cleanlyWaitFor;
		this.forciblyCleanup = forciblyCleanup;
	}



	@Override
	public void close() throws IOException
	{
		try
		{
			fromPuppeteer.close();
		}
		finally
		{
			try
			{
				toPuppeteer.close();
			}
			finally
			{
				forciblyCleanup.close();
			}
		}
	}
	
	
	
	public static PDFPlumberPuppeteerEmissary openLocal(String python3Interpreter, File pdfplumberpuppeteerFile, @Nullable String pythonpath) throws IOException
	{
		ProcessBuilder b = new ProcessBuilder(listof(python3Interpreter, pdfplumberpuppeteerFile.getAbsolutePath()));
		
		if (pythonpath != null)
			b.environment().put("PYTHONPATH", pythonpath);
		
		Process p = b.start();
		
		InputStream fromPuppeteer = p.getInputStream();
		OutputStream toPuppeteer = p.getOutputStream();
		RunnableThrowingIOException waiter = OSUtilities.processWaiter(p);
		
		return new PDFPlumberPuppeteerEmissary(fromPuppeteer, toPuppeteer, waiter, p::destroyForcibly);
	}
}
