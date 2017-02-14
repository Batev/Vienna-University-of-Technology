package core;

import javax.swing.*;
import javax.swing.Timer;

import enemies.Enemy;
import players.Direction;
import players.Player;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.util.*;
import java.util.List;

public class Engine extends JPanel implements ActionListener, KeyListener {

    private final double fieldArea;

    // an array containing the colors to fill the polygons
    private Color[] colors;

    // the map saves the color of every polygon
    private Map<GeneralPath, Color> polygonColor;

    private Line2D startLine;
    private Line2D endLine;

    private Graphics2D g2d;

    private int startX;
    private int startY;

    private Timer timer;

    private boolean isPressed;

    private boolean isDrawn;

    private Player player;

    private Enemy enemy;

    // the temporary lines that are drawn be the player are going to be saved in this list
    private List<Line2D> currentLines;

    private GeneralPath polygon;

    // list with all the islands(polygons) that have been drawn by the player
    private List<GeneralPath> polygons;

    public Engine(Player player, Enemy enemy) {
        super();

        setBackground(Color.WHITE);

        setPreferredSize(new Dimension(Mondrian.WIDTH, Mondrian.HEIGHT));

        // making sure the Panel will stay with the given width and height
        setMaximumSize(new Dimension(Mondrian.WIDTH, Mondrian.HEIGHT));
        setMinimumSize(new Dimension(Mondrian.WIDTH, Mondrian.HEIGHT));

        setDoubleBuffered(true);

        // call step() 60 fps
        this.timer = new Timer(1000/60, this);
        this.timer.start();

        this.player = player;
        this.enemy = enemy;

        this.polygon =  new GeneralPath(GeneralPath.WIND_EVEN_ODD);

        this.fieldArea = (Mondrian.WIDTH * Mondrian.HEIGHT);

        // initialize the array colors with the possible colors
        this.colors = new Color[] {Color.RED, Color.YELLOW, Color.BLACK, Color.LIGHT_GRAY};

        this.isPressed = false;
        this.isDrawn = false;

        this.currentLines = new ArrayList<Line2D>();
        this.polygons = new LinkedList<GeneralPath>();
        this.polygonColor = new HashMap<GeneralPath, Color>();
    }

    @Override
    public void paintComponent(Graphics g) {

        this.g2d = (Graphics2D)g;

        super.paintComponent(this.g2d);

        this.drawCurrentLines();

        this.drawFinalizedPolygon();

        this.drawNewLine();

        this.enemy.drawObject(this.g2d);

        this.drawPolygons();

        this.player.drawObject(this.g2d);
        
        this.gameOver();
    }

    // draws all the lines that were made by the player when the player has left a safe location for the last time
    public void drawCurrentLines() {

        // no polygon is being made
        this.isDrawn = false;

        // initialize the lines
        this.startLine = new Line2D.Double(0,0,0,0);
        this.endLine = new Line2D.Double(0,0,0,0);

        if ((!this.currentLines.isEmpty())) {

            // adds the last line to the list and draws it, when the player reaches the bounds
            if (this.player.isOnBounds()) {
                // create a new line which will be given the value of the last line from the list with the current lines
                Line2D lastLine = this.currentLines.get(this.currentLines.size() - 1);
                // if the coordinates of the last line in the list meet the requirements of the if statement
                // then the last line would be added to the list
                if ( (lastLine.getX2() == (Mondrian.WIDTH - this.player.getVelocity()) ||
                      lastLine.getX2() == (this.player.getVelocity())) ||
                     (lastLine.getY2() == (Mondrian.HEIGHT - this.player.getVelocity()) ||
                      lastLine.getY2() == (this.player.getVelocity()))) {

                    // adds the last line to the list
                    Line2D line = new Line2D.Double(this.startX, this.startY, this.player.getX(), this.player.getY());
                    this.currentLines.add(line);
                }
            }

            // get the first and the last line from the current lines
            this.startLine = this.currentLines.get(0);
            this.endLine = this.currentLines.get(this.currentLines.size() - 1);

            // checks if the player has finished an island or a is on safe position
            if ((!this.player.isOnBounds())) {
                // iterates through the lines that have been drawn by the player by far
                for (Line2D line : this.currentLines) {
                    // setting the color to black before drawing a line
                    this.g2d.setColor(Color.BLACK);
                    // if the player is still moving around the field, a new line would be drawn
                    this.g2d.draw(new Line2D.Double(line.getX1(), line.getY1(), line.getX2(), line.getY2()));
                }
            }
        }
    }

