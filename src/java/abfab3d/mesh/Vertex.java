package abfab3d.mesh;

import javax.vecmath.Point3d;

/**
 * TODO: Add docs
 *
 * @author Alan Hudson
 */
public interface Vertex {
    int getID();

    void setID(int id);

    Point3d getPoint();
/*
    double getX();
    double getY();
    double getZ();
*/
    void setPoint(Point3d p);

/*
    void setPoint(double x, double y, double z);
*/
    HalfEdge getLink();

    void setLink(HalfEdge link);

    Vertex getNext();

    void setNext(Vertex next);

    Vertex getPrev();

    void setPrev(Vertex prev);

    Object getUserData();

    void setUserData(Object userData);

    int getUserDataPos();

    void setUserDataPos(int userData);

    boolean isRemoved();

    void setRemoved(boolean removed);
}
