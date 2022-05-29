package rebound.tieins.pdfplumberpuppetteer;

public class PDFPlumberPuppeteerException
extends RuntimeException
{
	private static final long serialVersionUID = 1l;
	
	public PDFPlumberPuppeteerException()
	{
		super();
	}
	
	public PDFPlumberPuppeteerException(String message)
	{
		super(message);
	}
	
	public PDFPlumberPuppeteerException(Throwable cause)
	{
		super(cause);
	}
	
	public PDFPlumberPuppeteerException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
