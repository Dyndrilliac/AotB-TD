
package com.games.aotb;

import java.io.IOException;
import java.util.ArrayList;

import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.audio.sound.SoundManager;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.debug.Debug;

import com.badlogic.gdx.math.Vector2;

public class Tower extends Sprite
{
	protected static class TowerRunnable implements Runnable
	{
		private final Scene				scene;
		private final Tower				tower;
		private final ArrayList<Tower>	towerArray;

		public TowerRunnable(final Scene scene, final Tower tower, final ArrayList<Tower> towerArray)
		{
			this.scene = scene;
			this.tower = tower;
			this.towerArray = towerArray;
		}

		@Override
		public void run()
		{
			try
			{
				this.scene.unregisterTouchArea(this.tower);
				this.tower.detachSelf();
				this.towerArray.remove(this.tower);
			}
			catch (final Exception e)
			{
				Debug.e(e);
			}
		}
	}

	public static Scene getScene()
	{
		return Tower.scene;
	}
	
	public static int getTotalTowers()
	{
		return Tower.totalTowers;
	}
	
	public static ArrayList<Tower> getTowerArray()
	{
		return Tower.towerArray;
	}
	
	public static void loadSound(final SoundManager soundmanager, final SimpleBaseGameActivity activity)
	{
		try
		{
			Tower.firing_sound = SoundFactory.createSoundFromAsset(soundmanager, activity, Tower.FIRING_SOUND);
		}
		catch (final IOException e)
		{
			Debug.e(e);
		}
	}
	
	public static void setLastCheckedX(final float x)
	{
		Tower.lastCheckedX = x;
	}
	
	public static void setLastCheckedY(final float y)
	{
		Tower.lastCheckedY = y;
	}
	
	private static void setScene(final Scene scene)
	{
		Tower.scene = scene;
	}

	protected static void setTotalTowers(final int numberOfTowers)
	{
		Tower.totalTowers = numberOfTowers;
	}
	
	protected static void setTowerArray(final ArrayList<Tower> towerArray)
	{
		Tower.towerArray = towerArray;
	}
	
	private static final int		Z_INDEX				= 1000;
	public static final String		FIRING_SOUND		= "tower.ogg";

	public static final String		TEXTURE				= "tower.png";
	public static final int			COOLDOWN			= 500;												// In milliseconds! Controls rate of fire.
	public static final int			DAMAGE				= 200;
	public static final int			WIDTH				= 100;												// In pixels.
	public static final int			HEIGHT				= 100;												// In pixels.
	private String					dmgType				= "Piercing";										// Identifies type of damage dealt by this tower.
																											
	private float					cdMod				= 1.0f;											// Modifies the cooldown for this tower by a
	// percentage. Allows you to upgrade basic towers.
	private float					dmgMod				= 1.0f;											// Modifies the damage for this tower. Allows you to
	// upgrade basic towers.
	private int						cost				= 50;												// Cost of this tower in credits.
	private static ArrayList<Tower>	towerArray			= null;
	private static Sound			firing_sound		= null;
	private static Scene			scene				= null;
	private static float			lastCheckedX		= 0;

	private static float			lastCheckedY		= 0;
	private static int				totalTowers			= 0;												// Total number of towers.
	private TextureRegion			bulletTexture		= null;
	private TextureRegion			hitAreaTextureGood	= null;
	private TextureRegion			hitAreaTextureBad	= null;
	private TowerRange				towerRangeGood		= null;
	private TowerRange				towerRangeBad		= null;

	private Projectile				bulletSprite		= null;

	private ArrayList<Projectile>	bulletsArray		= null;

	private boolean					moveable			= true;

	private boolean					placeError			= false;

	private boolean					hitAreaShown		= false;

	private boolean					hitAreaGoodShown	= false;

	private boolean					hitAreaBadShown		= false;

	private final float				totalCooldown		= (Tower.COOLDOWN * this.getCooldownModifier());

	private long					lastFire			= 0;

