
package com.games.aotb;

import java.util.ArrayList;

import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.MoveByModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.IModifier.IModifierListener;

public class Projectile extends Sprite
{
	protected static class TrajectoryReturn
	{
		public double	t;
		public double	x;
		public double	y;
		public double	enemyAngle;
	}
	
	public static final String	TEXTURE	= "bullet.png";

	public static final float	SPEED	= 100.0f;		// Distance to move per update.
														
	private final Scene			scene;
	private Enemy				target;
	private Tower				source;
	private MoveByModifier		trajectory;
	private float				totalDamage;

	public Projectile(
		final float x,
		final float y,
		final float width,
		final float height,
		final TextureRegion textureRegion,
		final VertexBufferObjectManager vbom,
		final Scene scene)
	{
		super(x, y, width, height, textureRegion, vbom);
		this.scene = scene;
	}

	protected TrajectoryReturn findTrajectory(final int curLink, final double prevDist)
	{
		// Higher Y means down, so the angles here are upside down.
		TrajectoryReturn trajectoryReturn = new TrajectoryReturn();
		trajectoryReturn.enemyAngle =
			Math.atan2(
				this.target.getPath().getXYPath().get(curLink).getY_float() - this.target.getPath().getXYPath().get(curLink - 1).getY_float(),
				this.target.getPath().getXYPath().get(curLink).getX_float() - this.target.getPath().getXYPath().get(curLink - 1).getX_float());

		if (prevDist < 0)
		{
			trajectoryReturn.x = this.target.getMidX();
			trajectoryReturn.y = this.target.getMidY();
		}
		else
		{
			// We need to make up for the other links the Enemy has to travel before getting to this link.
			// enemyAngle is within [-pi,pi].
			Double backAngle = trajectoryReturn.enemyAngle + Math.PI;
			trajectoryReturn.x =
				(this.target.getPath().getXYPath().get(curLink - 1).getX_float() + (Math.cos(backAngle) * prevDist) + this.target.getMidX())
					- this.target.getX();
			trajectoryReturn.y =
				(this.target.getPath().getXYPath().get(curLink - 1).getY_float() + (Math.sin(backAngle) * prevDist) + this.target.getMidY())
					- this.target.getY();
		}

		// Angle of the hypotenuse (-pi to pi).
		final double enemyToTowerAngle = Math.atan2(this.getMidY() - trajectoryReturn.y, this.getMidX() - trajectoryReturn.x);
		// This angle should be between 0 and pi/2.
		double incAngle = enemyToTowerAngle - trajectoryReturn.enemyAngle;

		while (incAngle <= -Math.PI)
		{
			incAngle += 2 * Math.PI;
		}

		while (incAngle > Math.PI)
		{
			incAngle -= 2 * Math.PI;
		}

		// Starting distance.
		final double d0 = Math.sqrt(Math.pow(this.getMidX() - trajectoryReturn.x, 2) + Math.pow(this.getMidY() - trajectoryReturn.y, 2));
		// Starting x distance to target.
		final double x0 = d0 * Math.cos(incAngle);

		// Quadratic formula.
		final double a = Math.pow(Enemy.SPEED, 2) - Math.pow(Projectile.SPEED, 2);
		final double b = -2 * Enemy.SPEED * x0;
		final double c = Math.pow(d0, 2);
		trajectoryReturn.t = (-b - Math.sqrt(Math.pow(b, 2) - (4 * a * c))) / (2 * a);

		return trajectoryReturn;
	}

	public float getMidX()
	{
		return (this.getX() + (this.getWidth() / 2));
	}

	public float getMidY()
	{
		return (this.getY() + (this.getHeight() / 2));
	}

	public void setTarget(final Tower tower, final Enemy enemy)
	{
		this.target = enemy;
		this.source = tower;
		this.totalDamage = (Tower.DAMAGE * this.source.getDamageModifier());
	}

	public void shoot(final ArrayList<Enemy> enemyArray, final BaseGameActivity myContext)
	{
		int origLink = this.target.getCurrentLink();

		// Start with the distance from the Enemy to the next point.
		double totalDist =
			Math.sqrt(Math.pow(this.target.getPath().getXYPath().get(origLink).getX_float() - this.target.getX(), 2)
				+ Math.pow(this.target.getPath().getXYPath().get(origLink).getY_float() - this.target.getY(), 2));
		TrajectoryReturn trajReturn = this.findTrajectory(origLink, -1);
		int curLink = origLink;

		// If it takes longer for the bullet than it takes for the enemy to turn the corner, look at the next link.
		while ((trajReturn.t > (totalDist / Enemy.SPEED)) && (curLink < (this.target.getPath().getXYPath().size() - 1)))
		{
			// If it equals the size, then we're on the last link.
			curLink++;
			trajReturn = this.findTrajectory(curLink, totalDist);
			totalDist +=
				Math.sqrt(Math.pow(this.target.getPath().getXYPath().get(curLink).getX_float()
					- this.target.getPath().getXYPath().get(curLink - 1).getX_float(), 2)
					+ Math
						.pow(this.target.getPath().getXYPath().get(curLink).getY_float() - this.target.getPath().getXYPath().get(curLink - 1).getY_float(), 2));
		}

		double bulletDist = Projectile.SPEED * trajReturn.t;
		double enemyTravelDist = Enemy.SPEED * trajReturn.t;

		// d=r*t
		// t=d/r
		this.trajectory = new MoveByModifier((float)(bulletDist / Projectile.SPEED),
			(float)((trajReturn.x - this.getMidX()) + (Math.cos(trajReturn.enemyAngle) * enemyTravelDist)),
			(float)((trajReturn.y - this.getMidY()) + (Math.sin(trajReturn.enemyAngle) * enemyTravelDist)));
		
		this.trajectory.addModifierListener(new IModifierListener<IEntity>()
			{
			@Override
			public void onModifierFinished(final IModifier<IEntity> modifier, final IEntity item)
			{
				myContext.getEngine().runOnUpdateThread(new Runnable()
				{
					@Override
					public void run()
					{
						Projectile.this.detachSelf();
					}
				});

				Projectile.this.source.removeBullet(Projectile.this);

				// Enemy takes damage.
				Projectile.this.target.setInboundDamage(Projectile.this.target.getInboundDamage() - Projectile.this.totalDamage);

				if (Projectile.this.target.takeDamage(Projectile.this.totalDamage, Projectile.this.source.getDamageType()) < 1)
				{
					// Enemy dies.
					if (Projectile.this.target.isAlive())
					{
						// This prevents getting multiple credits for one kill.
						Projectile.this.target.setAlive(false);
						TowerMain.addCredits(Enemy.LOOT);
					}

					myContext.getEngine().runOnUpdateThread(new Runnable()
					{
						@Override
						public void run()
						{
							Projectile.this.scene.detachChild(Projectile.this.target);
						}
					});

					// TODO: Death Animation
					enemyArray.remove(Projectile.this.target);
				}
			}
			
			@Override
			public void onModifierStarted(final IModifier<IEntity> modifier, final IEntity item)
			{
				// TODO
			}
			});

		this.registerEntityModifier(this.trajectory);
		this.target.setInboundDamage(this.target.getInboundDamage() + this.totalDamage);
	}
}