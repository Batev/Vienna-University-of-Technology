package players;


import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import core.Mondrian;

public class CircularPlayer extends PlayerObject {

    // player's bounds
    private Rectangle2D bounds;

    private double diameter;

    public CircularPlayer(int x, int y, int velocity, Color color, double diameter) {
        super(x, y, velocity, color);
        this.diameter = diameter;
        // setting the bounds of the ball
        this.bounds = new Rectangle2D.Double(super.getX() - (this.diameter /2),super.getY() - (this.diameter /2),diameter,diameter);
    }

    @Override
    public double getLength() {
        return this.diameter;
    }

    @Override
    public boolean intersects(Rectangle2D bounds) {
        return this.bounds.intersects(bounds) || bounds.intersects(this.bounds);
    }

    @Override
    public Rectangle2D getBounds() {
        return this.bounds;
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

        this.updateBounds();
    }

    private void updateBounds() {
        // update the bounding box's position
        this.bounds.setRect(super.getX() - (this.diameter /2),super.getY() - (this.diameter /2),diameter,diameter);
    }

    @Override
    public void drawObject(Graphics2D g2d) {

        // enable anti-aliasing and pure stroke
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // construct a shape and draw it
        Ellipse2D.Double shape = new Ellipse2D.Double(super.getX() - (this.diameter / 2), super.getY() - (this.diameter / 2), diameter, diameter);
        g2d.setColor(super.getColor());
        g2d.fill(shape);
        g2d.draw(shape);
    }
}
