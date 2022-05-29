package rebound.apps.pdfmonkey;

import static rebound.math.geom2d.GeometryUtilities2D.*;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.JPanel;
import rebound.dataformats.pdf.BasicPDFPageRenderable.BasicPDFPageRenderableRaster;
import rebound.math.geom2d.AffineTransformUtilities;
import rebound.math.geom2d.TranslationAndUniformScaleTransform2D;
import rebound.util.functional.FunctionInterfaces.UnaryFunction;

/**
 * A Swing-based panel that displays a PDF page image.
 * + Modified by RP from com.sun.pdfview.viewer.PagePanel to be simpler for integration with other code! ^_^
 * + Also to only display an update if it's complete--that way the user never has to worry if a portion of it is genuinely empty or just not yet rendered!  (idk, personal preference; no worries :3 )
 */
public class BasicPDFAwtComponentWrapperStandardImplementation
extends JPanel
implements BasicPDFAwtComponentWrapper
{
	private static final long serialVersionUID = 1L;
	
	
	
	
	
	
	
	
	protected @Nullable BasicPDFPageRenderableRaster currentPageNT;
	
	/** The image of the rendered PDF page being displayed */
	protected Image currentImage;
	
	protected boolean hasLastSize = false;
	protected int lastWidth, lastHeight;
	
	protected boolean dirty = true;
	
	protected double x, y, scale;
	
	protected AwtPaintable overlay;
	protected UnaryFunction<TranslationAndUniformScaleTransform2D, TranslationAndUniformScaleTransform2D> transformFilter;
	
	
	public BasicPDFAwtComponentWrapperStandardImplementation()
	{
		this.x = 0;
		this.y = 0;
		this.scale = 1;
		
		this.addComponentListener(new ComponentListener()
		{
			@Override
			public void componentShown(ComponentEvent e)
			{
			}
			
			@Override
			public void componentResized(ComponentEvent e)
			{
				runTransformFilterIfGiven();
				markDirty();
			}
			
			@Override
			public void componentMoved(ComponentEvent e)
			{
			}
			
			@Override
			public void componentHidden(ComponentEvent e)
			{
			}
		});
	}
	
	
	
	
	
	@Override
	public Component getAWTComponent()
	{
		return this;
	}
	
	@Nullable
	public BasicPDFPageRenderableRaster getCurrentPage()
	{
		return currentPageNT;
	}
	
	public void setCurrentPage(BasicPDFPageRenderableRaster page)
	{
		if (page != currentPageNT)
		{
			this.currentPageNT = page;
			markDirtyAndFireUpdated();
		}
	}
	
	
	
	
	
	
	@Override
	public Point2D getCurrentPageTranslation()
	{
		return pointOrVector2D(x, y);
	}
	
	@Override
	public double getCurrentScale()
	{
		return scale;
	}
	
	@Override
	public void setCurrentPageTranslation(double deltaX, double deltaY)
	{
		if (deltaX != this.x || deltaY != this.y)
		{
			this.x = deltaX;
			this.y = deltaY;
			runTransformFilterIfGiven();
			markDirtyAndFireUpdated();
		}
	}
	
	@Override
	public void setCurrentScale(double scaleFactor)
	{
		if (scaleFactor != this.scale)
		{
			this.scale = scaleFactor;
			runTransformFilterIfGiven();
			markDirtyAndFireUpdated();
		}
	}
	
	@Override
	public void setCurrentPageTranslationAndScale(double deltaX, double deltaY, double scaleFactor)
	{
		if (deltaX != this.x || deltaY != this.y || scaleFactor != this.scale)
		{
			this.x = deltaX;
			this.y = deltaY;
			this.scale = scaleFactor;
			runTransformFilterIfGiven();
			markDirtyAndFireUpdated();
		}
	}
	
	
	@Override
	public UnaryFunction<TranslationAndUniformScaleTransform2D, TranslationAndUniformScaleTransform2D> getTransformFilter()
	{
		return transformFilter;
	}
	
	@Override
	public void setTransformFilter(UnaryFunction<TranslationAndUniformScaleTransform2D, TranslationAndUniformScaleTransform2D> transformFilter)
	{
		if (transformFilter != this.transformFilter)
		{
			this.transformFilter = transformFilter;
			runTransformFilterIfGiven();
			markDirtyAndFireUpdated();
		}
	}
	
	@Override
	public void rerunTransformFilter()
	{
		runTransformFilterIfGiven();
		markDirtyAndFireUpdated();
	}
	
	
	protected void runTransformFilterIfGiven()
	{
		if (transformFilter != null)
		{
			TranslationAndUniformScaleTransform2D newTransform = transformFilter.f(getCurrentConstrainedTransform());
			
			this.x = newTransform.getX();
			this.y = newTransform.getY();
			this.scale = newTransform.getScale();
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	protected void markDirty()
	{
		dirty = true;
		currentImage = null;
	}
	
	protected void markDirtyAndFireUpdated()
	{
		markDirty();
		repaint();
	}
	
	protected boolean checkDirtyAndMarkClean()
	{
		boolean old = dirty;
		if (dirty)
			dirty = false;
		return old;
	}
	
	
	
	protected void rerenderPage()
	{
		if (currentPageNT == null)
		{
			// no page
			currentImage = null;
		}
		else
		{
			//PDFPage currentImplPage = currentOurPage.getImplPage();
			
			
			Dimension sz = getSize();
			if ((sz.width | sz.height) == 0)  //sz.width == 0 || sz.height == 0
			{
				// no image to draw.
				return;
			}
			
			
			
			runTransformFilterIfGiven();
			
			
			// Calculate the transform *we* need to use (for switching between left and right-handed coordinates X'D )
			double s = getCurrentScale();
			AffineTransform transform = AffineTransformUtilities.newScaleAndTranslateTransform(1/s, -x/s, y/s + getCurrentPage().getPageBoundaries().getHeight() - getAWTComponent().getHeight()/s);
			
			//Equivalent ^wwww^
			//AffineTransform transform = new AffineTransform();
			//transform.translate(0, getCurrentPage().getPageBoundaries().getHeight());
			//transform.scale(1, -1);
			//transform.concatenate(getCurrentTransformDisplaySpaceToPageSpace());
			
			
			
			// calculate the clipping rectangle in page space from the component bounds in screen space and the current transform parameters! :D
			
			Rectangle2D clipInPageSpace = transform.createTransformedShape(new Rectangle2D.Double(0, 0, this.getWidth(), this.getHeight())).getBounds2D();
			
			Dimension pageSize = getUnstretchedSize(sz.width, sz.height, clipInPageSpace);
			
			// get the new image
			currentImage = currentPageNT.getRenderedImage(pageSize.width, pageSize.height, clipInPageSpace);
			
			hasLastSize = true;
			lastWidth = sz.width;
			lastHeight = sz.height;
		}
	}
	
	protected Dimension getUnstretchedSize(int width, int height, @Nonnull Rectangle2D clip)
	{
		final boolean swapDimensions = doesRotationSwapDimensions();
		final double srcHeight = swapDimensions ? clip.getWidth() : clip.getHeight();
		final double srcWidth = swapDimensions ? clip.getHeight() : clip.getWidth();
		double ratio = srcHeight / srcWidth;
		double askratio = (double) height / (double) width;
		if (askratio > ratio)
		{
			// asked for something too high
			height = (int) (width * ratio + 0.5);
		}
		else
		{
			// asked for something too wide
			width = (int) (height / ratio + 0.5);
		}
		
		return new Dimension(width, height);
	}
	
	protected boolean doesRotationSwapDimensions()
	{
		//return getRotation() == 90 || getRotation() == 270;
		return false;  //Todo support rotation XD
	}
	
	
	
	
	
	
	
	
	
	public void paint(Graphics g)
	{
		Dimension sz = getSize();
		
		//If our size as an AWT Component changed since last time, re-render the image!
		//Todo: only do this if necessary ^^"
		if (!hasLastSize || sz.width != lastWidth || sz.height != lastHeight)
		{
			markDirty();
		}
		
		if (checkDirtyAndMarkClean())
		{
			rerenderPage();
		}
		
		
		g.setColor(getBackground());
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if (currentImage != null)
		{
			int imageWidth = currentImage.getWidth(null);
			int imageHeight = currentImage.getHeight(null);
			
			if ((imageWidth == sz.width && imageHeight <= sz.height) || (imageHeight == sz.height && imageWidth <= sz.width))
			{
				g.drawImage(currentImage, 0, 0, this);
			}
		}
		
		
		if (overlay != null)
		{
			overlay.paint((Graphics2D) g);
		}
	}
	
	
	
	@Override
	public AwtPaintable getOverlay()
	{
		return overlay;
	}
	
	@Override
	public void setOverlay(AwtPaintable overlay)
	{
		this.overlay = overlay;
		markDirtyAndFireUpdated();
	}
}