    // draws the new line that was made by the player
    public void drawNewLine() {

        // if the player is moving
        if(this.isPressed) {

            // check if the player is not on an island
            if ((!this.player.isOnBounds())) {

                // save the current line in a list with the current lines and all the lines
                Line2D line = new Line2D.Double(this.startX, this.startY, this.player.getX(), this.player.getY());
                this.currentLines.add(line);

                if ((!this.currentLines.isEmpty())) {
                    // get the first and the last line from the current lines
                    this.startLine = this.currentLines.get(0);
                    this.endLine = this.currentLines.get(this.currentLines.size() - 1);
                }

                if ((!this.isOnIsland())) {
                    // draw a suitable line after the ball
                    switch (this.player.getDirection()) {
                        case UP:
                            this.g2d.draw(new Line2D.Double(this.startX, this.startY, (this.player.getX()), (this.player.getY()) + (this.player.getLength() / 2)));
                            break;
                        case DOWN:
                            this.g2d.draw(new Line2D.Double(this.startX, this.startY, this.player.getX(), (this.player.getY() - (this.player.getLength() / 2))));
                            break;
                        case RIGHT:
                            this.g2d.draw(new Line2D.Double(this.startX, this.startY, (this.player.getX() - (this.player.getLength() / 2)), this.player.getY()));
                            break;
                        case LEFT:
                            this.g2d.draw(new Line2D.Double(this.startX, this.startY, (this.player.getX() + (this.player.getLength() / 2)), this.player.getY()));
                            break;
                    }
                }
            }
        }

        else {
            // the last coordinates of the player before he starts drawing
            this.startX = this.player.getX();
            this.startY = this.player.getY();
        }
    }

    // draws all the polygons that have been drawn by the player so far
    public void drawPolygons() {
        //Collections.reverse(this.polygons); 

        for (GeneralPath polygon : this.polygons) {
             // every polygon has a random color
             if (this.polygonColor.containsKey(polygon)) {
             this.g2d.setColor(this.polygonColor.get(polygon));
             }
             else {
             this.g2d.setColor(Color.YELLOW);
             }
            this.g2d.draw(polygon);
            this.g2d.fill(polygon);
        }
        //Collections.reverse(this.polygons);
    }

