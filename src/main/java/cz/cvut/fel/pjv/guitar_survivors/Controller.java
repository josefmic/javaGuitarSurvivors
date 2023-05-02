package cz.cvut.fel.pjv.guitar_survivors;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Main javafx application class
 */
public class Controller extends Application {
    //Java FX node structure
    private Pane pane;
    private Button startGame;

    //Player movements booleans
    private final BooleanProperty aPressed = new SimpleBooleanProperty();
    private final BooleanProperty dPressed = new SimpleBooleanProperty();
    private final BooleanProperty wPressed = new SimpleBooleanProperty();
    private final BooleanProperty sPressed = new SimpleBooleanProperty();
    private String lastPressed = "a";
    private final BooleanBinding keyPressed = wPressed.or(aPressed).or(sPressed).or(dPressed);

    //Player object and config
    private Player player;
    private Rectangle healthbar;
    private Text displayLevel;
    private boolean pickedUp = false;

    //Engine
    private Engine engine;
    private long gameTime;
    private long frameTime;
    private int fps;


    //Weapons
    private Weapon guitar;
    private Weapon bass;
    private Weapon drums;
    private Weapon piano;

    //Model
    private Model model;

    //Logger boolean
    private boolean logger;

    /**
     * @param stage javafx stage
     * @throws IOException
     * Application start function
     */
    @Override
    public void start(Stage stage) throws IOException {
        //HERE TURN ON/OFF LOGGING
        logger = true;

        //Set game
        BorderPane border = new BorderPane();
        pane = new Pane();
        engine = new Engine();
        model = new Model();

        //config
        model.setResX(1920);
        model.setResY(1080);

        //Set important model vars
        model.setEnemyHealth(5);
        model.setKillCount(0);
        model.setLevel(1);
        model.setSpawnRate(new BigDecimal("4"));

        //Add bg image
        pane.getChildren().add(new ImageView(new Image(getClass().getResourceAsStream("images/start.png"))));

        //Setting layout
        border.setCenter(pane);
        pane.setStyle("-fx-background-color: white;");
        startGame = new Button("START GAME");
        pane.getChildren().add(startGame);

        //Setting Scene
        Scene scene = new Scene(border, model.getResX(), model.getResY());

        //Adding button to the center
        startGame.setLayoutX(model.getResX()/2.0-100);
        startGame.setLayoutY(model.getResY()/2.0);

        //Calling function to create button to start the game
        startGameButton();

        //Player config
        Map playerData = engine.getPlayerData();
        player = new Player(
                new Image(getClass().getResourceAsStream("images/player_sprites/sprite (1).png")),
                (Integer)Integer.parseInt((String)playerData.get("health")),
                (Integer)Integer.parseInt((String)playerData.get("speed"))
            );

        //Set player starting position
        player.setLayoutX(model.getResX()/2);
        player.setLayoutY(model.getResY()/2);

        //Player movement
        playerMovementSetup(scene);

        //Healthbar
        healthbar = new Rectangle();
        healthbar.setFill(Color.rgb(136,8,8));
        healthbar.setHeight(50);
        healthbar.setWidth(30*player.getHealth());
        healthbar.setLayoutX(50);
        healthbar.setLayoutY(50);

        //Display level
        displayLevel = new Text("Level: " + model.getLevel());
        displayLevel.setLayoutX(model.getResX()-200);
        displayLevel.setLayoutY(80);
        displayLevel.setStyle("-fx-font: 40 arial; -fx-font-weight: bold;");

        //Move enemies
        model.setEnemySpeed(player.getSpeed() - 4);

        //Weapons
        guitar = new Weapon("guitar");
        bass = new Weapon("bass");
        drums = new Weapon("drums");
        piano = new Weapon("piano");

        //Call spawn weapons
        spawnWeapons();

        //Setting icon, title and scene
        stage.getIcons().add(new Image(getClass().getResourceAsStream("images/icon.png")));
        stage.setTitle("Guitar survivors");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Game controller method
     */
    private void gameController() {
        //Set game frame to 0
        model.setFrames(0);
        //Start game animation
        AnimationTimer gameControllerTimer = new AnimationTimer() {
            @Override
            public void handle(long l) {
                //Detect if a collectible weapon has been picked up
                if (pickedUp) {
                    if (!guitar.getActive()) {
                        guitar.setLayoutY(player.getLayoutY() + 200);
                        guitar.setLayoutX(player.getLayoutX());
                    }
                    setActiveWeapons();
                    despawnWeapons();
                    pickedUp = false;
                }
                //Activate guitar
                if (guitar.getActive() && !guitar.getAdded()) {
                    pane.getChildren().add(guitar);
                    guitar.setAdded();
                }
                //Activate bass
                if (bass.getActive() && !bass.getAdded()) {
                    pane.getChildren().add(bass);
                    bass.setAdded();
                }
                //Activate drums
                if (drums.getActive() && !drums.getAdded()) {
                    pane.getChildren().add(drums);
                    drums.setAdded();
                }
                //Activate piano
                if (piano.getActive() && !piano.getAdded()) {
                    pane.getChildren().add(piano);
                    piano.setAdded();
                }

                HashMap<String, HashMap<String, Double>> movement = new HashMap<>();

                int frames = model.spawnTimer();

                //Call logger
                if (logger && frames % 60 == 0) {
                    logger();
                }

                movement.put("player", model.getPlayerMovement(player, getPressed())); //Get player movement coordinates
                movement.put("playerDistance", model.getPlayerMovementDistance(player, getPressed())); //Get the direction and amount the player is going
                movement.put("enemy", model.getEnemyMovement(pane, player)); //Get coordinates for all enemies
                movement.put("killedEnemies", model.getKilledEnemies(pane)); //Get all enemies that should be dead
                movement.put("weapons", model.getWeaponMovement(pane, player)); //Get coordinates for weapons to move

                //Call redraw function that handles all redrawing in the scene
                redraw(movement, frames);

                //End game if player has been killed
                if (player.getHealth() <= 0) {
                    this.stop();
                    endGame();
                }
            }
        };
        gameControllerTimer.start();
    }

    /**
     * @param movement
     * @param frames
     * Method that redraws the scene every frame
     */
    public void redraw(HashMap movement, int frames) {
        //Move player
        player.setLayoutX((Double) ((HashMap) movement.get("player")).get("x"));
        player.setLayoutY((Double) ((HashMap) movement.get("player")).get("y"));

        //Move enemies
        int ind = 0;
        Image[] enemySprites = engine.getEnemySprites();
        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy) {
                //Only the movement
                double xMove = (Double) ((HashMap) ((HashMap) movement.get("enemy")).get(ind)).get("x");
                enemy.setLayoutX(xMove);
                enemy.setLayoutY((Double) ((HashMap) ((HashMap) movement.get("enemy")).get(ind)).get("y"));
                //Animation of enemies
                if (xMove < ((Enemy) enemy).getLastX()) {
                    if (frames % 20 == 0) {
                        ((Enemy)enemy).setImage(enemySprites[0]);
                    } else if (frames % 10 == 0) {
                        ((Enemy)enemy).setImage(enemySprites[1]);
                    }
                } else {
                    if (frames % 20 == 0) {
                        ((Enemy)enemy).setImage(enemySprites[2]);
                    } else if (frames % 10 == 0) {
                        ((Enemy)enemy).setImage(enemySprites[3]);
                    }
                }
                ((Enemy) enemy).setLastX(xMove);
                ind++;
            }
        }

