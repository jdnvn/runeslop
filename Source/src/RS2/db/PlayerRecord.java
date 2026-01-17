package RS2.db;

import RS2.Settings;

public class PlayerRecord {
    public int id = -1;
    public String username;
    public String password;
    public int heightLevel;
    public int teleportToX;
    public int teleportToY;
    public int playerRights;
    public int crystalBowArrowCount;
    public int skullTimer;
    public int playerMagicBook;
    public double specAmount;
    public long teleBlockDelay;
    public int teleBlockLength;
    public int autoRet;
    public boolean accountFlagged;
    public int[] playerEquipment;
    public int[] playerEquipmentN;
    public int[] playerAppearance;
    public int[] playerLevel;
    public int[] playerXP;
    public int[] playerItems;
    public int[] playerItemsN;
    public int[] bankItems;
    public int[] bankItemsN;
    public long[] friends;
    public long[] ignores;
    public boolean newPlayer = true;
    public boolean exists = false;
    public boolean loadError = false;

    PlayerRecord(String user) {
        username = user;
        playerEquipment = new int[14];
        playerEquipmentN = new int[14];
        playerAppearance = new int[13];
        playerLevel = new int[25];
        playerXP = new int[25];
        playerItems = new int[28];
        playerItemsN = new int[28];
        bankItems = new int[Settings.BANK_SIZE];
        bankItemsN = new int[Settings.BANK_SIZE];
        friends = new long[200];
        ignores = new long[200];
    }
}
