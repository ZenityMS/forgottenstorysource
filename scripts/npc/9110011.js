/* Build Item Array */
var Items = Array(
	"Common" => Array(
	),
	
	"Rare" => Array(
	),
	
	"Unique" => Array(
	)
);


/* Start Main Function */

// Primary Variables
var Open = "Common";
var cRandom = 0;
var iRandom = 0;
var i = 0;

var cRandom = (Math.floor(Math.random() * 100)) / 100; // Math.random() gives floating integer, {* 100} then floor to a 2 digit integer then create a solid decimal by {/ 100}.

if(cRandom <= 8 && cRandom > 4){	// 5 - 8 (40%)
	Open = "Rare";
}else{					// 9 (10%)
	Open = "Unique";
}

for(i = 0; i <= cRandom; i++){
	Items[Open].sort(function() { return (Math.random() - 0.5); });
}

// Check for room in all slots then either dispose or continue code.
// Remove 1 Gachapon Ticket.

var iRandom = (Math.floor(Math.random() * (Items[Open].length - 1))) / 100;

if(Items[Open][iRandom][1] <= 1){
	// Make sure to use 1 as amount not the written amount. Allows us to use this for everything but an integer over 1.
	// Grant - Items[Open][iRandom][0] || 1
}else if(Items[Open][iRandom][1] > 1){
	// Grant - Items[Open][iRandom][0] || Amount - Items[Open][iRandom][0]
}
