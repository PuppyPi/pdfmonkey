package rebound.apps.pdfmonkey;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JFrame;
import rebound.apps.pdfmonkey.actualpdfrendering.RendererOnPDF;
import rebound.apps.pdfmonkey.worlds.MultiPageWorld;
import rebound.apps.reboundcommonsbuilding.packaging.jar.annotations.PackageAsSimpleJar;
import rebound.dataformats.pdf.BasicPDFSystem;
import rebound.dataformats.pdf.impls.sunpdfrenderer.SunPDFRendererBasedBasicPDFSystem;
import rebound.hci.graphics2d.gui.recomponent.components.SimpleFuzzyBorder;
import rebound.hci.util.awt.AWTEventDispatchingThread;
import rebound.hci.util.awt.JavaGUIUtilities;
import rebound.util.functional.FunctionInterfaces.UnaryFunction;

//Todo at least an indicating/display-only scrollbarish indicator on the side even if it doesn't have input abilities
//Todo arrow keys moving it a little bit

//Todo softcoded configurable preferencessssssss for keyboard keys and such X'D

@PackageAsSimpleJar(value="/fix/rpsout/tools/jars/rt/pdfmonkey.jar", maxMemory=6*1073741824l)
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
		
		if (args.length == 0)
		{
			app.openNewWindow();
		}
		else
		{
			for (String arg : args)
				app.openNewWindow(new File(arg).getAbsoluteFile());
		}
	}
	
	
	
	
	
	
	
	
	
	protected BasicPDFSystem pdfSystem;
	protected Set<PDFMonkeyWindow> windows = new HashSet<>();
	
	public PDFMonkeyApp(BasicPDFSystem pdfSystem)
	{
		this.pdfSystem = pdfSystem;
	}
	
	
	public PDFMonkeyWindow newWindow()
	{
		//Todo softcode ^^'
		BorderMaker pageBorderMaker = () ->
		{
			SimpleFuzzyBorder border = new SimpleFuzzyBorder();
			border.setPageBorderThicknessInPixels(3);
			border.setInnermostColor(new Color(189, 189, 189));
			return border;
		};
		
		
		//UnaryFunction<RendererOnPDF, PDFWorldReComponentWrapper> pdfWorldMaker = pdf -> new SinglePageWorld(pdf, pageBorderMaker);
		UnaryFunction<RendererOnPDF, PDFWorldReComponentWrapper> pdfWorldMaker = pdf -> MultiPageWorld.newMultiPageWorld_VerticalLayout(pdf, pageBorderMaker);
		
		
		final PDFMonkeyWindow newWindow = new PDFMonkeyWindow(this, pdfWorldMaker);
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
	
	public void openNewWindow()
	{
		PDFMonkeyWindow monkeyWindow = newWindow();
		monkeyWindow.setVisible(true);
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
		monkeyWindow.openFileFromOtherWindowSharingCacheAndCopyDisplaySettings(window);
		monkeyWindow.setVisible(true);
	}
	
	
	
	public BasicPDFSystem getPDFSystem()
	{
		return pdfSystem;
	}
}
