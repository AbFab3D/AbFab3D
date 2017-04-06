/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2016
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.io.input;

/**
 * A URI mapping interface.  Allows users to modify URI's before resolution
 *
 * @author Alan Hudson
 */
public interface URIMapper {

    /**
     * Transform a URI
     *
     * @param uri The original
     * @return  The transformed URI
     */
    public MapResult mapURI(String uri);

    static class MapResult {
        public String uri;
        public boolean sensitiveData;

        public MapResult(String uri, boolean sensitiveData) {
            this.uri = uri;
            this.sensitiveData = sensitiveData;
        }
    }
}