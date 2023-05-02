package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.image.Image;

/**
 * Class that handles collectible items (weapons)
 */
public class Collectible extends Object{
    private String type;

    Collectible(String type) {
        super(new Engine().getWeaponImage(type));
        this.type = type;
    }

    /**
     * @return type
     */
    public String getType() {
        return type;
    }
}
