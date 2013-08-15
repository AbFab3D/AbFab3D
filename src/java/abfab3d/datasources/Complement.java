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

package abfab3d.datasources;


import abfab3d.util.DataSource;
import abfab3d.util.Initializable;
import abfab3d.util.Vec;


/**
   Boolean complement.  The datasource is the opposite of the input.
   <embed src="doc-files/Complement.svg" type="image/svg+xml"/> 
 
 * @author Vladimir Bulatov
 */
public class Complement extends TransformableDataSource {

    private DataSource dataSource = null;

    /**
     * Complement of the given datasource.
     * @param source  object to which the complement is generated
     */
    public Complement(DataSource source) {
        dataSource = source;
    }

    /**
     * @noRefGuide
     */
    public int initialize() {

        super.initialize();

        if (dataSource instanceof Initializable) {
            ((Initializable) dataSource).initialize();
        }

        return RESULT_OK;

    }

    /**
     * Get the data value for a pnt
     *
     * @return 1 if pnt is inside of box of given size and center 0 otherwise
     * @noRefGuide
     */
    public int getDataValue(Vec pnt, Vec data) {

        super.transform(pnt);

        int res = dataSource.getDataValue(pnt, data);
        if (res != RESULT_OK) {
            // bad result in source 
            data.v[0] = 1;
            return res;
        } else {
            // we have good result
            // make complement
            data.v[0] = 1 - data.v[0];
            return RESULT_OK;
        }
    }
} // class Complement

