package net.sf.odinms.net.channel.handler;

import java.util.ArrayList;
import net.sf.odinms.client.MapleClient;
import net.sf.odinms.net.AbstractMaplePacketHandler;
import net.sf.odinms.tools.data.input.SeekableLittleEndianAccessor;

public class PartySearchHandler extends AbstractMaplePacketHandler {
    //All bolleans as for now, all is useless... just for testing somehow, but I changed method while making

    boolean All = false;
    boolean Beginner = false;
    boolean AllWarriors = false;
    boolean Warrior1 = false;
    boolean Warrior2 = false;
    boolean Warrior3 = false;
    boolean AllMagician = false;
    boolean Magician1 = false;
    boolean Magician2 = false;
    boolean Magician3 = false;
    boolean AllPirate = false;
    boolean Pirate1 = false;
    boolean Pirate2 = false;
    boolean AllThief = false;
    boolean Thief1 = false;
    boolean Thief2 = false;
    boolean AllBowman = false;
    boolean Bowman1 = false;
    boolean Bowman2 = false;
    ArrayList<Integer> boxsumconstructor = new ArrayList<Integer>();

    public void handlePacket(SeekableLittleEndianAccessor slea, MapleClient c) {
        c.getPlayer().resetAfkTime();
        int min = slea.readInt(); //I now that it need to do the party part, but meh, box is more important atm.
        int max = slea.readInt(); //same
        int person = slea.readInt(); // same
        int box = slea.readInt();
        System.out.println("MINIMUM PLAYERS: " + min);
        System.out.println("MAXIMUM PLAYERS: " + max);
        System.out.println("AMOUNT OF PLAYERS: " + person);
        System.out.println("BOX VALUE: " + box);
        String binary = Integer.toBinaryString(box);
        String reverse = "";
        System.out.println("Binary: " + binary);
        for (int z = 0; z < binary.length(); z++) {
            reverse = binary.charAt(z) + reverse;
        }
        System.out.println("Total Reverse: " + reverse);
        char letters;
        for (int z = 0; z < reverse.length(); z++) {
            letters = reverse.charAt(z);
            System.out.println("1 By 1 for jobs Current one is : " + letters);
            isJob(letters, z, c);
        }
    }

    public void isJob(char binary, int times, MapleClient c) {
        times++;
        if (binary == 1) {
            if (times == 1) {
                All = true;
                System.out.println("All have been choosen");
            } else if (times == 2) {
                Beginner = true;
                System.out.println("it's Beginners");
            } else if (times == 3) {
                AllWarriors = true;
                System.out.println("All Warriors are here");
            } else if (times == 4) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Fighter");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Crusader");
                } else {
                    System.out.println("it's a Hero");
                }
                Warrior1 = true;
            } else if (times == 5) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Page");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a White Knight");
                } else {
                    System.out.println("it's a Paladin");
                }
                Warrior2 = true;
            } else if (times == 6) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Spearman");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Dragon Knight");
                } else {
                    System.out.println("it's a Dark Knight");
                }
                Warrior3 = true;
            } else if (times == 7) {
                AllMagician = true;
                System.out.println("All Magicians are here");
            } else if (times == 8) {
                Magician1 = true;
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Ice Lighting Wizard");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Ice Lighting Mage");
                } else {
                    System.out.println("it's a Ice Lighting ArchMage");
                }
            } else if (times == 9) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Fire Poison Wizard");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Fire Poison Mage");
                } else {
                    System.out.println("it's a Fire Posion ArchMage");
                }
                Magician2 = true;
            } else if (times == 10) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Cleric");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Priest");
                } else {
                    System.out.println("it's a Bishop");
                }
                Magician3 = true;
            } else if (times == 11) {
                AllPirate = true;
                System.out.println("All Pirate are here");
            } else if (times == 12) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Gunslinger");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Outlaw");
                } else {
                    System.out.println("it's a Corsair");
                }
                Pirate1 = true;
            } else if (times == 13) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Brawler");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Marauder");
                } else {
                    System.out.println("it's a Buccaneer");
                }
                Pirate2 = true;
            } else if (times == 14) {
                System.out.println("All Thiefs are here");
                AllThief = true;
            } else if (times == 15) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Assasin");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Hermit");
                } else {
                    System.out.println("it's a Night Lord");
                }
                Thief1 = true;
            } else if (times == 16) {
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Bandit");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Chief Bandit");
                } else {
                    System.out.println("it's a Shadower");
                }
                Thief2 = true;
            } else if (times == 17) {
                AllBowman = true;
                System.out.println("All Bowmans are here");
            } else if (times == 18) {
                Bowman1 = true;
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Hunter");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Ranger");
                } else {
                    System.out.println("it's a Bow Master");
                }
            } else if (times == 19) {
                Bowman2 = true;
                if (c.getPlayer().getLevel() < 70) {
                    System.out.println("it's a Crossbow Man");
                } else if (c.getPlayer().getLevel() < 120) {
                    System.out.println("it's a Sniper");
                } else {
                    System.out.println("it's a Marksman");
                }
            }
        }
    }
}
