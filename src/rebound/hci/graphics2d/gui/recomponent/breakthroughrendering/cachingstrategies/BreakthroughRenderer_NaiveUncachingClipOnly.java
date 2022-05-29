package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies;

import static rebound.math.SmallFloatMathUtilities.*;
import java.awt.geom.Rectangle2D;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.AbstractBreakthroughRenderer;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ImageAndActualRegion;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.ReRenderableImage;

public class BreakthroughRenderer_NaiveUncachingClipOnly
extends AbstractBreakthroughRenderer
{
	public BreakthroughRenderer_NaiveUncachingClipOnly(ReRenderableImage renderer)
	{
		super(renderer);
	}
	
	
	@Override
	protected ImageAndActualRegion getImage(Rectangle2D clip, double scale)
	{
		ImageAndActualRegion r = renderer.render(clip.getX() * scale, clip.getY() * scale, roundCeilS32(clip.getWidth() * scale), roundCeilS32(clip.getHeight() * scale), scale);
		
		return new ImageAndActualRegion(r.getImage(), r.getActualXInDisplaySpace() / scale, r.getActualYInDisplaySpace() / scale);
	}
}
