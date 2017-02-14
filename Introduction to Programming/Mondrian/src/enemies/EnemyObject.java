package enemies;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.Random;

public abstract class EnemyObject implements Enemy {

    // enemy's properties
    private int x,y;
    private Color color;


    // enemy's x,y movement change (speed)
    private double deltaX;
    private double deltaY;

    // setting the enemy's properties
    public EnemyObject(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;

        this.deltaX = 3.0;
        this.deltaY = -3.0;
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int newX) {
        this.x = newX;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int newY) {
        this.y = newY;
    }

    public double getDeltaX() {
        return this.deltaX;
    }

    public void setDeltaX(double deltaX) {
        this.deltaX = deltaX;
    }

    public double getDeltaY() {
        return this.deltaY;
    }

    public void setDeltaY(double deltaY) {
        this.deltaY = deltaY;
    }

    public Color getColor() {
        return this.color;
    }

    @Override
    // changes the direction and the speed of the enemy on a random base
    public void changeDeltaX(double min, double max) {

        Random rand = new Random();

        // if deltaX = 0 or the deltaY = 0, then moves the enemy horizontal or vertical
        double randNum = min + (max-min) * rand.nextDouble();
        if (randNum > 2.0 || randNum < -2.0) {
            this.deltaX = randNum;
        }
        else {
            this.changeDeltaX(min,max);
        }
    }

    @Override
    public void changeDeltaY(double min, double max) {

        Random rand = new Random();

        // if deltaX = 0 or the deltaY = 0, then moves the enemy horizontal or vertical
        double randNum = min + (max-min) * rand.nextDouble();
        if (randNum > 2.0 || randNum < -2.0) {
            this.deltaY = randNum;
        }
        else {
            this.changeDeltaY(min, max);
        }
    }

    @Override
    public abstract Rectangle2D getBounds();

    @Override
    public abstract void update();

    @Override
    public abstract void intersect(GeneralPath path);

    @Override
    public abstract void drawObject(Graphics2D g2d);
}
