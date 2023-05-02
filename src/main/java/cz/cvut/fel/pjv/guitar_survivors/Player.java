package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.image.Image;

/**
 * Class player
 */
public class Player extends Object{
    private Integer health;
    private Integer speed;
    Player(Image image, Integer health, Integer speed) {
        super(image);
        this.health = health;
        this.speed = speed;
    }

    /**
     * @return health
     */
    public Integer getHealth() {
        return health;
    }

    /**
     * @param dmg
     * Deal damage to player
     */
    public void dealDamage(Integer dmg) {
        health -= dmg;
    }

    /**
     * @return speed
     */
    public Integer getSpeed() {
        return speed;
    }

}
