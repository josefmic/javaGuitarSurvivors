package cz.cvut.fel.pjv.guitar_survivors;

/**
 * Weapon class that is the base for all weapons
 */
public class Weapon extends Object{
    private String imgUrl;
    private boolean active;
    private String type;
    private Controller controller;
    private Engine engine;
    private boolean added = false;
    private boolean max = false;
    private int tick = 0;

    Weapon(String type) {
        super(new Engine().getWeaponImage(type));
        this.type = type;
        active = false;
    }

    /**
     * @return true if weapon is active
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * set weapon as active
     */
    public void setActive() {
        active = true;
    }

    /**
     * @return type of weapon
     */
    public String getType() {
        return type;
    }

    /**
     * @return tick of exploding note
     */
    public boolean tick() {
        if (tick == 120) {
            return true;
        }
        tick++;
        return false;
    }

    /**
     * Set weapon added to screen
     */
    public void setAdded() {
        added = true;
    }

    /**
     * @return info if weapon has been added to screen
     */
    public boolean getAdded() {
        return added;
    }
}
