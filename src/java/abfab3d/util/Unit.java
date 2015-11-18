/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.util;

/**
 * various conversion coefficients 
 *
 * @author Tony Wong
 */
public enum Unit{
	NONE (1.0),
	M (1.0),
	CM (0.01),
	MM (0.001), // mm -> meters
	M3 (1.0), // meters^3
	MM3 (1.E-9), // mm^3 -> meters^3
	CM3 (1.E-6), // cm^3 -> meters^3
	FT (0.304), // ft -> meters
	IN (0.0254), // inches -> meters
	UM (1.e-6), // micron -> meters
	PT (0.0254/72); // points -> meters

	private double conversion;
	
	Unit(double conversion) {
		this.conversion = conversion;
	}
	
	public double getConversionVal(double val) {
		return conversion * val;
	}
}
