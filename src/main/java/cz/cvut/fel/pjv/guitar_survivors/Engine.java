package cz.cvut.fel.pjv.guitar_survivors;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;

import java.io.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * Class that handles all files
 */
public class Engine {

    /**
     * @param filename - name of file
     * @return file or null
     * Load a file from 'files' directory
     */
    public File loadFile(String filename) {
        try {
            return new File("files/"+filename);
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @return Player data in a map
     * @throws IOException
     * Get data about the user
     */
    public Map getPlayerData() throws IOException {
        File playerFile = loadFile("player.json");
        ObjectMapper objectMapper = new ObjectMapper();

        Map<String,String> player = objectMapper.readValue(playerFile, new TypeReference<>(){});
        return player;
    }

    /**
     * @return Map with active weapons
     * Gets active weapons from json file
     */
    public Map getActiveWeapons()  {
        File playerFile = loadFile("activeWeapons.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Boolean> weapons = new HashMap<>();
        try {
            weapons = objectMapper.readValue(playerFile, new TypeReference<>() {
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return weapons;
    }

    /**
     * @param weapon
     * Saves active weapons into the json file
     */
    public void setActiveWeapons(String weapon)  {
        File playerFile = loadFile("activeWeapons.json");
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Boolean> weapons = new HashMap<>();
        try {
            weapons = objectMapper.readValue(playerFile, new TypeReference<>() {
            });

            weapons.put(weapon, true);

            System.out.println(weapons);

            objectMapper.writeValue(playerFile, weapons);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param type
     * @return Weapon image
     * Gets weapon image based on the type of weapon
     */
    public Image getWeaponImage(String type) {
        try {
            return new Image(getClass().getResourceAsStream("images/weapon_sprites/" + type + ".png"));
        } catch (MissingResourceException ex) {
            ex.printStackTrace();
        }

        return new Image(getClass().getResourceAsStream("images/icon.png"));
    }

    /**
     * @return All player sprites for player animation
     */
    public Image[] getPlayerSprites() {
        Image[] playerSprites = new Image[12];
        for (int i = 1; i <= 12; i++) {
            playerSprites[i-1] = new Image(getClass().getResourceAsStream("images/player_sprites/sprite ("+i+").png"));
        }
        return playerSprites;
    }

    /**
     * @return All enemy sprites for enemy animation
     */
    public Image[] getEnemySprites() {
        Image[] enemySprites = new Image[4];
        for (int i = 0; i <= 3; i++) {
            enemySprites[i] = new Image(getClass().getResourceAsStream("images/enemy_sprites/enemy"+i+".png"));
        }
        return enemySprites;
    }
}
