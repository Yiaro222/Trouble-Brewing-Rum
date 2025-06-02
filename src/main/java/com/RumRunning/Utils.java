package com.RumRunning;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.kit.KitType;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class Utils
extends      Overlay
{
    private final Client               client;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final ItemManager          itemManager;

    private final TroubleBrewingPlugin plugin;
    private final Config               config;

    public static boolean inMinigame;
    public static boolean onRedTeam;

    /* All of these WorldPoints and WorldAreas are for the ground level but
     * can still be useful for the upstairs hopper room. */
    public static final WorldPoint RED_TEAM_LOCATION  = new WorldPoint(3815, 3000, 0);
    public static final WorldPoint BLUE_TEAM_LOCATION = new WorldPoint(3815, 2950, 0);

    public static final WorldArea RED_HALF  = new WorldArea(3776, 2976, 64, 32, 0);
    public static final WorldArea BLUE_HALF = new WorldArea(3776, 2944, 64, 32, 0);

    public static final WorldArea RED_TEAM_BASE  = new WorldArea(3805, 2997, 19, 9, 0);
    public static final WorldArea BLUE_TEAM_BASE = new WorldArea(3806, 2946, 19, 9, 0);

    /* These are roughly the three islands and their bridges */
    public static final WorldArea MIDDLE_CENTRE = new WorldArea(3800, 2959, 20, 30, 0);
    public static final WorldArea MIDDLE_EAST   = new WorldArea(3820, 2959, 17, 30, 0);
    public static final WorldArea MIDDLE_WEST   = new WorldArea(3781, 2959, 19, 30, 0);

    /* It's possible multiple of these are true at once. These are just rectangles, so when in a lobby, the player will
     * also be contained in NEAR_LOBBY. */
    public static final WorldArea RED_TEAM_LOBBY             = new WorldArea(3803, 3010, 10, 10, 0);
    public static final WorldArea BLUE_TEAM_LOBBY            = new WorldArea(3813, 3010, 10, 10, 0);
    public static final WorldArea BLUE_TEAM_LOBBY_SKIP_TILES = new WorldArea(3822, 3010, 1,  10, 0);
    public static final WorldArea NEAR_LOBBY                 = new WorldArea(3800, 3008, 26, 17, 0);

    public static final int DRAW_DISTANCE = 28;

    public static BufferedImage ICON_LOGS;
    public static BufferedImage ICON_TINDERBOX;
    public static BufferedImage ICON_BUCKET_OF_WATER;
    public static BufferedImage ICON_PIPE_SECTION;
    public static BufferedImage ICON_BRIDGE_SECTION;
    public static BufferedImage ICON_LUMBER_PATCH;

    public static Font FONT;

    @Inject
    private Utils(Client               client,
                  ModelOutlineRenderer modelOutlineRenderer,
                  ItemManager          itemManager,
                  TroubleBrewingPlugin plugin,
                  Config config)
    {
        this.client               = client;
        this.modelOutlineRenderer = modelOutlineRenderer;
        this.itemManager          = itemManager;
        this.plugin               = plugin;
        this.config               = config;
        
        ICON_LOGS            = itemManager.getImage(ItemID.LOGS);
        ICON_TINDERBOX       = itemManager.getImage(ItemID.TINDERBOX);
        ICON_BUCKET_OF_WATER = itemManager.getImage(ItemID.BUCKET_WATER);
        ICON_PIPE_SECTION    = itemManager.getImage(ItemID.BREW_PIPE_SECTION);
        ICON_BRIDGE_SECTION  = itemManager.getImage(ItemID.BREW_BRIDGE_SECTION);
        ICON_LUMBER_PATCH    = itemManager.getImage(ItemID.BREW_LUMBER_PATCH);

        FONT = new Font(FontManager.getRunescapeFont().getName(), Font.PLAIN, config.fontSize());
    }

    @Override
    public Dimension
    render(Graphics2D graphics)
    {
        inMinigame(client);
        if (inMinigame)
            onRedTeam(client);

        return null;
    }

    public static boolean
    inMinigame(Client client)
    {
        final Player player = client.getLocalPlayer();

        // Check if player is inside the Minigame chunk
        if (player.getWorldLocation().getRegionID() == 15150)
        {
             inMinigame = true;
        }
        else inMinigame = false;

        return inMinigame;
    }

    public static boolean
    onRedTeam(Client client)
    {
        int headSlot;
        
        headSlot = client.getLocalPlayer()
                         .getPlayerComposition().getEquipmentId(KitType.HEAD);
        onRedTeam = headSlot == ItemID.BREW_RED_PIRATE_HAT;
        
        return onRedTeam;
    }

    public static WorldArea
    getTeamsHalf()
    {
        if (onRedTeam)
            return RED_HALF;
        else
            return BLUE_HALF;
    }

    public static void
    drawHighlightedGameObject(Graphics2D           graphics,
                              ModelOutlineRenderer outlineRenderer,
                              Config               config,
                              GameObject           obj,
                              Config.HighlightType type,
                              Color                colour)
    {
        if (type == Config.HighlightType.NONE)
        {
            return;
        }
        else if (type == Config.HighlightType.OUTLINE)
        {
            outlineRenderer.drawOutline(obj, config.outlineWidth(), colour, 1);
        }
        else if (type == Config.HighlightType.HULL_OUTLINE)
        {
            graphics.setColor(colour);
            graphics.draw(obj.getConvexHull());
        }
        else if (type == Config.HighlightType.HULL_FILLED)
        {
            graphics.setColor(colour);
            graphics.fill(obj.getConvexHull());
        }
        else if (type == Config.HighlightType.CLICKBOX_OUTLINE)
        {
            graphics.setColor(colour);
            graphics.draw(obj.getClickbox());
        }
        else if (type == Config.HighlightType.CLICKBOX_FILLED)
        {
            graphics.setColor(colour);
            graphics.fill(obj.getClickbox());
        }
    }

    /* Non-static version with fewer parameters */
    public void
    drawHighlightedGameObject(Graphics2D           graphics,
                              GameObject           obj,
                              Config.HighlightType type,
                              Color                colour)
    {
        drawHighlightedGameObject(graphics, modelOutlineRenderer, config, obj, type, colour);
    }

    public void
    configChanged(ConfigChanged event)
    {
        FONT = new Font(FontManager.getRunescapeFont().getName(), Font.PLAIN, config.fontSize());
    }
}