    public void drawFinalizedPolygon() {

        /** 1.creates a current polygon but not the whole one */
        if (this.player.isOnBounds() && (!this.currentLines.isEmpty())) {

            for (Line2D line : this.currentLines) {
                // when the player is already on a safe position, a polygon is going to be created
                // the first line will be added to the polygon with moveTo
                if (line.equals(this.startLine)) {
                    this.polygon.moveTo(line.getX1(), line.getY1());
                    this.polygon.lineTo(line.getX2(), line.getY2());
                }
                // all other lines with lineTo
                else {
                    this.polygon.lineTo(line.getX1(), line.getY1());
                    this.polygon.lineTo(line.getX2(), line.getY2());
                }
            }
            // confirms that a current polygon has been created
            this.isDrawn = true;
            // clears the list of lines so that the new lines could be added
            this.currentLines = new ArrayList<Line2D>();
        }


        /** 2.finalizes the a polygon the right way */
        // checks if a current polygon is drawn and the player is located on a safe place (island)
        // and draws the polygon that does not include the enemy
        if (this.isDrawn && this.player.isOnBounds()) {

            // creates two polygons which will cover the whole area and check in which part is the enemy
            GeneralPath polygon1 = (GeneralPath) this.polygon.clone();
            GeneralPath polygon2 = (GeneralPath) this.polygon.clone();

            // when the starting point at coordinates x == 0 starts
            if (this.startLine.getX1() == 0) {

                if (this.endLine.getY2() == 0) {

                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.endLine.getY2());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(Mondrian.WIDTH, this.endLine.getY2());
                    polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                    polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getY2() == Mondrian.HEIGHT) {
                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.endLine.getY2());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(Mondrian.WIDTH, this.endLine.getY2());
                    polygon2.lineTo(Mondrian.WIDTH, 0);
                    polygon2.lineTo(this.startLine.getX1(), 0);
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == 0) {
                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // checks in if the end point is over the start point
                    if (this.startLine.getY1() > this.endLine.getY2()) {
                        // makes the second area
                        polygon2.lineTo(this.startLine.getX1(), 0);
                        polygon2.lineTo(Mondrian.WIDTH, 0);
                        polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                        polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    } else {
                        // makes the second area
                        polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                        polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                        polygon2.lineTo(Mondrian.WIDTH, 0);
                        polygon2.lineTo(this.startLine.getX1(), 0);
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    }
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == Mondrian.WIDTH) {
                    // makes the startLine area
                    polygon1.lineTo(this.endLine.getX2(), 0);
                    polygon1.lineTo(this.startLine.getX1(), 0);
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), Mondrian.HEIGHT);
                    polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                }
            } else if (this.startLine.getX1() == Mondrian.WIDTH) {

                if (this.endLine.getY2() == 0) {
                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.endLine.getY2());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(0, this.endLine.getY2());
                    polygon2.lineTo(0, Mondrian.HEIGHT);
                    polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getY2() == Mondrian.HEIGHT) {
                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.endLine.getY2());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(0, this.endLine.getY2());
                    polygon2.lineTo(0, 0);
                    polygon2.lineTo(this.startLine.getX1(), 0);
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == 0) {
                    // makes the first area
                    polygon1.lineTo(this.endLine.getX2(), 0);
                    polygon1.lineTo(this.startLine.getX1(), 0);
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), Mondrian.HEIGHT);
                    polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == Mondrian.WIDTH) {
                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // checks in if the end point is over the start point
                    if (this.startLine.getY1() > this.endLine.getY2()) {
                        // makes the second area
                        polygon2.lineTo(this.startLine.getX1(), 0);
                        polygon2.lineTo(0, 0);
                        polygon2.lineTo(0, Mondrian.HEIGHT);
                        polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    } else {
                        // makes the second area
                        polygon2.lineTo(this.startLine.getX1(), Mondrian.HEIGHT);
                        polygon2.lineTo(0, Mondrian.HEIGHT);
                        polygon2.lineTo(0, 0);
                        polygon2.lineTo(this.startLine.getX1(), 0);
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    }
                    this.polygonContainsEnemy(polygon1, polygon2);
                }

            } else if (this.startLine.getY1() == 0) {

                if (this.endLine.getY2() == 0) {

                    // makes the startLine area
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // checks in if the end point is over the start point
                    if (this.startLine.getX1() > this.endLine.getX2()) {
                        // makes the second area
                        polygon2.lineTo(0, this.startLine.getY1());
                        polygon2.lineTo(0, Mondrian.HEIGHT);
                        polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                        polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    } else {
                        // makes the second area
                        polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                        polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                        polygon2.lineTo(0, Mondrian.HEIGHT);
                        polygon2.lineTo(0, this.startLine.getY1());
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    }
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getY2() == Mondrian.HEIGHT) {

                    // makes the first area
                    polygon1.lineTo(0, this.endLine.getY2());
                    polygon1.lineTo(0, this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(Mondrian.WIDTH, this.endLine.getY2());
                    polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == 0) {

                    // makes the first area
                    polygon1.lineTo(this.startLine.getY1(), this.endLine.getX2());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), Mondrian.HEIGHT);
                    polygon2.lineTo(Mondrian.WIDTH, Mondrian.HEIGHT);
                    polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == Mondrian.WIDTH) {

                    // makes the first area
                    polygon1.lineTo(this.endLine.getX2(), this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), Mondrian.HEIGHT);
                    polygon2.lineTo(0, Mondrian.HEIGHT);
                    polygon2.lineTo(0, this.startLine.getY1());
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                }

            } else if (this.startLine.getY1() == Mondrian.HEIGHT) {

                if (this.endLine.getY2() == 0) {

                    // makes the first area
                    polygon1.lineTo(0, this.endLine.getY2());
                    polygon1.lineTo(0, this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(Mondrian.WIDTH, this.endLine.getY2());
                    polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getY2() == Mondrian.HEIGHT) {

                    // makes the first area
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // checks in if the end point is over the start point
                    if (this.startLine.getX1() > this.endLine.getX2()) {
                        // makes the second area
                        polygon2.lineTo(0, this.startLine.getY1());
                        polygon2.lineTo(0, 0);
                        polygon2.lineTo(Mondrian.WIDTH, 0);
                        polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    } else {
                        // makes the second area
                        polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                        polygon2.lineTo(Mondrian.WIDTH, 0);
                        polygon2.lineTo(0, 0);
                        polygon2.lineTo(0, this.startLine.getY1());
                        polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                        polygon2.closePath();
                    }
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == 0) {

                    // makes the first area
                    polygon1.lineTo(this.endLine.getX2(), this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), 0);
                    polygon2.lineTo(Mondrian.WIDTH, 0);
                    polygon2.lineTo(Mondrian.WIDTH, this.startLine.getY1());
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                } else if (this.endLine.getX2() == Mondrian.WIDTH) {

                    // makes the first area
                    polygon1.lineTo(this.endLine.getX2(), this.startLine.getY1());
                    polygon1.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon1.closePath();

                    // makes the second area
                    polygon2.lineTo(this.endLine.getX2(), 0);
                    polygon2.lineTo(0, 0);
                    polygon2.lineTo(0, this.startLine.getY1());
                    polygon2.lineTo(this.startLine.getX1(), this.startLine.getY1());
                    polygon2.closePath();

                    // checks in which polygon is not the enemy
                    this.polygonContainsEnemy(polygon1, polygon2);
                }
            }
            // adding the current polygon to the list
            this.polygons.add(this.polygon);

             // puts a polygon in the map with a random-generated color
             this.polygonColor.put(this.polygon,this.randomColor());

            // clear the current polygon
            this.polygon =  new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        }
    }

    // checks if a polygon contains the enemy (helper method to drawFinalizedPolygon())
    public void polygonContainsEnemy(GeneralPath polygon1, GeneralPath polygon2) {
        if ((!polygon1.contains(this.enemy.getX(), this.enemy.getY()))) {
            this.polygon = (GeneralPath)polygon1.clone();
        }
        else if ((!polygon2.contains(this.enemy.getX(), this.enemy.getY()))) {
            this.polygon = (GeneralPath)polygon2.clone();
        }
    }

    // checks if the enemy touches a current line
    public boolean enemyIntersectsLine() {
        for (Line2D elem : this.currentLines) {
            if(elem.intersects(this.enemy.getBounds())) {
                return true;
            }
        }
        return false;
    }

    public void bounce() {
        // checks if the enemy is in contact with a polygon that has been drawn
        for (GeneralPath elem : this.polygons) {
            this.enemy.intersect(elem);
        }
    }

    public boolean isOnIsland() {
        for (GeneralPath polygon : this.polygons) {
            if (polygon.contains(this.player.getX(), this.player.getY())) {
                return true;
            }
        }
        return false;
    }

    public void gameOver() {
        if (this.isDead()) {
            this.drawEndOptions(Color.RED, "You Lose!");
        }
        else if (this.youWon()) {

            this.drawEndOptions(Color.BLUE, "You Won!");
        }
    }

    // helper method to gameOver()
    private void drawEndOptions(Color color, String message) {
        timer.stop();
        this.g2d.setColor(color);
        this.g2d.setFont(new Font(Font.DIALOG, Font.BOLD, 18));
        this.g2d.drawString(message, (Mondrian.WIDTH/3), (Mondrian.HEIGHT/2));
        this.g2d.setFont(new Font(Font.DIALOG, Font.BOLD, 14));
        this.g2d.drawString("Press esc to exit.", (Mondrian.WIDTH / 3), (Mondrian.HEIGHT - (Mondrian.HEIGHT / 3)));
    }

    public boolean isDead() {
        // if the player is not on a safe position and gets touched by the ball the game is over
        if ((!this.player.isOnBounds()) && (!this.isOnIsland())) {
            if (this.player.intersects(this.enemy.getBounds()) || this.enemyIntersectsLine()) {
                return true;
            }
        }
        return false;
    }

    // checks whether you have filled more than 80% of the filled
    public boolean youWon() {

        double surface = this.calculateFilledSurface();

        return (surface > (this.fieldArea * (80.0/100.0)));
    }

    // helper method to youWon()
    private double calculateFilledSurface() {

        double result = 0;

        if ((!this.polygons.isEmpty())) {
            for (GeneralPath polygon : this.polygons) {
                result += (polygon.getBounds2D().getHeight() * polygon.getBounds2D().getWidth());
            }
        }

        result *= (80.0/100.0);
        return result;
    }

    // makes sure all the needed actions are performed
    @Override
    public void actionPerformed(ActionEvent e){
        this.update();
    }

    // updates all the elements on the field
    public void update() {

        this.player.update();

        this.enemy.update();

        this.bounce();

        // resets the position of the enemy, if it has stuck in a polygon
        this.resetEnemy();

        this.repaint();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        // if a key is not pressed
        if ((!this.isPressed)) {

            int keyCode = e.getKeyCode();
            // makes sure that two keys wont be pressed at the same time
            this.isPressed = true;

            // checks which key is pressed
            switch (keyCode) {
                case KeyEvent.VK_UP:
                    this.player.setDirection(Direction.UP);
                    break;
                case KeyEvent.VK_DOWN:
                    this.player.setDirection(Direction.DOWN);
                    break;
                case KeyEvent.VK_LEFT:
                    this.player.setDirection(Direction.LEFT);
                    break;
                case KeyEvent.VK_RIGHT:
                    this.player.setDirection(Direction.RIGHT);
                    break;
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // stays empty, because we do not need it
    }

    @Override
    public void keyReleased(KeyEvent e) {

        // makes sure that no key is pressed
        this.isPressed = false;

        int keyCode = e.getKeyCode();

        // changes the direction of the enemy on a random base
        this.changeEnemyDirection();

        // when a key is released the player stops moving
        switch (keyCode) {
            case KeyEvent.VK_UP:
                this.player.setDirection(null);
                break;
            case KeyEvent.VK_DOWN:
                this.player.setDirection(null);
                break;
            case KeyEvent.VK_LEFT:
                this.player.setDirection(null);
                break;
            case KeyEvent.VK_RIGHT:
                this.player.setDirection(null);
                break;
            case KeyEvent.VK_ESCAPE:
                System.exit(0);
                break;
        }
    }

    // changes the direction of the enemy on a random base
    public void changeEnemyDirection() {

        // not every time when a key is pressed, the enemy changes his direction
        Random randBool = new Random();
        // the changing of the direction of the enemy would be randomly decided
        boolean changeDirection = randBool.nextBoolean();
        // changes the direction of the enemy
        if (changeDirection) {
            this.enemy.changeDeltaX(-4, 4);
            this.enemy.changeDeltaY(-4, 4);
        }
    }

    // picks a random color from the array
    public Color randomColor() {

        int min = 0;
        int max = this.colors.length - 1;

        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randNum = rand.nextInt((max - min) + 1) + min;

        return this.colors[randNum];
    }

    // helper method for resetEnemy()
    private boolean EngineCenterFilled() {
        // checks if the player has already build a polygon that covers the center of the field
        for (GeneralPath polygon : this.polygons) {
            if (polygon.contains((Mondrian.WIDTH / 2), (Mondrian.HEIGHT / 2))) {
                return true;
            }
        }
        return false;
    }

    public void resetEnemy() {
    // if the enemy is on an island he would be transported to the center if it is not filled
    for (GeneralPath polygon : this.polygons) {
        if (polygon.contains(this.enemy.getX(),this.enemy.getY())) {
            if (!this.EngineCenterFilled()) {
                this.enemy.setX((Mondrian.WIDTH / 2));
                this.enemy.setY((Mondrian.HEIGHT / 2));
                break;
            }
            else {
                Random rand = new Random();
                this.enemy.setX((int)(Mondrian.WIDTH * rand.nextDouble()));
                this.enemy.setY((int)(Mondrian.HEIGHT * rand.nextDouble()));
                break;
                }
            }
        }
    }
}
