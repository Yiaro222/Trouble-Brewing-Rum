
package com.RumRunning;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;
import javax.inject.Inject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;



@Slf4j
public class TroubleBrewingSaboOverlay
extends      Overlay
{
	private final Client               client;
	private final ModelOutlineRenderer modelOutlineRenderer;
	private final ItemManager          itemManager;
	
	private final TroubleBrewingPlugin plugin;
	private final TroubleBrewingConfig config;
	
	private final int WBUCK  = 1929;
	private final int LPATCH = 8932;
	private final int BPIPE  = 8930;
	private final int BRIDGE = 8979;
	
	private class
	GObj
	{
		public GameObject game_object;
		
		public int repair_item_id;
		public int amount_needed; /* As in, if a hopper is fully burnt, then it
		                           * requires 3 lumber to repair to normal. */
		
		/* GameObject ID, world x and y coords. Used for finding the correct
		 * object to apply to game_object. */
		public int id, x, y;
		public String name;
		
		public
		GObj(int id, int x, int y, int repair_item_id, int stages, String name)
		{
			this.game_object = null;
			
			this.amount_needed  = stages;
			this.repair_item_id = repair_item_id;
			
			this.id   = id;
			this.x    = x;
			this.y    = y;
			this.name = name;
		}
	}
	
	private List<GObj> objs = new ArrayList<>(List.of
	(
		/* On fire */
		new GObj(15849, 3811, 3000, WBUCK,  1, "brew_hopper_red_burning_2"),
		new GObj(15848, 3811, 3000, WBUCK,  1, "brew_hopper_red_burning_1"),
		
		/* Fire wasn't put out, completely burnt */
		new GObj(15854, 3811, 3000, LPATCH, 3, "brew_hopper_red_destroyed"),
		
		/* These are essentially the same. If XXX52 is wet and if left for a few
		 * seconds it will will become XXX50. */
		new GObj(15850, 3811, 3000, LPATCH, 2, "brew_hopper_red_damaged_1"),
		new GObj(15852, 3811, 3000, LPATCH, 2, "brew_hopper_red_wet_1"),
		
		/* Same here XXX53 -> XXX51, if left */
		new GObj(15851, 3811, 3000, LPATCH, 1, "brew_hopper_red_damaged_2"),
		new GObj(15853, 3811, 3000, LPATCH, 1, "brew_hopper_red_wet_2"),
		
		/* Same for red team coloured water hopper */
		new GObj(15849, 3811, 3003, WBUCK,  1, "brew_hopper_red_burning_2"),
		new GObj(15848, 3811, 3003, WBUCK,  1, "brew_hopper_red_burning_1"),
		new GObj(15854, 3811, 3003, LPATCH, 3, "brew_hopper_red_destroyed"),
		new GObj(15850, 3811, 3003, LPATCH, 2, "brew_hopper_red_damaged_1"),
		new GObj(15852, 3811, 3003, LPATCH, 2, "brew_hopper_red_wet_1"),
		new GObj(15851, 3811, 3003, LPATCH, 1, "brew_hopper_red_damaged_2"),
		new GObj(15853, 3811, 3003, LPATCH, 1, "brew_hopper_red_wet_2"),
		
		/* Red team Bark Hopper */
		new GObj(15849, 3813, 3003, WBUCK,  1, "brew_hopper_red_burning_2"),
		new GObj(15848, 3813, 3003, WBUCK,  1, "brew_hopper_red_burning_1"),
		new GObj(15854, 3813, 3003, LPATCH, 3, "brew_hopper_red_destroyed"),
		new GObj(15850, 3813, 3003, LPATCH, 2, "brew_hopper_red_damaged_1"),
		new GObj(15852, 3813, 3003, LPATCH, 2, "brew_hopper_red_wet_1"),
		new GObj(15851, 3813, 3003, LPATCH, 1, "brew_hopper_red_damaged_2"),
		new GObj(15853, 3813, 3003, LPATCH, 1, "brew_hopper_red_wet_2"),
		
		/* Red team Grubs Hopper */
		new GObj(15849, 3815, 3003, WBUCK,  1, "brew_hopper_red_burning_2"),
		new GObj(15848, 3815, 3003, WBUCK,  1, "brew_hopper_red_burning_1"),
		new GObj(15854, 3815, 3003, LPATCH, 3, "brew_hopper_red_destroyed"),
		new GObj(15850, 3815, 3003, LPATCH, 2, "brew_hopper_red_damaged_1"),
		new GObj(15852, 3815, 3003, LPATCH, 2, "brew_hopper_red_wet_1"),
		new GObj(15851, 3815, 3003, LPATCH, 1, "brew_hopper_red_damaged_2"),
		new GObj(15853, 3815, 3003, LPATCH, 1, "brew_hopper_red_wet_2"),
		
		
		/* On fire */
		new GObj(15875, 3822, 2951, WBUCK,  1, "brew_hopper_blue_burning_2"),
		new GObj(15874, 3822, 2951, WBUCK,  1, "brew_hopper_blue_burning_1"),
		
		/* Fire wasn't put out, completely burnt */
		new GObj(15880, 3822, 2951, LPATCH, 3, "brew_hopper_blue_destroyed"),
		
		/* These are essentially the same. If XXX52 is wet and if left for a few
		 * seconds it will will become XXX50. */
		new GObj(15876, 3822, 2951, LPATCH, 2, "brew_hopper_blue_damaged_1"),
		new GObj(15878, 3822, 2951, LPATCH, 2, "brew_hopper_blue_wet_1"),
		
		/* Same here XXX53 -> XXX51, if left */
		new GObj(15877, 3822, 2951, LPATCH, 1, "brew_hopper_blue_damaged_2"),
		new GObj(15879, 3822, 2951, LPATCH, 1, "brew_hopper_blue_wet_2"),
		
		/* Same for blue team coloured water hopper */
		new GObj(15875, 3822, 2948, WBUCK,  1, "brew_hopper_blue_burning_2"),
		new GObj(15874, 3822, 2948, WBUCK,  1, "brew_hopper_blue_burning_1"),
		new GObj(15880, 3822, 2948, LPATCH, 3, "brew_hopper_blue_destroyed"),
		new GObj(15876, 3822, 2948, LPATCH, 2, "brew_hopper_blue_damaged_1"),
		new GObj(15878, 3822, 2948, LPATCH, 2, "brew_hopper_blue_wet_1"),
		new GObj(15877, 3822, 2948, LPATCH, 1, "brew_hopper_blue_damaged_2"),
		new GObj(15879, 3822, 2948, LPATCH, 1, "brew_hopper_blue_wet_2"),
		
		/* blue team Bark Hopper */
		new GObj(15875, 3820, 2948, WBUCK,  1, "brew_hopper_blue_burning_2"),
		new GObj(15874, 3820, 2948, WBUCK,  1, "brew_hopper_blue_burning_1"),
		new GObj(15880, 3820, 2948, LPATCH, 3, "brew_hopper_blue_destroyed"),
		new GObj(15876, 3820, 2948, LPATCH, 2, "brew_hopper_blue_damaged_1"),
		new GObj(15878, 3820, 2948, LPATCH, 2, "brew_hopper_blue_wet_1"),
		new GObj(15877, 3820, 2948, LPATCH, 1, "brew_hopper_blue_damaged_2"),
		new GObj(15879, 3820, 2948, LPATCH, 1, "brew_hopper_blue_wet_2"),
		
		/* blue team Grubs Hopper */
		new GObj(15875, 3818, 2948, WBUCK,  1, "brew_hopper_blue_burning_2"),
		new GObj(15874, 3818, 2948, WBUCK,  1, "brew_hopper_blue_burning_1"),
		new GObj(15880, 3818, 2948, LPATCH, 3, "brew_hopper_blue_destroyed"),
		new GObj(15876, 3818, 2948, LPATCH, 2, "brew_hopper_blue_damaged_1"),
		new GObj(15878, 3818, 2948, LPATCH, 2, "brew_hopper_blue_wet_1"),
		new GObj(15877, 3818, 2948, LPATCH, 1, "brew_hopper_blue_damaged_2"),
		new GObj(15879, 3818, 2948, LPATCH, 1, "brew_hopper_blue_wet_2"),
		
		/* blue team Bitternut Hopper */
		new GObj(15875, 3816, 2948, WBUCK,  1, "brew_hopper_blue_burning_2"),
		new GObj(15874, 3816, 2948, WBUCK,  1, "brew_hopper_blue_burning_1"),
		new GObj(15880, 3816, 2948, LPATCH, 3, "brew_hopper_blue_destroyed"),
		new GObj(15876, 3816, 2948, LPATCH, 2, "brew_hopper_blue_damaged_1"),
		new GObj(15878, 3816, 2948, LPATCH, 2, "brew_hopper_blue_wet_1"),
		new GObj(15877, 3816, 2948, LPATCH, 1, "brew_hopper_blue_damaged_2"),
		new GObj(15879, 3816, 2948, LPATCH, 1, "brew_hopper_blue_wet_2"),
		
		
		/* Pipes work exactly the same -
		 * n+0 = 15837 = normal/fixed
		 * n+1 = 15838 = burning C2
		 * n+2 = 15839 = burning C1
		 * n+3 = 15840 = burnt 2 mats to fix
		 * n+4 = 15841 = burnt 1 mat  to fix
		 * n+5 = 15842 = burnt/wet C2 2 mats to fix, changes to n+3 if left
		 * n+6 = 15843 = burnt/wet C1 1 mat  to fix, changes to n+4 if left
		 * n+7 = 15844 = Totally burnt / C3, 3 mats to fix
		*/
		new GObj(15839, 3819, 3003, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3819, 3003, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3819, 3003, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3819, 3003, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3819, 3003, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3819, 3003, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3819, 3003, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3818, 3003, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3818, 3003, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3818, 3003, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3818, 3003, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3818, 3003, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3818, 3003, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3818, 3003, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3815, 3004, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3815, 3004, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3815, 3004, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3815, 3004, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3815, 3004, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3815, 3004, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3815, 3004, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3814, 3004, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3814, 3004, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3814, 3004, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3814, 3004, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3814, 3004, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3814, 3004, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3814, 3004, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3813, 3004, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3813, 3004, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3813, 3004, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3813, 3004, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3813, 3004, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3813, 3004, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3813, 3004, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3812, 3002, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3812, 3002, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3812, 3002, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3812, 3002, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3812, 3002, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3812, 3002, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3812, 3002, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3812, 3001, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3812, 3001, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3812, 3001, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3812, 3001, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3812, 3001, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3812, 3001, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3812, 3001, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		new GObj(15839, 3813, 2999, WBUCK, 1, "brew_pipes_red_burning_2"),
		new GObj(15838, 3813, 2999, WBUCK, 1, "brew_pipes_red_burning_1"),
		new GObj(15844, 3813, 2999, BPIPE, 3, "brew_pipes_red_destroyed"),
		new GObj(15840, 3813, 2999, BPIPE, 2, "brew_pipes_red_damaged_1"),
		new GObj(15842, 3813, 2999, BPIPE, 2, "brew_pipes_red_wet_1"),
		new GObj(15841, 3813, 2999, BPIPE, 1, "brew_pipes_red_damaged_2"),
		new GObj(15843, 3813, 2999, BPIPE, 1, "brew_pipes_red_wet_2"),
		
		
		new GObj(15865, 3814, 2948, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3814, 2948, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3814, 2948, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3814, 2948, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3814, 2948, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3814, 2948, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3814, 2948, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3815, 2948, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3815, 2948, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3815, 2948, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3815, 2948, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3815, 2948, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3815, 2948, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3815, 2948, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3818, 2947, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3818, 2947, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3818, 2947, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3818, 2947, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3818, 2947, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3818, 2947, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3818, 2947, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3819, 2947, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3819, 2947, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3819, 2947, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3819, 2947, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3819, 2947, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3819, 2947, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3819, 2947, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3820, 2947, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3820, 2947, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3820, 2947, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3820, 2947, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3820, 2947, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3820, 2947, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3820, 2947, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3821, 2949, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3821, 2949, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3821, 2949, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3821, 2949, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3821, 2949, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3821, 2949, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3821, 2949, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3821, 2950, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3821, 2950, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3821, 2950, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3821, 2950, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3821, 2950, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3821, 2950, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3821, 2950, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		new GObj(15865, 3820, 2952, WBUCK, 1, "brew_pipes_blue_burning_2"),
		new GObj(15864, 3820, 2952, WBUCK, 1, "brew_pipes_blue_burning_1"),
		new GObj(15870, 3820, 2952, BPIPE, 3, "brew_pipes_blue_destroyed"),
		new GObj(15866, 3820, 2952, BPIPE, 2, "brew_pipes_blue_damaged_1"),
		new GObj(15868, 3820, 2952, BPIPE, 2, "brew_pipes_blue_wet_1"),
		new GObj(15867, 3820, 2952, BPIPE, 1, "brew_pipes_blue_damaged_2"),
		new GObj(15869, 3820, 2952, BPIPE, 1, "brew_pipes_blue_wet_2"),
		
		
		
		new GObj(15857, 3806, 2984, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3806, 2984, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3806, 2984, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3806, 2984, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3806, 2984, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3806, 2984, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3806, 2984, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		new GObj(15857, 3806, 2983, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3806, 2983, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3806, 2983, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3806, 2983, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3806, 2983, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3806, 2983, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3806, 2983, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		new GObj(15857, 3787, 2982, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3787, 2982, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3787, 2982, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3787, 2982, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3787, 2982, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3787, 2982, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3787, 2982, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		new GObj(15857, 3786, 2982, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3786, 2982, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3786, 2982, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3786, 2982, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3786, 2982, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3786, 2982, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3786, 2982, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		new GObj(15857, 3826, 2984, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3826, 2984, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3826, 2984, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3826, 2984, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3826, 2984, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3826, 2984, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3826, 2984, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		new GObj(15857, 3827, 2984, WBUCK,  1, "brew_bridge_red_burning_2"),
		new GObj(15856, 3827, 2984, WBUCK,  1, "brew_bridge_red_burning_1"),
		new GObj(15862, 3827, 2984, BRIDGE, 3, "brew_bridge_red_destroyed"),
		new GObj(15858, 3827, 2984, BRIDGE, 2, "brew_bridge_red_damaged_1"),
		new GObj(15859, 3827, 2984, BRIDGE, 2, "brew_bridge_red_wet_1"),
		new GObj(15860, 3827, 2984, BRIDGE, 1, "brew_bridge_red_damaged_2"),
		new GObj(15861, 3827, 2984, BRIDGE, 1, "brew_bridge_red_wet_2"),
		
		
		new GObj(15883, 3809, 2964, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3809, 2964, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3809, 2964, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3809, 2964, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3809, 2964, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3809, 2964, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3809, 2964, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		new GObj(15883, 3809, 2965, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3809, 2965, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3809, 2965, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3809, 2965, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3809, 2965, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3809, 2965, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3809, 2965, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		new GObj(15883, 3793, 2965, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3793, 2965, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3793, 2965, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3793, 2965, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3793, 2965, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3793, 2965, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3793, 2965, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		new GObj(15883, 3792, 2965, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3792, 2965, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3792, 2965, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3792, 2965, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3792, 2965, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3792, 2965, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3792, 2965, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		new GObj(15883, 3828, 2960, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3828, 2960, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3828, 2960, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3828, 2960, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3828, 2960, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3828, 2960, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3828, 2960, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		new GObj(15883, 3729, 2960, WBUCK,  1, "brew_bridge_blue_burning_2"),
		new GObj(15882, 3729, 2960, WBUCK,  1, "brew_bridge_blue_burning_1"),
		new GObj(15888, 3729, 2960, BRIDGE, 3, "brew_bridge_blue_destroyed"),
		new GObj(15884, 3729, 2960, BRIDGE, 2, "brew_bridge_blue_damaged_1"),
		new GObj(15885, 3729, 2960, BRIDGE, 2, "brew_bridge_blue_wet_1"),
		new GObj(15886, 3729, 2960, BRIDGE, 1, "brew_bridge_blue_damaged_2"),
		new GObj(15887, 3729, 2960, BRIDGE, 1, "brew_bridge_blue_wet_2"),
		
		
		// TODO: i cba to test how many pipes are needed to repair it
		new GObj(15937, 3606, 2997, WBUCK, 1, "brew_water_pump_fire"),
		new GObj(15938, 3606, 2997, BPIPE, 1, "brew_water_pump_damaged"),
		new GObj(15937, 3607, 2954, WBUCK, 1, "brew_water_pump_fire"),
		new GObj(15938, 3607, 2954, BPIPE, 1, "brew_water_pump_damaged")
	));
	
	/*
	 * TODO:
	 * > Maybe have a notification + chat message when its been saboed
	 * >> The chat message should give some sort of description of where it happened
	 * > Maybe add other flammable objects like trees
	 * > Highlight the required items on the table UI
	 * > Add icons to show the repair cost
	 * */
	
	
	
	@Inject
	private
	TroubleBrewingSaboOverlay(Client               client,
	                          ModelOutlineRenderer modelOutlineRenderer,
	                          ItemManager          itemManager,
	                          TroubleBrewingPlugin plugin,
	                          TroubleBrewingConfig config)
	{
		this.client               = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
		this.itemManager          = itemManager;
		this.plugin               = plugin;
		this.config               = config;
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		Player player;
		/* Check the player is in TB, and their plane, and whatnot */
		/* Check config.hightlightType (or whatever) to see if the option is even enabled */
		
		player = client.getLocalPlayer();
		
		for (int i = 0; i < objs.size(); ++i)
		{
			if (objs.get(i).game_object != null &&
			    objs.get(i).game_object.getWorldLocation().getPlane() ==
			    player.getWorldLocation().getPlane())
			{
				graphics.draw(objs.get(i).game_object.getClickbox());
			}
		}
		
		return(null);
	}
	
	public void
	gameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOADING)
		{
			for (int i = 0; i < objs.size(); ++i)
			{
				objs.get(i).game_object = null;
			}
		}
	}
	
	public void
	gameObjectSpawned(GameObjectSpawned event)
	{
		final GameObject gameObject = event.getGameObject();
		final WorldPoint wp         = gameObject.getWorldLocation();
		
		for (int i = 0; i < objs.size(); ++i)
		{
			if (objs.get(i).id == gameObject.getId() &&
			    objs.get(i).x  == wp.getX()          &&
			    objs.get(i).y  == wp.getY())
			{
				objs.get(i).game_object = gameObject;
				return;
			}
		}
	}
	
	public void
	gameObjectDespawned(GameObjectDespawned event)
	{
		final GameObject gameObject = event.getGameObject();
		
		for (int i = 0; i < objs.size(); ++i)
		{
			if (objs.get(i).game_object == gameObject)
			{
				objs.get(i).game_object = null;
			}
		}
	}
	
}



