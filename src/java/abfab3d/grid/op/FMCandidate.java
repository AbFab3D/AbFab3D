/*****************************************************************************
 *                        Shapeways, Inc Copyright (c) 2013
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package abfab3d.grid.op;

public class FMCandidate {
    
    int x, y, z;
    int value;
    public FMCandidate(){
    }

    public FMCandidate(int x, int y, int z, int value){
        this.x = x;
        this.y = y;
        this.z = z;
        this.value = value;
    }
    
    public boolean equals(Object obj){
        FMCandidate p = (FMCandidate)obj;
        return (p.x == x) && (p.y  == y) && (p.z == z);
    }
    public int hashCode(){
        return x * 123456 + y * 5678 + z * 9101112 + 121;
    }

    public void set(FMCandidate cand){
        this.x = cand.x;
        this.y = cand.y;
        this.z = cand.z;
        this.value = cand.value;

    }


}