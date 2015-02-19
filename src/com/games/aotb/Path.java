
package com.games.aotb;

import java.util.ArrayList;
import java.util.Iterator;

import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.util.algorithm.path.ICostFunction;
import org.andengine.util.algorithm.path.IPathFinderMap;
import org.andengine.util.algorithm.path.astar.IAStarHeuristic;
import org.andengine.util.algorithm.path.astar.NullHeuristic;

public class Path implements Cloneable
{
	private Point									end				= null;
	private Enemy									enemy			= null;
	private Level									level			= null;
	private TMXLayer								tmxlayer		= null;
	private Iterator<Point>							iterator		= null;
	private ArrayList<Point>						xyPath			= null;
	private ArrayList<Point>						waypoints		= null;
	private org.andengine.util.algorithm.path.Path	rcPath			= null;

	private static final double						TOLERANCE		= 0.000001;

	protected ICostFunction<Enemy>					costCallback	= new ICostFunction<Enemy>()
																	{
																		@Override
																		public float getCost(
																			final IPathFinderMap<Enemy> pathFinderMap,
			final int fromX,
			final int fromY,
			final int toX,
			final int toY,
			final Enemy entity)
																		{
																			return 1.0f;
																		}
																	};

		protected IAStarHeuristic<Enemy>				heuristic		= new NullHeuristic<Enemy>();

		protected IPathFinderMap<Enemy>					pathMap			= new IPathFinderMap<Enemy>()
																	{
																		@Override
																		public boolean isBlocked(final int x, final int y, final Enemy entity)
																		{
																			try
																			{
																				TMXTile tmxTile =
																					TowerMain.tmxLayer.getTMXTileAt(
																						TowerMain.getXFromCol(x),
							TowerMain.getYFromRow(y));
																				TMXProperties<TMXTileProperty> tmxTileProperties =
																					TowerMain.tmxTiledMap.getTMXTileProperties(tmxTile.getGlobalTileID());
																				
																				if (tmxTileProperties.containsTMXProperty("Collidable", "False"))
																				{
																					return true;
																				}
																				else
																				{
																					return false;
																				}
																			}
																			catch (final Exception e)
																			{
																				for (int i = 0; i < Path.this.level.getGoalPoint().length; i++)
																				{
																					if ((x == Path.this.level.getGoalPoint()[i].getX_int())
																						&& (y == Path.this.level.getGoalPoint()[i].getY_int()))
																					{
																						return false;
																					}
																				}
																				
																				for (int i = 0; i < Path.this.level.getStartPoint().length; i++)
																				{
																					if ((x == Path.this.level.getStartPoint()[i].getX_int())
																						&& (y == Path.this.level.getStartPoint()[i].getY_int()))
																					{
																						return false;
																					}
																				}
																				
																				return true;
																			}
																		}
																	};

			public Path(final Enemy enemy, final Point end, final TMXLayer tmxlayer, final Level level)
			{
				this.end = end;
				this.level = level;
				this.enemy = enemy;
				this.tmxlayer = tmxlayer;
				this.waypoints = new ArrayList<Point>();
				this.iterator = this.waypoints.iterator();

				if (this.findPath())
				{
					this.convertRCPathToXY();
					this.optimizeXYPath();
				}
			}

			public Path(
		final Enemy enemy,
				final Point end,
				final TMXLayer tmxlayer,
				final Level level,
				final ArrayList<Point> xyPath,
				final org.andengine.util.algorithm.path.Path rcPath)
			{
				this.enemy = enemy;
				this.waypoints = new ArrayList<Point>();
				this.iterator = this.waypoints.iterator();
				this.end = end;
				this.tmxlayer = tmxlayer;
				this.level = level;
				this.xyPath = xyPath;
				this.rcPath = rcPath;
			}

			public void add(final int index, final Point waypoint)
			{
				this.waypoints.add(index, waypoint);
			}

			public void add(final Point waypoint)
			{
				this.waypoints.add(waypoint);
			}

			public boolean checkRemainingPath(final int x, final int y)
			{
				if (this.rcPath != null)
				{
					final int length = this.getRCPath().getLength();
					final int[] xArray = new int[length];
					final int[] yArray = new int[length];

					for (int i = 0; i < length; i++)
					{
						xArray[i] = this.getRCPath().getX(i);
						yArray[i] = this.getRCPath().getY(i);
					}
			
			final int enemyCol = TowerMain.getColFromX(this.enemy.getX());
					final int enemyRow = TowerMain.getRowFromY(this.enemy.getY());

					for (int i = (length - 1); i >= 0; i--)
					{
						if ((xArray[i] == x) && (yArray[i] == y))
						{
							return true;
						}
						else
					if ((xArray[i] == enemyCol) && (yArray[i] == enemyRow))
					{
						return false;
					}
					}
				}
				else
				{
					return false;
				}

				return false;
			}

			public Path clone(final Enemy newEnemy)
			{
				if (this.rcPath == null)
				{
					return null;
				}
				else
				{
					org.andengine.util.algorithm.path.Path tempRCPath = new org.andengine.util.algorithm.path.Path(this.getRCPath().getLength());

					for (int i = 0; i < this.getRCPath().getLength(); i++)
					{
						tempRCPath.set(i, this.getRCPath().getX(i), this.getRCPath().getY(i));
					}

					ArrayList<Point> tempXYPath = new ArrayList<Point>();

					for (int i = 0; i < this.getXYPath().size(); i++)
					{
						tempXYPath.add(new Point(this.getXYPath().get(i).getX_float(), this.getXYPath().get(i).getY_float()));
					}

					return new Path(newEnemy, this.end, this.tmxlayer, this.level, tempXYPath, tempRCPath);
				}
			}

