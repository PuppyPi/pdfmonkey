package rebound.apps.pdfmonkey.actualpdfrendering;

import static java.util.Objects.*;
import java.lang.ref.SoftReference;
import javax.annotation.Nonnull;
import rebound.apps.pdfmonkey.PDFPageComponentWrapper;

public class CachingRendererOnPDF
{
	protected final RendererOnPDF underlying;
	
	protected SoftReference<PDFPageComponentWrapper>[] cache;
	
	
	public CachingRendererOnPDF(@Nonnull RendererOnPDF underlying)
	{
		this.underlying = requireNonNull(underlying);
		this.cache = new SoftReference[underlying.getNumberOfPages()];
	}
	
	
	
	public RendererOnPDF getUnderlying()
	{
		return underlying;
	}
	
	
	
	
	public PDFPageComponentWrapper getForPage(int pageIndexZerobased)
	{
		SoftReference<PDFPageComponentWrapper> c = cache[pageIndexZerobased];
		
		if (c != null)
		{
			PDFPageComponentWrapper r = c.get();
			
			if (r != null)
			{
				return r;
			}
		}
		
		
		//else: remake it :3
		{
			PDFPageComponentWrapper r = underlying.newForPage(pageIndexZerobased);
			c = new SoftReference<PDFPageComponentWrapper>(r);
			cache[pageIndexZerobased] = c;
			return r;
		}
	}
}
