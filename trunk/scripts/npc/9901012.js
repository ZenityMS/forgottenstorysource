var status = 0;

function start() {
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection) {
    if (mode == -1) 
        cm.dispose();
    else {
        if (mode == 0 && status == 0) 
            cm.dispose();
        if (mode == 1)
            status++;
        else
            status--;
        if (status == 0) 
        cm.sendOk("Hi I'm Pat! You know I'm asian cause I'm so pro <33. Btw I like pizza :D. But #bcat eyes#k are my fave! :)");
        else if (status == 1) {
        cm.sendOk("So, I'm VERY hungry :) I need you to collect at least 20 #v4031568# from the monsters below.");
       } else if (status == 2) {
        cm.sendYesNo("Have you collected 20 #v4031568# yet?");
      } else if (status == 3) {
        if (cm.haveItem (4031568, 20)) {
        cm.warp(222020111, 0);       
        cm.showEffect("quest/party/clear");
        cm.gainExp(10000);
        cm.gainItem(4031568, -20);
        // cm.sendOk("You have been a good student even though you were such a noob. Good luck in #bOniStory!#k");
        cm.dispose();
       } else {
        cm.sendOk("You do not have 20 #v4031568# yet. Hurry I'm going to starve :(");
        cm.dispose();
}        
}
}
}