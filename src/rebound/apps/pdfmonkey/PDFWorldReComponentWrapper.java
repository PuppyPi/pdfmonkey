package rebound.apps.pdfmonkey;

import java.awt.Component;
import java.awt.Container;
import javax.annotation.Nonnull;
import rebound.annotations.semantic.temporal.ConstantReturnValue;
import rebound.hci.graphics2d.gui.TraceableCollage;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentZoomer;
import rebound.math.SmallIntegerMathUtilities;

/**
 * This represents the {@link ReComponent page(s)} and also the borders and the arrangement of the pages (single-page-only? multiple in a sequence? multiple juxtaposed without borders between? tiled in a grid? etc. :> )
 * 
 * Any page preloading or unloading is automatically handled by intercepting {@link Component#paint(java.awt.Graphics)} calls and to determine which region is actually being seen :>
 * Logically (as far as anything using this component is concerned), *all pages are always visible*!
 * But as far as {@link Container awt is concerned}, some pages may be {@link Container#add(Component) loaded} and {@link Container#remove(Component) unloaded} periodically to save memory.
 * 
 * • The Child Identifier in {@link TraceableCollage} is the zero-based page index and the "child space" is the PDF page space (which is right-handed with opposite y direction, remember!)
 */
public interface PDFWorldReComponentWrapper
extends TraceableCollage<Integer>
{
	@ConstantReturnValue
	@Nonnull
	public ReComponent getComponent();
	
	
	
	
	public void setDisplaySettingsFromOtherOfSameRuntimeTypeOfIgnoreIfImmutable(PDFWorldReComponentWrapper other);
	
	
	
	
	/**
	 * @param pageIndexZerobased the implementation should {@link SmallIntegerMathUtilities#progmod(int, int) wrap} it if the provided value is out of bounds
	 */
	public void gotoPage(int pageIndexZerobased, ReComponentZoomer zoomer);
}
