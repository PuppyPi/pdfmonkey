package rebound.apps.pdfmonkey;

import static java.awt.event.KeyEvent.*;
import static java.util.Collections.*;
import static java.util.Objects.*;
import static rebound.file.FSUtilities.*;
import static rebound.hci.util.awt.JavaGUIUtilities.*;
import static rebound.math.geom2d.AffineTransformUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import static rebound.util.collections.BasicCollectionUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.awt.Color;
import java.awt.Container;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import rebound.apps.pdfmonkey.actualpdfrendering.RasterBasedRendererOnPDF;
import rebound.apps.pdfmonkey.actualpdfrendering.RendererOnPDF;
import rebound.bits.DataEncodingUtilities;
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.pdf.BasicPDFSystem;
import rebound.dataformats.texttable.rcsv.RCSV;
import rebound.exceptions.BinarySyntaxException;
import rebound.hci.graphics2d.gui.TraceableCollage.PointLocationResult;
import rebound.hci.graphics2d.gui.TraceableCollage.RectangleSinglePageLocationResult;
import rebound.hci.graphics2d.gui.awt.AwtAndSimpleTrackersUtilities;
import rebound.hci.graphics2d.gui.awt.AwtAndSimpleTrackersUtilities.MouseSuite;
import rebound.hci.graphics2d.gui.awt.components.AwtPaneForReComponent;
import rebound.hci.graphics2d.gui.awt.components.SolidColorComponent;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentZoomerWithMouseInput;
import rebound.hci.graphics2d.gui.recomponent.components.ReComponentZoomerWithMouseInput.WheelMode;
import rebound.hci.graphics2d.gui.recomponent.components.RectangleDraggingOverlay;
import rebound.hci.graphics2d.gui.simpletrackers.ButtonRepeater;
import rebound.hci.graphics2d.gui.simpletrackers.ButtonRepeater.RepeatableKeyCombinationAndAction;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.ButtonInputReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.buttons.SimpleButtonStateTracker;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.DragReceiver;
import rebound.hci.graphics2d.gui.simpletrackers.pointing.integer.PointingMotionInputReceiver.AbsolutePointingMotionInputReceiver;
import rebound.hci.util.awt.SimpleDragAndDrop;
import rebound.io.util.FSIOUtilities;
import rebound.math.SmallIntegerMathUtilities;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;
import rebound.math.geom.ints.analogoustojavaawt.IntRectangle;
import rebound.testing.WidespreadTestingUtilities;
import rebound.util.collections.NestedListsSimpleTable;
import rebound.util.collections.SimpleTable;
import rebound.util.crypto.CryptographyAndDigestUtilities;
import rebound.util.crypto.CryptographyAndDigestUtilities.StandardDigestType;
import rebound.util.functional.FunctionInterfaces.UnaryFunction;

