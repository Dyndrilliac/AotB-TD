
package com.games.aotb;

import java.util.ArrayList;

import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

public class BuildTowerTouchHandler implements IOnAreaTouchListener
{
	public static Tower getTower()
	{
		return BuildTowerTouchHandler.tower;
	}

	public static void setTower(final Tower newTower)
	{
		BuildTowerTouchHandler.tower = newTower;
	}
	
	private static Tower		tower				= null;
	private boolean				showHitArea;
	private boolean				currentlyDragging	= false;
	private double				distTraveled		= 0;
	private float				lastX				= 0;
	private float				lastY				= 0;
	private float				firstX				= 0;
	private float				firstY				= 0;

	private float				startingOffsetX		= 0;
	private float				startingOffsetY		= 0;
	private Scene				scene				= null;
	private Tower				buildTower			= null;
	private ArrayList<Tower>	towerArray			= null;
	private TextureRegion		bulletTexture		= null;
	private TextureRegion		towerTexture		= null;
	private TextureRegion		hitAreaTextureGood	= null;

	private TextureRegion		hitAreaTextureBad	= null;
	
	private BaseGameActivity	myContext			= null;
	
	public BuildTowerTouchHandler(final Tower buildTower, final Scene scene, final int credits,
		final ArrayList<Tower> towerArray, final TextureRegion hagTex,
		final TextureRegion habTex, final TextureRegion bTex,
		final TextureRegion tTex, final Level level, final BaseGameActivity context,
		final VertexBufferObjectManager vbom)
	{
		this.scene = scene;
		this.buildTower = buildTower;
		this.towerArray = towerArray;
		this.bulletTexture = bTex;
		this.towerTexture = tTex;
		this.hitAreaTextureGood = hagTex;
		this.hitAreaTextureBad = habTex;
		this.myContext = context;
	}
	
	@Override
	public boolean onAreaTouched(final TouchEvent sceneTouchEvent, final ITouchArea touchArea,
		final float touchAreaLocalX, final float touchAreaLocalY)
	{
		if (sceneTouchEvent.isActionUp())
		{
			if (BuildTowerTouchHandler.getTower() != null)
			{
				BuildTowerTouchHandler.getTower().setHitAreaShown(this.scene, false);
				BuildTowerTouchHandler.getTower().setMoveable(false);

				if (BuildTowerTouchHandler.getTower().hasPlaceError() || (TowerMain.getCredits() < this.buildTower.getCost()))
				{
					BuildTowerTouchHandler.getTower().remove(false, this.myContext);
				}
				else
				{
					BuildTowerTouchHandler.getTower().canPlace(true, this.myContext, BuildTowerTouchHandler.getTower());
					TowerMain.addCredits(-this.buildTower.getCost());
				}

				BuildTowerTouchHandler.setTower(null);
			}
		}
		else
			if (sceneTouchEvent.isActionMove())
			{
				if (BuildTowerTouchHandler.getTower() == null)
				{
					final float newX = TowerMain.sceneTransX(sceneTouchEvent.getX() + this.startingOffsetX) - this.buildTower.getXHandleOffset();
					final float newY = TowerMain.sceneTransY(sceneTouchEvent.getY() + this.startingOffsetY) - this.buildTower.getYHandleOffset();
					
					// TODO: Replace assignment with invocation of static method setTower().
					BuildTowerTouchHandler.tower = new Tower(this.bulletTexture, newX, newY, Tower.WIDTH, Tower.HEIGHT, this.towerTexture,
						this.hitAreaTextureGood, this.hitAreaTextureBad, this.scene, this.towerArray,
						BuildTowerTouchHandler.this.myContext.getVertexBufferObjectManager())
					{
						@Override
						public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY)
						{
							return BuildTowerTouchHandler.this.towerTouchEvent(pSceneTouchEvent, this);
						}
					};
					
					BuildTowerTouchHandler.getTower().checkClearSpotAndPlace(this.scene, newX, newY, this.myContext);
					BuildTowerTouchHandler.getTower().setHitAreaShown(this.scene, true);
					this.towerArray.add(BuildTowerTouchHandler.getTower());
					this.scene.registerTouchArea(BuildTowerTouchHandler.getTower());
					this.scene.attachChild(BuildTowerTouchHandler.getTower());
				}
				else
				{
					if (BuildTowerTouchHandler.getTower().isMoveable())
					{
						final float newX = TowerMain.sceneTransX(sceneTouchEvent.getX()) - BuildTowerTouchHandler.getTower().getXHandleOffset();
						final float newY = TowerMain.sceneTransY(sceneTouchEvent.getY()) - BuildTowerTouchHandler.getTower().getYHandleOffset();
						BuildTowerTouchHandler.getTower().checkClearSpotAndPlace(this.scene, newX, newY, this.myContext);
					}
				}
			}

		return true;
	}
	
	private boolean towerTouchEvent(final TouchEvent sceneTouchEvent, final Tower thisTower)
	{
		if (sceneTouchEvent.isActionDown())
		{
			if (!this.currentlyDragging)
			{
				this.lastX = sceneTouchEvent.getX();
				this.lastY = sceneTouchEvent.getY();
				this.firstX = this.lastX;
				this.firstY = this.lastY;
				this.distTraveled = 0;
				this.showHitArea = true;
				this.currentlyDragging = true;
				return true;
			}
			else
			{
				return true;
			}
		}
		else
			if (sceneTouchEvent.isActionMove())
			{
				this.distTraveled +=
					Math.sqrt((Math.pow(this.lastX - sceneTouchEvent.getX(), 2)) + (Math.pow(this.lastY - sceneTouchEvent.getY(), 2)))
						* TowerMain.zoomCamera.getZoomFactor();
				this.lastX = sceneTouchEvent.getX();
				this.lastY = sceneTouchEvent.getY();
				
				if (this.distTraveled < Tower.HEIGHT)
				{
					return true;
				}
				else
				{
					if (this.showHitArea)
					{
						this.startingOffsetX = this.firstX - this.lastX;
						this.startingOffsetY = this.firstY - this.lastY;
						TowerMain.currentXOffset = this.lastX - this.firstX;
						TowerMain.currentYOffset = this.lastY - this.firstY;
					}
					
					this.showHitArea = false;
					return false;
				}
			}
			else
				if (sceneTouchEvent.isActionUp())
				{
					TowerMain.currentXOffset = 0;
					TowerMain.currentYOffset = 0;
					
					if (this.showHitArea)
					{
						TowerMain.addCredits(this.buildTower.getCost());
						thisTower.remove(thisTower, true, this.myContext);
						this.currentlyDragging = false;
						return true;
					}
					else
					{
						this.showHitArea = false;
						this.currentlyDragging = false;
						return false;
					}
				}
				else
				{
					this.showHitArea = false;
					return false;
				}
	}
}