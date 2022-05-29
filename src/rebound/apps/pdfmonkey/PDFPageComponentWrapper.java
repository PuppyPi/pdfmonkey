package rebound.apps.pdfmonkey;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import rebound.annotations.semantic.temporal.ConstantReturnValue;
import rebound.hci.graphics2d.gui.recomponent.ReComponent;

public interface PDFPageComponentWrapper
{
	@ConstantReturnValue
	public ReComponent getComponent();
	
	public Point2D transformFromComponentSpaceToPageSpace(Point2D inComponentSpace);
	public Point2D transformFromPageSpaceToComponentSpace(Point2D inPageSpace);
	
	public Rectangle2D transformRectangleFromComponentSpaceToPageSpace(Rectangle2D inComponentSpace);
	public Rectangle2D transformRectangleFromPageSpaceToComponentSpace(Rectangle2D inPageSpace);
}
