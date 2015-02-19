
package com.games.aotb;

public class Point
{
	private Double	x	= 0.0;
	private Double	y	= 0.0;

	public Point(final double x, final double y)
	{
		this.setCoords_double(x, y);
	}

	public Point(final float x, final float y)
	{
		this.setCoords_float(x, y);
	}

	public Point(final int x, final int y)
	{
		this.setCoords_int(x, y);
	}

	public double getX_double()
	{
		return this.x.floatValue();
	}

	public float getX_float()
	{
		return this.x.floatValue();
	}

	public int getX_int()
	{
		return this.x.intValue();
	}

	public double getY_double()
	{
		return this.y.floatValue();
	}

	public float getY_float()
	{
		return this.y.floatValue();
	}

	public int getY_int()
	{
		return this.y.intValue();
	}

	public void setCoords_double(final double x, final double y)
	{
		this.x = x;
		this.y = y;
	}

	public void setCoords_float(final float x, final float y)
	{
		this.x = (double)x;
		this.y = (double)y;
	}

	public void setCoords_int(final int x, final int y)
	{
		this.x = (double)x;
		this.y = (double)y;
	}
}