public class PDFMonkeyWindow
extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	
	protected final @Nonnull PDFMonkeyApp app;
	protected final @Nonnull UnaryFunction<RendererOnPDF, PDFWorldReComponentWrapper> pdfWorldMaker;
	
	protected final @Nonnull AwtPaneForReComponent awtAdapter;
	protected final @Nonnull RectangleDraggingOverlay selectionDragger;
	protected final @Nonnull ReComponentZoomerWithMouseInput zoomer;
	protected @Nullable PDFWorldReComponentWrapper pdfWorld;
	protected @Nullable RendererOnPDF pdf;
	protected BasicPDFFile pdfFile;
	protected String pdfFileLocatorFormatted;
	protected String pdfFileHashFormatted;
	
	
	
	protected SolidColorComponent divider;
	protected SolidColorComponent pageIndexLabelBackdrop;
	protected JLabel pageIndexLabel;
	
	protected Color dragBoxColorExtractTable = new Color(192, 0, 0);
	protected Color dragBoxColorExtractText = new Color(64, 64, 192);
	
	
	protected boolean showMouseCoordinatesInPageSpace = true;
	
	protected MouseSuite trackers;
	protected ButtonRepeater buttonRepeater = new ButtonRepeater();
	
	
	
	
	public static interface DragOperation
	{
		public void run(int pageIndexZerobased, Rectangle2D regionInPageSpace);
	}
	
	protected DragOperation dragOperation = null;
	
	protected final DragOperation extractTextOperation = this::extractText;
	protected final DragOperation extractTableOperation = this::extractTable;
	
	
	
	
	
	
	public PDFMonkeyWindow(@Nonnull PDFMonkeyApp app, UnaryFunction<RendererOnPDF, PDFWorldReComponentWrapper> pdfWorldMaker)
	{
		super("PDF Monkey!! Ook! :D");
		
		this.app = requireNonNull(app);
		this.pdfWorldMaker = pdfWorldMaker;
		
		this.awtAdapter = new AwtPaneForReComponent();
		
		this.zoomer = new ReComponentZoomerWithMouseInput();
		
		this.selectionDragger = new RectangleDraggingOverlay(zoomer, new DragReceiver()
		{
			@Override
			public void dragStarted(int startX, int startY)
			{
			}
			
			@Override
			public void dragUpdated(int startX, int startY, int currentX, int currentY)
			{
			}
			
			@Override
			public void dragCompleted(int startX, int startY, int endX, int endY)
			{
				Rectangle r = intrectFromTwoPoints(startX, startY, endX, endY);
				ourDragCompleted(r.x, r.y, r.width, r.height);
			}
			
			@Override
			public void dragCancelled()
			{
			}
		});
		
		awtAdapter.setContainedComponent(selectionDragger);
		
		Container cp = new JPanel(null, false)
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public void doLayout()
			{
				int w = getWidth();
				int h = getHeight();
				
				
				int pageLabelHeight = 20;
				int dividerHeight = 1;
				
				int pdfPaneHeight = h - (pageLabelHeight + dividerHeight);
				
				
				awtAdapter.setLocation(0, 0);
				awtAdapter.setSize(w, pdfPaneHeight);
				
				divider.setLocation(0, pdfPaneHeight);
				divider.setSize(w, dividerHeight);
				
				pageIndexLabel.setLocation(0, pdfPaneHeight + dividerHeight);
				pageIndexLabel.setSize(w, pageLabelHeight);
				pageIndexLabelBackdrop.setLocation(0, pdfPaneHeight + dividerHeight);
				pageIndexLabelBackdrop.setSize(w, pageLabelHeight);
			}
		};
		
		
		
		cp.add(awtAdapter);
		
		
		Color pageIndexLabelBG = new Color(192, 192, 255);
		
		pageIndexLabel = new JLabel();
		pageIndexLabel.setFont(Font.decode("Arial-12"));
		pageIndexLabel.setForeground(new Color(0, 0, 128));
		pageIndexLabel.setHorizontalAlignment(JLabel.CENTER);
		pageIndexLabel.setVerticalAlignment(JLabel.CENTER);
		cp.add(pageIndexLabel);
		
		divider = new SolidColorComponent(new Color(0, 0, 128));
		cp.add(divider);
		
		pageIndexLabelBackdrop = new SolidColorComponent(pageIndexLabelBG);  //pageIndexLabel.setBackground(pageIndexLabelBG) wasn't doing anything apparently!!  (or JLabels just never/unreliably draw their background???)   \o/
		cp.add(pageIndexLabelBackdrop);
		
		
		this.setContentPane(cp);
		
		
		
		
		SimpleDragAndDrop.setupSimpleFilesDrag(this, data ->
		{
			if (data instanceof Set)
				handleFilesDropped(data);
		});
		
		
		initKeyTracker();
		
		
		recalculateStatusBar();
		doLayout();
	}
	
	public @Nullable IntPoint getCurrentMousePositionInPDFWorldSpace()
	{
		return pdfWorld == null ? null : pdfWorld.getComponent().getCurrentCursorPosition();
	}
	
	
	
	
	
	
	
	
	
	protected @Nonnull PDFWorldReComponentWrapper newPDFWorld(RendererOnPDF pdf)
	{
		PDFWorldReComponentWrapper w = pdfWorldMaker.f(pdf);
		
		w.getComponent().getMouseMotionBroadcaster().addReceiver(new AbsolutePointingMotionInputReceiver()
		{
			@Override
			public void pointingLost()
			{
				recalculateStatusBar();
			}
			
			@Override
			public void pointingAbsoluteMotion(int newX, int newY)
			{
				recalculateStatusBar();
			}
		});
		
		return w;
	}
	
	
	
	public void displayPDF(@Nullable BasicPDFFile pdfFile)
	{
		RendererOnPDF pdf = new RasterBasedRendererOnPDF(pdfFile);
		displayPDF(pdf, pdfFile);
	}
	
	
	public void displayPDF(RendererOnPDF pdf, @Nullable BasicPDFFile pdfFile)
	{
		if (hasPDFLoaded())
		{
			clearAllInputActionsDueToPDFFileChanging();
			this.zoomer.setContainedComponent(null);
			this.pdfWorld = null;
		}
		
		if (pdf != null && pdf.getNumberOfPages() > 0)
		{
			this.pdf = pdf;
			this.pdfFile = pdfFile;
			
			File f = pdfFile.getLocalFileIfApplicable();
			this.pdfFileLocatorFormatted = f == null ? "<unknown filename>" : f.getName();
			
			if (f == null)
			{
				this.pdfFileHashFormatted = "<unknown file>";
			}
			else
			{
				try
				{
					this.pdfFileHashFormatted = "MD5:"+DataEncodingUtilities.encodeHexNoDelimiter(CryptographyAndDigestUtilities.performStandardDigest(FSIOUtilities.readAll(f), StandardDigestType.MD5), DataEncodingUtilities.HEX_UPPERCASE);
				}
				catch (IOException exc)
				{
					exc.printStackTrace();
					this.pdfFileHashFormatted = "<i/o error>";
				}
			}
			
			this.pdfWorld = newPDFWorld(pdf);
			zoomer.setContainedComponent(pdfWorld.getComponent());
			recalculateStatusBar();
		}
		else
		{
			this.pdf = null;
			this.pdfFile = null;
			this.pdfWorld = null;
			recalculateStatusBar();
		}
	}
	
	public boolean hasPDFLoaded()
	{
		return pdfWorld != null;
	}
	
	public RendererOnPDF getCurrentPDF()
	{
		return pdf;
	}
	
	public BasicPDFSystem getPDFSystem()
	{
		return app.getPDFSystem();
	}
	
	public BasicPDFFile getPDFFile()
	{
		return pdfFile;
	}
	
	public void setPDFFile(BasicPDFFile pdfFile)
	{
		this.pdfFile = pdfFile;
	}
	
	
	
	
	
	
	
	public void openFile(File file)
	{
		temporarilySetStatusBarText("Loading "+file.getName()+" ...");
		
		
		
		BasicPDFFile pdfFile;
		
		try
		{
			pdfFile = getPDFSystem().load(file);
		}
		catch (BinarySyntaxException exc)
		{
			JOptionPane.showMessageDialog(null, "PDF decoding error while loading file "+file.getAbsolutePath()+"\n("+exc.getMessage()+")", "Error opening file!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		catch (IOException exc)
		{
			JOptionPane.showMessageDialog(null, "I/O error while loading file "+file.getAbsolutePath()+"\n("+exc.getMessage()+")", "Error opening file!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		
		displayPDF(pdfFile);
		
		
		temporarilySetStatusBarText("Done! :D");
	}
	
	
	public void openFileFromOtherWindowSharingCacheAndCopyDisplaySettings(PDFMonkeyWindow other)
	{
		displayPDF(other.getCurrentPDF(), other.getPDFFile());
		
		if (other.hasPDFLoaded())
		{
			WidespreadTestingUtilities.asrt(this.hasPDFLoaded());
			
			this.pdfWorld.setDisplaySettingsFromOtherOfSameRuntimeTypeOfIgnoreIfImmutable(other.pdfWorld);
		}
		else
		{
			WidespreadTestingUtilities.asrt(!this.hasPDFLoaded());
		}
		
		this.zoomer.setZoomSettingsFromOther(other.zoomer);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public void ourDragCompleted(int x, int y, int w, int h)
	{
		if (hasPDFLoaded())
		{
			IntRectangle regionInViewerSpace = new IntRectangle(x, y, w, h);
			
			if (dragOperation != null)
			{
				Rectangle2D regionInCanvasSpace = transformAxisAlignedRectangleOPC(intRectToFloat(regionInViewerSpace), zoomer.getCurrentTransformDisplaySpaceToCanvasSpace());
				
				RectangleSinglePageLocationResult<Integer> r = pdfWorld.findRectangleInPageSpace(regionInCanvasSpace);
				
				if (r != null)
				{
					Rectangle2D regionInPageSpace = r.getRectangleInPageSpace();
					
					int pageIndexZerobased = r.getChildIdentifier();
					
					dragOperation.run(pageIndexZerobased, regionInPageSpace);
					
					this.repaint();
				}
			}
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	public List<String> produceInfoRecord(int pageIndexZerobased, @Nullable Rectangle2D regionInPageSpace)
	{
		if (regionInPageSpace == null)
			return listof(pdfFileLocatorFormatted, pdfFileHashFormatted, Integer.toString(pageIndexZerobased + 1));
		else
			return listof(pdfFileLocatorFormatted, pdfFileHashFormatted, Integer.toString(pageIndexZerobased + 1), Double.toString(regionInPageSpace.getX()), Double.toString(regionInPageSpace.getY()), Double.toString(regionInPageSpace.getWidth()), Double.toString(regionInPageSpace.getHeight()));
	}
	
	
	public void extractText(int pageIndexZerobased, Rectangle2D regionInPageSpace)
	{
		if (pdfFile != null)
		{
			try
			{
				BasicPDFPage page = pdfFile.getPages().get(pageIndexZerobased);
				
				String result = PDFMonkeyBusiness.extractRegionOfPageAsText(page, regionInPageSpace);
				
				String infoLine = RCSV.serializeRCSV(new NestedListsSimpleTable<String>(singletonList(produceInfoRecord(pageIndexZerobased, regionInPageSpace))));
				String finalResult = result.trim() + "\n" + infoLine;
				
				setClipboardText(finalResult);
				temporarilySetStatusBarText("Done extracting text!");
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error extracting text from region in page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void extractEntirePageText(int pageIndexZerobased)
	{
		try
		{
			BasicPDFPage page = pdfFile.getPages().get(pageIndexZerobased);
			
			String result = PDFMonkeyBusiness.extractEntirePageAsText(page);
			
			String infoLine = RCSV.serializeRCSV(new NestedListsSimpleTable<String>(singletonList(produceInfoRecord(pageIndexZerobased, null))));
			String finalResult = result.trim() + "\n" + infoLine;
			
			setClipboardText(finalResult);
			temporarilySetStatusBarText("Done extracting text!");
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error extracting text from page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	public void extractTable(int pageIndexZerobased, Rectangle2D regionInPageSpace)
	{
		if (pdfFile != null)
		{
			try
			{
				BasicPDFPage page = pdfFile.getPages().get(pageIndexZerobased);
				
				SimpleTable<String> table = PDFMonkeyBusiness.extractRegionOfPageAsTable(page, regionInPageSpace);
				
				List<String> infoLine = produceInfoRecord(pageIndexZerobased, regionInPageSpace);
				
				appendRowExpandingIP(table, infoLine, "");
				
				setClipboardText(RCSV.serializeRCSV(table));
				temporarilySetStatusBarText("Done extracting table!");
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
				JOptionPane.showMessageDialog(null, "Error extracting table from region in page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
	public void extractEntirePageTable(int pageIndexZerobased)
	{
		try
		{
			BasicPDFPage page = pdfFile.getPages().get(pageIndexZerobased);
			
			SimpleTable<String> table = PDFMonkeyBusiness.extractEntirePageAsTable(page);
			
			List<String> infoLine = produceInfoRecord(pageIndexZerobased, null);
			
			appendRowExpandingIP(table, infoLine, "");
			
			setClipboardText(RCSV.serializeRCSV(table));
			temporarilySetStatusBarText("Done extracting table!");
		}
		catch (Exception exc)
		{
			exc.printStackTrace();
			JOptionPane.showMessageDialog(null, "Error extracting table from page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	
	
	
	
	
	
	
	//protected DecimalFormat mouseCoorsFormat = new DecimalFormat("0.000");  //someday we'll have fractional coordinates again XD''
	protected DecimalFormat mouseCoorsFormat = new DecimalFormat("0");
	
	protected void recalculateStatusBar()
	{
		if (!statusBarInTemporaryTextMode)
		{
			String t = "";
			
			if (hasPDFLoaded())
			{
				boolean gotit = false;
				
				if (showMouseCoordinatesInPageSpace)
				{
					//Show mouse coordinates! :D
					
					IntPoint p = getCurrentMousePositionInPDFWorldSpace();
					
					if (p != null)
					{
						PointLocationResult<Integer> pp = pdfWorld.findPointInPageSpace(p.getX(), p.getY());
						
						if (pp != null)
						{
							int i = pp.getChildIdentifier();
							t = "Page "+(i+1)+ " / " + pdf.getNumberOfPages();
							
							t += "   ("+mouseCoorsFormat.format(pp.getPointInPageSpaceX())+", "+mouseCoorsFormat.format(pp.getPointInPageSpaceY())+")";
							
							gotit = true;
						}
					}
				}
				
				if (!gotit)
				{
					Integer arbitraryPageIndexZerobased = getCurrentlyViewedPageIndexZerobased();
					
					if (arbitraryPageIndexZerobased != null)
					{
						int i = arbitraryPageIndexZerobased;
						t = "Page "+(i+1)+ " / " + pdf.getNumberOfPages();
					}
				}
			}
			else
			{
				t += "No file loaded!";
			}
			
			
			
			t += isExtractTableMode() ? "     <drag to extract table!!>" : (isExtractTextMode() ? "     <drag to extract text!!>" : "");
			t += zoomer.isLockMode() ? "" : "     (unlocked scroll)";
			
			
			
			pageIndexLabel.setText(t);
			repaint();
		}
	}
	
	protected ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(1);
	protected long temporaryStatusBarPeriodMS = 500;
	protected ScheduledFuture<?> currentTemporaryStatusBarScheduledTask;
	protected Runnable currentTemporaryStatusBarTaskBody;
	protected boolean statusBarInTemporaryTextMode = false;
	
	protected void temporarilySetStatusBarText(String text)
	{
		if (currentTemporaryStatusBarScheduledTask != null)
		{
			currentTemporaryStatusBarScheduledTask.cancel(false);
			currentTemporaryStatusBarScheduledTask = null;
			currentTemporaryStatusBarTaskBody = null;
		}
		
		
		Runnable task = new Runnable()
		{
			@Override
			public void run()
			{
				if (currentTemporaryStatusBarTaskBody == this)
				{
					statusBarInTemporaryTextMode = false;
					recalculateStatusBar();
					currentTemporaryStatusBarScheduledTask = null;
					currentTemporaryStatusBarTaskBody = null;
				}
			}
		};
		
		
		pageIndexLabel.setText(text);
		statusBarInTemporaryTextMode = true;
		currentTemporaryStatusBarScheduledTask = scheduler.schedule(() -> EventQueue.invokeLater(task), temporaryStatusBarPeriodMS, TimeUnit.MILLISECONDS);
		currentTemporaryStatusBarTaskBody = task;
	}
	
	
	
	
	
	
	
	public @Nullable Integer getCurrentlyViewedPageIndexZerobased()
	{
		return pdfWorld.getArbitraryPageInRectangle(zoomer.transformRectangleFromDisplaySpaceToCanvasSpace(rect(0, 0, zoomer.getWidth(), zoomer.getHeight())));
	}
	
	public double pageScrollAmountX()
	{
		//Todo softcode ^^'
		return zoomer.getWidth() * 0.3d;
	}
	
	public double pageScrollAmountY()
	{
		//Todo softcode ^^'
		return zoomer.getHeight() * 0.3d;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void clearAllInputActionsDueToPDFFileChanging()
	{
		selectionDragger.cancelDrag();
	}
	
	
	
	
	
	
	protected SimpleButtonStateTracker keyTracker;
	
	
	protected void initKeyTracker()
	{
		keyTracker = AwtAndSimpleTrackersUtilities.setupKeySuite(this, new ButtonInputReceiver()
		{
			@Override
			public void setButtonState(int keyCode, boolean newState)
			{
				boolean actionModifierDown = isActionModifierDown();
				boolean shiftModifierDown = isShiftModifierDown();
				
				
				if (newState == true)
				{
					if (keyCode == VK_ESCAPE)
					{
						selectionDragger.cancelDrag();
					}
					
					
					else if (keyCode == VK_N && actionModifierDown)
					{
						app.openNewDuplicateWindow(PDFMonkeyWindow.this);
					}
					
					//Navigation within the page ^www^
					else if (keyCode == VK_EQUALS)
					{
						if (hasPDFLoaded())
							zoomer.multiplyScaleButKeepCenterInCoincidence(zoomer.getRelativeZoomFactor());
						selectionDragger.cancelDrag();
					}
					else if (keyCode == VK_MINUS)
					{
						if (hasPDFLoaded())
							zoomer.multiplyScaleButKeepCenterInCoincidence(1/zoomer.getRelativeZoomFactor());
						selectionDragger.cancelDrag();
					}
					else if (keyCode == VK_0)
					{
						if (hasPDFLoaded())
							zoomer.setToNaturalScaleButKeepCenterInCoincidence();
						selectionDragger.cancelDrag();
					}
					else if (keyCode == VK_A)
					{
						if (hasPDFLoaded())
							zoomer.setToFitToCanvas();
						selectionDragger.cancelDrag();
					}
					else if (keyCode == VK_W)
					{
						if (hasPDFLoaded())
							zoomer.setToFitToWidthButKeepCenterYsInCoincidence();
						selectionDragger.cancelDrag();
					}
					else if (keyCode == VK_H)
					{
						if (hasPDFLoaded())
							zoomer.setToFitToHeightButKeepCenterXsInCoincidence();
						selectionDragger.cancelDrag();
					}
					
					
					else if (keyCode == VK_L)
					{
						zoomer.setLockMode(!zoomer.isLockMode());
						recalculateStatusBar();
						
						selectionDragger.cancelDrag();
					}
					
					
					
					
					
					
					
					
					
					
					//Changing pagesssss!! :DDD
					else if (keyCode == VK_J)
					{
						if (hasPDFLoaded())
						{
							boolean zeroBased = keyTracker.getButtonState(VK_ALT);
							
							int i = getCurrentlyViewedPageIndexZerobased();
							
							Object response = JOptionPane.showInputDialog(null, "Enter the new page "+(zeroBased ? "index" : "number")+"! :D", "Jump to Page", JOptionPane.QUESTION_MESSAGE, null, null, String.valueOf(i + (zeroBased ? 0 : 1)));
							
							if (response != null)  //null = cancelled ^wwwww^
							{
								String a = (String) response;
								
								boolean valid = false;
								int newPageIndex = 0;
								{
									try
									{
										newPageIndex = Integer.parseInt(a);
										valid = true;
									}
									catch (NumberFormatException exc)
									{
									}
								}
								
								if (valid)
								{
									if (!zeroBased)
										newPageIndex--;
									
									newPageIndex = SmallIntegerMathUtilities.progmod(newPageIndex, getCurrentPDF().getNumberOfPages());
									
									pdfWorld.gotoPage(newPageIndex, zoomer);
									
									recalculateStatusBar();
								}
							}
						}
					}
					
					else if (keyCode == VK_HOME)
					{
						zoomer.setCurrentCanvasTranslationAndScale(zoomer.getCurrentCanvasTranslationX(), 0, zoomer.getCurrentScale());
						recalculateStatusBar();
						repaint();
					}
					else if (keyCode == VK_END)
					{
						zoomer.translateToPutIntoCoincidence(pointOrVector2D(0, zoomer.getCanvasHeight()), pointOrVector2D(0, zoomer.getHeight()));
						recalculateStatusBar();
						repaint();
					}
					
					
					
					
					
					
					//THE ACTUAL MONKEY BUSINESS!!! XDDD
					else if (keyCode == VK_P)
					{
						if (hasPDFLoaded())
						{
							setClipboardText(Integer.toString(getCurrentlyViewedPageIndexZerobased()+1));
						}
					}
					else if (keyCode == VK_F)
					{
						if (hasPDFLoaded())
						{
							if (pdfFile != null)
							{
								File file = pdfFile.getLocalFileIfApplicable();
								
								if (file != null)
								{
									setClipboardText(normpath(file.getAbsoluteFile()).getPath());
								}
								else
								{
									setClipboardText("<unknown path!!>");
								}
							}
							else
							{
								setClipboardText("<unknown backing!!>");
							}
						}
					}
					
					else if (keyCode == VK_X && actionModifierDown)
					{
						if (hasPDFLoaded())
						{
							Integer pageIndexZerobased = getCurrentlyViewedPageIndexZerobased();
							
							if (pageIndexZerobased != null)
							{
								extractEntirePageText(pageIndexZerobased);
							}
						}
					}
					else if (keyCode == VK_T && actionModifierDown)
					{
						if (hasPDFLoaded())
						{
							Integer pageIndexZerobased = getCurrentlyViewedPageIndexZerobased();
							
							if (pageIndexZerobased != null)
							{
								extractEntirePageTable(pageIndexZerobased);
							}
						}
					}
				}
				
				
				
				
				
				
				
				
				
				//Both key state transitions (true and false) bc why not? X3
				if (keyCode == VK_CONTROL || keyCode == VK_META || keyCode == VK_SHIFT || keyCode == VK_ALT || keyCode == VK_T || keyCode == VK_X)
				{
					recalculateStatusBar();
				}
				
				
				
				if (keyCode == VK_X || keyCode == VK_C)
				{
					selectionDragger.cancelDrag();
				}
				
				
				
				boolean extractDragMode = isExtractTableMode() || isExtractTextMode();
				
				zoomer.setWheelMode(actionModifierDown ? WheelMode.Zoom : (shiftModifierDown ? WheelMode.X : WheelMode.Y));
				zoomer.setDraggingEnabled(!extractDragMode);
				
				dragOperation = isExtractTextMode() ? extractTextOperation : (isExtractTableMode() ? extractTableOperation : null);
				selectionDragger.setDragBoxColor(isExtractTextMode() ? dragBoxColorExtractText : (isExtractTableMode() ? dragBoxColorExtractTable : null));
				selectionDragger.setDraggingEnabled(extractDragMode);
				
				
				
				buttonRepeatForGotoPageUp.setKeyCombinationDown(keyTracker.getButtonState(VK_OPEN_BRACKET) || (keyTracker.getButtonState(VK_PAGE_UP) && actionModifierDown));
				buttonRepeatForGotoPageDown.setKeyCombinationDown(keyTracker.getButtonState(VK_CLOSE_BRACKET) || (keyTracker.getButtonState(VK_PAGE_DOWN) && actionModifierDown));
				buttonRepeatForPageUpX.setKeyCombinationDown(keyTracker.getButtonState(VK_PAGE_UP) && shiftModifierDown && !actionModifierDown);
				buttonRepeatForPageDownX.setKeyCombinationDown(keyTracker.getButtonState(VK_PAGE_DOWN) && shiftModifierDown && !actionModifierDown);
				buttonRepeatForPageUpY.setKeyCombinationDown(keyTracker.getButtonState(VK_PAGE_UP) && !shiftModifierDown && !actionModifierDown);
				buttonRepeatForPageDownY.setKeyCombinationDown(keyTracker.getButtonState(VK_PAGE_DOWN) && !shiftModifierDown && !actionModifierDown);
				
				
				
				
				//Probably unnecessary but why not? X3
				recalculateStatusBar();
			}
		});
	}
	
	
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForGotoPageUp = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		gotoPageRelative(-1);
	});
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForGotoPageDown = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		gotoPageRelative(1);
	});
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForPageUpX = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		xPageScrollRelative(-1);
	});
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForPageDownX = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		xPageScrollRelative(1);
	});
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForPageUpY = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		yPageScrollRelative(-1);
	});
	
	protected RepeatableKeyCombinationAndAction buttonRepeatForPageDownY = buttonRepeater.newCombinationAwtEventQueue(() ->
	{
		yPageScrollRelative(1);
	});
	
	
	
	
	protected void gotoPageRelative(int r)
	{
		if (hasPDFLoaded())
		{
			pdfWorld.gotoPage(getCurrentlyViewedPageIndexZerobased() + r, zoomer);
			recalculateStatusBar();
			repaint();
		}
	}
	
	protected void xPageScrollRelative(int signum)
	{
		if (hasPDFLoaded())
		{
			zoomer.setCurrentCanvasTranslationAndScale(zoomer.getCurrentCanvasTranslationX() + -signum*pageScrollAmountX(), zoomer.getCurrentCanvasTranslationY(), zoomer.getCurrentScale());
			recalculateStatusBar();
			repaint();
		}
	}
	
	protected void yPageScrollRelative(int signum)
	{
		if (hasPDFLoaded())
		{
			zoomer.setCurrentCanvasTranslationAndScale(zoomer.getCurrentCanvasTranslationX(), zoomer.getCurrentCanvasTranslationY() + -signum*pageScrollAmountY(), zoomer.getCurrentScale());
			recalculateStatusBar();
			repaint();
		}
	}
	
	
	
	
	
	
	
	
	protected boolean isExtractTextMode()
	{
		return keyTracker.getButtonState(VK_X) && !keyTracker.getButtonState(VK_T) && !isActionModifierDown();
	}
	
	protected boolean isExtractTableMode()
	{
		return keyTracker.getButtonState(VK_T) && !keyTracker.getButtonState(VK_X) && !isActionModifierDown();
	}
	
	
	
	
	protected boolean isActionModifierDown()
	{
		return keyTracker.getButtonState(PDFMonkeyApp.ActionKeyCode);
	}
	
	protected boolean isShiftModifierDown()
	{
		return keyTracker.getButtonState(KeyEvent.VK_SHIFT);
	}
	
	
	
	
	
	
	
	
	
	public void handleFilesDropped(Set<File> files)
	{
		if (files.size() == 1 && !this.hasPDFLoaded())
		{
			this.openFile(getSingleElement(files));
		}
		else
		{
			for (File f : files)
				app.openNewWindow(f);
		}
	}
}
