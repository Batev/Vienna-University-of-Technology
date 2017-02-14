package players;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import core.Mondrian;

public class RectangularPlayer extends PlayerObject {

    // player's bounds
    private Rectangle2D bounds;

    private double side;

    public RectangularPlayer(int x, int y, int velocity, Color color, double side) {
        super(x, y, velocity, color);
        this.side = side;
        // setting the bounds of the ball
        this.bounds = new Rectangle2D.Double(super.getX() - (this.side /2),super.getY() - (this.side /2), side, side);
    }

    @Override
    public double getLength() {
        return this.side;
    }

    @Override
    public void update() {

        if(super.getDirection() == Direction.UP && super.getY() > 0) {
            int temp = super.getY() - super.getVelocity();
            super.setY(temp);
        }
        if(super.getDirection() == Direction.DOWN && super.getY() < Mondrian.HEIGHT) {
            int temp = super.getY() + super.getVelocity();
            super.setY(temp);
        }
        if(super.getDirection() == Direction.LEFT && super.getX() > 0) {
            int temp = super.getX() - super.getVelocity();
            super.setX(temp);
        }
        if(super.getDirection() == Direction.RIGHT && super.getX() < Mondrian.WIDTH) {
            int temp = super.getX() + super.getVelocity();
            super.setX(temp);
        }

        // update the bounding box's position
        this.updateBounds();
    }

    private void updateBounds() {
        // update the bounding box's position
        this.bounds.setRect(super.getX() - (this.side /2),super.getY() - (this.side /2), side, side);
    }

    @Override
    public void drawObject(Graphics2D g2d) {

        // enable anti-aliasing and pure stroke
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // construct a shape and draw it
        Rectangle2D.Double shape = new Rectangle2D.Double(super.getX() - (this.side /2), super.getY() - (this.side /2), this.side, this.side);
        g2d.setColor(super.getColor());
        g2d.fill(shape);
        g2d.draw(shape);
    }

    @Override
    public boolean intersects(Rectangle2D bounds) {
        return this.bounds.intersects(bounds) || bounds.intersects(this.bounds);
    }

    @Override
    public Rectangle2D getBounds() {
        return this.bounds;
    }
}
