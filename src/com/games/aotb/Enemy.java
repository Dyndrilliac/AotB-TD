package com.games.aotb;

import java.util.ArrayList;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.PathModifier;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.IModifier.IModifierListener;

public class Enemy extends Sprite implements Cloneable
{
	public static final int    LOOT       = 25;
	public static final int    MAX_HEALTH = 10000;
	public static final float  SPEED      = 40.0f; // Distance to move per update.
	public static final String TEXTURE    = "enemy.png";
	public static final int    WIDTH      = 74; // In pixels.
	public static final int    HEIGHT     = 74; // In pixels.
	
	private static final double TOLERANCE = 0.000001;
	
	private static ArrayList<Enemy> enemyArray;
	private static Level            level;
	
	private PathModifier trajectory    = null;
	private ProgressBar  healthBar     = null;
	private Path         path          = null;
	private boolean      isAlive       = true;
	private float        health        = Enemy.MAX_HEALTH;
	private float        inboundDamage = 0;
	
	@Override
	public Enemy clone()
	{
		final Enemy returnEnemy = new Enemy(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getTextureRegion(), this.getVertexBufferObjectManager());
		returnEnemy.setPath(this.getPath().clone(returnEnemy));
		returnEnemy.setHealthBar(this.getHealthBar().clone(returnEnemy));
		return returnEnemy;
	}
	
	public void createPath(final Point end, final BaseGameActivity myContext, final TMXLayer tmxlayer, final ArrayList<Enemy> enemyArray)
	{
		this.setPath(new Path(this, end, tmxlayer, Enemy.level));
	}
	
	public Enemy(final float x, final float y, final float width, final float height, 
				 final ITextureRegion iTextureRegion, final VertexBufferObjectManager vbom, 
				 final Level level, final ArrayList<Enemy> enemyArray)
	{
		super(x, y, width, height, iTextureRegion, vbom);
		Enemy.setLevel(level);
		Enemy.setEnemyArray(enemyArray);
		this.setHealthBar(new ProgressBar(0, 0, 100, 10, Enemy.MAX_HEALTH, Enemy.MAX_HEALTH, vbom));
		this.getHealthBar().setProgressColor(1.0f, 0.0f, 0.0f, 1.0f).setFrameColor(0.4f, 0.4f, 0.4f, 1.0f).setBackColor(0.0f, 0.0f, 0.0f, 0.2f);
	}
	
	public Enemy(final float x, final float y, final float width, final float height, 
				 final ITextureRegion iTextureRegion, final VertexBufferObjectManager vbom)
	{
		super(x, y, width, height, iTextureRegion, vbom);
	}
	
	public int getCol()
	{
		return TowerMain.getColFromX(this.getX());
	}
	
	public int getCurrentLink()
	{
		// First find out which two points the Enemy is between.
		double myError;
		// Default to the last link.
		int curLink = this.getPath().getXYPath().size() - 1;
		
		if (this.getPath().getXYPath().size() > 2)
		{
			for(int i = 1; i<this.getPath().getXYPath().size(); i++)
			{
				if (((this.getPath().getXYPath().get(i-1).getY_float() <= this.getY()) && (this.getY() <= this.getPath().getXYPath().get(i).getY_float())) || ((this.getPath().getXYPath().get(i).getY_float() <= this.getY()) && (this.getY() <= this.getPath().getXYPath().get(i-1).getY_float())))
				{
					if (((this.getPath().getXYPath().get(i-1).getX_float() <= this.getX()) && (this.getX() <= this.getPath().getXYPath().get(i).getX_float())) || ((this.getPath().getXYPath().get(i).getX_float() <= this.getX()) && (this.getX() <= this.getPath().getXYPath().get(i-1).getX_float())))
					{
						if ((Math.abs(this.getPath().getXYPath().get(i).getX_float() - this.getPath().getXYPath().get(i-1).getX_float())) < Enemy.TOLERANCE)
						{
							if (Math.abs(this.getX() - this.getPath().getXYPath().get(i-1).getX_float()) < Enemy.TOLERANCE)
							{
								curLink = i;
								i = this.getPath().getXYPath().size()-1;
							}
						}
						else
						{
							double m = (this.getPath().getXYPath().get(i).getY_float() - this.getPath().getXYPath().get(i-1).getY_float()) / (this.getPath().getXYPath().get(i).getX_float() - this.getPath().getXYPath().get(i-1).getX_float());
							// b = y-mx
							double b = this.getPath().getXYPath().get(i-1).getY_float() - (m * this.getPath().getXYPath().get(i-1).getX_float());
							
							// Check to see if our point is on the line.
							myError = (m*this.getX()+b) - this.getY();
							
							// Should be zero if our point is on the line.
							if (Math.abs(myError) < Enemy.TOLERANCE)
							{
								curLink = i;
								i = (this.getPath().getXYPath().size() - 1);
							}				
						}
					}
				}
			}
			
			return curLink;
		}
		else
		{
			return -1;
		}
	}
	
