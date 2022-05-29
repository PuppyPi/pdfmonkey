package rebound.hci.graphics2d.gui.recomponent.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import rebound.annotations.semantic.allowedoperations.WritableValue;

//Todo make this breakthrough-rendered too so it's not pixelly on zooming in XD

public class SimpleFuzzyBorder
extends AbstractBorderReComponent
{
	protected int pageBorderThicknessInPixels = 3;
	protected Color innermostColor = new Color(189, 189, 189);
	
	
	public void paintBorder(Graphics2D g)
	{
		//Paint the page borderrrrr!! :DD
		
		//todo account for rounding better ^^'
		
		
		final int alphaValueInnermost = 255;
		final int alphaValueOutermost = 0;
		
		final int cr = innermostColor.getRed();
		final int cg = innermostColor.getGreen();
		final int cb = innermostColor.getBlue();
		
		
		@WritableValue Rectangle b = new Rectangle(0, 0, this.getWidth(), this.getHeight());
		
		for (int i = 0; i < pageBorderThicknessInPixels; i++)
		{
			int alpha = (alphaValueInnermost - alphaValueOutermost) * i / (pageBorderThicknessInPixels - 1) + alphaValueOutermost;
			g.setColor(new Color(cr, cg, cb, alpha));
			
			g.drawRect(b.x, b.y, b.width - 1, b.height - 1);
			
			b.x++;
			b.y++;
			b.width -= 2;
			b.height -= 2;
		}
	}
	
	
	
	@Override
	public Insets getInsets()
	{
		return new Insets(
		pageBorderThicknessInPixels,
		pageBorderThicknessInPixels,
		pageBorderThicknessInPixels,
		pageBorderThicknessInPixels
		);
	}
	
	
	
	
	
	
	public int getPageBorderThicknessInPixels()
	{
		return pageBorderThicknessInPixels;
	}
	
	public void setPageBorderThicknessInPixels(int pageBorderThicknessInPixels)
	{
		this.pageBorderThicknessInPixels = pageBorderThicknessInPixels;
	}
	
	public Color getInnermostColor()
	{
		return innermostColor;
	}
	
	public void setInnermostColor(Color innermostColor)
	{
		this.innermostColor = innermostColor;
	}
}
