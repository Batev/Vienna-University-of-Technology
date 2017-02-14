package enemies;

import java.awt.*;
import java.awt.geom.*;

import core.Mondrian;

public class CircularEnemy extends EnemyObject {

    private int diameter;

    // enemy's bounds
    private Rectangle2D bounds;

    // points that are perpendicular to the center of the enemy
    private Point2D cDown,cUp,cRight,cLeft;

    // points that surround the enemy like a rectangle
    private Point2D leftUp,leftDown,rightUp,rightDown;

    // setting the enemy's properties
    public CircularEnemy(int x, int y, Color color, int diameter) {
        super(x, y, color);
        this.diameter = diameter;
        this.bounds = new Rectangle2D.Double(super.getX(),super.getY(), diameter, diameter);
    }

    @Override
    public Rectangle2D getBounds() {
        return this.bounds;
    }

    @Override
    public void update() {

        // updates the movement if there is contact with a wall
        this.bounceOfWall();

        this.updateBounds();

        // update enemy's bounding rectangle points
        this.leftUp = new Point2D.Double((super.getX() - (this.diameter/2))- ((this.diameter/2)), (super.getY() - (this.diameter/2))- ((this.diameter/2)));
        this.leftDown = new Point2D.Double((super.getX() - (this.diameter/2))- ((this.diameter/2)), (super.getY() + (this.diameter/2) + (this.diameter/2)));
        this.rightUp = new Point2D.Double((super.getX() + (this.diameter/2) + (this.diameter/2)),(super.getY() - (this.diameter/2))- ((this.diameter/2)));
        this.rightDown = new Point2D.Double((super.getX() + (this.diameter/2) + (this.diameter/2)),(super.getY() + (this.diameter/2) + (this.diameter/2)));

        // update enemy's center bounding points
        this.cDown = new Point2D.Double(super.getX(), (super.getY() + (this.diameter/2)) + ((this.diameter/2)));
        this.cUp = new Point2D.Double(super.getX(), (super.getY() - (this.diameter/2)) - ((this.diameter/2)));
        this.cLeft = new Point2D.Double((super.getX()  - (this.diameter/2)) - ((this.diameter/2)), super.getY());
        this.cRight = new Point2D.Double((super.getX()  + (this.diameter/2)) + ((this.diameter/2)), super.getY());

    }

    @Override
    public void intersect(GeneralPath path) {

         // checks if three of the points that surround the ball are in contact with a polygon
         if (path.contains(cLeft) && path.contains(leftUp) && path.contains(cUp)
          ||(path.contains(cRight) && path.contains(rightUp) && path.contains(cUp))
          ||(path.contains(cRight) && path.contains(rightDown) && path.contains(cDown))
          ||(path.contains(cLeft) && path.contains(leftDown) && path.contains(cDown))) {
             double tempX = super.getDeltaX() * (-1);
             super.setDeltaX(tempX);
             double tempY = super.getDeltaY() * (-1);
             super.setDeltaY(tempY);
         }

         // checks if two of the points that surround the ball are in contact with a polygon
         else if ((path.contains(cLeft) && path.contains(leftUp)) ||
                 (path.contains(cRight) && path.contains(rightUp)) ||
                 (path.contains(cRight) && path.contains(rightDown)) ||
                 (path.contains(cLeft) && path.contains(leftDown))) {

             double tempX = super.getDeltaX() * (-1);
             super.setDeltaX(tempX);
         }
         else if ((path.contains(cUp) && path.contains(leftUp)) ||
                 (path.contains(cUp) && path.contains(rightUp)) ||
                 (path.contains(cDown) && path.contains(rightDown)) ||
                 (path.contains(cDown) && path.contains(leftDown))) {

             double tempY = super.getDeltaY() * (-1);
             super.setDeltaY(tempY);
         }

         // checks if a point that surround the ball is in contact with a polygon
         else if ((path.contains(this.cLeft)) || (path.contains(this.cRight))) {
             double tempX = super.getDeltaX() * (-1);
             super.setDeltaX(tempX);
         }
         else if ((path.contains(this.cUp)) || (path.contains(this.cDown))) {
             double tempY = super.getDeltaY() * (-1);
             super.setDeltaY(tempY);
         }
         else if ((path.contains(this.leftDown)) || (path.contains(this.leftUp)) ||
                 (path.contains(this.rightDown)) || (path.contains(this.rightUp))) {

             double tempX = super.getDeltaX() * (-1);
             super.setDeltaX(tempX);
             double tempY = super.getDeltaY() * (-1);
             super.setDeltaY(tempY);
         }
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
        this.bounds.setRect(super.getX() - (this.diameter /2),super.getY() - (this.diameter /2), this.diameter, this.diameter);
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