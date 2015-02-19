
package com.games.aotb;

import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

public class TowerRange extends Sprite
{
	private static final int	Z_INDEX	= 2000; // Show above the towers!
												
	public TowerRange(final float x, final float y, final TextureRegion textureRegion, final VertexBufferObjectManager vbom)
	{
		super(x, y, textureRegion, vbom);
		this.setZIndex(TowerRange.Z_INDEX);
	}
}