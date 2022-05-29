package rebound.dataformats.pdf;

import java.io.File;
import java.io.IOException;
import rebound.exceptions.BinarySyntaxException;

public interface BasicPDFSystem
{
	public BasicPDFFile load(File file) throws IOException, BinarySyntaxException;
	
	
	
	
	
	
	
	
	//	public default BasicPDFFile load(File file) throws IOException, BinarySyntaxException
	//	{
	//		try (FileInputStream in = new FileInputStream(file))
	//		{
	//			return load(in);
	//		}
	//	}
	
	
	//	public default BasicPDFFile load(byte[] data) throws BinarySyntaxException
	//	{
	//		ByteArrayInputStream in = new ByteArrayInputStream(data);
	//		
	//		try
	//		{
	//			return load(in);
	//		}
	//		catch (IOException exc)
	//		{
	//			throw BinarySyntaxException.inst(exc);
	//		}
	//	}
	//	
	//	
	//	public BasicPDFFile load(InputStream in) throws IOException, BinarySyntaxException;
}
