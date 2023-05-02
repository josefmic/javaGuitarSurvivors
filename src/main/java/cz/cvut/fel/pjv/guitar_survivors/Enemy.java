package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.image.Image;

/**
 * Enemy class
 */
public class Enemy extends Object {

    private double health;
    private double lastX = 0;
    Enemy(Image image, Integer health) {
        super(image);
        this.health = health;
    }

    /**
     * @return health
     */
    public double getHealth() {
        return health;
    }

    /**
     * @param dmg
     * Deal demage
     */
    public void dealDamage(double dmg) {
        health -= dmg;
    }

    /**
     * @param x
     * Set last enemy X
     */
    public void setLastX(double x) {
        lastX = x;
    }

    /**
     * @return lastX
     */
    public double getLastX() {
        return lastX;
    }
}
