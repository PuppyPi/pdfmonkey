package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies;

import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.WeakHashMap;
import javax.annotation.Nullable;
import rebound.annotations.semantic.reachability.gc.StrongReferenceIsImportantHere;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.AbstractBreakthroughRenderer;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

public class BreakthroughRenderer_ZoomLevelCachingWholeImage
extends AbstractBreakthroughRenderer
{
	protected Map<Double, ImageAndActualRegion> renderingCache = new WeakHashMap<>();
	protected @StrongReferenceIsImportantHere double lastRenderingScale;
	protected @StrongReferenceIsImportantHere @Nullable ImageAndActualRegion lastRendering;
	
	
	
	
	
	public BreakthroughRenderer_ZoomLevelCachingWholeImage(ReRenderableImage renderer)
	{
		super(renderer);
	}
	
	
	
	
	
	@Override
	protected ImageAndActualRegion getImage(Rectangle2D clip, double scale)
	{
		if (lastRendering != null && scale == lastRenderingScale)
		{
			return lastRendering;
		}
		else
		{
			ImageAndActualRegion c = renderingCache.get(scale);
			
			if (c == null)
			{
				c = renderer.render(0, 0, roundCeilS32(renderer.getWidthInNaturalSpace() * scale), roundCeilS32(renderer.getHeightInNaturalSpace() * scale), scale);
				renderingCache.put(scale, c);
			}
			
			lastRendering = c;
			lastRenderingScale = scale;
			
			return c;
		}
	}
}