	public Tower(final TextureRegion bulletTexture, final float x, final float y, final float width, final float height, final TextureRegion textureRegion,
		final TextureRegion hitAreaTextureGood, final TextureRegion hitAreaTextureBad, final Scene sene, final ArrayList<Tower> towerArray,
		final VertexBufferObjectManager vbom)
	{
		super(x, y, width, height, textureRegion, vbom);
		Tower.setScene(Tower.scene);
		this.bulletTexture = bulletTexture;
		this.bulletsArray = new ArrayList<Projectile>();
		this.hitAreaTextureBad = hitAreaTextureBad;
		this.hitAreaTextureGood = hitAreaTextureGood;
		this.towerRangeGood = new TowerRange(0, 0, this.hitAreaTextureGood, this.getVertexBufferObjectManager());
		this.towerRangeBad = new TowerRange(0, 0, this.hitAreaTextureBad, this.getVertexBufferObjectManager());
		this.towerRangeGood.setPosition((this.getWidth() / 2) - (this.towerRangeGood.getWidth() / 2), (this.getHeight() / 2)
			- (this.towerRangeGood.getHeight() / 2));
		this.towerRangeBad.setPosition((this.getWidth() / 2) - (this.towerRangeBad.getWidth() / 2), (this.getHeight() / 2)
			- (this.towerRangeBad.getHeight() / 2));
		this.setZIndex(Tower.Z_INDEX);
		Tower.setTowerArray(towerArray);
		Tower.setTotalTowers(Tower.getTotalTowers() + 1);
	}

	public boolean canPlace(final boolean assignPaths, final BaseGameActivity myContext, final Tower tw)
	{
		float newX = tw.getX();
		float newY = tw.getY();
		final TMXTile tmxTile = TowerMain.tmxLayer.getTMXTileAt(newX, newY);

		if (tmxTile != null)
		{
			int backupTileID = tmxTile.getGlobalTileID();
			tmxTile.setGlobalTileID(TowerMain.tmxTiledMap, TowerMain.TILEID_BLOCKED);
			boolean towerNotAllowed = false;
			Path[] tempPaths = new Path[Enemy.getEnemyArray().size() + TowerMain.enemyClone.length];
			boolean[] needsNewPath = new boolean[Enemy.getEnemyArray().size() + TowerMain.enemyClone.length];

			if (assignPaths)
			{
				for (int i = 0; i < Enemy.getEnemyArray().size(); i++)
				{
					Enemy enemy = Enemy.getEnemyArray().get(i);

					if (enemy.getPath() != null)
					{
						if (enemy.getPath().checkRemainingPath(TowerMain.getColFromX(newX), TowerMain.getRowFromY(newY)))
						{
							tempPaths[i] = new Path(enemy, TowerMain.currentLevel.getGoalPoint()[0], TowerMain.tmxLayer, TowerMain.currentLevel);

							if (tempPaths[i].getRCPath() == null)
							{
								towerNotAllowed = true;
								break;
							}

							needsNewPath[i] = true;
						}
						else
						{
							needsNewPath[i] = false;
						}
					}
				}
			}

			if (!towerNotAllowed)
			{
				for (int i = 0; i < TowerMain.enemyClone.length; i++)
				{
					if (TowerMain.enemyClone[i].getPath() == null)
					{
						towerNotAllowed = true;
					}
					else
					{
						if (TowerMain.enemyClone[i].getPath().getRCPath().contains(TowerMain.getColFromX(newX), TowerMain.getColFromX(newY)))
						{
							tempPaths[Enemy.getEnemyArray().size() + i] =
								new Path(TowerMain.enemyClone[i], TowerMain.currentLevel.getGoalPoint()[0], TowerMain.tmxLayer, TowerMain.currentLevel);

							if (tempPaths[Enemy.getEnemyArray().size() + i].getRCPath() == null)
							{
								towerNotAllowed = true;
							}

							needsNewPath[Enemy.getEnemyArray().size() + i] = true;
						}
					}
				}
			}

			if (!assignPaths)
			{
				tmxTile.setGlobalTileID(TowerMain.tmxTiledMap, backupTileID);
				return !towerNotAllowed;
			}
			else
			{
				if (towerNotAllowed)
				{
					tw.remove(false, myContext);
					tmxTile.setGlobalTileID(TowerMain.tmxTiledMap, backupTileID);
					return false;
				}
				else
				{
					for (int i = 0; i < Enemy.getEnemyArray().size(); i++)
					{
						if (needsNewPath[i])
						{
							Enemy enemy = Enemy.getEnemyArray().get(i);
							enemy.setPath(tempPaths[i]);
							enemy.stop();
							enemy.startMoving(myContext);
						}
					}

					for (int i = 0; i < TowerMain.enemyClone.length; i++)
					{
						if (needsNewPath[Enemy.getEnemyArray().size() + i])
						{
							TowerMain.enemyClone[i].setPath(tempPaths[Enemy.getEnemyArray().size() + i]);
						}
					}

					return true;
				}
			}
		}
		else
		{
			// The tile is null because the tower is inside the black void, so return false.
			return false;
		}
	}

