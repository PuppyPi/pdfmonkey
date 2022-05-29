package rebound.apps.pdfmonkey;

import static java.awt.event.KeyEvent.*;
import static rebound.file.FSUtilities.*;
import static rebound.hci.util.awt.JavaGUIUtilities.*;
import static rebound.math.geom2d.AffineTransformUtilities.*;
import static rebound.math.geom2d.GeometryUtilities2D.*;
import static rebound.util.BasicExceptionUtilities.*;
import static rebound.util.collections.CollectionUtilities.*;
import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
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
import rebound.dataformats.pdf.BasicPDFFile;
import rebound.dataformats.pdf.BasicPDFPage;
import rebound.dataformats.pdf.BasicPDFSystem;
import rebound.dataformats.pdf.util.BasicPDFPageRenderableRasterAdapter;
import rebound.dataformats.texttable.rcsv.RCSV;
import rebound.exceptions.BinarySyntaxException;
import rebound.happygames.shared.util.ButtonInputReceiver;
import rebound.happygames.shared.util.PointingMotionInputReceiver.AbsolutePointingMotionInputReceiver;
import rebound.happygames.shared.util.SimpleAbsoluteCursorPositionTracker;
import rebound.happygames.shared.util.SimpleButtonStateTracker;
import rebound.hci.graphics2d.gui.awt.components.SolidColorComponent;
import rebound.hci.util.awt.JavaGUIUtilities;
import rebound.math.SmallIntegerMathUtilities;
import rebound.math.geom.ints.analogoustojavaawt.IntPoint;
import rebound.math.geom.ints.analogoustojavaawt.IntRectangle;
import rebound.math.geom2d.SmallIntegerBasicGeometry2D;
import rebound.math.geom2d.TranslationAndUniformScaleTransform2D;

