package rebound.hci.graphics2d.gui.awt.components;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import rebound.hci.graphics2d.java2d.BasicPaintable;

/**
 * Call {@link #realPaint(Graphics)} in your {@link BasicPaintable} if you want the underlying thing to be painted :3
 */
public class AwtComponentPaintInterceptingDecorator
extends AwtComponentDecorator
{
	private static final long serialVersionUID = 1L;
	
	
	protected BasicPaintable paintable;
	
	
	public AwtComponentPaintInterceptingDecorator()
	{
		super();
	}
	
	public AwtComponentPaintInterceptingDecorator(Component underlyingComponent, BasicPaintable paintable)
	{
		super(underlyingComponent);
		setPaintableFireless(paintable);
	}
	
	
	public BasicPaintable getPaintable()
	{
		return paintable;
	}
	
	public void setPaintableFireless(BasicPaintable paintable)
	{
		this.paintable = paintable;
	}
	
	public void setPaintable(BasicPaintable paintable)
	{
		if (this.paintable != paintable)
		{
			this.paintable = paintable;
			this.repaint();
		}
	}
	
	
	
	@Override
	public void paint(Graphics g)
	{
		Graphics2D g2d = (Graphics2D) g;
		
		BasicPaintable p = getPaintable();
		if (p != null)
			p.paint(g2d);
	}
	
	public void realPaint(Graphics g)
	{
		super.paint(g);
	}
}
