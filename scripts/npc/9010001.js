// NPC: Tia
// Location: Perion
// Guild Territories NPC

// By Sathon


var status;

function start()
{
	status = -1;
	action(1, 0, 0);
}


function action(mode, type, selection)
{
	if (mode == -1)
		cm.dispose();

	else if (status >= 2 && mode == 0)
	{
		cm.dispose();
		return;
	}

	else if (mode == 1)
		status++;
		
	else
		status--;
	

	if (status == 0)
	{
		var str = "Hello there, mighty warrior. I can tell you everything you need to know about #rguild territories.#k \r\n\r\n";
		str += "Please choose an option from the list below: \r\n";
		str += "#L0##bWhat are guild territories, and how do they work?#k#l \r\n";
		str += "#L1##bWho are the current owners of the territories?#k#l \r\n";
		str += "#L2##bHow many points does my guild have right now?#k#l \r\n"
                str += "#L4##bI'm ready! Warp me to the town territories.#k#l \r\n"   
		str += "#L3##bWhat monsters do I kill to get points?#k#l";
		
		cm.sendSimple(str);
	}
	
	else if (status == 1)
	{
		if (selection == 0)
		{
			var str = "Guild territories are a custom feature here at #rMapleZtory#k. Each town here is a territory. Guilds can capture territories, and take ownership of them. When a guild owns a territory, its members get special rewards and benefits, such as bonus exp on maps surrounding that town.\r\n\r\n";
			str += "To capture a territory, guild members must kill certain monsters that will give their guild points. Each town has different monsters that will give points, and some monsters give more points than the others, depending on their strength. Once your guild reaches a certain number of points for a territory, your guild owns that territory, and the points for that territory for #eall #nthe guilds are reset to zero.\r\n\r\n";
			str += "Once a guild owns a territory, other guilds can try to take it from them by earning the required number of points. The guild that owns the territory can defend it by recapturing it themselves, before another guild gets the chance. If a guild that already owns a territory recaptures the territory, they advance their #bownership level#k. The maximum #bownership level#k is #gfive#k (is a guild recaptures a territory when they are already level #gfive#k, the level does not go up) and guilds get better rewards for having a higher #bownership level#k.\r\n\r\n";
			str += "If your guild owns a territory, you will receive #bbonus exp#k whenever they kill monsters on maps surrounding that town. The higher the ownership level, the higher the #bbonus exp#k. It ranges from an extra #d20%#k for #rlevel 1#k to a whole #d100%#k (double exp) for #rlevel 5#k. This can be a big payoff if you own a town that has a boss near it.";
			
			cm.sendOk(str);
			cm.dispose();
		}
		
		else if (selection == 1)
		{
			var str = "The current standings for the territories are: \r\n\r\n";

			var territories = cm.getTerritoryStorage().getTerritories();
			
			for(var i = 0; i < territories.length; i++)
			{
				str += ("#r#e" + territories[i].getName() + " #n#k    Owner: #b" + territories[i].getOwnerName() + "#k    Level: #b" + territories[i].getOwnerLevel() + " \r\n");
			}
			
			cm.sendOk(str);
			cm.dispose();

		} else if (selection == 4)
		{
                var str = "Please pick a territory.#k \r\n\r\n";
		str += "#L10##bHenesys Territory#k#l \r\n";
		str += "#L11##bEllinia Territory#k#l \r\n";
		str += "#L12##bPerion Territory#k#l \r\n"
                str += "#L13##bKerning Territory#k#l \r\n";   
		
		cm.sendSimple(str);
                status = 99;
		}
		

		else if (selection == 2)
		{
			if(cm.getPlayer().getGuild != null)
			{
				var str = "Your guild's point values for the territories are: \r\n\r\n"
				
				var territories = cm.getTerritoryStorage().getTerritories();
				
				for(var i = 0; i < territories.length; i++)
				{
					str += ("#r#e" + territories[i].getName() + " #n#k    Points: #b" + cm.getGuild().getTerritoryPoints(territories[i].getId()) + "#k    (#b" + territories[i].getCapturePoints() + "#k required) \r\n");
				}
				
				cm.sendOk(str);
				cm.dispose();
			}
			
			else
			{
				cm.sendOk("You are not currently in a guild.");
				cm.dispose();
			}
		}
		
		else if (selection == 3)
		{
			var str = "These are the monsters that you kill to get points for respective territories:";
			
			var territories = cm.getTerritoryStorage().getTerritories();
			
			for(var i = 0; i < territories.length; i++)
			{
				str += ("\r\n\r\n #r#e" + territories[i].getName() + "#k#n");
				
				var monsters = cm.getTerritoryStorage().getMonsters(territories[i].getId());
				
				for(var j = 0; j < monsters.length; j++)
				{
					str += "\r\n#d#o" + monsters[j].getMonsterId() + "#: #b " + monsters[j].getPointValue() + "#k points";
				}
				
				
			}
			
			cm.sendOk(str);
			cm.dispose();
		
		} else if (selection == 10)
		{
                var str = "Please pick a territory.#k \r\n\r\n";
		str += "#L10##bHenesys Territory#k#l \r\n";
		str += "#L11##bEllinia Territory#k#l \r\n";
		str += "#L12##bPerion Territory#k#l \r\n"
                str += "#L13##bKerning Territory#k#l \r\n";   
		
		cm.sendSimple(str);

		}
		
		else
		{
			cm.dispose();
		}

		} else if (selection == 10) {		
                cm.warp(100000200, 0);

		} else if (selection == 11) {		
                cm.warp(101000200, 0);

		} else if (selection == 12) {		
                cm.warp(102000000, 0);

		} else if (selection == 13) {		
                cm.warp(103000000, 0);
	}
}