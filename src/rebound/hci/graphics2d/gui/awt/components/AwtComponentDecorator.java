package rebound.hci.graphics2d.gui.awt.components;

import static java.util.Objects.*;
import java.awt.Component;
import java.awt.Container;
import javax.annotation.Nullable;

public class AwtComponentDecorator
extends Container
{
	private static final long serialVersionUID = 1L;
	
	
	protected @Nullable Component underlyingComponent;
	
	public AwtComponentDecorator()
	{
	}
	
	public AwtComponentDecorator(@Nullable Component underlyingComponent)
	{
		setUnderlyingComponent(underlyingComponent);
	}
	
	
	
	
	@Override
	public void setSize(int width, int height)
	{
		super.setSize(width, height);
		
		Component u = underlyingComponent;
		if (u != null)
			u.setSize(width, height);
	}
	
	
	
	
	
	
	public @Nullable Component getUnderlyingComponent()
	{
		return underlyingComponent;
	}
	
	
	public void setUnderlyingComponent(@Nullable Component neu)
	{
		requireNonNull(neu);
		
		Component old = this.underlyingComponent;
		
		if (neu != old)
		{
			if (old != null)
			{
				this.remove(old);
			}
			
			if (neu != null)
			{
				this.add(neu);
				neu.setSize(this.getWidth(), this.getHeight());
			}
			
			this.underlyingComponent = neu;
		}
	}
}
