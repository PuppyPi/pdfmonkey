package rebound.tieins.pdfplumberpuppetteer;

import static java.util.Objects.*;
import static rebound.concurrency.ConcurrencyUtilities.*;
import static rebound.osint.OSUtilities.*;
import static rebound.util.BasicExceptionUtilities.*;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import rebound.util.functional.throwing.FunctionalInterfacesThrowingCheckedExceptionsStandard.RunnableThrowingIOException;

public abstract class AbstractPuppeteer
implements Closeable
{
	protected @Nonnull InputStream fromPuppet;
	protected @Nonnull OutputStream toPuppet;
	protected @Nullable RunnableThrowingIOException cleanlyWaitFor;
	protected @Nullable RunnableThrowingIOException forciblyCleanup;
	
	public AbstractPuppeteer(InputStream fromPuppet, OutputStream toPuppet, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		init(fromPuppet, toPuppet, cleanlyWaitFor, forciblyCleanup);
	}
	
	public AbstractPuppeteer(Process p)
	{
		this(p.getInputStream(), p.getOutputStream(), processWaiter(p), () -> {if (p.isAlive()) p.destroyForcibly();});
	}
	
	
	protected void init(InputStream fromPuppet, OutputStream toPuppet, RunnableThrowingIOException cleanlyWaitFor, RunnableThrowingIOException forciblyCleanup)
	{
		requireNonNull(fromPuppet);
		requireNonNull(toPuppet);
		this.fromPuppet = fromPuppet;
		this.toPuppet = toPuppet;
		this.cleanlyWaitFor = cleanlyWaitFor;
		this.forciblyCleanup = forciblyCleanup;
	}
	
	
	
	
	
	
	protected boolean closed = false;
	
	@Override
	public void close() throws IOException
	{
		boolean cleanShuttingDown = false;
		
		try
		{
			if (!closed)
			{
				this.closed = true;
				sendShutdownCleanlyButDontWait();
				cleanShuttingDown = true;
			}
		}
		finally
		{
			try
			{
				if (!cleanShuttingDown)
					fromPuppet.close();
			}
			finally
			{
				try
				{
					if (!cleanShuttingDown)
						toPuppet.close();
				}
				finally
				{
					try
					{
						if (cleanlyWaitFor != null)
						{
							Throwable[] tc = new Throwable[1];
							
							boolean cleanShuttingDown_ = cleanShuttingDown;
							
							Thread waiter = spawnDaemon(() ->
							{
								try
								{
									
									try
									{
										if (cleanShuttingDown_)
											waitForCleanShutDownResponse();
										
										cleanlyWaitFor.run();
									}
									finally
									{
										try
										{
											if (cleanShuttingDown_)
											{
												synchronized (this)
												{
													if (fromPuppet != null)
													{
														fromPuppet.close();
														fromPuppet = null;
													}
												}
											}
										}
										finally
										{
											if (cleanShuttingDown_)
											{
												synchronized (this)
												{
													if (toPuppet != null)
													{
														toPuppet.close();
														toPuppet = null;
													}
												}
											}
										}
									}
									
								}
								catch (Throwable t)
								{
									tc[0] = t;
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
								if (fromPuppet != null)
								{
									fromPuppet.close();
									fromPuppet = null;
								}
							}
						}
						finally
						{
							try
							{
								synchronized (this)
								{
									if (toPuppet != null)
									{
										toPuppet.close();
										toPuppet = null;
									}
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
	
	
	
	
	
	protected abstract void sendShutdownCleanlyButDontWait() throws IOException;
	protected abstract void waitForCleanShutDownResponse() throws IOException;
}