			protected void convertRCPathToXY()
			{
				this.xyPath = new ArrayList<Point>();

				for (int i = 0; i < this.getRCPath().getLength(); i++)
				{
					this.getXYPath().add(new Point(TowerMain.getXFromCol(this.getRCPath().getX(i)), TowerMain.getYFromRow(this.getRCPath().getY(i))));
				}

				this.getXYPath().add(0, new Point(this.enemy.getX(), this.enemy.getY()));
			}

			protected boolean findPath()
			{
				try
				{
					this.rcPath = TowerMain.finder.findPath(this.pathMap, TowerMain.colMin, TowerMain.rowMin, TowerMain.colMax, TowerMain.rowMax, this.enemy,
				this.enemy.getCol(), this.enemy.getRow(), this.end.getX_int(), this.end.getY_int(), TowerMain.allowDiagonal,
				this.heuristic, this.costCallback);

					if (this.rcPath == null)
					{
						return false;
					}
					else
					{
						return true;
					}
				}
				catch (final NullPointerException e)
				{
					this.rcPath = null;
					return false;
				}
			}

			public Point get(final int index)
			{
				return this.waypoints.get(index);
			}

			public double getCurrentAngle()
			{
				double m;
				double b;

				if (this.getXYPath().size() > 1)
				{
					for (int i = 0; i < (this.getXYPath().size() - 1); i++)
					{
						if (((this.getXYPath().get(i).getY_float() <= this.enemy.getY()) && (this.enemy.getY() <= this.getXYPath().get(i + 1).getY_float()))
					|| ((this.getXYPath().get(i + 1).getY_float() <= this.enemy.getY()) && (this.enemy.getY() <= this.getXYPath().get(i).getY_float())))
						{
							if (((this.getXYPath().get(i).getX_float() <= this.enemy.getX()) && (this.enemy.getX() <= this.getXYPath().get(i + 1).getX_float()))
						|| ((this.getXYPath().get(i + 1).getX_float() <= this.enemy.getX()) && (this.enemy.getX() <= this.getXYPath().get(i).getX_float())))
							{
								if ((this.getXYPath().get(i + 1).getX_float() - this.getXYPath().get(i).getX_float()) == 0)
								{
									if (Math.abs(this.enemy.getX() - this.getXYPath().get(i).getX_float()) < Path.TOLERANCE)
									{
										return Math.atan2((this.getXYPath().get(i + 1).getY_float() - this.getXYPath().get(i).getY_float()),
									(this.getXYPath().get(i + 1).getX_float() - this.getXYPath().get(i).getX_float()));
									}
								}
								else
								{
									m =
								(this.getXYPath().get(i + 1).getY_float() - this.getXYPath().get(i).getY_float())
									/ (this.getXYPath().get(i + 1).getX_float() - this.getXYPath().get(i).getX_float());
									b = this.getXYPath().get(i).getY_float() - (m * this.getXYPath().get(i).getX_float());

									if (Math.abs(((m * this.enemy.getX()) + b) - this.enemy.getY()) < Path.TOLERANCE)
									{
										return Math.atan2((this.getXYPath().get(i + 1).getY_float() - this.getXYPath().get(i).getY_float()),
									(this.getXYPath().get(i + 1).getX_float() - this.getXYPath().get(i).getX_float()));
									}
								}
							}
						}
					}
				}

				return 0;
			}

			public org.andengine.entity.modifier.PathModifier.Path getEntityPath()
			{
				if (this.xyPath == null)
				{
					return null;
				}
				else
				{
					org.andengine.entity.modifier.PathModifier.Path tempXYPath = new org.andengine.entity.modifier.PathModifier.Path(this.getXYPath().size());

					for (int i = 0; i < this.getXYPath().size(); i++)
					{
						tempXYPath = tempXYPath.to(this.getXYPath().get(i).getX_float(), this.getXYPath().get(i).getY_float());
					}

					return tempXYPath;
				}
			}

			public Iterator<Point> getIterator()
			{
				return this.iterator;
			}

			public Point getLast()
			{
				return this.waypoints.get(this.waypoints.size() - 1);
			}

			public Point getNext()
			{
				return this.getIterator().next();
			}

			public org.andengine.util.algorithm.path.Path getRCPath()
			{
				return this.rcPath;
			}

			public ArrayList<Point> getXYPath()
			{
				return this.xyPath;
			}

			public boolean hasNext()
			{
				return this.getIterator().hasNext();
			}

			protected void optimizeXYPath()
			{
				double currentAngle;
				double newAngle;
				double newAngleInverse;

				if (this.getXYPath().size() > 2)
				{
					currentAngle = Math.atan2((this.getXYPath().get(1).getY_float() - this.getXYPath().get(0).getY_float()),
				(this.getXYPath().get(1).getX_float() - this.getXYPath().get(0).getX_float()));

					for (int i = 1; i < (this.getXYPath().size() - 1); i++)
					{
						newAngle = Math.atan2((this.getXYPath().get(i + 1).getY_float() - this.getXYPath().get(i).getY_float()),
					(this.getXYPath().get(i + 1).getX_float() - this.getXYPath().get(i).getX_float()));

						newAngleInverse = ((newAngle > 0) ? (newAngle - Math.PI) : (newAngle + Math.PI));

						if ((Math.abs(newAngle - currentAngle) < Path.TOLERANCE) || (Math.abs(newAngleInverse - currentAngle) < Path.TOLERANCE))
						{
							this.getXYPath().remove(i);
							i--;
						}
						else
						{
							currentAngle = newAngle;
						}
					}
				}
			}

			public void remove(final int index)
			{
				this.waypoints.remove(index);
			}

			public void remove(final Point waypoint)
			{
				this.waypoints.remove(waypoint);
			}
}