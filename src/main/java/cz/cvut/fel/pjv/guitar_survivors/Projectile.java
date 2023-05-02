package cz.cvut.fel.pjv.guitar_survivors;

/**
 * Class projectile that is the base for drumsticks
 */
public class Projectile extends Object{
    private String imgUrl;
    private String type;

    private double[] angles;

    Projectile(String type, double[] angles) {
        super(new Engine().getWeaponImage(type));
        this.type = type;
        this.angles = angles;
    }

    /**
     * @return type of projectile
     */
    public String getType() {
        return type;
    }

    /**
     * @return angles of fyling drumstick
     */
    public double[] getAngles() {
        return angles;
    }
}
