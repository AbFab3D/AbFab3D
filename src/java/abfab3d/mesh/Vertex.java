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

    void setPoint(Point3d p);

    HalfEdge getLink();

    void setLink(HalfEdge link);

    Vertex getNext();

    void setNext(Vertex next);

    Vertex getPrev();

    void setPrev(Vertex prev);

    Object getUserData();

    void setUserData(Object userData);

    boolean isRemoved();

    void setRemoved(boolean removed);
}
