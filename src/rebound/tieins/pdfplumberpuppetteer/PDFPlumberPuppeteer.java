package rebound.tieins.pdfplumberpuppetteer;

import static java.util.Objects.*;
import static rebound.concurrency.ConcurrencyUtilities.*;
import static rebound.util.BasicExceptionUtilities.*;
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
import rebound.osint.OSUtilities;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

public class PDFPlumberPuppeteer
implements Closeable
{
	protected final @Nonnull Reader fromPuppeteer;
	protected final @Nonnull Writer toPuppeteer;
	protected final @Nullable RunnableThrowingIOException cleanlyWaitFor;
	protected final @Nullable RunnableThrowingIOException forciblyCleanup;
	
	public PDFPlumberPuppeteer(InputStream fromPuppeteer, OutputStream toPuppeteer, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		requireNonNull(fromPuppeteer);
		requireNonNull(toPuppeteer);
		this.fromPuppeteer = new InputStreamReader(fromPuppeteer, StandardCharsets.UTF_8);
		this.toPuppeteer = new OutputStreamWriter(toPuppeteer, StandardCharsets.UTF_8);
		this.cleanlyWaitFor = cleanlyWaitFor;
		this.forciblyCleanup = forciblyCleanup;
	}
	
	
	
	protected boolean closed = false;
	
	@Override
	public void close() throws IOException
	{
		final boolean cleanShuttingDown;
		
		try
		{
			if (!closed)
			{
				this.closed = true;
				sendShutdownCleanlyButDontWait();
				cleanShuttingDown = true;
			}
			else
			{
				cleanShuttingDown = false;
			}
		}
		finally
		{
			try
			{
				if (!cleanShuttingDown)
					fromPuppeteer.close();
			}
			finally
			{
				try
				{
					if (!cleanShuttingDown)
						toPuppeteer.close();
				}
				finally
				{
					try
					{
						if (cleanlyWaitFor != null)
						{
							Throwable[] tc = new Throwable[1];
							
							Thread waiter = spawnDaemon(() ->
							{
								try
								{
									if (cleanShuttingDown)
										waitForCleanShutDownResponse();
									
									cleanlyWaitFor.run();
								}
								catch (Throwable t)
								{
									tc[0] = t;
								}
								finally
								{
									try
									{
										if (cleanShuttingDown)
										{
											synchronized (this)
											{
												fromPuppeteer.close();
												fromPuppeteer = null;
											}
										}
									}
									finally
									{
										if (cleanShuttingDown)
										{
											synchronized (this)
											{
												toPuppeteer.close();
												toPuppeteer = null;
											}
										}
									}
								}
							});
							
							waiter.start();
							
							joinThreadFullyMS(waiter, 10*1000);  //Todo configurable maximum-time-to-wait
							
							if (tc[0] != null)
							{
								if (tc[0] instanceof IOException)
									throw (IOException)tc[0];
								else
									rethrowSafe(tc[0]);
							}
						}
					}
					finally
					{
						try
						{
							synchronized (this)
							{
								toPuppeteer.close();
								toPuppeteer = null;
							}
						}
						finally
						{
							try
							{
								synchronized (this)
								{
									fromPuppeteer.close();
									fromPuppeteer = null;
								}
							}
							finally
							{
								if (forciblyCleanup != null)
								{
									forciblyCleanup.run();
								}
							}
						}
					}
				}
			}
		}
	}
	
	
	public static PDFPlumberPuppeteer openLocal(String python3Interpreter, File pdfplumberpuppeteerFile, @Nullable String pythonpath) throws IOException
	{
		ProcessBuilder b = new ProcessBuilder(listof(python3Interpreter, pdfplumberpuppeteerFile.getAbsolutePath()));
		
		if (pythonpath != null)
			b.environment().put("PYTHONPATH", pythonpath);
		
		Process p = b.start();
		
		InputStream fromPuppeteer = p.getInputStream();
		OutputStream toPuppeteer = p.getOutputStream();
		RunnableThrowingIOException cleanlyWaitFor = OSUtilities.processWaiter(p);
		RunnableThrowingIOException forciblyCleanup = () -> {if (p.isAlive()) p.destroyForcibly();};
		
		return new PDFPlumberPuppeteer(fromPuppeteer, toPuppeteer, cleanlyWaitFor, forciblyCleanup);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