        //Remove killed enemies from the scene
        for (Enemy enemy : ((HashMap<String, Enemy>) movement.get("killedEnemies")).values()) {
            pane.getChildren().remove(enemy);
        }

        //Spawn enemies
        float spawns = model.getSpawnRate().multiply(new BigDecimal("60")).floatValue();
        if (frames % spawns == 0) {
            spawnEnemy();
        }

        //Move weapons
        for (Node weapon : pane.getChildren()) {
            if (weapon instanceof Weapon) {
                if (((Weapon) weapon).getActive()) {
                    weapon.setLayoutX((Double)((HashMap)((HashMap) movement.get("weapons")).get(((Weapon) weapon).getType())).get("x")
                    + (Double)((HashMap)movement.get("playerDistance")).get("x"));
                    weapon.setLayoutY((Double)((HashMap)((HashMap) movement.get("weapons")).get(((Weapon) weapon).getType())).get("y")
                    + (Double)((HashMap)movement.get("playerDistance")).get("y"));
                    if (((Weapon) weapon).getType() == "guitar") {
                        weapon.setRotate(weapon.getRotate()+Math.toDegrees(0.08));
                    }
                }
            }
        }

        //Spawn and despawn drumsticks
        if (frames % 60 == 0 && drums.getActive()) {
            spawnDrumsticks();
        }
        //Move drumsticks
        if (drums.getActive()) {
            moveDrumsticks();
        }

