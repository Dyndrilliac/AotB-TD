package com.games.aotb;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.sprite.ButtonSprite;
import org.andengine.entity.sprite.ButtonSprite.OnClickListener;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSCounter;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLoader;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.extension.tmx.util.exception.TMXLoadException;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.algorithm.path.astar.AStarPathFinder;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.content.Context;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.Toast;

public class TowerMain extends SimpleBaseGameActivity implements IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener, IOnMenuItemClickListener
{
	// Initial credits and lives.
	private static final int INITIAL_CREDITS = 1000;
	private static final int INITIAL_LIVES   = 5;

	// TileMap String Constants
	private static final String TMX_DESERT_STRING = "desert.tmx";
	private static final String TMX_GRID_STRING   = "grid.tmx";

	// Texture String Constants
	private static final String TEXTURE_HAG_STRING   = "towerRangeGood.png";
	private static final String TEXTURE_HAB_STRING   = "towerRangeBad.png";
	private static final String TEXTURE_PAUSE_STRING = "pause.png";
	private static final String TEXTURE_PLAY_STRING  = "play.png";

	// Snaps towers in place on the grid.
	public static boolean  enableSnap = true;
	public static TMXLayer tmxLayer   = null;

	// Allows for zooming in and out of the map.
	public static ZoomCamera zoomCamera = null;

	// Object representing our tiled map.
	public static TMXTiledMap tmxTiledMap = null;

	// Create Camera and Scenes
	public static int         cameraWidth   = 1280;
	public static int         cameraHeight  = 720;
	public static final float MAX_ZOOM       = 4.0f;
	public static final float MIN_ZOOM       = 0.5f;

	public static final int   TILEID_BLOCKED = 31;
	public static final int   TILEID_CLEAR   = 30;
	public static float       currentXOffset = 0;
	public static float       currentYOffset = 0;

	private TowerMain             self           = this;
	private SurfaceScrollDetector scrollDetector = null;
	private PinchZoomDetector     pinchZoomDetector = null;
	private float                 pinchZoomStartedCameraZoomFactor;

	private static Scene          mainScene;
	private static PauseableScene scene;
	private static ButtonSprite   pauseButton;
	private HUD hud;
	private ProgressBar waveProgress; // add to wave class

	// Set the enemy spawn point, enemy goal point (the point that needs to be defended), and the number of enemies in each wave.
	public static Point starts[] = { new Point(0, 0)  };                 // Enemy spawn point. Can be one block off the map and still be valid.
	public static Point ends[]   = { new Point(15, 1) };                 // Enemy goal point. Can be 1 block off the map and still be valid.
	public static int[] waves    = { 1, 5, 10, 20, 40, 80, 160, 320, 640 }; // Number of enemies in each wave.

	public static Level currentLevel = new Level(waves, starts, ends, TowerMain.TMX_DESERT_STRING);

	public BitmapTextureAtlas towerImage;
	public TextureRegion towerTexture;
	public ArrayList<Tower> arrayTower;
	public Tower buildBasicTower;

	public BitmapTextureAtlas bulletImage;
	public TextureRegion bulletTexture;

	public BitmapTextureAtlas enImage;
	public TextureRegion enTexture;
	public TextureRegion hitAreaTextureGood;
	public TextureRegion hitAreaTextureBad;
	public TextureRegion texPause;
	public TextureRegion texPlay;
	public static Enemy enemyClone[];
	public static AStarPathFinder<Enemy> finder;
	public static int colMin = -1;
	public static int rowMin = -1;
	public static int colMax;
	public static int rowMax;
	public static boolean allowDiagonal = false;
	public ArrayList<Enemy> arrayEn;

	// Touch support.
	public float touchX;
	public float touchY;
	public long touchDuration;

	// Enemy location.
	public float targetX;
	public float targetY;

	public Font font10;
	public Font font20;
	public Font font40;

