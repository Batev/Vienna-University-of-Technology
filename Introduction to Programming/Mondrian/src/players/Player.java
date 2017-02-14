package players;

import java.awt.geom.Rectangle2D;

import core.GraphicObject;
import core.Movable;

public interface Player extends GraphicObject, Movable, Controllable {

    public int getVelocity();

    public double getLength();

    public boolean isOnBounds();

    public boolean intersects(Rectangle2D bounds);
}
