var status = 0;

function start() {
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection) {
	if (mode == -1) {
		cm.dispose();
	} else {
	if (status >= 0 && mode == 0) {
		cm.dispose();
		return;
	}	
	if (mode == 1)
		status++;
	else
		status--;
	if (status == 0) {
        cm.sendSimple ("Hello there! I can tell you FAQ for #rMaplePassion!#k I realized that most new players for the server don't know what to do. \b\r\n#L0##e#eWhy can't I buy girl items in the Cash Shop?!\n\#l " +
                       "\r\n#L1##e#eWhere are all the NPC's??\n\#l" +
                       "\r\n#L2##e#eWhy can't I talk to NPC's?\n\#l" +
                       "\r\n#L3##e#eWhat are the commands for this server?\n\#l" +
                       "\r\n#L4##e#eWhat is the relationship system?\n\#l" +
                       "\r\n\r\nThis NPC was made by Rich and last updated 7/22");
	 } else if (selection == 0) {
        cm.sendOk ("Double click the items you want to equip to yourself and click the 'Buy Equipped Item' button in the top left corner of the Cash Shop");
        cm.dispose();      
	 } else if (selection == 1) {  
        cm.sendOk ("They are located in the All in One Npc located at the Free Market named 'Perry.'");  
        cm.dispose();  
	 } else if (selection == 2) {  
        cm.sendOk ("Try using the command @dispose");  
        cm.dispose(); 
	 } else if (selection == 3) {  
        cm.sendOk ("Use the command @commands or @help");  
        cm.dispose(); 
	 } else if (selection == 4) {  
        cm.sendOk ("You can use the following commands to use the Relationship System: \r\n #r@relationship#k - @relationship -charname- - Pends a Relationship Request \r\n#r@getrelationship#k - @getrelationship -charname- - Let's you see if a player is in a relationship\r\n #r@spousechat#k - @spousechat -Text- - Can be used to chat with your spouse\r\n #r@accept/@decline#k - Used to accept / decline pending relationship requests.");  
        cm.dispose(); 
} 
}
}
