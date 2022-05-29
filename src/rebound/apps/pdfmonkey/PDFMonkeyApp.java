package rebound.apps.pdfmonkey;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import rebound.apps.reboundcommonsbuilding.packaging.jar.annotations.PackageAsSimpleJar;
import rebound.dataformats.pdf.BasicPDFSystem;
import rebound.dataformats.pdf.impls.sunpdfrenderer.SunPDFRendererBasedBasicPDFSystem;
import rebound.hci.util.awt.AWTEventDispatchingThread;
import rebound.hci.util.awt.JavaGUIUtilities;

@PackageAsSimpleJar("/fix/rpsout/tools/jars/rt/pdfmonkey.jar")
public class PDFMonkeyApp
{
	public static final int ActionKeyCode = JavaGUIUtilities.getKeyCodeForActionKey();
	
	
	
	public static void main(String[] args)
	{
		EventQueue.invokeLater(() -> mainInAwtThread(args));
	}
	
	
	@AWTEventDispatchingThread
	public static void mainInAwtThread(String[] args)
	{
		BasicPDFSystem pdfSystem = new SunPDFRendererBasedBasicPDFSystem();
		
		PDFMonkeyApp app = new PDFMonkeyApp(pdfSystem);
		
		for (String arg : args)
			app.openNewWindow(new File(arg).getAbsoluteFile());
	}
	
	
	
	
	
	
	
	
	
	protected BasicPDFSystem pdfSystem;
	protected Set<PDFMonkeyWindow> windows = new HashSet<>();
	
	public PDFMonkeyApp(BasicPDFSystem pdfSystem)
	{
		this.pdfSystem = pdfSystem;
	}
	
	
	public PDFMonkeyWindow newWindow()
	{
		final PDFMonkeyWindow newWindow = new PDFMonkeyWindow(this);
		newWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newWindow.setSize(500, 500);
		windows.add(newWindow);
		
		newWindow.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				windows.remove(newWindow);
				
				if (windows.isEmpty())
					System.exit(0);
			}
		});
		
		return newWindow;
	}
	
	public void openNewWindow(File pdfFile)
	{
		PDFMonkeyWindow monkeyWindow = newWindow();
		
		monkeyWindow.openFile(pdfFile);
		
		monkeyWindow.setVisible(true);
	}
	
	public void openNewDuplicateWindow(PDFMonkeyWindow window)
	{
		PDFMonkeyWindow monkeyWindow = newWindow();
		
		monkeyWindow.openFileFromOtherWindowSharingCache(window);
		
		monkeyWindow.setCurrentPageByIndex(window.getCurrentPageIndex());
		
		monkeyWindow.setVisible(true);
	}
	
	
	
	public BasicPDFSystem getPDFSystem()
	{
		return pdfSystem;
	}
}
