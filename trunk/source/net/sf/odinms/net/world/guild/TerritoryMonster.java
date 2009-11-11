package net.sf.odinms.net.world.guild;

import java.io.Serializable;

public class TerritoryMonster implements Serializable {

    private int id;
    private int monsterid;
    private int territoryid;
    private int pointvalue;

    public TerritoryMonster(int id, int monsterid, int territoryid, int pointvalue) {
        this.id = id;
        this.monsterid = monsterid;
        this.territoryid = territoryid;
        this.pointvalue = pointvalue;
    }

    public int getId() {
        return id;
    }

    public int getMonsterId() {
        return monsterid;
    }

    public int getTerritoryId() {
        return territoryid;
    }

    public int getPointValue() {
        return pointvalue;
    }
}
