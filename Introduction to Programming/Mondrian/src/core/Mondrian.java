package core;

import enemies.CircularEnemy;
import enemies.Enemy;
import enemies.RectangularEnemy;
import players.CircularPlayer;
import players.Player;
import players.RectangularPlayer;

import java.awt.*;
import javax.swing.*;

public class Mondrian extends JFrame {

    private core.Engine Engine;
    public static final int WIDTH = 200;
    public static final int HEIGHT = 200;

    public Mondrian() {

        Player playerRect = new RectangularPlayer((WIDTH/2), HEIGHT, 2, Color.BLUE, 8);
        Player playerCircle = new CircularPlayer((WIDTH/2), HEIGHT, 2, Color.BLUE, 8);

        Enemy enemyCircle = new CircularEnemy((WIDTH/2), (HEIGHT/2), Color.RED, 12);
        Enemy enemyRect = new RectangularEnemy((WIDTH/2), (HEIGHT/2), Color.RED, 12);

        this.Engine = new Engine(playerRect, enemyCircle);

        add(this.Engine);

        addKeyListener(this.Engine);

        setResizable(false);
        pack();
        setTitle("Core.Mondrian");

        setDefaultCloseOperation(EXIT_ON_CLOSE);
   }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                Mondrian window = new Mondrian();
                //Set the default position of the frame in the center of the monitor
                window.setLocationRelativeTo(null);
                window.setVisible(true);
                //Enabling the frame to be focusable
                window.setFocusable(true);
            }
        });
    }
}