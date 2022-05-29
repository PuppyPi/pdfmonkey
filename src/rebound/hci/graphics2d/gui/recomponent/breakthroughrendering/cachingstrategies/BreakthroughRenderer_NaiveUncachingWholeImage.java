package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies;

import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.geom.Rectangle2D;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.AbstractBreakthroughRenderer;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

public class BreakthroughRenderer_NaiveUncachingWholeImage
extends AbstractBreakthroughRenderer
{
	public BreakthroughRenderer_NaiveUncachingWholeImage(ReRenderableImage renderer)
	{
		super(renderer);
	}
	
	
	@Override
	protected ImageAndActualRegion getImage(Rectangle2D clip, double scale)
	{
		return renderer.render(0, 0, roundCeilS32(renderer.getWidthInNaturalSpace() * scale), roundCeilS32(renderer.getHeightInNaturalSpace() * scale), scale);
	}
}
