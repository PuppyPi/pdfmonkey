package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies;

import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.geom.Rectangle2D;
import javax.annotation.Nullable;
import rebound.annotations.semantic.reachability.gc.StrongReferenceIsImportantHere;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.AbstractBreakthroughRenderer;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

public class BreakthroughRenderer_SingleLevelCachingWholeImage
extends AbstractBreakthroughRenderer
{
	protected @StrongReferenceIsImportantHere double lastRenderingScale;
	protected @StrongReferenceIsImportantHere @Nullable ImageAndActualRegion lastRendering;
	
	
	public BreakthroughRenderer_SingleLevelCachingWholeImage(ReRenderableImage renderer)
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
			ImageAndActualRegion c = renderer.render(0, 0, roundCeilS32(renderer.getWidthInNaturalSpace() * scale), roundCeilS32(renderer.getHeightInNaturalSpace() * scale), scale);
			
			lastRendering = c;
			lastRenderingScale = scale;
			
			return c;
		}
	}
}
