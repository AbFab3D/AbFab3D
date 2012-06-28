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

package abfab3d.io.shapeways;

/**
 * The various IO request types
 *
 * @author Russell Dodds
 * @version $Revision: 1.4 $
 */
public enum IORequestType {

    /** Request a model to be loaded */
    REQUEST_LOAD_MODEL,

    /** Request for a model to be loaded is canceled */
    CANCEL_LOAD_MODEL,

    /** Request a model to be saved */
    REQUEST_SAVE_MODEL,

    /** Request a model to be saved is currently in progress */
    REQUEST_SAVE_MODEL_IN_PROGRESS,

    /** Request a model to be updated */
    REQUEST_UPDATE_MODEL,

    /** Request a model to be updated is currently in progress */
    REQUEST_UPDATE_MODEL_IN_PROGRESS,

    /** Request for model to be saved is canceled */
    CANCEL_SAVE_MODEL,

    /** Request a model to be saved to the cart*/
    REQUEST_SAVE_TO_CART,

    /** Request for model to be saved to the cart is canceled */
    CANCEL_SAVE_TO_CART,

    /** Request a model to be saved to the cart*/
    REQUEST_ORDER_MODEL,

    /** Request for model to be saved to the cart is canceled */
    CANCEL_ORDER_MODEL,

    /** Request the materials to be loaded into memory */
    REQUEST_LOAD_MATERIAL,

    /** Request the font to be changed */
    REQUEST_CHANGE_FONT,

    /** Request the price of a model */
    REQUEST_MODEL_PRICE

}
