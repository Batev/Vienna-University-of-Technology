package enemies;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import core.Mondrian;

public class RectangularEnemy extends EnemyObject {

    private int side;

    // enemy's bounds
    private Rectangle2D bounds;

    // points that surround the enemy like a rectangle
    private Point2D leftUp,leftDown,rightUp,rightDown;

    public RectangularEnemy(int x, int y, Color color, int side) {
        super(x, y, color);
        this.side = side;
        this.bounds = new Rectangle2D.Double(super.getX(), super.getY(), this.side, this.side);
    }

    @Override
    public Rectangle2D getBounds() {
        return this.bounds;
    }

    @Override
    public void update() {
        // updates the movement if there is contact with a wall
        this.bounceOfWall();

        // update the enemy bounding rectangle's position
        this.updateBounds();

        // update enemy's bounding rectangle points
        this.leftUp = new Point2D.Double((super.getX() - (this.side/2))- ((this.side/2)), (super.getY() - (this.side/2))- ((this.side/2)));
        this.leftDown = new Point2D.Double((super.getX() - (this.side/2))- ((this.side/2)), (super.getY() + (this.side/2) + (this.side/2)));
        this.rightUp = new Point2D.Double((super.getX() + (this.side/2) + (this.side/2)),(super.getY() - (this.side/2))- ((this.side/2)));
        this.rightDown = new Point2D.Double((super.getX() + (this.side/2) + (this.side/2)),(super.getY() + (this.side/2) + (this.side/2)));
    }

    // if the enemy has reached the limits of the field it bounces of the wall
    private void bounceOfWall() {

        //where will the ball be after it moves?
        double nextLeft = super.getX() + super.getDeltaX();
        double nextRight = super.getX() + super.getDeltaX();
        double nexTop = super.getY() + super.getDeltaY();
        double nextBottom = super.getY() + super.getDeltaY();


        // ball bounces off top and bottom of screen
        if (nexTop < 0 || nextBottom > Mondrian.HEIGHT) {
            double temp = super.getDeltaY() * (-1);
            super.setDeltaY(temp);
        }

        // will the ball go off the left side?
        if (nextLeft < 0) {
            double temp = super.getDeltaX() * (-1);
            super.setDeltaX(temp);
        }

        // will the ball go off the right side?
        if (nextRight > Mondrian.WIDTH) {
            double temp = super.getDeltaX() * (-1);
            super.setDeltaX(temp);
        }

        double tempX = super.getX() + super.getDeltaX();
        double tempY = super.getY() + super.getDeltaY();
        //move the ball
        super.setX((int)tempX);
        super.setY((int)tempY);
    }

    private void updateBounds() {
        // update the enemy bounding rectangle's position
        this.bounds.setRect(super.getX() - (this.side /2),super.getY() - (this.side /2), this.side, this.side);
    }

    @Override
    public void intersect(GeneralPath path) {
        if ((path.contains(this.leftDown) && path.contains(this.leftUp))
          ||(path.contains(this.rightUp) && path.contains(this.rightDown))) {
            double tempX = super.getDeltaX() * (-1);
            super.setDeltaX(tempX);
        }
        else if((path.contains(this.leftUp) && path.contains(this.rightUp))
             || (path.contains(this.leftDown) && path.contains(this.rightDown))) {
            double tempY = super.getDeltaY() * (-1);
            super.setDeltaY(tempY);
        }
        else if(path.contains(this.rightDown) || path.contains(this.rightUp)
             || path.contains(this.leftDown) || path.contains(this.leftUp)) {
            double tempX = super.getDeltaX() * (-1);
            super.setDeltaX(tempX);
            double tempY = super.getDeltaY() * (-1);
            super.setDeltaY(tempY);
        }
    }

    @Override
    public void drawObject(Graphics2D g2d) {

        // enable anti-aliasing and pure stroke
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        // construct a shape and draw it
        Rectangle2D.Double shape = new Rectangle2D.Double(super.getX() - (this.side / 2), super.getY() - (this.side / 2), this.side, this.side);
        g2d.setColor(super.getColor());
        g2d.fill(shape);
        g2d.draw(shape);
    }
}
