package com.games.aotb;

import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class ProgressBar extends Rectangle
{
	private static final float FRAME_LINE_WIDTH = 5.0f;
	
	private final Line[] frameLines = new Line[4];
	
	private Rectangle progressRectangle = null;
	private float     maxValue          = 0.0f;
	private float     currentValue      = 0.0f;
	
	public ProgressBar clone(Enemy returnEnemy)
	{
		final ProgressBar returnProgressBar = new ProgressBar(getX(), getY(), getWidth(), getHeight(), maxValue, currentValue, getVertexBufferObjectManager());
		
		// Fix progressRectangle.
		returnProgressBar.progressRectangle.setColor(this.progressRectangle.getColor());
		
		// Fix frame lines.
		for (int i = 0; i < this.frameLines.length; i++)
		{
			returnProgressBar.frameLines[i].setColor(this.frameLines[i].getColor());
		}
		
		// Fix back color.
		returnProgressBar.setColor(this.getColor());
		
		// Attach it to the enemy.
		returnEnemy.attachChild(returnProgressBar);
		return returnProgressBar;
	}
	
	public float getMax()
	{
		return this.maxValue;
	}
	
	public float getProgress()
	{
		return this.currentValue;
	}
	
	public ProgressBar(final float x, final float y, final float width, final float height, final float maxValue, final float startValue, final VertexBufferObjectManager vbom)
	{
		super(x, y, width, height, vbom);
		this.progressRectangle = new Rectangle(0, 0, width, height, vbom);
		this.setMax(maxValue);
		this.setProgress(startValue);
		
		
		// Define top line.
		this.frameLines[0] = new Line(0, 0, 0 + width, 0, ProgressBar.FRAME_LINE_WIDTH, vbom);
		// Define right line.
		this.frameLines[1] = new Line(0 + width, 0, 0 + width, 0 + height, ProgressBar.FRAME_LINE_WIDTH, vbom);
		// Define bottom line.
		this.frameLines[2] = new Line(0 + width, 0 + height, 0, 0 + height, ProgressBar.FRAME_LINE_WIDTH, vbom);
		// Define left line.
		this.frameLines[3] = new Line(0, 0 + height, 0, 0, ProgressBar.FRAME_LINE_WIDTH, vbom);
		
		// Draw progress.
		this.attachChild(this.progressRectangle);
		
		// Draw lines.
		for (int i = 0; i < this.frameLines.length; i++)
		{
			this.attachChild(this.frameLines[i]); 
		}
	}
	
	public ProgressBar setBackColor(final float red, final float green, final float blue, final float alpha)
	{
		this.setColor(red, green, blue, alpha);
		return this;
	}
	
	public ProgressBar setFrameColor(final float red, final float green, final float blue, final float alpha)
	{
		for (int i = 0; i < this.frameLines.length; i++)
		{
			this.frameLines[i].setColor(red, green, blue, alpha);
		}
		
		return this;
	}
	
	public ProgressBar setMax(final float maxValue)
	{
		this.maxValue = maxValue;
		return this;
	}
	
	public ProgressBar setProgress(final float progress)
	{
		this.currentValue = progress;
		
		if (this.currentValue < 0)
		{
			this.currentValue = 0;
		}
		else if (this.currentValue > this.maxValue)
		{
			this.currentValue = this.maxValue;
		}
		
		this.progressRectangle.setWidth(this.getWidth() * progress / this.maxValue);
		return this;
	}
	
	public ProgressBar setProgressColor(final float red, final float green, final float blue, final float alpha)
	{
		this.progressRectangle.setColor(red, green, blue, alpha);
		return this;
	}
}