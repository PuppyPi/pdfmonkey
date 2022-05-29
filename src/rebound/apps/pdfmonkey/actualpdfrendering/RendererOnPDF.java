package rebound.apps.pdfmonkey.actualpdfrendering;

import rebound.annotations.semantic.reachability.ThrowAwayValue;
import rebound.apps.pdfmonkey.PDFPageComponentWrapper;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;

public interface RendererOnPDF
{
	public int getNumberOfPages();
	
	
	/**
	 * Visually, pages are immutable static things, but practically they may cache images for only the currently-displayed region at the current zoom level, and so using one instance in two or more parent components may cause cache contention!
	 * Hence the use of {@link ReComponent} which {@link ReComponent#setParentForRepainting(Runnable) explicitly prevents that} and the whole {@link ThrowAwayValue} thing :3
	 */
	@ThrowAwayValue
	public PDFPageComponentWrapper newForPage(int pageIndexZerobased);
}
