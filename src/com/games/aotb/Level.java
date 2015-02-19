
package com.games.aotb;

public class Level
{
	private String	mapFileName	= null;
	private Point[]	goalPoint	= null;
	private Point[]	startPoint	= null;
	private int[]	wave		= null;

	public Level(final int[] waves, final Point[] start, final Point[] goal, final String mapFileName)
	{
		this.setMapFileName(mapFileName);
		this.setGoalPoint(goal);
		this.setStartPoint(start);
		this.setWave(waves);
	}

	public Point[] getGoalPoint()
	{
		return this.goalPoint;
	}

	public String getMapFileName()
	{
		return this.mapFileName;
	}

	public Point[] getStartPoint()
	{
		return this.startPoint;
	}

	public int[] getWave()
	{
		return this.wave;
	}

	public void setGoalPoint(final Point[] goalPoint)
	{
		this.goalPoint = goalPoint;
	}

	public void setMapFileName(final String mapFileName)
	{
		this.mapFileName = mapFileName;
	}

	public void setStartPoint(final Point[] startPoint)
	{
		this.startPoint = startPoint;
	}

	public void setWave(final int[] wave)
	{
		this.wave = wave;
	}
}