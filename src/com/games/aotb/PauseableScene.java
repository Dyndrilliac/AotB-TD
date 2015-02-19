
package com.games.aotb;

import org.andengine.entity.scene.Scene;

public class PauseableScene extends Scene
{
	private boolean	isPaused	= false;

	public PauseableScene()
	{
		super();
	}

	public boolean isPaused()
	{
		return this.isPaused;
	}

	@Override
	protected void onManagedUpdate(final float secondsElapsed)
	{
		if (this.isPaused())
		{
			return;
		}

		super.onManagedUpdate(secondsElapsed);
	}

	public void setPaused(final boolean isPaused)
	{
		this.isPaused = isPaused;
	}
}