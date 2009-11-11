/* Coded By lilAznDevinDev.*/

var wui = 0; 

function start() { 
    cm.sendSimple ("Hello, I'm a merchant for MapleZtory! If you got any #v4031183# you could exchange them For prizes. But Please Remeber To Pick Wisley I Do Not Give Any Refunds. Please Also remeber More Prizes Will be Added.\r\n#L0# #b#b2#k #v4031183# For A  #i2070018#\n\  #l\r\n#L1##b3#k #v4031183# For  #i3010028#\n\ #l\r\n#L2##b5#k #v4031183# For A  #i1022060#\n\  #l\r\n#L3##b7#k #v4031183# For A #i1012106#\n\  #l\r\n#L4##b9#k #v4031183# For  #i1122010#\n\  #l\r\n#L5##b10#k #v4031183# For A  #i1122014#\n\  #l\r\n#L6##b12#k #v4031183# For A  #i1122005#\n\  #l\r\n#L7##b15#k #v4031183# For A #i1122006#\n\ ");
}

function action(mode, type, selection) { 
cm.dispose(); 
    if (selection == 0) { 

 if (cm.haveItem(4031183, 2)) {
                      cm.gainItem(2070018,800);
                      cm.gainItem(4031183, -2);
                      cm.sendOk("Well done, Here is your set of Balanced Fury stars.");
                      cm.dispose();
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For Balanced Fury stars. ");
                cm.dispose(); 
                } 

     
        } else if (selection == 1) { 


                    if (cm.haveItem(4031183, 3)) {
                     cm.gainItem(3010028,1);  
                      cm.gainItem(4031183, -3);
                      cm.sendOk("Well done!, Here is your ??? chair."); 
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For an ??? chair. "); 
                cm.dispose(); 
                } 


        } else if (selection == 2) { 

                    if (cm.haveItem(4031183, 5)) {
                      cm.gainItem(1022060,1);  
                      cm.gainItem(4031183, -5);
                      cm.sendOk("Well done!, Here Is your White Raccoon Mask."); 
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For a White Raccoon Mask. ");
                cm.dispose();
                }



        } else if (selection == 3) {


                    if (cm.haveItem(4031183, 7)) {

                      cm.gainItem(1012106,1);
                      cm.gainItem(4031183, -7);
                      cm.sendOk("Well done!, Here Is your #r Rat Mouth#k.");
                      cm.dispose();
            } else {
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For a Rat Mouth. ");
                cm.dispose();
                }



        } else if (selection == 4) { 
   
                    if (cm.haveItem(4031183,9)) {
                   
                      cm.gainItem(1122010,1);
                      cm.gainItem(4031183, -9);
                      cm.sendOk("Well done!, Here Is Your Horus's Eye.");
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For a Horus's Eye. ");
                cm.dispose();
                } 

        } else if (selection == 5) { 

                    if (cm.haveItem(4031183, 10)) {
                      cm.gainItem(1122014,1);  
                      cm.gainItem(4031183, -10);
                      cm.sendOk("We'll Done! Here Is Your Silver Duputy Star, Please go Collect More #v4031183#."); 
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For a Silver Deputy Star. "); 
                cm.dispose(); 
                }

 
         } else if (selection == 6) { 

                    if (cm.haveItem(4031183, 12)) {
 
                      cm.gainItem(1122005,1);
                      cm.gainItem(4031183, -12);
                      cm.sendOk("We'll Done On Getting A Bowtie (Black), Please go Collect more #v4031183#.");
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You Do Not Have Enough #v4031183# For a Bowtie (Black). ");
                cm.dispose();
                } 


            } else if (selection == 7) { 

                    if (cm.haveItem(4031183, 15)) {

                      cm.gainItem(1122006,1);
                      cm.gainItem(4031183, -15);
                      cm.sendOk("Congratz On Getting The Top Prize A Bow-tie (Blue)!, Please go Collect more #v4031183#."); 
                      cm.dispose(); 
            } else { 
                cm.sendOk(" Sorry. You do not have enough #v4031183# for a Bow-tie (Blue). "); 
                cm.dispose(); 
                } 


        cm.dispose(); 
        }     


     
}  