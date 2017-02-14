package enemies;

import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import core.GraphicObject;
import core.Movable;

public interface Enemy extends GraphicObject, Movable, Directable {

    void setX(int x);

    void setY(int y);

    Rectangle2D getBounds();

    void intersect(GeneralPath path);
}
