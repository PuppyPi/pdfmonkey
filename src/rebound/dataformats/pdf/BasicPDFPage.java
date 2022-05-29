package rebound.dataformats.pdf;

import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import rebound.annotations.semantic.allowedoperations.ReadonlyValue;
import rebound.annotations.semantic.temporal.ConstantReturnValue;

public interface BasicPDFPage
{
	@ConstantReturnValue
	@Nonnull
	public BasicPDFFile getContainingFile();
	
	public int getPageIndex();
	
	
	
	@ReadonlyValue
	@ConstantReturnValue
	@Nonnull
	public Rectangle2D getPageBoundaries();
}
