
package com.RumRunning;

import net.runelite.api.Client;
import net.runelite.api.PlayerComposition;
import net.runelite.api.events.GameTick;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.kit.KitType;



/* I really wanted recolouring to work so blue team could have a blue pirate's
 * hat, but it's just not possible. The clue scroll pirate's hat cannot be
 * recoloured (at least with get/setColourToReplaceWith()), and if I recolour
 * the red team's hat to blue, then it also effects the red team's one. There
 * just isn't a way I could find to use the same itemid for both teams with
 * their own colours.
 *
 * There is an issue with hair. Red team's default hat has hair, but it has to
 * be hidden when equipping the bandana. As I don't cache anything here, it
 * isn't possible to re-apply it when using the default hat - and actually,
 * it isn't as simple as that either. Because it seems like a character's normal
 * hairstyle is different than the id when wearing a hat. So seemingly it would
 * be a huge amount of work to get hairstyles working */
class Headgear
{
	private final Client client;
	private final Config config;
	
	private final int pHatID = ItemID.PIRATEHAT;
	private final int rHatID = ItemID.BREW_RED_PIRATE_HAT;
	private final int rBanID = ItemID.PIRATE_BANDANA_RED;
	private final int bBanID = ItemID.BREW_BLUE_PIRATE_HAT;
	
	private final int headID = KitType.HEAD.getIndex();
	private final int hairID = KitType.HAIR.getIndex();
	
	private final int offset = PlayerComposition.ITEM_OFFSET;
	
	/* I saw these values when looking at how other plugins handled visual swaps.
	 * I'm not even sure if these are "correct" but they work */
	private final int hatBaldM = 256;
	private final int baldM    = 0;
	private final int hatBaldF = 301;
	private final int baldF    = 45;
	
	
	
	Headgear(Client client, Config config)
	{
		this.client = client;
		this.config = config;
	}
	
	public void
	gameTick(GameTick gameTick)
	{
		/* For all the players in the scene, including self */
		for (var player: client.getWorldView(-1).players())
		{
			int[]   kit       = player.getPlayerComposition().getEquipmentIds();
			boolean male      = player.getPlayerComposition().getGender() == 0;
			int     hOffsetID = kit[headID] - offset;
			
			if (hOffsetID == rHatID || hOffsetID == rBanID)
			{
				if (config.redTeamHatType() == Config.RedTeamHatType.DEFAULT)
				{
					kit[headID] = rHatID + offset;
					kit[hairID] = male ? hatBaldM : hatBaldF;
				}
				else if (config.redTeamHatType() == Config.RedTeamHatType.BANDANA)
				{
					kit[headID] = rBanID + offset;
					kit[hairID] = male ? baldM : baldF;
				}
			}
			else if (hOffsetID == bBanID || hOffsetID == pHatID)
			{
				if (config.blueTeamHatType() == Config.BlueTeamHatType.DEFAULT)
				{
					kit[headID] = bBanID + offset;
					kit[hairID] = male ? baldM : baldF;
				}
				else if (config.blueTeamHatType() == Config.BlueTeamHatType.PIRATE_HAT)
				{
					kit[headID] = pHatID + offset;
					kit[hairID] = male ? hatBaldM : hatBaldF;
				}
			}
			
			/* This applies kit's changes to the player model */
			player.getPlayerComposition().setHash();
		}
		
	}
	
}



