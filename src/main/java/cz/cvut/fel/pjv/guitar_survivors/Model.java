package cz.cvut.fel.pjv.guitar_survivors;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Shape;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Model {
    private int frames;
    private double resX;
    private double resY;
    private int enemyHealth;
    private int killCount;
    private int level;
    private BigDecimal spawnRate;
    private int enemySpeed;

    private boolean leveledUp;

    /**
     * @param num
     */
    public void setResX(double num) {
        resX = num;
    }

    /**
     * @return resX
     */
    public double getResX() {
        return resX;
    }

    /**
     * @param num
     */
    public void setResY(double num) {
        resY = num;
    }

    /**
     * @return resY
     */
    public double getResY() {
        return resY;
    }

    /**
     * @return enemyHealth
     */
    public int getEnemyHealth() {
        return enemyHealth;
    }

    /**
     * @param num
     */
    public void setEnemyHealth(int num) {
        enemyHealth = num;
    }

    /**
     * @return killCount
     */
    public int getKillCount() {
        return killCount;
    }

    /**
     * @param num
     */
    public void setKillCount(int num) {
        killCount = num;
    }

    /**
     * @return Current level
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param num
     */
    public void setLevel(int num) {
        level = num;
    }

    /**
     * @return Spawn rate
     */
    public BigDecimal getSpawnRate() {
        return spawnRate;
    }

    /**
     * @param num
     */
    public void setSpawnRate(BigDecimal num) {
        spawnRate = num;
    }

    /**
     * @param num
     */
    public void setEnemySpeed(int num) {
        enemySpeed = num;
    }

    /**
     * @param frames
     */
    public void setFrames(int frames) {
        this.frames = frames;
    }

    /**
     * @param x
     * @param y
     * @return Random coordinates so that an enemy doesn't spawn too close to the player
     */
    public double[] spawnEnemyDistance(double x, double y) {
        double[] move = new double[2];
        if (resX > 0 && resY > 0) {
            while (true) {
                double randX = ThreadLocalRandom.current().nextDouble(0, resX);
                double randY = ThreadLocalRandom.current().nextDouble(0, resY);

                double distance = Math.sqrt(Math.pow((randX - x), 2) + Math.pow((randY - y), 2));

                if (distance >= 100) {
                    move[0] = randX;
                    move[1] = randY;
                    return move;
                }
            }
        }
        return move;
    }

    /**
     * @param px
     * @param py
     * @param ex
     * @param ey
     * @return Coordinates so that the enemy moves towards the player every frame
     */
    public double[] moveEnemyDistance(double px, double py, double ex, double ey) {
        double[] move = new double[2];
        double x = px - ex;
        double y = py - ey;
        double diagonal = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        double alpha = Math.acos((x / diagonal));
        double beta = Math.acos((y / diagonal));

        move[0] = enemySpeed * Math.cos(alpha);
        move[1] = enemySpeed * Math.cos(beta);

        return move;
    }

    /**
     * @param a
     * @param b
     * @return Distance between two given nodes
     */
    public double nodeDistance(Node a, Node b) {
        return Math.sqrt(Math.pow((a.getLayoutX() - b.getLayoutX()), 2) + Math.pow((a.getLayoutY() - b.getLayoutY()), 2));
    }

    /**
     * @return True if leveled up, False if not
     */
    public boolean enemyKilled() {
        //Add to kill count and conditions to level up
        killCount++;
        if ((killCount % 5) == 0 && killCount != 0 && level <= 6) {
            level++;
            return true;
        } else if ((killCount % 10) == 0 && killCount != 0 && level <= 12) {
            level++;
            return true;
        } else if ((killCount % 20) == 0 && killCount != 0 && level <= 24) {
            level++;
            return true;
        } else if ((killCount % 40) == 0 && killCount != 0) {
            level++;
            return true;
        }
        return false;
    }

    /**
     * @param px
     * @param py
     * @param gx
     * @param gy
     * @return Coordinates that move the guitar around the player
     */
    public double[] moveGuitar(double px, double py, double gx, double gy) {
        double[] move = new double[2];

        //Rotational matrix to rotate guitar around player
        move[0] = px + (gx - px) * Math.cos(0.08) - (gy - py) * Math.sin(0.08);
        move[1] = py + (gx - px) * Math.sin(0.08) + (gy - py) * Math.cos(0.08);

        return move;
    }

    /**
     * @param player
     * @param bools
     * @return Coordinates for the player movement
     */
    public HashMap getPlayerMovement(Player player, HashMap<String, Boolean> bools) {
        HashMap<String, Double> coords = new HashMap<>();

        Integer playerSpeed = player.getSpeed();
        double playerX = player.getLayoutX();
        double playerY = player.getLayoutY();

        double setX = playerX;
        double setY = playerY;

        //Make sure player isn't going faster when going diagonally
        int keysPressed = 0;
        for (Boolean key : bools.values()) {
            keysPressed++;
        }

        double realSpeed = (double)playerSpeed;
        if (keysPressed >= 3) {
            realSpeed = Math.sqrt(Math.pow(playerSpeed, 2)/2);
        }

        //Default player movement
        if (bools.get("pressed")) {
            if (bools.get("a") && playerX >= 0) {
                setX = playerX - realSpeed;
            }
            if (bools.get("d") && playerX <= resX - 46) {
                setX = playerX + realSpeed;
            }
            if (bools.get("w") && playerY >= 0) {
                setY = playerY - realSpeed;
            }
            if (bools.get("s") && playerY <= resY - 64) {
                setY = playerY + realSpeed;
            }
        }

        //Put the calculated coords into a hashmap
        coords.put("x", setX);
        coords.put("y", setY);

        return coords;
    }

    /**
     * @param pane
     * @param player
     * @return Coordinates for enemy movement
     */
    public HashMap getEnemyMovement(Pane pane, Player player) {

        HashMap<Integer, HashMap<String, Double>> coords = new HashMap<>();
        int ind = 0;

        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy) {

                HashMap<String, Double> singleCoords = new HashMap<>();

                //Default enemy movement
                double[] move = moveEnemyDistance(
                        player.getLayoutX(),
                        player.getLayoutY(),
                        enemy.getLayoutX(),
                        enemy.getLayoutY()
                );

                double newX = enemy.getLayoutX() + move[0];
                double newY = enemy.getLayoutY() + move[1];

                //Collision with player
                Double playerD = nodeDistance(player, enemy);

                //Deal damage to player upon contact with enemy
                if (playerD <= 20) {
                    player.dealDamage(1);
                    //And move enemy back away from player
                    newX = enemy.getLayoutX() - move[0] * 5;
                    newY = enemy.getLayoutY() - move[1] * 5;
                }

                //Collision with weapons
                for (Node weapon : pane.getChildren()) {
                    if (weapon instanceof Weapon) {
                        if (((Weapon) weapon).getType() == "guitar") {
                            //Collision with guitar
                            Double guitarD = nodeDistance(weapon, enemy);

                            //Deal damage upon contact with guitar
                            if (guitarD <= 50) {
                                ((Enemy) enemy).dealDamage(1);
                                //And knock enemy away
                                newX = enemy.getLayoutX() - move[0] * 5;
                                newY = enemy.getLayoutY() - move[1] * 5;
                            }
                        }
                    }
                }

                //Collision with bass wave or note wave
                for (Node wave : pane.getChildren()) {
                    if (wave instanceof Shape) {
                        double[] moveBass = moveEnemyDistance(
                                wave.getLayoutX(),
                                wave.getLayoutY(),
                                enemy.getLayoutX(),
                                enemy.getLayoutY()
                        );
                        double waveD = nodeDistance(wave, enemy);
                        if (wave.getId() == "bass") {
                            if ((waveD - 10 * wave.getScaleX()) <= 10 && (waveD - 10 * wave.getScaleX()) > 0) {
                                newX = enemy.getLayoutX() - moveBass[0] * 2;
                                newY = enemy.getLayoutY() - moveBass[1] * 2;
                                ((Enemy) enemy).dealDamage(1);
                            }
                        } else {
                            if ((waveD - 10 * wave.getScaleX()) <= 30 && (waveD - 10 * wave.getScaleX()) > 0) {
                                newX = enemy.getLayoutX() - moveBass[0] * 5;
                                newY = enemy.getLayoutY() - moveBass[1] * 5;
                                ((Enemy) enemy).dealDamage(2);
                            }
                        }
                    }
                }

                //Collision with drumsticks
                for (Node drumstick : pane.getChildren()) {
                    if (drumstick instanceof Projectile) {
                        Double drumstickD = nodeDistance(drumstick, enemy);

                        //Deal damage upon contact with guitar
                        if (drumstickD <= 20) {
                            ((Enemy) enemy).dealDamage(0.3);
                            //And knock enemy away
                            newX = enemy.getLayoutX() - move[0] * 5;
                            newY = enemy.getLayoutY() - move[1] * 5;
                        }
                    }
                }

                //Put the coordinates values into a hashmap
                singleCoords.put("x", newX);
                singleCoords.put("y", newY);

                coords.put(ind, singleCoords);
                ind++;
            }
        }

        return coords;
    }

    /**
     * @param pane
     * @return HashMap of enemies that should be removed from pane
     */
    public HashMap getKilledEnemies(Pane pane) {

        HashMap<String, Enemy> returnMap = new HashMap<>();

        leveledUp = false;

        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy) {
                //Detect if enemy has been killed
                if (((Enemy) enemy).getHealth() <= 0) {
                    returnMap.put("killed", (Enemy)enemy);
                    leveledUp = enemyKilled();
                }

            }
        }

        //Make game harder upon level up
        if (leveledUp) {
            if (spawnRate.compareTo(new BigDecimal("1")) > 0) {
                spawnRate = spawnRate.subtract(new BigDecimal("0.5"));
                enemyHealth += 3;
            } else if (spawnRate.compareTo(new BigDecimal("0.5")) > 0) {
                spawnRate = spawnRate.subtract(new BigDecimal("0.1"));
            } else if (spawnRate.compareTo(new BigDecimal("0.15")) > 0) {
                spawnRate = spawnRate.subtract(new BigDecimal("0.05"));
            }
        }

        return returnMap;
    }

    /**
     * @return Frames that reset every 10 seconds
     */
    public int spawnTimer() {
        if (frames <= 10*60) {
            frames++;
        } else {
            frames = 1;
        }
        return frames;
    }

    /**
     * @param player
     * @param bools
     * @return Get player movement distance so that the weapons move with him
     */
    public HashMap getPlayerMovementDistance(Player player, HashMap<String, Boolean> bools) {
        HashMap<String, Double> coords = new HashMap<>();

        Integer playerSpeed = player.getSpeed();
        double playerX = player.getLayoutX();
        double playerY = player.getLayoutY();

        double setX = 0;
        double setY = 0;

        //Make sure player isn't going faster when going diagonally
        int keysPressed = 0;
        for (Boolean key : bools.values()) {
            keysPressed++;
        }

        double realSpeed = (double)playerSpeed;
        if (keysPressed >= 3) {
            realSpeed = Math.sqrt(Math.pow(playerSpeed, 2)/2);
        }

        //Default player movement
        if (bools.get("pressed")) {
            if (bools.get("a") && playerX >= 0) {
                setX = -realSpeed;
            }
            if (bools.get("d") && playerX <= resX - 64) {
                setX = realSpeed;
            }
            if (bools.get("w") && playerY >= 0) {
                setY = -realSpeed;
            }
            if (bools.get("s") && playerY <= resY - 64) {
                setY = realSpeed;
            }
        }

        //Put the calculated coords into a hashmap
        coords.put("x", setX);
        coords.put("y", setY);

        return coords;
    }

    /**
     * @param pane
     * @param player
     * @return Coordinates for weapon movement
     */
    public HashMap getWeaponMovement(Pane pane, Player player) {
        HashMap<String, HashMap<String, Double>> coords = new HashMap<>();
        for (Node weapon : pane.getChildren()) {
            if (weapon instanceof Weapon) {
                if (((Weapon) weapon).getActive()) {
                    //Move guitar in a circular motion
                    if (((Weapon) weapon).getType() == "guitar") {
                        double[] move = moveGuitar(
                            player.getLayoutX(),
                            player.getLayoutY(),
                            weapon.getLayoutX(),
                            weapon.getLayoutY()
                        );
                        HashMap<String, Double> guitarCoords = new HashMap<>();
                        guitarCoords.put("x", move[0]);
                        guitarCoords.put("y", move[1]);

                        coords.put("guitar", guitarCoords);
                    }

                    //Move bass with player
                    if (((Weapon) weapon).getType() == "bass") {
                        HashMap<String, Double> bassCoords = new HashMap<>();
                        bassCoords.put("x", player.getLayoutX());
                        bassCoords.put("y", player.getLayoutY()+10);

                        coords.put("bass", bassCoords);
                    }
                    //Move drums with player
                    if (((Weapon) weapon).getType() == "drums") {
                        HashMap<String, Double> drumCoords = new HashMap<>();
                        drumCoords.put("x", player.getLayoutX()-10);
                        drumCoords.put("y", player.getLayoutY()-35);

                        coords.put("drums", drumCoords);
                    }

                    //Move piano with player
                    if (((Weapon) weapon).getType() == "piano") {
                        HashMap<String, Double> pianoCoords = new HashMap<>();
                        pianoCoords.put("x", player.getLayoutX()-40);
                        pianoCoords.put("y", player.getLayoutY()+15);

                        coords.put("piano", pianoCoords);
                    }
                }
            }
        }

        return coords;
    }

    /**
     * @param pane
     * @param player
     * @return Angles for drumstick firing at enemies
     */
    public double[] getDrumstickAngles(Pane pane, Player player) {
        Random rand = new Random();
        double[] angles = {0,0};

        double lowestDistance = Double.POSITIVE_INFINITY;

        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy) {
                double diagonal = nodeDistance(player, enemy);
                if (diagonal <= lowestDistance) {
                    lowestDistance = diagonal;

                    double[] move = new double[2];

                    double x = player.getLayoutX() - enemy.getLayoutX();
                    double y = player.getLayoutY() - enemy.getLayoutY();

                    double alpha = Math.acos((x / diagonal));
                    double beta = Math.acos((y / diagonal));

                    angles[0] = alpha;
                    angles[1] = beta;
                }
            }
        }

        return angles;
    }

    /**
     * @param drumstick
     * @return Coordinates for drumsticks flying
     */
    public double[] moveDrumstick(Projectile drumstick) {
        double[] move = new double[2];

        move[0] = drumstick.getLayoutX() - (20 * Math.cos(drumstick.getAngles()[0]));
        move[1] = drumstick.getLayoutY() - (20 * Math.cos(drumstick.getAngles()[1]));

        return move;
    }

    /**
     * @param player
     * @return random coordinates for weapon spawn
     */
    public HashMap getWeaponSpawn(Player player) {
        HashMap<String, double[]> coords = new HashMap<>();
        Engine engine = new Engine();
        double[] coordinate = spawnEnemyDistance(player.getLayoutX(), player.getLayoutY());
        Map<String, Boolean> weapons = engine.getActiveWeapons();

        for (Map.Entry<String, Boolean> entry : weapons.entrySet()) {
            if (!entry.getValue()) {
                double[] singleCoord = new double[2];
                singleCoord[0] = coordinate[0];
                singleCoord[1] = coordinate[1];
                coords.put(entry.getKey(), singleCoord);
                break;
            }
        }
        return coords;
    }

    /**
     * @return info if there has been levelUp this frame
     */
    public boolean getLeveledUp() {
        return leveledUp;
    }

    /**
     * Set leveled up to false
     */
    public void setLeveledUp() {
        leveledUp = false;
    }

    /**
     * @param pane
     * @param player
     * @return true if weapon has been collected, false otherwise
     */
    public boolean collectWeapon(Pane pane, Player player) {
        for (Node weapon : pane.getChildren()) {
            if (weapon instanceof Collectible) {
                if (nodeDistance(player, weapon) <= 50) {
                    Engine engine = new Engine();
                    engine.setActiveWeapons(((Collectible) weapon).getType());
                    return true;
                }
            }
        }
        return false;
    }
}
