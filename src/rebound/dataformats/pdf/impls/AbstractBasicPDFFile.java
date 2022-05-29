package rebound.dataformats.pdf.impls;

import java.io.File;
import java.util.Collections;
import java.util.List;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPage;

public abstract class AbstractBasicPDFFile
implements BasicPDFFile
{
	protected final List<? extends BasicPDFPage> pages;
	protected final List<? extends BasicPDFPage> pagesRO;
	protected final File localFileIfApplicable;
	
	public AbstractBasicPDFFile(List<? extends BasicPDFPage> pages, File localFileIfApplicable)
	{
		this.pages = pages;
		this.pagesRO = Collections.unmodifiableList(pages);
		this.localFileIfApplicable = localFileIfApplicable;
	}
	
	@Override
	public List<? extends BasicPDFPage> getPages()
	{
		return pagesRO;
	}
	
	@Override
	public File getLocalFileIfApplicable()
	{
		return localFileIfApplicable;
	}
}
