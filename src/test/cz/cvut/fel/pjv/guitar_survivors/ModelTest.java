package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;

class ModelTest {
    private Model model;

    /**
     * Setup method
     */
    @BeforeEach
    public void setup() {
        model = new Model();
    }

    /**
     * @param number
     * Test if enemies will actually spawn further than 100px from the player
     */
    @ParameterizedTest
    @ValueSource(doubles = {100, 300, 500, 700})
    public void testSpawnEnemyDistance(double number) {
        double[] coords = model.spawnEnemyDistance(number, number);
        double distance = Math.sqrt(Math.pow((number - coords[0]), 2) + Math.pow((number - coords[1]), 2));
        Assertions.assertTrue(distance >= 100);
    }

    /**
     * Test enemy movement coordinates
     */
    @Test
    public void testMoveEnemyDistance() {
        double playerX = 0;
        double playerY = 0;
        double enemyX = 200;
        double enemyY = 0;

        model.setEnemySpeed(10);

        double[] coords = model.moveEnemyDistance(playerX, playerY, enemyX, enemyY);
        Assertions.assertEquals(-10, coords[0]);
    }


    /**
     * Test calculation of node distances
     */
    @Test
    public void testNodeDistance() {
        Rectangle shape1 = new Rectangle();
        Rectangle shape2 = new Rectangle();
        shape1.setLayoutX(0);
        shape1.setLayoutY(0);
        shape2.setLayoutX(200);
        shape2.setLayoutY(0);

        Assertions.assertEquals(200, model.nodeDistance(shape1, shape2));
    }


    /**
     * Test if there should be a level up
     */
    @Test
    public void testEnemyKilled() {
        model.setLevel(1);
        model.setKillCount(4);
        Assertions.assertTrue(model.enemyKilled());
    }


    /**
     * Test guitar movement
     */
    @Test
    public void testMoveGuitar() {
        double[] coords = model.moveGuitar(0,0, 0, 100);
        Assertions.assertEquals(-7.991469396917269, coords[0]);
        Assertions.assertEquals(99.68017063026194, coords[1]);
    }
}