	public static ArrayList<Enemy> getEnemyArray()
	{
		return Enemy.enemyArray;
	}
	
	public float getHealth()
	{
		return this.health;
	}
	
	public ProgressBar getHealthBar()
	{
		return this.healthBar;
	}
	
	public float getInboundDamage()
	{
		return this.inboundDamage;
	}
	
	public static Level getLevel()
	{
		return Enemy.level;
	}
	
	public float getMidX()
	{
		return (this.getX() + (this.getWidth() / 2));
	}
	
	public float getMidY()
	{
		return (this.getY() + (this.getHeight() / 2));
	}
	
	public Path getPath()
	{
		return this.path;
	}
	
	public int getRow()
	{
		return TowerMain.getRowFromY(this.getY());
	}
	
	public PathModifier getTrajectory()
	{
		return this.trajectory;
	}
	
	public boolean isAlive()
	{
		return this.isAlive;
	}
	
	public void setAlive(final boolean isAlive)
	{
		this.isAlive = isAlive;
	}
	
	public static void setEnemyArray(final ArrayList<Enemy> enemyArray)
	{
		Enemy.enemyArray = enemyArray;
	}
	
	protected void setHealth(final float health)
	{
		this.health = health;
	}
	
	public void setHealthBar(final ProgressBar healthBar)
	{
		this.healthBar = healthBar;
	}
	
	public void setInboundDamage(final float inboundDamage)
	{
		this.inboundDamage = inboundDamage;
	}
	
	public static void setLevel(final Level level)
	{
		Enemy.level = level;
	}
	
	public void setPath(final Path path)
	{
		this.path = path;
	}
	
	public void setTrajectory(final PathModifier trajectory)
	{
		this.trajectory = trajectory;
	}
	
	public void stop()
	{
		unregisterEntityModifier(this.getTrajectory());
	}
	
	public float takeDamage(final float totalDamage, final String type)
	{
		// TODO: Distinguish Between Damage Types.
		this.setHealth(this.getHealth() - totalDamage);
		this.getHealthBar().setProgress(this.getHealth());
		return this.getHealth();
	}
	
	public void startMoving(final BaseGameActivity myContext)
	{
		if (this.getPath() != null)
		{
			// Convert our path type to their path type.
			final org.andengine.entity.modifier.PathModifier.Path tempPath = this.getPath().getEntityPath();

			if (tempPath != null)
			{
				// Find the total length of the path.
				final float distance = tempPath.getLength();

				// d = r*t
				// t = d/r
				this.setTrajectory(new PathModifier(distance / Enemy.SPEED, tempPath));
				this.getTrajectory().addModifierListener(new IModifierListener<IEntity>()
				{
					@Override
					public void onModifierStarted(final IModifier<IEntity> modifier, final IEntity item)
					{
						// TODO
					}

					@Override
					public void onModifierFinished(final IModifier<IEntity> modifier, final IEntity item)
					{
						myContext.getEngine().runOnUpdateThread(new Runnable()
						{
							@Override
							public void run()
							{
								Enemy.this.detachSelf();
								Enemy.enemyArray.remove(Enemy.this);
								
								// Subtract a life.
								TowerMain.subtractLives(1);
							}
						});
					}
				});
				
				registerEntityModifier(this.getTrajectory());
			}
		}
	}
}