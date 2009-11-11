/* Honorable Rock
	Guild Territories NPC
	NPC Script entirely written by Ethan Jenkins
	aka Sathon from elitems.org
	- Perion
*/

var status = 0;

importPackage(net.sf.odinms.client);

function start()
{
	status = -1;
	action(1, 0, 0);
}

function action(mode, type, selection)
{
	if (mode == -1)
	{
		cm.dispose();
	}
	
	else
	{
		if (mode == 0 && status == 0)
		{
			cm.dispose();
			return;
		}
		
		if (mode == 1)
			status++;
			
		else
			status--;
		
		if (status == 0)
		{
			var selStr = "Hello, I manage all Guild Territory operations here at #rMapleZtory#k. Please choose from the options below.#b";
			selStr += "\r\n#L0#View your guild's stats.#l";
			selStr += "\r\n#L1#Show current owners.#l";
			selStr += "\r\n#L2#I'm lost, help me!#l";
			cm.sendSimple(selStr);
		}
		
		else if (status == 1)
		{
				if(selection == 0)
				{
					var sendStr = "Territories your guild owns: #r" + cm.getTownsOwnedString();
					sendStr += "\r\n\r\n#kCurrent Points:";
					sendStr += "\r\nHenesys: #b" + cm.getTownPoints("henesys") + " points";
					sendStr += "\r\n#kKerning: #b" + cm.getTownPoints("kerning") + " points";
					sendStr += "\r\n#kEllinia: #b" + cm.getTownPoints("ellinia") + " points";
					sendStr += "\r\n#kPerion: #b" + cm.getTownPoints("perion") + " points";
					
					cm.sendOk(sendStr);
				}
				
				else if(selection == 1)
				{
					var sendStr = "Current Owners: ";
					sendStr += "\r\nHenesys: #b" + cm.getTownOwner("henesys") + "#k     Level: #r" + cm.getTownOwnerLevel("henesys");
					sendStr += "\r\n#kKerning: #b" + cm.getTownOwner("kerning") + "#k     Level: #r" + cm.getTownOwnerLevel("kerning");
					sendStr += "\r\n#kEllinia: #b" + cm.getTownOwner("ellinia") + "#k     Level: #r" + cm.getTownOwnerLevel("ellinia");
					sendStr += "\r\n#kPerion: #b" + cm.getTownOwner("perion") + "#k     Level: #r" + cm.getTownOwnerLevel("perion");
					
					cm.sendOk(sendStr);
				}
				
				else if(selection == 2)
				{
					var selStr = "This is the EliteMS Guild Territory System. Guilds race each other to earn points towards ownership of a guild. Guild members gain points for their guilds by killing certain monsters. When a guild reaches 2500 points for a town, that guild captures the town and all guilds' points for that town are reset. If a guild re-captures a town they already own, they advance their ownership level. There are rewards for owning a town that are better for higher ownership levels.#b";
					selStr += "\r\n#L0#What monsters do I kill to get points?#l";
					selStr += "\r\n#L1#What are the rewards for owning a town?#l";
					
					cm.sendSimple(selStr);
				}
				
				else
				{
					cm.dispose();
				}
		}
		
		else if (status == 2)
		{
			if(selection == 0)
			{
				var sendStr = "Monsters that give #r1 #kpoint each:";
				sendStr += "\r\nHenesys: #bBlue Snail";
				sendStr += "\r\n#kKerning: #bOrange Mushroom";
				sendStr += "\r\n#kEllinia: #bSlime";
				sendStr += "\r\n#kPerion: #bStump";
				
				sendStr += "\r\n\r\n#kMonsters that give #r5 #kpoints each:";
				sendStr += "\r\nHenesys: #bIron Hog";
				sendStr += "\r\n#kKerning: #bWraith";
				sendStr += "\r\n#kEllinia: #bLupin";
				sendStr += "\r\n#kPerion: #bFire Boar";
				
				cm.sendOk(sendStr);
			}
			
			else if(selection == 1)
			{
				var sendStr = "Currently, your guild will get an extra exp bonus depending on what level of ownership they have. You get [level] X 100% exp bonus. For example, level 1 is a 100% exp bonus, and level 3 is a 300% exp bonus. However, the highest it can go is 500%. So you may increase your level to whatever you want, however anything over level 5 still gives a 500% exp bonus.";
				
				cm.sendOk(sendStr);
			}
			
			else
			{	
				cm.dispose();
			}
		}
	}
}	