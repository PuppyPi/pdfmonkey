package rebound.tieins.pdfplumberpuppetteer;

import java.io.File;
import rebound.util.res.TemporaryAppResourceGroupUnpacker;

public class PDFPlumberPythonCode
{
	protected static final TemporaryAppResourceGroupUnpacker unpacker;
	
	static
	{
		unpacker = TemporaryAppResourceGroupUnpacker.unpackStandard(PDFPlumberPythonCode.class, "python");
		
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			unpacker.cleanup();
		}));
	}
	
	
	
	public static File getPDFPlumberPuppetPythonFile()
	{
		return unpacker.getFile("pdfplumberpuppet.py");
	}
	
	public static File getPDFPlumberPythonLibrariesFolder()
	{
		return unpacker.getFile("lib");
	}
}