	public void checkClearSpotAndPlace(final Scene scene, final float newX, final float newY, final BaseGameActivity myContext)
	{
		try
		{
			float tempX = newX;
			float tempY = newY;
			final TMXTile tmxTile = TowerMain.tmxLayer.getTMXTileAt(tempX + this.getXHandleOffset(), tempY + this.getYHandleOffset());

			if (tmxTile == null)
			{
				this.setTowerPlaceError(scene, true);
				this.setPosition(tempX, tempY);
			}
			else
			{
				if (TowerMain.enableSnap)
				{
					tempX = tmxTile.getTileX();
					tempY = tmxTile.getTileY();
				}

				this.setPosition(tempX, tempY);
				final TMXProperties<TMXTileProperty> tmxTileProperties = TowerMain.tmxTiledMap.getTMXTileProperties(tmxTile.getGlobalTileID());

				if (tmxTileProperties.containsTMXProperty("Collidable", "False"))
				{
					this.setTowerPlaceError(scene, true);
				}
				else
				{
					if ((tempX != Tower.lastCheckedX) || (tempY != Tower.lastCheckedY))
					{
						if (this.canPlace(false, myContext, this))
						{
							this.setTowerPlaceError(scene, false);
						}
						else
						{
							this.setTowerPlaceError(scene, true);
						}
					}
				}
			}

			Tower.setLastCheckedX(tempX);
			Tower.setLastCheckedY(tempY);
		}
		catch (Exception e)
		{
			Debug.e(e);
		}
	}

	public double distanceTo(final Enemy enemy)
	{
		return Math.sqrt(Math.pow(this.getMidX() - enemy.getMidX(), 2) + Math.pow(this.getMidY() - enemy.getMidY(), 2));
	}

	public void fire(final Enemy enemy, final Scene scene, final ArrayList<Enemy> enemyArray, final BaseGameActivity myContext)
	{
		if (!TowerMain.paused)
		{
			try
			{
				if ((enemy.getHealth() - enemy.getInboundDamage()) > 0)
				{
					this.fire(enemy, this, scene, enemyArray, myContext);
				}
			}
			catch (final Exception e)
			{
				Debug.e(e);
			}
		}
	}

	public boolean fire(final Enemy target, final Tower source, final Scene scene, final ArrayList<Enemy> enemyArray, final BaseGameActivity myContext)
	{
		long elapsed = System.currentTimeMillis() - this.lastFire;
		
		if ((elapsed > (long)this.totalCooldown) && (!this.moveable))
		{
			this.bulletSprite = new Projectile(source.getMidX(), source.getMidY(), 10f, 10f, this.bulletTexture, this.getVertexBufferObjectManager(), scene);
			this.bulletSprite.setTarget(this, target);
			this.bulletSprite.shoot(enemyArray, myContext);
			this.bulletsArray.add(this.bulletSprite);
			this.lastFire = System.currentTimeMillis();
			Tower.firing_sound.play();
			Sprite myBullet = this.getBulletSprite();
			scene.attachChild(myBullet);
			return true;
		}
		else
		{
			return false;
		}
	}

	public ArrayList<Projectile> getBulletsArray()
	{
		return this.bulletsArray;
	}

	public Sprite getBulletSprite()
	{
		return this.bulletSprite;
	}

	public int getCol()
	{
		return TowerMain.getColFromX(this.getX());
	}

	public float getCooldownModifier()
	{
		return this.cdMod;
	}

	public int getCost()
	{
		return this.cost;
	}

	public float getDamageModifier()
	{
		return this.dmgMod;
	}

	public String getDamageType()
	{
		return this.dmgType;
	}

	public boolean getHitAreaShown()
	{
		return this.hitAreaShown;
	}

	public float getMidX()
	{
		return (this.getX() + (this.getWidth() / 2));
	}

	public float getMidY()
	{
		return (this.getY() + (this.getHeight() / 2));
	}

	public Vector2 getPosition()
	{
		return new Vector2(this.getX(), this.getY());
	}

	public int getRow()
	{
		return TowerMain.getRowFromY(this.getY());
	}

	public float getXHandleOffset()
	{
		return (this.getWidth() / 2);
	}

	public float getYHandleOffset()
	{
		return (this.getHeight() / 2);
	}

	public boolean hasPlaceError()
	{
		return this.placeError;
	}

	public boolean isMoveable()
	{
		return this.moveable;
	}

	public float maxRange()
	{
		return (this.towerRangeGood.getHeight() / 2.0f);
	}

	public void remove(final boolean resetTile, final BaseGameActivity myContext)
	{
		this.remove(this, resetTile, myContext);
	}

