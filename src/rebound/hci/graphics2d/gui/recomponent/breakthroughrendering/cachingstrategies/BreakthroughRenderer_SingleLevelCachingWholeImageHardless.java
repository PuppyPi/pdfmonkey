package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies;

import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import javax.annotation.Nullable;
import rebound.annotations.semantic.reachability.gc.StrongReferenceIsImportantHere;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.AbstractBreakthroughRenderer;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

public class BreakthroughRenderer_SingleLevelCachingWholeImageHardless
extends AbstractBreakthroughRenderer
{
	protected @StrongReferenceIsImportantHere double lastRenderingScale;
	protected @StrongReferenceIsImportantHere @Nullable SoftReference<ImageAndActualRegion> lastRendering;
	
	
	public BreakthroughRenderer_SingleLevelCachingWholeImageHardless(ReRenderableImage renderer)
	{
		super(renderer);
	}
	
	
	
	@Override
	protected ImageAndActualRegion getImage(Rectangle2D clip, double scale)
	{
		if (lastRendering != null && scale == lastRenderingScale)
		{
			ImageAndActualRegion r = lastRendering.get();
			
			if (r != null)
				return r;
		}
		
		//else
		{
			ImageAndActualRegion c = renderer.render(0, 0, roundCeilS32(renderer.getWidthInNaturalSpace() * scale), roundCeilS32(renderer.getHeightInNaturalSpace() * scale), scale);
			
			lastRendering = new SoftReference<>(c);
			lastRenderingScale = scale;
			
			return c;
		}
	}
}