	public final FPSCounter fpsCounter = new FPSCounter();
	public Text fpsText;
	public static Text creditText;
	public static Text livesText;
	public static Rectangle creditMask;
	public static Rectangle livesMask;
	public static int credits;
	public static int lives;
	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	public static boolean paused = false;

	@Override
	public EngineOptions onCreateEngineOptions() {

		// Setup Camera

		final DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		cameraWidth = metrics.heightPixels;
		cameraWidth = metrics.widthPixels;

		zoomCamera = new ZoomCamera(0, 0, cameraWidth, cameraWidth);
		zoomCamera.setBounds(-cameraWidth * 0.25f, -cameraWidth * 0.25f, cameraWidth * 1.25f, cameraWidth * 1.25f);
		zoomCamera.setBoundsEnabled(true);

		final EngineOptions mEngine = new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new FillResolutionPolicy(), zoomCamera);

		if (MultiTouch.isSupported(this)) {
			if (MultiTouch.isSupportedDistinct(this))
				Toast.makeText(this, "MultiTouch detected Pinch Zoom will work properly!", Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(this, "MultiTouch detected, but your device has problems distinguishing between fingers", Toast.LENGTH_LONG).show();
		} else
			Toast.makeText(this, "Sorry your device does NOT support MultiTouch! Use Zoom Buttons.", Toast.LENGTH_LONG).show();
		mEngine.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
		return mEngine;
	}

	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		// Load Textures

		mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(getTextureManager(), 1024, 1024);
		towerTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, Tower.TEXTURE);


		bulletTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, TowerMain.TEXTURE_HAG_STRING);

		enTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, Enemy.TEXTURE);
		hitAreaTextureGood = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, TowerMain.TEXTURE_HAG_STRING);
		hitAreaTextureBad = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, TowerMain.TEXTURE_HAB_STRING);
		texPause = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, TowerMain.TEXTURE_PAUSE_STRING);
		texPlay = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, TowerMain.TEXTURE_PLAY_STRING);
		try
		{
			this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 0, 0));
			this.mBitmapTextureAtlas.load();
		}
		catch (TextureAtlasBuilderException e)
		{
			Debug.e(e);
		}


		// Load Sounds

		SoundFactory.setAssetBasePath("sfx/");
		Tower.loadSound(mEngine.getSoundManager(), this);

		// Load Fonts

		font10 = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 10);
		font20 = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20);
		font40 = FontFactory.create(getFontManager(), getTextureManager(), 256, 256, TextureOptions.BILINEAR, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 40);

		font10.load();
		font20.load();
		font40.load();

	}

	@Override
	protected Scene onCreateScene() {
		mainScene = new Scene();
		mainScene.setBackground(new Background(Color.WHITE));

		scene = createGameScene();
		mainScene.setChildScene(scene);

		return mainScene;
	}

	private PauseableScene createGameScene(){
		mEngine.registerUpdateHandler(new FPSLogger());
		PauseableScene scene = new PauseableScene();
		hud = new HUD();
		waveProgress = new ProgressBar(20, 70, 100, 10, currentLevel.getWave().length, 0, getVertexBufferObjectManager());
		waveProgress.setProgressColor(1.0f, 0.0f, 0.0f, 1.0f).setFrameColor(0.4f, 0.4f, 0.4f, 1.0f).setBackColor(0.0f, 0.0f, 0.0f, 0.2f);
		waveProgress.setProgress(0);
		zoomCamera.setHUD(hud);
		hud.attachChild(waveProgress);

		try {
			final TMXLoader tmxLoader = new TMXLoader(getAssets(), mEngine.getTextureManager(), TextureOptions.BILINEAR_PREMULTIPLYALPHA, getVertexBufferObjectManager(),
					new ITMXTilePropertiesListener() {
				@Override
				public void onTMXTileWithPropertiesCreated(final TMXTiledMap pTMXTiledMap, final TMXLayer pTMXLayer, final TMXTile pTMXTile,
						final TMXProperties<TMXTileProperty> pTMXTileProperties) {
				}
			});
			// Load the Desert Map
			TowerMain.tmxTiledMap = tmxLoader.loadFromAsset("tmx/" + TowerMain.TMX_GRID_STRING);
		} catch (final TMXLoadException e) {
			Debug.e(e);
		}

		tmxLayer = TowerMain.tmxTiledMap.getTMXLayers().get(0);
		colMax = tmxLayer.getTileColumns() - 1 + 1;
		rowMax = tmxLayer.getTileRows() - 1 + 1;
		scene.attachChild(tmxLayer);


		scrollDetector = new SurfaceScrollDetector(this);
		pinchZoomDetector = new PinchZoomDetector(this);
		pinchZoomDetector.setEnabled(true);

		mEngine.registerUpdateHandler(fpsCounter);
		final Rectangle fpsMask = makeColoredRectangle(cameraWidth - 100, 20, 20, 100, .8f, .8f, .8f, 1f);
		hud.attachChild(fpsMask);
		fpsText = new Text(cameraWidth - 100, 20, font20, "FPS:", "FPS: xxx.xx".length(), getVertexBufferObjectManager());
		creditMask = makeColoredRectangle(20, 20, 40, 100, .8f, .8f, .8f, 1f);
		hud.attachChild(creditMask);

		pauseButton = new ButtonSprite(TowerMain.cameraWidth - 180, 20, texPause, texPlay, getVertexBufferObjectManager(), pauseListener);
		hud.attachChild(pauseButton);
		hud.registerTouchArea(pauseButton);

		creditText = new Text(20, 20, font40, "$", 12, getVertexBufferObjectManager());
		livesText = new Text(20, 40 + creditText.getHeight() + waveProgress.getHeight(), font40, "", 12, getVertexBufferObjectManager());
		livesMask = makeColoredRectangle(20, 40 + creditText.getHeight() + waveProgress.getHeight(), 40, 100, .8f, .8f, .8f, 1f);
		hud.attachChild(livesMask);

		credits = TowerMain.INITIAL_CREDITS;
		addCredits(0);

		lives = TowerMain.INITIAL_LIVES;
		subtractLives(0);

		hud.attachChild(fpsText);
		hud.attachChild(creditText);
		hud.attachChild(livesText);

		arrayTower = new ArrayList<Tower>();

		arrayEn = new ArrayList<Enemy>();
		finder = new AStarPathFinder<Enemy>();

		scene.registerUpdateHandler(hudLoop);

		scene.registerUpdateHandler(loop);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.setOnSceneTouchListener(this);

		hud.setTouchAreaBindingOnActionDownEnabled(true);

		buildBasicTower = new Tower(bulletTexture, 150, 0, Tower.WIDTH, Tower.HEIGHT, towerTexture, hitAreaTextureGood, hitAreaTextureBad, scene, arrayTower,
				getVertexBufferObjectManager());
		hud.attachChild(buildBasicTower);
		hud.registerTouchArea(buildBasicTower);
		final BuildTowerTouchHandler btth = new BuildTowerTouchHandler(buildBasicTower, scene, credits, arrayTower, hitAreaTextureGood, hitAreaTextureBad, bulletTexture,
				towerTexture, currentLevel, this, self.getVertexBufferObjectManager());

		hud.setOnAreaTouchListener(btth);
		enemyClone = new Enemy[currentLevel.getStartPoint().length];

		for (int i = 0; i < currentLevel.getStartPoint().length; i++) {
			enemyClone[i] = new Enemy(getXFromCol(currentLevel.getStartPoint()[i].getX_int()), getXFromCol(currentLevel.getStartPoint()[i].getY_int()), Enemy.WIDTH, Enemy.HEIGHT, enTexture, getVertexBufferObjectManager(), currentLevel, arrayEn); 
			enemyClone[i].createPath(currentLevel.getGoalPoint()[0], this, tmxLayer, arrayEn);
		}
		start_waves();
		return scene;
	}

	private boolean isZooming = false;

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
	{
		if (BuildTowerTouchHandler.getTower() != null)
		{
			BuildTowerTouchHandler.getTower().detachSelf();// this ensures that bad towers get removed
			BuildTowerTouchHandler.setTower(null);
		}

		pinchZoomDetector.onTouchEvent(pSceneTouchEvent);
		if (pinchZoomDetector.isZooming()) {
			scrollDetector.setEnabled(false);
			isZooming = true;
		} else if (!isZooming) {
			if (pSceneTouchEvent.isActionDown() || !scrollDetector.isEnabled()) {
				scrollDetector.setEnabled(true);
			}
			scrollDetector.onTouchEvent(pSceneTouchEvent);
		}
		if (pSceneTouchEvent.isActionUp()) {
			isZooming = false;
		}

		return true;
	}

	OnClickListener pauseListener = new OnClickListener() {
		@Override
		public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
			togglePauseGame();
		}
	};

	IUpdateHandler loop = new IUpdateHandler() {
		@Override
		public void reset() {
		}

		@Override
		public void onUpdate(float pSecondsElapsed) {

			collision();

		}
	};
	IUpdateHandler hudLoop = new IUpdateHandler() {
		@Override
		public void reset() {
		}

		@Override
		public void onUpdate(float pSecondsElapsed) {
			fpsText.setText("FPS: " + new DecimalFormat("#.##").format(fpsCounter.getFPS()));
		}
	};

	public void collision() {
		// TODO: Put inside a thread.
		if (arrayEn.size() > 0) {
			for (int j = 0; j < arrayEn.size(); j++) {
				final Enemy enemy = arrayEn.get(j);

				for (int k = 0; k < arrayTower.size(); k++) {
					final Tower tower = arrayTower.get(k);

					if (tower.distanceTo(enemy) < tower.maxRange()) {
						tower.fire(enemy, scene, arrayEn, this);
					}
				}
			}
		}
	}

	public static long getCredits()
	{
		return credits;
	}

	public static void addCredits(long enCredits)
	{
		credits += enCredits;
		creditText.setText("$" + credits);
		creditMask.setWidth(creditText.getWidth());
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event)
	{
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK)
		{
			return true;
		} else {
			return false;
		}
	}	

	public static void subtractLives(long pLives) {
		lives -= pLives;
		if (lives < 1) {
			loseGame();
			lives = 0;
		}
		livesText.setText(lives + " Lives");
		livesMask.setWidth(livesText.getWidth());
	}

	private static void loseGame()
	{
		if (!TowerMain.paused)
		{
			togglePauseGame();
		}

		System.exit(0);
	}

	int currentWaveNum = 0;
	int currentEnemyCount = 0;
	int currentDelayBetweenWaves = 0;
	final float delay = 4; // TODO: Put this stuff in a wave class.
	final int delayBetweenWaves = 6;
	TimerHandler enemy_handler;

	public void start_waves() {
		enemy_handler = new TimerHandler(delay, true, new ITimerCallback() {
			@Override
			public void onTimePassed(TimerHandler pTimerHandler) {
				if ((!paused) && (currentWaveNum < currentLevel.getWave().length)) {
					new Random();

					if (currentLevel.getWave()[currentWaveNum] > currentEnemyCount) {

						for (int i = 0; i < currentLevel.getStartPoint().length; i++) {
							Enemy enemy;
							enemy = enemyClone[i].clone();
							scene.attachChild(enemy);

							enemy.startMoving(TowerMain.this);

							arrayEn.add(enemy);
							currentEnemyCount++;
						}
						waveProgress.setProgress(currentWaveNum + 1);
					} else if (currentDelayBetweenWaves < delayBetweenWaves) {
						currentDelayBetweenWaves++;
					}

					if (currentDelayBetweenWaves >= delayBetweenWaves) {
						currentEnemyCount = 0;
						currentDelayBetweenWaves = 0;
						currentWaveNum++;
					}
				} else if (!paused) {
					getEngine().unregisterUpdateHandler(enemy_handler);
				}
			}
		});
		getEngine().registerUpdateHandler(enemy_handler);

	}

	private Rectangle makeColoredRectangle(final float pX, final float pY, final float pWidth, final float pHeight, final float pRed, final float pGreen, final float pBlue,
			final float pAlpha) {

		final Rectangle coloredRect = new Rectangle(pX, pY, pHeight, pWidth, getVertexBufferObjectManager());
		coloredRect.setColor(pRed, pGreen, pBlue, pAlpha);
		return coloredRect;
	}

	public static float sceneTransX(float x) {
		final float myZoom = zoomCamera.getZoomFactor();
		final float myXOffset = zoomCamera.getCenterX() - TowerMain.cameraWidth / 2 / myZoom;
		final float newX = x / myZoom + myXOffset;
		return newX;
	}

	public static float sceneTransY(float y) {
		final float myZoom = zoomCamera.getZoomFactor();
		final float myYOffset = zoomCamera.getCenterY() - TowerMain.cameraWidth / 2 / myZoom;
		final float newY = y / myZoom + myYOffset;
		return newY;
	}

	public static float getPanX() {
		return zoomCamera.getCenterX();
	}

	public static float getPanY() {
		return zoomCamera.getCenterY();
	}

	public static float getZoom() {
		return zoomCamera.getZoomFactor();
	}

	private void scenePan(float pDistanceX, float pDistanceY) {
		final float zoomFactor = TowerMain.zoomCamera.getZoomFactor();
		TowerMain.zoomCamera.offsetCenter((-pDistanceX) / zoomFactor - currentXOffset, (-pDistanceY) / zoomFactor - currentYOffset);
		currentXOffset = 0;
		currentYOffset = 0;
	}

	@Override
	public void onScrollStarted(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		scenePan(pDistanceX, pDistanceY);
	}

	@Override
	public void onScroll(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		scenePan(pDistanceX, pDistanceY);
	}

	@Override
	public void onScrollFinished(final ScrollDetector pScollDetector, final int pPointerID, final float pDistanceX, final float pDistanceY) {
		scenePan(pDistanceX, pDistanceY);
	}

	@Override
	public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
		final float zoomFactor = TowerMain.zoomCamera.getZoomFactor();
		pinchZoomStartedCameraZoomFactor = zoomFactor;
		currentXOffset = 0;
		currentYOffset = 0;
	}

	@Override
	public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		TowerMain.zoomCamera.setZoomFactor(Math.min(Math.max(TowerMain.MIN_ZOOM, pinchZoomStartedCameraZoomFactor * pZoomFactor), TowerMain.MAX_ZOOM));

	}

	@Override
	public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
		TowerMain.zoomCamera.setZoomFactor(Math.min(Math.max(TowerMain.MIN_ZOOM, pinchZoomStartedCameraZoomFactor * pZoomFactor), TowerMain.MAX_ZOOM));
	}

	public static void togglePauseGame() {
		paused = !paused;
		pauseButton.setCurrentTileIndex((paused) ? 1 : 0);
		scene.setPaused(paused);
	}

	public static TextureRegion loadSprite(TextureManager tm, Context c, String strtex) {
		TextureRegion tr;
		BitmapTextureAtlas towerImage;
		towerImage = new BitmapTextureAtlas(tm, 512, 512);
		tr = BitmapTextureAtlasTextureRegionFactory.createFromAsset(towerImage, c, strtex, 0, 0);
		tm.loadTexture(towerImage);
		return tr;
	}

	public static int getColFromX(float pX) {

		return (int) Math.floor(pX / tmxTiledMap.getTileWidth());
	}

	public static int getRowFromY(float pY) {
		return (int) Math.floor(pY / tmxTiledMap.getTileHeight());
	}

	public static float getXFromCol(int pC) {
		return Math.round((float)pC * tmxTiledMap.getTileWidth());
	}

	public static float getYFromRow(int pR) {
		return Math.round((float)pR * tmxTiledMap.getTileHeight());
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
	{
		return false;
	}

	public static boolean isPaused() {
		return paused;
	}

	public static void setPaused(boolean paused) {
		TowerMain.paused = paused;
	}
}
