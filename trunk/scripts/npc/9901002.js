/*
junior is gay: author: junior is gay ok lets go
 */
var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
   
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) {
            cm.sendSimple ("Hey! I've heard a little bit about you and your " + cm.getPlayer().getReborns() +" rebirths...lets see what I can do...I have 8 affiliations with 8 different people. After you take on these 8 different people, you will be able to take on the quest of a #rTrue Hero#k. so...what would you like to do? \r\nDo you want to super rebirth? \r\n  \b\r\n#L0##rSuper RB for Earrings (reccomended for 1st RB)#k\n\#l\r\n#L1##bSuper RB for a Shirt (Reccomended for 2nd)#k\n\#l\r\n#L2##rSuper RB for shorts (reccomended for 3rd)#k\n\#l\r\n#L3##bSuper RB for shoes (reccomended for 4th)#k\n\#l\r\n#L4##rSuper RB for a hat (reccomended for 5th)#k\n\#l\r\n#L6##bSuper RB for gloves (reccomended for 6th)#k#l\r\n#L7##rSuper RB for a ring (reccomended for last super RB)#k#l");
        } else if (status == 1) {
            switch(selection) {
                case 0: cm.openNpc(9300014); break;
                case 1: cm.openNpc(9300003); break;
                case 2: cm.openNpc(9120023); break;
                case 3: cm.openNpc(2094001); break;
                case 4: cm.openNpc(2081003); break;
                case 6: cm.openNpc(9100204); break;
                case 7: cm.openNpc(1012107); break;
                  cm.dispose();

                }
            }
     
}
