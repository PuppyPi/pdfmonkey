package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering;

import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_NaiveUncachingClipOnly;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_NaiveUncachingWholeImage;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_SingleLevelCachingWholeImage;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_SingleLevelCachingWholeImageHardless;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_ZoomLevelCachingWholeImage;
import rebound.hci.graphics2d.gui.recomponent.breakthroughrendering.cachingstrategies.BreakthroughRenderer_ZoomLevelCachingWholeImageHardless;

//TODO BreakthroughRenderer_ZoomLevelCachingTiledForClipOnly !!

public class BreakthroughRenderingUtilities
{
	public static final BreakthroughRenderingAdapterSpi AdapterNaiveWholeImage = BreakthroughRenderer_NaiveUncachingWholeImage::new;
	public static final BreakthroughRenderingAdapterSpi AdapterNaiveClipOnly = BreakthroughRenderer_NaiveUncachingClipOnly::new;
	public static final BreakthroughRenderingAdapterSpi AdapterSingleLevelCachingWholeImage = BreakthroughRenderer_SingleLevelCachingWholeImage::new;
	public static final BreakthroughRenderingAdapterSpi AdapterSingleLevelCachingWholeImageHardless = BreakthroughRenderer_SingleLevelCachingWholeImageHardless::new;
	public static final BreakthroughRenderingAdapterSpi AdapterZoomLevelCachingWholeImage = BreakthroughRenderer_ZoomLevelCachingWholeImage::new;
	public static final BreakthroughRenderingAdapterSpi AdapterZoomLevelCachingWholeImageHardless = BreakthroughRenderer_ZoomLevelCachingWholeImageHardless::new;
	//Todo TileCaching (so when they zoom in, the whole image isn't re-rendered X'D )
	
	
	public static final BreakthroughRenderingAdapterSpi AdapterDefault = AdapterSingleLevelCachingWholeImageHardless;  //Hardcache-less so that if the components are invisible or rarely seen they can unload *every* (perhaps quite costly! XD ) image resource :3      (Better to be slow than cause OutOfMemoryError's in the default, right?! XD )
}