        //Spawn bass rings
        if (bass.getActive() && frames % 300 == 0) {
            spawnBassRings();
        }
        //Move bass and note rings
        if (bass.getActive() || piano.getActive()) {
            explodeBassAndNote();
        }

        //Handle notes (mines)
        if (frames % (3 * 60) == 0 && piano.getActive()) {
            spawnNote();
        }
        //Spawn note explosion and despawn the note
        if (piano.getActive()) {
            spawnNoteExplosion();
        }

        //Spawn collectible weapons
        if (model.getLeveledUp() && model.getLevel() % 3 == 0) {
            spawnCollectible();
        }
        //Set leveled up to false
        model.setLeveledUp();

        //Collect weapon
        pickedUp = model.collectWeapon(pane, player);

        //Lower healthbar
        healthbar.setWidth(30*player.getHealth());

        //Level up on display
        displayLevel.setText("Level: " + model.getLevel());

        //Player animation
        Image[] playerSprites = engine.getPlayerSprites();
        if (keyPressed.get()) {
            if (aPressed.get()) {
                if (frames % 16 == 0) {
                    player.setImage(playerSprites[4]);
                } else if (frames % 8 == 0) {
                    player.setImage(playerSprites[5]);
                }
                lastPressed = "a";
            } else if (dPressed.get()) {
                if (frames % 16 == 0) {
                    player.setImage(playerSprites[10]);
                } else if (frames % 8 == 0) {
                    player.setImage(playerSprites[11]);
                }
                lastPressed = "d";
            } else if (wPressed.get()) {
                if (frames % 16 == 0) {
                    player.setImage(playerSprites[7]);
                } else if (frames % 8 == 0) {
                    player.setImage(playerSprites[8]);
                }
                lastPressed = "w";
            } else if (sPressed.get()) {
                if (frames % 16 == 0) {
                    player.setImage(playerSprites[1]);
                } else if (frames % 8 == 0) {
                    player.setImage(playerSprites[2]);
                }
                lastPressed = "s";
            }
        } else {
            switch(lastPressed) {
                case("a"):
                    player.imageProperty().set(playerSprites[3]);
                    break;
                case("d"):
                    player.imageProperty().set(playerSprites[9]);
                    break;
                case("w"):
                    player.imageProperty().set(playerSprites[6]);
                    break;
                case("s"):
                    player.imageProperty().set(playerSprites[0]);
                    break;
            }
        }
    }

    /**
     * @return
     * Put key inputs into a hashmap, so that the movement can be handled in Model
     */
    private HashMap getPressed() {
        HashMap<String, Boolean> pressed = new HashMap<>();

        pressed.put("a", aPressed.get());
        pressed.put("d", dPressed.get());
        pressed.put("w", wPressed.get());
        pressed.put("s", sPressed.get());
        pressed.put("pressed", keyPressed.get());

        return pressed;
    }

    /**
     * Button to start the game
     */
    private void startGameButton() {
        //Button styling
        startGame.setStyle("-fx-font-weight: bold; -fx-font-size: 20px;-fx-background-color: linear-gradient(#620712, #9c0303);-fx-color: gray;-fx-border-radius:  7px; -fx-cursor: pointer; -fx-alignment: CENTER;");
        startGame.setMinWidth(170);
        startGame.setMinHeight(50);
        startGame.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //When clicked, add all necessary items into pane
                pane.getChildren().clear();
                pane.getChildren().add(new ImageView(new Image(getClass().getResourceAsStream("images/level.png"))));
                pane.getChildren().add(player);
                pane.getChildren().add(healthbar);
                pane.getChildren().add(displayLevel);
                //Start game timer
                gameTime = System.nanoTime();
                frameTime = System.nanoTime();
                fps = 0;
                //Call the main game controller
                gameController();
            }
        });
    }

    /**
     * @param scene
     * Listen to key presses and releases on w, s, a, d
     */
    private void playerMovementSetup(Scene scene) {
        //Listens to key pressed
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case A:
                    aPressed.set(true);
                    break;
                case D:
                    dPressed.set(true);
                    break;
                case W:
                    wPressed.set(true);
                    break;
                case S:
                    sPressed.set(true);
                    break;
            }
        });

        //Listens to key released
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case A:
                    aPressed.set(false);
                    break;
                case D:
                    dPressed.set(false);
                    break;
                case W:
                    wPressed.set(false);
                    break;
                case S:
                    sPressed.set(false);
                    break;
            }
        });
    }

    /**
     * @throws IOException
     * Method that spawns weapons when active
     */
    private void spawnWeapons() throws IOException {
        setActiveWeapons();
        if (guitar.getActive()) {
            guitar.setLayoutY(player.getLayoutY() + 200);
            guitar.setLayoutX(player.getLayoutX());
        }
    }

    /**
     * Sets active weapons at the start or when a weapon has been collected
     */
    private void setActiveWeapons() {
        Map weapons = engine.getActiveWeapons();
        if ((Boolean) weapons.get("guitar")) {
            guitar.setActive();
        }
        if ((Boolean) weapons.get("bass")) {
            bass.setActive();
        }
        if ((Boolean) weapons.get("drums")) {
            drums.setActive();
        }
        if ((Boolean) weapons.get("piano")) {
            piano.setActive();
        }
    }

    /**
     * Puts enemy into pane
     */
    private void spawnEnemy() {
        Enemy enemy = new Enemy(new Image(getClass().getResourceAsStream("images/enemy_sprites/enemy0.png")), model.getEnemyHealth());
        double[] move = model.spawnEnemyDistance(player.getLayoutX(), player.getLayoutY());
        enemy.setLayoutX(move[0]);
        enemy.setLayoutY(move[1]);
        pane.getChildren().add(enemy);
    }

    /**
     * Spawns bass rings every n ticks
     */
    private void spawnBassRings() {
        Circle circleBig = new Circle(10);
        Circle circleSmall = new Circle(9.9);
        Shape circle = Shape.subtract(circleBig, circleSmall);
        circle.setFill(Color.CYAN);
        circle.setId("bass");
        circle.setLayoutX(player.getLayoutX()+30);
        circle.setLayoutY(player.getLayoutY()+30);
        pane.getChildren().add(circle);
    }

    /**
     * Moves bass rings and handles note explosion
     */
    private void explodeBassAndNote() {
        List<Shape> toDelete = new ArrayList<>();
        for (Node weapon : pane.getChildren()) {
            if (weapon instanceof Shape) {
                if (weapon.getId() == "bass") {
                    //Move bass wave out
                    weapon.setScaleX(weapon.getScaleX() + 0.7);
                    weapon.setScaleY(weapon.getScaleY() + 0.7);

                    //Add to array to prevent concurrent modification exception
                    if (weapon.getScaleX() >= 100) {
                        toDelete.add((Shape) weapon);
                    }
                } else if (weapon.getId() == "note" || weapon.getId() == "note_max") {
                    //Explode out
                    if (weapon.getId() == "note") {
                        weapon.setScaleX(weapon.getScaleX() + 1.3);
                        weapon.setScaleY(weapon.getScaleY() + 1.3);
                    } else { //Explode in
                        weapon.setScaleX(weapon.getScaleX() - 0.7);
                        weapon.setScaleY(weapon.getScaleY() - 0.7);
                    }

                    //Add to array to prevent concurrent modification exception
                    if (weapon.getScaleX() <= 1 && weapon.getId() == "note_max") {
                        toDelete.add((Shape) weapon);
                    }
                    //Change color to darker orange
                    if (weapon.getScaleX() >= 20) {
                        weapon.setId("note_max");
                        ((Shape)weapon).setFill(Color.rgb(124, 10, 2));
                    }
                }
            }
        }
        //Remove rings when explosion ended
        for (Shape shape : toDelete) {
            pane.getChildren().remove(shape);
        }
    }

    /**
     * Handles spawning drumsticks every n ticks
     */
    private void spawnDrumsticks() {
        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy && pane.getChildren().contains(enemy)) {
                double[] angles = model.getDrumstickAngles(pane, player);
                Projectile drumStick = new Projectile("drumstick", angles);
                drumStick.setLayoutX(player.getLayoutX());
                drumStick.setLayoutY(player.getLayoutY());
                pane.getChildren().add(drumStick);
                break;
            }
        }
    }

    /**
     * Fires drumstick towards the closest enemy and keep it going that way
     */
    private void moveDrumsticks() {
        List<Projectile> toDelete = new ArrayList<>();
        for (Node drumstick : pane.getChildren()) {
            if (drumstick instanceof Projectile) {
                //Handle actual movement
                double[] move = model.moveDrumstick((Projectile) drumstick);
                drumstick.setLayoutX(move[0]);
                drumstick.setLayoutY(move[1]);
                drumstick.setRotate(drumstick.getRotate()+20);

                //Add to array to prevent concurrent modification exception
                if (drumstick.getLayoutX() >= model.getResX() || drumstick.getLayoutX() <= 0 ||
                        drumstick.getLayoutY() >= model.getResY() || drumstick.getLayoutY() <= 0) {
                    toDelete.add((Projectile) drumstick);
                }
            }
        }
        //Delete items from pane
        for (Projectile projectile : toDelete) {
            pane.getChildren().remove(projectile);
        }
    }

    /**
     * Spawn note if piano is active
     */
    private void spawnNote() {
        Weapon note = new Weapon("note");
        note.setLayoutX(player.getLayoutX()+30);
        note.setLayoutY(player.getLayoutY()+30);
        pane.getChildren().add(note);
    }

    /**
     * Spawn note explosion circle
     */
    private void spawnNoteExplosion() {
        for (Node note : pane.getChildren()) {
            if (note instanceof Weapon && ((Weapon)note).getType() == "note" && ((Weapon) note).tick()) {
                Circle circleBig = new Circle(10);
                Circle circleSmall = new Circle(9.2);
                Shape circle = Shape.subtract(circleBig, circleSmall);
                circle.setId("note");
                circle.setFill(Color.rgb(226, 88, 34));
                circle.setLayoutX(note.getLayoutX());
                circle.setLayoutY(note.getLayoutY());
                pane.getChildren().add(circle);
                pane.getChildren().remove(note);
                break;
            }
        }
    }

    /**
     * Spawn weapons every n levels for the player to pick up
     */
    private void spawnCollectible() {
        HashMap<String, double[]> coords = model.getWeaponSpawn(player);
        for (Map.Entry<String, double[]> entry : coords.entrySet()) {
            Collectible weapon = new Collectible(entry.getKey());
            pane.getChildren().add(weapon);
            weapon.setLayoutX(entry.getValue()[0]);
            weapon.setLayoutY(entry.getValue()[1]);
        }
    }

    /**
     * Despawn collectible weapons when they have been picked up or if any of the same type of
     * weapon has been collected
     */
    private void despawnWeapons() {
        ArrayList<Collectible> toRemove = new ArrayList<>();
        for (Node weapon : pane.getChildren()) {
            if (weapon instanceof Collectible) {
                Map weapons = engine.getActiveWeapons();

                //Add to array to prevent concurrent modification exception
                if ((Boolean) weapons.get(((Collectible) weapon).getType())) {
                    toRemove.add((Collectible) weapon);
                }
            }
        }
        //Remove from pane
        for (Collectible weapon : toRemove) {
            pane.getChildren().remove(weapon);
        }
    }

    /**
     * End of game function that removes everything from pane and shows end screen
     */
    private void endGame() {
        //Remove everything from pane
        pane.getChildren().clear();

        //Add background
        pane.getChildren().add(new ImageView(new Image(getClass().getResourceAsStream("images/end.png"))));

        //Add text with number of enemies killed
        Text kills = new Text();
        pane.getChildren().add(kills);
        String killsText = "Ed Sheerans killed: " + model.getKillCount();
        kills.setText(killsText);
        kills.setLayoutX(model.getResX()/5);
        kills.setLayoutY(model.getResY()/1.5);
        kills.setStyle("-fx-font-size: 30px;");
        kills.setFill(Color.WHITE);

        //Add text with game time
        Text time = new Text();
        pane.getChildren().add(time);

        //--- Code from https://stackoverflow.com/questions/6118922/convert-seconds-value-to-hours-minutes-seconds
        long totalSecs = TimeUnit.SECONDS.convert((System.nanoTime() - gameTime), TimeUnit.NANOSECONDS);

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        //------

        String timeOfPlay = "Time survived: " + timeString;
        time.setText(timeOfPlay);
        time.setLayoutX(model.getResX()/5);
        time.setLayoutY(model.getResY()/4);
        time.setStyle("-fx-font-size: 30px;");
        time.setFill(Color.WHITE);

        //Add text with level number
        Text levels = new Text();
        pane.getChildren().add(levels);

        String levelsText = "Level reached: " + model.getLevel();
        levels.setText(levelsText);
        levels.setLayoutX(model.getResX()/1.5);
        levels.setLayoutY(model.getResY()/1.5);
        levels.setStyle("-fx-font-size: 30px;");
        levels.setFill(Color.WHITE);
    }

    private void logger() {
        double frameRate = 60000000000.0 / -(frameTime - (frameTime = System.nanoTime()));
        int alive = 0;
        for (Node enemy : pane.getChildren()) {
            if (enemy instanceof Enemy) {
                alive++;
            }
        }

        //--- Code from https://stackoverflow.com/questions/6118922/convert-seconds-value-to-hours-minutes-seconds
        long totalSecs = TimeUnit.SECONDS.convert((System.nanoTime() - gameTime), TimeUnit.NANOSECONDS);

        long hours = totalSecs / 3600;
        long minutes = (totalSecs % 3600) / 60;
        long seconds = totalSecs % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        //---

        System.out.println("| FPS: " + (int) frameRate + " | Health: " + player.getHealth() + " | Kills: " + model.getKillCount() + " | Level: " + model.getLevel() + " | Time survived: " + timeString + " | Enemies alive: " + alive + " |");
    }

    /**
     * @param args
     * main function
     */
    public static void main(String[] args) {
        launch();
    }
}