	public void remove(final Tower tower, final boolean resetTile, final BaseGameActivity myContext)
	{
		myContext.getEngine().runOnUpdateThread(new TowerRunnable(Tower.scene, tower, Tower.towerArray));

		Tower.setTotalTowers(Tower.getTotalTowers() - 1);

		if (resetTile)
		{
			try
			{
				TowerMain.tmxLayer.getTMXTileAt(tower.getX(), tower.getY()).setGlobalTileID(TowerMain.tmxTiledMap, TowerMain.TILEID_CLEAR);
				Path path;

				for (Enemy enemy: Enemy.getEnemyArray())
				{
					path = new Path(enemy, TowerMain.currentLevel.getGoalPoint()[0], TowerMain.tmxLayer, TowerMain.currentLevel);
					enemy.setPath(path);
					enemy.stop();
					enemy.startMoving(myContext);
				}

				for (Enemy enemy: TowerMain.enemyClone)
				{
					path = new Path(enemy, TowerMain.currentLevel.getGoalPoint()[0], TowerMain.tmxLayer, TowerMain.currentLevel);
					enemy.setPath(path);
				}
			}
			catch (final NullPointerException e)
			{
				// In the black void, no need to reset the tile.
			}
		}
	}
	
	public void removeBullet(final Projectile bullet)
	{
		this.bulletsArray.remove(bullet);
	}

	public void setCooldownModifier(final int cdMod)
	{
		this.cdMod = cdMod;
	}

	public void setCost(final int cost)
	{
		this.cost = cost;
	}

	public void setDamageModifier(final int dmgMod)
	{
		this.dmgMod = dmgMod;
	}

	public void setDamageType(final String dmgType)
	{
		this.dmgType = dmgType;
	}

	public void setHitAreaShown(final Scene scene, final boolean showHitArea)
	{
		if (this.moveable)
		{
			this.towerRangeGood.setPosition(
				(this.getWidth() / 2) - (this.towerRangeGood.getWidth() / 2),
				(this.getHeight() / 2) - (this.towerRangeGood.getHeight() / 2));
			this.towerRangeBad.setPosition(
				(this.getWidth() / 2) - (this.towerRangeBad.getWidth() / 2),
				(this.getHeight() / 2) - (this.towerRangeBad.getHeight() / 2));
		}
		else
		{
			this.towerRangeGood.setPosition(
				(this.getX() + (this.getWidth() / 2)) - (this.towerRangeGood.getWidth() / 2),
				(this.getY() + (this.getHeight() / 2)) - (this.towerRangeGood.getHeight() / 2));
			this.towerRangeBad.setPosition((this.getX() + (this.getWidth() / 2)) - (this.towerRangeBad.getWidth() / 2), (this.getY() + (this.getHeight() / 2))
				- (this.towerRangeBad.getHeight() / 2));
		}
		
		if (showHitArea)
		{
			if (this.placeError)
			{
				if (!this.hitAreaBadShown)
				{
					if (this.isMoveable())
					{
						this.attachChild(this.towerRangeBad);
					}
					else
					{
						scene.attachChild(this.towerRangeBad);
					}

					this.hitAreaBadShown = true;
				}

				if (this.hitAreaGoodShown)
				{
					if (this.isMoveable())
					{
						this.detachChild(this.towerRangeGood);
					}
					else
					{
						scene.detachChild(this.towerRangeGood);
					}

					this.hitAreaGoodShown = false;
				}
			}
			else
			{
				if (this.hitAreaBadShown)
				{
					if (this.isMoveable())
					{
						this.detachChild(this.towerRangeBad);
					}
					else
					{
						scene.detachChild(this.towerRangeBad);
					}

					this.hitAreaBadShown = false;
				}

				if (!this.hitAreaGoodShown)
				{
					if (this.isMoveable())
					{
						this.attachChild(this.towerRangeGood);
					}
					else
					{
						scene.attachChild(this.towerRangeGood);
					}

					this.hitAreaGoodShown = true;
				}
			}
		}
		else
		{
			if (this.hitAreaGoodShown)
			{
				if (this.isMoveable())
				{
					this.detachChild(this.towerRangeGood);
				}
				else
				{
					scene.detachChild(this.towerRangeGood);
				}

				this.hitAreaGoodShown = false;
			}

			if (this.hitAreaBadShown)
			{
				if (this.isMoveable())
				{
					this.detachChild(this.towerRangeBad);
				}
				else
				{
					scene.detachChild(this.towerRangeBad);
				}

				this.hitAreaBadShown = false;
			}
		}

		this.hitAreaShown = showHitArea;
	}

	public void setMoveable(final boolean moveable)
	{
		this.moveable = moveable;
	}

	public void setTowerPlaceError(final Scene scene, final boolean towerPlaceError)
	{
		this.placeError = towerPlaceError;

		if (this.hitAreaShown)
		{
			this.setHitAreaShown(scene, this.hitAreaShown);
		}
	}
}
