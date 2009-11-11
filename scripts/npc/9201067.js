var Equip = new Array(1072238, 1072239, 1072344, 1082223, 1122010, 1082246, 1032030, 1102145);
var Equipc = Math.floor(Math.random()*Equip.length);
var Scroll = new Array(2340000);
var itemamount = Math.floor(Math.random()*2+1);

importPackage(net.sf.rise.client);

function start() { 
 status = -1; 
 action(1, 0, 0); 
} 
function action(mode, type, selection) { 
 if (mode == -1) { 
  cm.dispose(); 
 } else { 
  if (status >= 0 && mode == 0) { 
   cm.sendOk("Come back anytime you feel like it!"); 
   cm.dispose(); 
   return; 
  } 
  if (mode == 1) 
   status++; 
  else 
   status--; 
  if (status == 0) {
     cm.sendNext("#bHello " + cm.getPlayer().getName() + "#k I hold the #rLucky Charm#k There is a small chance you will obtain it. You can try though for 100 #rMILLION#k Mesos.");
     }else if(status == 1){
     cm.sendSimple("So are you SURE you want to do this? #r\r\n#L0#Sure!#l\r\n#L1#No thanks I'm poor#l#k");
     }else if(status == 2){
            if(selection == 0){
            var rand = 1 + Math.floor(Math.random() * 10);
               if (cm.getPlayer().getMeso() >= 100000000) {
                if (rand == 2 || rand == 4 || rand == 6 || rand == 8 || rand == 10 || rand == 1 || rand == 3 || rand == 9) {
                    cm.sendOk("Sorry, you lost. Please try again to see if you get the #bLucky Charm#k");
                    cm.gainMeso(-100000000);
                }
              else if (rand == 5 || rand == 7) {
              cm.gainItem(4032031, 1);
                }
            } else {
              cm.sendOk("You don't have enough mesos");
            }
              } else if(selection == 1) {
              cm.sendOk("Poor ass noob :(");
              cm.dispose();
             }
         }
     }
 }

