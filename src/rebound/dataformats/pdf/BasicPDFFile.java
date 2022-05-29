package rebound.dataformats.pdf;

import java.io.File;
import java.util.List;
import javax.annotation.Nullable;
import rebound.annotations.semantic.allowedoperations.ReadonlyValue;
import rebound.annotations.semantic.temporal.ConstantReturnValue;

public interface BasicPDFFile
{
	public default boolean isEmpty()
	{
		return getNumberOfPages() == 0;
	}
	
	public default int getNumberOfPages()
	{
		return getPages().size();
	}
	
	
	@ConstantReturnValue
	@ReadonlyValue
	public List<? extends BasicPDFPage> getPages();
	
	
	
	@Nullable
	@ConstantReturnValue
	public File getLocalFileIfApplicable();
}
