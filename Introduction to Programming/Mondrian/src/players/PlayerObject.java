package players;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import core.Mondrian;

public abstract class PlayerObject implements Player {

    // player's properties
    private int x,y;
    private int velocity;
    private Color color;
    private Direction direction;

    public PlayerObject(int x, int y, int velocity, Color color) {
        this.x = x;
        this.y = y;
        this.velocity = velocity;
        this.color = color;
        this.direction = null;
    }

    @Override
    public int getX() {
        return this.x;
    }

    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return this.y;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Override
    public int getVelocity() {
        return this.velocity;
    }

    public void setVelocity(int velocity) {
        this.velocity = velocity;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    public abstract double getLength();

    @Override
    public boolean isOnBounds() {
        return this.x == 0 || this.y == 0 || this.x == Mondrian.WIDTH || this.y == Mondrian.HEIGHT;
    }

    @Override
    public abstract void update();

    @Override
    public abstract void drawObject(Graphics2D g2d);

    public abstract boolean intersects(Rectangle2D bounds);

    public abstract Rectangle2D getBounds();
}
