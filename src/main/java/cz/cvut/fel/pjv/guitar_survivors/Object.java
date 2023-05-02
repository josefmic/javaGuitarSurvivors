package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


/**
 * Class extending ImageView that is the base for almost all object in pane
 */
public class Object extends ImageView {
    private String fightClass;

    Object(Image image) {
        super(image);
    }

}
