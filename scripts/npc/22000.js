var status = 0;
var items = Array(4000138, 4032013, 4001084);
var amount = Array(10, 1, 5);
var check = 0;


function start(){
    status = -1;
    action(1, 0, 0);
}

function action(mode, type, selection){
    if(mode == -1){
        cm.dispose();
    }else{
        if(mode == 1){
            status++;
        }else{
            status--;
        }
        if(status == 0){
           if (cm.getLevel() >= 200) {
            cm.sendYesNo("Hello, I am in charge of the #dRebirth Quest#k. As a travler, I have lost various items in my journy. I will need your help to get them back. If you do so I'll reborn you. I need you to get the following items: #d1#k #i4032013#, #d10#k #i4000138#, #d5#k #i4001084# can you do that?");
             } else {
            cm.sendOk("Pssh. I only speak to those who are level 200. Come back when you are stronger.......");
            cm.dispose();
        }
        }else if(status == 1){
            for(i = 0; i<items.length; i++){
                if(cm.haveItem(items[i], amount[i])){
                    check++;
                }
            }
            if(check == 3){
                for(i=0; i<items.length; i++){
                    cm.gainItem(items[i], -amount[i]);
                }
                cm.sendOk("Thanks a lot! I just gave you #d1#k Rebirth Point and I have reborned you.");
                cm.doReborn();
                cm.getPlayer().gainRebirthPoints(1);
                cm.dispose();
            }else{
                cm.sendOk("Where are my items?");
                cm.dispose();
            }
        }
    }
}