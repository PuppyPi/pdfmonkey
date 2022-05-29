package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering;

import static java.util.Objects.*;
import static rebound.hci.graphics2d.ImageUtilities.*;
import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nonnull;
import rebound.exceptions.ReadonlyUnsupportedOperationException;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;
import rebound.hci.graphics2d.gui.recomponent.components.AbstractReComponent;

/**
 * This exposes an "immutable" raster image-based {@link ReComponent}, but which internally recomputes and caches the image as needed for various zoom-levels :3
 *  (Eg, if it's rendering something complicated and expensive like complex vector graphics or a scientific scalar field data plot and wants to render it once to an image instead of re-rendering it every frame XD )
 */
public abstract class AbstractBreakthroughRenderer
extends AbstractReComponent
{
	protected final ReRenderableImage renderer;
	
	
	public AbstractBreakthroughRenderer(@Nonnull ReRenderableImage renderer)
	{
		this.renderer = requireNonNull(renderer);
	}
	
	
	
	@Override
	public void paint(Graphics2D g)
	{
		double scale;
		{
			AffineTransform t = g.getTransform();
			double sx = t.getScaleX();
			double sy = t.getScaleY();
			scale = sx == sy ? sx : (sx + sy) / 2;  //todo: average it before or after reciprocation?
		}
		
		
		Rectangle2D clip;
		{
			Shape c = g.getClip();
			clip = c instanceof Rectangle2D ? (Rectangle2D)c : c.getBounds2D();
		}
		
		//System.out.println("Original Clip: "+clip+", Scale = "+scale);  //debug
		
		if (clip.getWidth() > 0 && clip.getHeight() > 0)
		{
			ImageAndActualRegion r = getImage(clip, scale);
			Image i = r.getImage();
			
			int x = roundClosestArbtiesS32(r.getActualXInDisplaySpace());
			int y = roundClosestArbtiesS32(r.getActualYInDisplaySpace());
			int w = roundClosestArbtiesS32(getWidthImmediately(i) / scale);
			int h = roundClosestArbtiesS32(getHeightImmediately(i) / scale);
			
			//System.out.println(" → "+x+" "+y+" "+w+" "+h);  //debug
			//System.out.println();  //debug
			
			g.drawImage(i, x, y, w, h, null);
		}
	}
	
	
	
	protected abstract ImageAndActualRegion getImage(Rectangle2D clip, double scale);
	
	
	
	
	
	
	
	
	
	@Override
	public Integer getWidth()
	{
		return renderer.getWidthInNaturalSpace();
	}
	
	@Override
	public Integer getHeight()
	{
		return renderer.getHeightInNaturalSpace();
	}
	
	
	
	@Override
	public void setSize(Integer width, Integer height) throws UnsupportedOperationException
	{
		throw new ReadonlyUnsupportedOperationException();
	}
}
