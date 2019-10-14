/*****************************************************************************
 * Shapeways, Inc Copyright (c) 2019
 * Java Source
 * <p/>
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 * <p/>
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 ****************************************************************************/
package abfab3d.shapejs;

public class RenderOptions {
	public int aaSamples;
	public Quality quality;
	public Quality shadowQuality;
	public int rayBounces;

	// How much to scale the rendering size.  Usually used to make moving navigation smaller for speed
	public double renderScale = 1.0;

	public RenderOptions() {
		aaSamples = 1;
		quality = Quality.DRAFT;
		shadowQuality = Quality.DRAFT;
		rayBounces = 0;
	}
	
	public RenderOptions(int aaSamples, Quality quality, int rayBounces, Quality shadowQuality, double renderScale) {
		this.aaSamples = aaSamples;
		this.quality = quality;
		this.shadowQuality = shadowQuality;
		this.rayBounces = rayBounces;
		this.renderScale = renderScale;
	}
}
