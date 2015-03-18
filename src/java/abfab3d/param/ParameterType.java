/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2015
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package abfab3d.param;

/**
 * The valid Parameter types.
 *
 * @author Alan Hudson
 */
public enum ParameterType {
    
    BOOLEAN, DATE_TIME, DOUBLE, FLOAT, BYTE, SHORT, INTEGER, LONG, MAP, STRING, URI, ENUM,
        BOOLEAN_LIST, DATE_TIME_LIST, DOUBLE_LIST, FLOAT_LIST, BYTE_LIST, SHORT_LIST, INTEGER_LIST,
        LONG_LIST, MAP_LIST, STRING_LIST, URI_LIST, ENUM_LIST, VECTOR_3D, AXIS_ANGLE_4D,SNODE, SNODE_LIST, MATRIX_4D,
        OBJECT, LOCATION,LOCATION_LIST;
}
