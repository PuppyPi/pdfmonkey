package rebound.hci.graphics2d.gui.recomponent.breakthroughrendering;

import java.awt.image.renderable.RenderableImage;
import rebound.annotations.semantic.simpledata.Finite;
import rebound.annotations.semantic.simpledata.Positive;
import rebound.annotations.semantic.temporal.ConstantReturnValue;

/**
 * The image defines a "natural coordinate space" which is what would be seen if rendering it to the screen at a scale factor of 1
 *  (todo: using physical screen units instead of pixels would be the truly correct way so using a different monitor would never change the actual sizes of things, but Java2D doesn't expose that information or operate along those lines unfortunately)
 * 
 * 
 * (Like {@link RenderableImage} except simpler and allows rendering arbitrary regions as well as scale factors, and doesn't allow aspect-ratio-altering scaling)
 */
public interface ReRenderableImage
{
	/**
	 * + Note that the way the parameters are specified, it's impossible for the image provided to be rendered stretched nonuniformly between x and y with its aspect ratio altered; that is, only scaling is possible, not stretching.
	 * 
	 * + The returned image may have a different offset/size than requested because it may desire to render more than requested or less (and eg, if it renders some pixels at a lower x/y, it needs a way of conveying that the image should be translated when displayed!)
	 * 
	 * @param xInDisplaySpace a float so that the full nonintegerness of the Natural Coordinate Space can be utilized! :D
	 * @param yInDisplaySpace a float so that the full nonintegerness of the Natural Coordinate Space can be utilized! :D
	 * @param widthInDisplaySpace an integer so that the exact size of the returned image can be displayed :>
	 * @param heightInDisplaySpace an integer so that the exact size of the returned image can be displayed :>
	 * @param scale Display/Natural, so scale > 1 means the display space rectangle's width is a bigger number than the natural space rectangle's width :>
	 * @return an image with a size preferably of widthInDisplaySpace x heightInDisplaySpace
	 */
	public ImageAndActualRegion render(double xInDisplaySpace, double yInDisplaySpace, int widthInDisplaySpace, int heightInDisplaySpace, @Positive @Finite double scale);
	
	
	
	@ConstantReturnValue
	@Positive
	public int getWidthInNaturalSpace();
	
	@ConstantReturnValue
	@Positive
	public int getHeightInNaturalSpace();
}