public class PDFMonkeyWindow
extends JFrame
{
	private static final long serialVersionUID = 1L;
	
	
	protected final PDFMonkeyApp app;
	protected BasicPDFAwtComponentWrapper pdfComponent;
	
	protected SolidColorComponent divider;
	protected SolidColorComponent pageIndexLabelBackdrop; //???
	protected JLabel pageIndexLabel;
	protected int pageBorderThicknessInPixels = 10;
	
	protected boolean lockMode = true;
	protected double wheelScrollModeFactor = 50;  //pixels per wheel-click-amount ^wwww^
	protected Color dragBoxColorExtractTable = new Color(192, 0, 0);
	protected Color dragBoxColorExtractText = new Color(64, 64, 192);
	protected boolean showMouseCoordinatesInPageSpace = true;
	
	
	public PDFMonkeyWindow(PDFMonkeyApp app)
	{
		super("PDF Monkey!! Ook! :D");
		
		this.enableEvents(AWTEvent.KEY_EVENT_MASK | AWTEvent.FOCUS_EVENT_MASK | AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK | AWTEvent.MOUSE_WHEEL_EVENT_MASK);
		
		this.app = app;
		this.pdfComponent = new BasicPDFAwtComponentWrapperStandardImplementation();
		
		this.setContentPane(new JPanel(null, false)
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
				
				
				if (hasPDFLoaded())
				{
					Component currentAwtComponent = pdfComponent.getAWTComponent();
					currentAwtComponent.setLocation(0, 0);
					currentAwtComponent.setSize(w, pdfPaneHeight);
				}
				
				divider.setLocation(0, pdfPaneHeight);
				divider.setSize(w, dividerHeight);
				
				pageIndexLabel.setLocation(0, pdfPaneHeight + dividerHeight);
				pageIndexLabel.setSize(w, pageLabelHeight);
				pageIndexLabelBackdrop.setLocation(0, pdfPaneHeight + dividerHeight);
				pageIndexLabelBackdrop.setSize(w, pageLabelHeight);
			}
		});
		
		
		Color pageIndexLabelBG = new Color(192, 192, 255);
		
		pageIndexLabel = new JLabel();
		pageIndexLabel.setFont(Font.decode("Arial-12"));
		pageIndexLabel.setBackground(pageIndexLabelBG);
		pageIndexLabel.setForeground(new Color(0, 0, 128));
		pageIndexLabel.setHorizontalAlignment(JLabel.CENTER);
		pageIndexLabel.setVerticalAlignment(JLabel.CENTER);
		this.getContentPane().add(pageIndexLabel);
		
		divider = new SolidColorComponent(new Color(0, 0, 128));
		this.getContentPane().add(divider);
		
		pageIndexLabelBackdrop = new SolidColorComponent(pageIndexLabelBG);  //pageIndexLabel.setBackground(pageIndexLabelBG) wasn't doing anything apparently!!  (or JLabels just never/unreliably draw their background???)   \o/
		this.getContentPane().add(pageIndexLabelBackdrop);
		
		
		
		
		this.setDropTarget(new DropTarget()
		{
			private static final long serialVersionUID = 1L;
			
			@Override
			public synchronized void drop(DropTargetDropEvent dtde)
			{
				//#ProEventDriven:
				//One issue with the two-threads model is that (I think!) we can't (safely) give proper information from the user of the engine as to whether *it* supports/used what was dropped!!    (we can only say if *we*--the engine--support merely decoding the format!)
				//but oh well, that's not a big enough deal to warrant switching the entire model I think X'D
				
				
				Transferable t = dtde.getTransferable();
				
				
				
				//Try files! :D
				{
					Set<File> f;
					
					//					try
					//					{
					f = JavaGUIUtilities.getFilesTransferrableContents(t, () -> dtde.acceptDrop(DnDConstants.ACTION_LINK));
					//					}
					//					catch (IOException exc)
					//					{
					//						String msg = "I/O Error receiving Drag&Drop drop!!\n";
					//						msg += ExceptionPrettyPrintingUtilities.getNicelyFormattedStandardStacktrace(exc);
					//						System.err.println(msg);
					//						dtde.rejectDrop();
					//						return;
					//					}
					
					if (f != null)
					{
						dtde.dropComplete(true);
						
						handleFilesDropped(f);
						
						return;
					}
				}
				
				
				
				dtde.rejectDrop();
			}
		});
		
		recalculateStatusBar();
		doLayout();
	}
	
	
	
	protected void paintPDFOverlay(Graphics2D g)
	{
		if (hasPDFLoaded())
		{
			//Paint the page borderrrrr!! :DD
			{
				//todo account for rounding better ^^'
				IntRectangle closestPageBorder = roundIntRectOutwards(pdfComponent.getCurrentPageBorderInDisplaySpace());
				
				int valueInnermost = 189;
				int valueOutermost = 255;
				
				
				IntRectangle b = new IntRectangle(closestPageBorder);
				for (int i = 0; i < pageBorderThicknessInPixels; i++)
				{
					int value = (valueOutermost - valueInnermost) * i / (pageBorderThicknessInPixels - 1) + valueInnermost;
					g.setColor(new Color(value, value, value));
					
					g.drawRect(b.x - 1, b.y - 1, b.width + 1, b.height + 1);
					
					b.x--;
					b.y--;
					b.width += 2;
					b.height += 2;
				}
			}
			
			
			
			//Paint the current drag box rectangle!! :DDD
			{
				if (inDrag == DragType.OperationRegionExtractTable || inDrag == DragType.OperationRegionExtractText)
				{
					IntPoint now = mouseTracker.getCurrentCursorPosition();
					
					if (now != null)
					{
						IntRectangle dragBox = SmallIntegerBasicGeometry2D.irectTwoPoints(dragStart, now);
						
						Color dragBoxColor = inDrag == DragType.OperationRegionExtractTable ? dragBoxColorExtractTable : dragBoxColorExtractText;
						
						g.setColor(dragBoxColor);
						g.drawRect(dragBox.x - 1, dragBox.y - 1, dragBox.width + 1, dragBox.height + 1);
					}
				}
			}
		}
	}
	
	protected IntRectangle getPagePlusBorderInDisplaySpace()
	{
		//todo account for rounding better ^^'
		IntRectangle closestPageBorder = roundIntRectOutwards(pdfComponent.getCurrentPageBorderInDisplaySpace());
		return SmallIntegerBasicGeometry2D.irect(closestPageBorder.x - 1 - pageBorderThicknessInPixels, closestPageBorder.x - 1 - pageBorderThicknessInPixels, closestPageBorder.width + 1 + pageBorderThicknessInPixels*2, closestPageBorder.height + 1 + pageBorderThicknessInPixels*2);
	}
	
	
	
	
	
	
	
	
	public void displayFile(@Nullable BasicPDFFile pdfFile)
	{
		if (hasPDFLoaded())
		{
			pdfComponent.setOverlay(null);
			pdfComponent.setTransformFilter(null);
			clearAllInputActionsDueToPDFFileChanging();
			this.getContentPane().remove(pdfComponent.getAWTComponent());
		}
		
		if (pdfFile != null && pdfFile.getNumberOfPages() > 0)
		{
			setCurrentPage(pdfFile.getPages().get(0));
			pdfComponent.setOverlay(this::paintPDFOverlay);
			pdfComponent.setTransformFilter(this::recalculateTransformIfLocked);
			this.getContentPane().add(pdfComponent.getAWTComponent());
			recalculateStatusBar();
		}
		else
		{
			pdfComponent = null;
			recalculateStatusBar();
		}
	}
	
	public boolean hasPDFLoaded()
	{
		return pdfComponent.getCurrentPage() != null;
	}
	
	public BasicPDFFile getCurrentPDFFile()
	{
		BasicPDFPage page = pdfComponent.getCurrentPage();
		return page == null ? null : page.getContainingFile();
	}
	
	public BasicPDFAwtComponentWrapper getPDFComponent()
	{
		return pdfComponent;
	}
	
	public BasicPDFSystem getPDFSystem()
	{
		return app.getPDFSystem();
	}
	
	public void setCurrentPageByIndex(int pageIndex)
	{
		BasicPDFFile pdfFile = getCurrentPDFFile();
		
		if (pdfFile == null)
			throw new IllegalStateException();
		
		setCurrentPage(pdfFile.getPages().get(pageIndex));
		
		recalculateStatusBar();
		repaint();
	}
	
	public void setCurrentPage(BasicPDFPage page)
	{
		pdfComponent.setCurrentPage(BasicPDFPageRenderableRasterAdapter.toRasterRendering(page));
	}
	
	public int getCurrentPageIndex()
	{
		BasicPDFPage page = pdfComponent.getCurrentPage();
		if (page == null)
			throw new IllegalStateException();
		return page.getPageIndex();
	}
	
	public void switchPageRelative(int numberOfPages)
	{
		if (hasPDFLoaded())
		{
			setCurrentPageByIndex(SmallIntegerMathUtilities.progmod(getCurrentPageIndex() + numberOfPages, getCurrentPDFFile().getNumberOfPages()));
		}
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
		
		
		displayFile(pdfFile);
		
		temporarilySetStatusBarText("Done! :D");
	}
	
	
	public void openFileFromOtherWindowSharingCache(PDFMonkeyWindow other)
	{
		displayFile(other.getCurrentPDFFile());
	}
	
	
	
	
	protected DecimalFormat mouseCoorsFormat = new DecimalFormat("0.000");
	
	protected void recalculateStatusBar()
	{
		if (!statusBarInTemporaryTextMode)
		{
			String t;
			
			BasicPDFPage page = pdfComponent.getCurrentPage();
			
			if (page != null)
			{
				int i = page.getPageIndex();
				t = "Page "+(i+1)+ " ("+i+"z)  /  " + page.getContainingFile().getNumberOfPages();
			}
			else
			{
				t = "No file loaded!";
			}
			
			t += "     ["+(isExtractTableMode() ? "<drag to extract table!!>  " : (isExtractTextMode() ? "<drag to extract text!!>  " : ""))+(getWheelMode() == WheelMode.Zoom ? "w=z" : (getWheelMode() == WheelMode.Y ? "w=y" : "w=x"))+(lockMode ? "" : " unlocked")+"]";
			
			
			if (showMouseCoordinatesInPageSpace)
			{
				//Show mouse coordinates! :D
				if (hasPDFLoaded())
				{
					IntPoint p = mouseTracker.getCurrentCursorPosition();
					Point2D currentMousePointInDisplaySpace = p == null ? null : intPointToFloat(p);
					
					if (currentMousePointInDisplaySpace != null)
					{
						Point2D currentMousePointInPageSpace = pdfComponent.transformPointFromDisplaySpaceToPageSpace(currentMousePointInDisplaySpace);
						t += "   ("+mouseCoorsFormat.format(currentMousePointInPageSpace.getX())+", "+mouseCoorsFormat.format(currentMousePointInPageSpace.getY())+")";
					}
				}
			}
			
			
			pageIndexLabel.setText(t);
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected TranslationAndUniformScaleTransform2D recalculateTransformIfLocked(TranslationAndUniformScaleTransform2D currentTransform)
	{
		if (hasPDFLoaded() && lockMode)
		{
			Rectangle2D pb = pdfComponent.getCurrentPage().getPageBoundaries();
			Component ac = pdfComponent.getAWTComponent();
			
			//Retranslate to keep in bounds!
			{
				Point2D newOriginAkaTranslation = getPossiblyTruncatedCanvasOriginAkaTranslationKeepingInBounds(pb, rect(0, 0, ac.getWidth(), ac.getHeight()), pdfComponent.getCurrentTransformPageSpaceToDisplaySpace(), 0.5, 0.5);
				return new TranslationAndUniformScaleTransform2D(newOriginAkaTranslation, currentTransform.getScale());
			}
		}
		
		return currentTransform;
	}
	
	
	protected static Point2D getPossiblyTruncatedCanvasOriginAkaTranslationKeepingInBounds(Rectangle2D canvasBoundsInOwnSpace, Rectangle2D viewportBoundsInOwnSpace, AffineTransform transformFromCanvasSpaceToViewportSpace, double xPositioningFactorIfCanvasIsTooSmall, double yPositioningFactorIfCanvasIsTooSmall)
	{
		Rectangle2D canvasBoundsInViewportSpace = transformAxisAlignedRectangleOPC(canvasBoundsInOwnSpace, transformFromCanvasSpaceToViewportSpace);
		
		double oldMinX = canvasBoundsInViewportSpace.getMinX();
		double oldMinY = canvasBoundsInViewportSpace.getMinY();
		
		double newMinX = oldMinX;
		double newMinY = oldMinY;
		
		if (canvasBoundsInViewportSpace.getWidth() < viewportBoundsInOwnSpace.getWidth())
		{
			newMinX = (viewportBoundsInOwnSpace.getWidth() - canvasBoundsInViewportSpace.getWidth()) * xPositioningFactorIfCanvasIsTooSmall;
		}
		else
		{
			if (canvasBoundsInViewportSpace.getMinX() > viewportBoundsInOwnSpace.getMinX())
				newMinX = viewportBoundsInOwnSpace.getMinX();
			else if (canvasBoundsInViewportSpace.getMaxX() < viewportBoundsInOwnSpace.getMaxX())
				//newMaxX = viewportBoundsInOwnSpace.getMaxX();
				//maxX - minX = width
				//-minX = width - maxX
				//minX = maxX - width
				newMinX = viewportBoundsInOwnSpace.getMaxX() - canvasBoundsInViewportSpace.getWidth();
		}
		
		
		if (canvasBoundsInViewportSpace.getHeight() < viewportBoundsInOwnSpace.getHeight())
		{
			newMinY = (viewportBoundsInOwnSpace.getHeight() - canvasBoundsInViewportSpace.getHeight()) * yPositioningFactorIfCanvasIsTooSmall;
		}
		else
		{
			if (canvasBoundsInViewportSpace.getMinY() > viewportBoundsInOwnSpace.getMinY())
				newMinY = viewportBoundsInOwnSpace.getMinY();
			else if (canvasBoundsInViewportSpace.getMaxY() < viewportBoundsInOwnSpace.getMaxY())
				//newMaxY = viewportBoundsInOwnSpace.getMaxY();
				//maxY - minY = height
				//-minY = height - maxY
				//minY = maxY - height
				newMinY = viewportBoundsInOwnSpace.getMaxY() - canvasBoundsInViewportSpace.getHeight();
		}
		
		
		return pointOrVector2D(newMinX, newMinY);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void clearAllInputActionsDueToPDFFileChanging()
	{
		cancelDrag();
	}
	
	
	
	
	protected double relativeZoomFactor = 1.5;
	
	
	protected SimpleButtonStateTracker keyTracker = new SimpleButtonStateTracker(0x10000,
	new ButtonInputReceiver()
	{
		@Override
		public void setButtonState(int keyCode, boolean newState)
		{
			boolean actionModifierDown = isActionModifierDown();
			
			
			if (newState == true)
			{
				if (keyCode == VK_N && actionModifierDown)
				{
					app.openNewDuplicateWindow(PDFMonkeyWindow.this);
				}
				
				//Navigation within the page ^www^
				else if (keyCode == VK_EQUALS)
				{
					if (hasPDFLoaded())
						pdfComponent.multiplyScaleButKeepCenterInCoincidence(relativeZoomFactor);
					cancelDrag();
				}
				else if (keyCode == VK_MINUS)
				{
					if (hasPDFLoaded())
						pdfComponent.multiplyScaleButKeepCenterInCoincidence(1/relativeZoomFactor);
					cancelDrag();
				}
				else if (keyCode == VK_A)
				{
					if (hasPDFLoaded())
						pdfComponent.setToFitToPage();
					cancelDrag();
				}
				else if (keyCode == VK_W)
				{
					if (hasPDFLoaded())
						pdfComponent.setToFitToWidthButKeepCenterYsInCoincidence();
					cancelDrag();
				}
				else if (keyCode == VK_H)
				{
					if (hasPDFLoaded())
						pdfComponent.setToFitToHeightButKeepCenterXsInCoincidence();
					cancelDrag();
				}
				
				
				else if (keyCode == VK_L)
				{
					lockMode = !lockMode;
					recalculateStatusBar();
					
					if (hasPDFLoaded() && lockMode)
						pdfComponent.rerunTransformFilter();
					
					cancelDrag();
				}
				
				
				
				
				
				
				
				
				
				
				//Changing pagesssss!! :DDD
				else if (keyCode == VK_J)
				{
					if (hasPDFLoaded())
					{
						boolean zeroBased = keyTracker.getButtonState(VK_ALT);
						
						Object response = JOptionPane.showInputDialog(null, "Enter the new page "+(zeroBased ? "index" : "number")+"! :D", "Jump to Page", JOptionPane.QUESTION_MESSAGE, null, null, String.valueOf(getCurrentPageIndex() + (zeroBased ? 0 : 1)));
						
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
								
								newPageIndex = SmallIntegerMathUtilities.progmod(newPageIndex, getCurrentPDFFile().getNumberOfPages());
								setCurrentPageByIndex(newPageIndex);
							}
						}
					}
				}
				else if (keyCode == VK_OPEN_BRACKET || keyCode == VK_PAGE_UP)
				{
					switchPageRelative(-1);
				}
				else if (keyCode == VK_CLOSE_BRACKET || keyCode == VK_PAGE_DOWN)
				{
					switchPageRelative(+1);
				}
				
				
				
				
				
				
				//THE ACTUAL MONKEY BUSINESS!!! XDDD
				else if (keyCode == VK_P)
				{
					if (hasPDFLoaded())
					{
						setClipboardText(Integer.toString(getCurrentPageIndex()+1));
					}
				}
				else if (keyCode == VK_F)
				{
					if (hasPDFLoaded())
					{
						File currentFile = getCurrentPDFFile().getLocalFileIfApplicable();
						
						if (currentFile != null)
						{
							setClipboardText(normpath(currentFile.getAbsoluteFile()).getPath());
						}
						else
						{
							setClipboardText("<unknown path!!>");
						}
					}
				}
				
				else if (keyCode == VK_X && actionModifierDown)
				{
					if (hasPDFLoaded())
					{
						try
						{
							setClipboardText(PDFMonkeyBusiness.extractEntirePageAsText(pdfComponent.getCurrentPage()));
							temporarilySetStatusBarText("Done!");
						}
						catch (Exception exc)
						{
							JOptionPane.showMessageDialog(null, "Error extracting text from page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				else if (keyCode == VK_T && actionModifierDown)
				{
					if (hasPDFLoaded())
					{
						try
						{
							setClipboardText(RCSV.serializeRCSV(PDFMonkeyBusiness.extractEntirePageAsTable(pdfComponent.getCurrentPage())));
							temporarilySetStatusBarText("Done!");
						}
						catch (Exception exc)
						{
							JOptionPane.showMessageDialog(null, "Error extracting table from page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
			
			
			
			//Both key state transitions bc why not? X3
			if (keyCode == VK_CONTROL || keyCode == VK_META || keyCode == VK_SHIFT || keyCode == VK_ALT || keyCode == VK_T || keyCode == VK_X)
			{
				recalculateStatusBar();
			}
			
			
			
			if (inDrag == DragType.OperationRegionExtractText && !isExtractTextMode())
				cancelDrag();
			else if (inDrag == DragType.OperationRegionExtractTable && !isExtractTableMode())
				cancelDrag();
		}
	});
	
	
	
	public static enum WheelMode
	{
		X,
		Y,
		Zoom,
	}
	
	public @Nonnull WheelMode getWheelMode()
	{
		return isActionModifierDown() ? WheelMode.Zoom : (isShiftModifierDown() ? WheelMode.X : WheelMode.Y);
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
	
	
	
	
	
	
	
	protected static enum DragType
	{
		Translation,
		OperationRegionExtractText,
		OperationRegionExtractTable,
	}
	
	protected DragType inDrag = null;        //..XDDD YUS
	protected IntPoint dragStart;
	protected Point2D contentTranslationAtDragStart;
	
	protected void startDrag(IntPoint pointInContentSpace)
	{
		if (hasPDFLoaded())
		{
			dragStart = SmallIntegerBasicGeometry2D.ipoint(pointInContentSpace.x, pointInContentSpace.y);
			
			if (isExtractTableMode())
			{
				inDrag = DragType.OperationRegionExtractTable;
			}
			else if (isExtractTextMode())
			{
				inDrag = DragType.OperationRegionExtractText;
			}
			else
			{
				inDrag = DragType.Translation;
				contentTranslationAtDragStart = pdfComponent.getCurrentPageTranslation();
			}
		}
	}
	
	protected void updateDrag(IntPoint pointInContentSpace)
	{
		if (hasPDFLoaded())
		{
			if (inDrag == DragType.Translation)
			{
				//newContentTranslation = pointNow - pointAtDragStart + originalValue;
				Point2D newContentTranslation = addVector(intPointToFloat(SmallIntegerBasicGeometry2D.subtractPoints(pointInContentSpace, dragStart)), contentTranslationAtDragStart);
				pdfComponent.setCurrentPageTranslation(newContentTranslation);
			}
			else
			{
				pdfComponent.getAWTComponent().repaint();
			}
		}
		else
		{
			cancelDrag();
		}
	}
	
	protected void releaseDragSuccessfully()
	{
		if (inDrag != null)
		{
			//Perform the drag operationnnnnnnnn!! \:DD/
			{
				if (hasPDFLoaded())
				{
					IntPoint now = mouseTracker.getCurrentCursorPosition();
					
					if (now != null)
					{
						if (inDrag == DragType.OperationRegionExtractText)
						{
							IntRectangle regionInDisplaySpace = SmallIntegerBasicGeometry2D.irectTwoPoints(dragStart, now);
							Rectangle2D regionInPageSpace = transformAxisAlignedRectangleOPC(intRectToFloat(regionInDisplaySpace), pdfComponent.getCurrentTransformDisplaySpaceToPageSpace());
							
							try
							{
								setClipboardText(PDFMonkeyBusiness.extractRegionOfPageAsText(pdfComponent.getCurrentPage(), regionInPageSpace));
								temporarilySetStatusBarText("Done extracting text!");
							}
							catch (Exception exc)
							{
								JOptionPane.showMessageDialog(null, "Error extracting text from region in page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
							}
						}
						else if (inDrag == DragType.OperationRegionExtractTable)
						{
							IntRectangle regionInDisplaySpace = SmallIntegerBasicGeometry2D.irectTwoPoints(dragStart, now);
							Rectangle2D regionInPageSpace = transformAxisAlignedRectangleOPC(intRectToFloat(regionInDisplaySpace), pdfComponent.getCurrentTransformDisplaySpaceToPageSpace());
							
							try
							{
								setClipboardText(RCSV.serializeRCSV(PDFMonkeyBusiness.extractRegionOfPageAsTable(pdfComponent.getCurrentPage(), regionInPageSpace)));
								temporarilySetStatusBarText("Done extracting table!");
							}
							catch (Exception exc)
							{
								JOptionPane.showMessageDialog(null, "Error extracting table from region in page!!: "+exc.getMessage(), "Error in extraction!!", JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				}
			}
			
			inDrag = null;
			dragStart = null;
			contentTranslationAtDragStart = null;
			
			if (hasPDFLoaded())
				pdfComponent.getAWTComponent().repaint();
		}
	}
	
	protected void cancelDrag()
	{
		if (inDrag != null)
		{
			inDrag = null;
			dragStart = null;
			contentTranslationAtDragStart = null;
			
			if (hasPDFLoaded())
				pdfComponent.getAWTComponent().repaint();
		}
	}
	
	
	
	protected SimpleButtonStateTracker mouseButtonTracker = new SimpleButtonStateTracker(3,
	new ButtonInputReceiver()
	{
		@Override
		public void setButtonState(int buttonIndex, boolean newState)
		{
			if (buttonIndex == 0 && mouseTracker.hasCursor())
			{
				if (newState)
				{
					startDrag(mouseTracker.getCurrentCursorPosition());
				}
				else
				{
					releaseDragSuccessfully();
				}
			}
		}
	});
	
	
	protected SimpleAbsoluteCursorPositionTracker mouseTracker = new SimpleAbsoluteCursorPositionTracker(new AbsolutePointingMotionInputReceiver()
	{
		@Override
		public void pointingLost()
		{
			cancelDrag();
		}
		
		@Override
		public void pointingAbsoluteMotion(int newX, int newY)
		{
			if (inDrag != null)
			{
				updateDrag(SmallIntegerBasicGeometry2D.ipoint(newX, newY));
			}
			
			if (showMouseCoordinatesInPageSpace)
				recalculateStatusBar();
		}
	});
	
	protected void realWheelDeal(double wheelRotationAmount)   //^wwwwwwwwwwwwwww^
	{
		if (mouseTracker.hasCursor())
		{
			if (hasPDFLoaded())
			{
				wheelRotationAmount = -wheelRotationAmount;
				
				WheelMode wheelMode = getWheelMode();
				
				if (wheelMode == WheelMode.Zoom)
					pdfComponent.setScaleButKeepPointInDisplaySpaceCoincident(pdfComponent.getCurrentScale() * Math.pow(relativeZoomFactor, wheelRotationAmount), intPointToFloat(mouseTracker.getCurrentCursorPosition()));
				else if (wheelMode == WheelMode.X)
					pdfComponent.setCurrentPageTranslationX(pdfComponent.getCurrentPageTranslation().getX() + wheelRotationAmount * wheelScrollModeFactor);
				else if (wheelMode == WheelMode.Y)
					pdfComponent.setCurrentPageTranslationY(pdfComponent.getCurrentPageTranslation().getY() + wheelRotationAmount * wheelScrollModeFactor);
				else
					throw newUnexpectedHardcodedEnumValueExceptionOrNullPointerException(wheelMode);
			}
		}
	}
	
	
	
	
	
	
	public void handleFilesDropped(Set<File> files)
	{
		if (!files.isEmpty())
		{
			//For now, just pick one at random ^^'
			
			openFile(getArbitraryElementThrowing(files));
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	//Input conversionnnnnnnnnn!! :DD
	
	@Override
	protected void processKeyEvent(KeyEvent e)
	{
		if (e.getID() == KeyEvent.KEY_PRESSED || e.getID() == KeyEvent.KEY_RELEASED)
		{
			keyTracker.setButtonState(e.getKeyCode(), e.getID() == KeyEvent.KEY_PRESSED);
			e.consume();
		}
		
		super.processKeyEvent(e);
	}
	
	@Override
	protected void processFocusEvent(FocusEvent e)
	{
		if (e.getID() == FocusEvent.FOCUS_LOST && e.getComponent() == this)
		{
			keyTracker.setAllButtonsToReleased();
			lostMouse();
		}
		
		super.processFocusEvent(e);
	}
	
	protected void lostMouse()
	{
		mouseButtonTracker.setAllButtonsToReleased();
		mouseTracker.pointingLost();
	}
	
	
	protected void ourProcessMouseyEvent(MouseEvent e)
	{
		if (e.getID() == MouseEvent.MOUSE_EXITED)
		{
			if (mouseButtonTracker.getStates().isEmpty())
				lostMouse();
		}
		else
		{
			IntPoint pointInWindowSpace = new IntPoint(e.getX(), e.getY());
			
			IntPoint pointInContentSpace = translatePointFromWindowSpaceIntoContentSpace(pointInWindowSpace);
			
			if (pointInContentSpace != null)
			{
				mouseTracker.pointingAbsoluteMotion(pointInContentSpace.x, pointInContentSpace.y);
				
				if (e.getID() == MouseEvent.MOUSE_PRESSED || e.getID() == MouseEvent.MOUSE_RELEASED)
				{
					mouseButtonTracker.setButtonState(e.getButton() - 1, e.getID() == MouseEvent.MOUSE_PRESSED);
				}
			}
			else
			{
				lostMouse();
			}
		}
		
		e.consume();
	}
	
	@Override
	protected void processMouseEvent(MouseEvent e)
	{
		ourProcessMouseyEvent(e);
		super.processMouseEvent(e);
	}
	
	@Override
	protected void processMouseMotionEvent(MouseEvent e)
	{
		ourProcessMouseyEvent(e);
		super.processMouseMotionEvent(e);
	}
	
	@Override
	protected void processMouseWheelEvent(MouseWheelEvent e)
	{
		IntPoint pointInWindowSpace = new IntPoint(e.getX(), e.getY());
		
		IntPoint pointInContentSpace = translatePointFromWindowSpaceIntoContentSpace(pointInWindowSpace);
		
		if (pointInContentSpace != null)
		{
			mouseTracker.pointingAbsoluteMotion(pointInContentSpace.x, pointInContentSpace.y);
			realWheelDeal(e.getWheelRotation());
		}
		else
		{
			lostMouse();
		}
		
		e.consume();
		
		
		super.processMouseWheelEvent(e);
	}
	
	
	
	
	@Nullable
	protected IntPoint translatePointFromWindowSpaceIntoContentSpace(@Nonnull IntPoint pointInWindowSpace)
	{
		Insets insets = this.getInsets();
		//int contentWidth = this.getWidth() - (insets.left + insets.right);
		//int contentHeight = this.getHeight() - (insets.top + insets.bottom);
		
		IntPoint pointInContentSpace = SmallIntegerBasicGeometry2D.ipoint(pointInWindowSpace.x - insets.left, pointInWindowSpace.y - insets.top);
		//return pointInContentSpace.x < 0 || pointInContentSpace.x >= contentWidth || pointInContentSpace.y < 0 || pointInContentSpace.y >= contentHeight ? null : pointInContentSpace;
		return pointInContentSpace;
	}
}
