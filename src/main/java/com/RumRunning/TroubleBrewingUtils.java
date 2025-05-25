package com.RumRunning;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class TroubleBrewingUtils
extends      Overlay
{
    private final Client               client;
    private final ModelOutlineRenderer modelOutlineRenderer;
    private final ItemManager          itemManager;

    private final TroubleBrewingPlugin plugin;
    private final TroubleBrewingConfig config;

    static boolean inMinigame;
    static boolean onRedTeam;

    public static final WorldPoint RED_TEAM_LOCATION  = new WorldPoint(3815, 3000, 0);
    public static final WorldPoint BLUE_TEAM_LOCATION = new WorldPoint(3815, 2950, 0);

    public static final int DRAW_DISTANCE = 28;

    public static BufferedImage ICON_LOGS;
    public static BufferedImage ICON_TINDERBOX;
    public static BufferedImage ICON_BUCKET_OF_WATER;
    public static BufferedImage ICON_PIPE_SECTION;
    public static BufferedImage ICON_BRIDGE_SECTION;
    public static BufferedImage ICON_LUMBER_PATCH;

    public static Font FONT;

    @Inject
    private TroubleBrewingUtils(Client               client,
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
    render(Graphics2D graphics) {
        final Player player = client.getLocalPlayer();

        // Check if player is inside the Minigame chunk
        if (!(player.getWorldLocation().getRegionID() == 15150))
        {
             inMinigame = false;
             return (null);
        }
        else inMinigame = true;

        // Check if player is in red team
        onRedTeam = client.getItemContainer(InventoryID.EQUIPMENT)
                          .getItem(EquipmentInventorySlot.HEAD
                          .getSlotIdx()).getId() == ItemID.BREW_RED_PIRATE_HAT;

        return null;
    }

    public static void
    drawHighlightedGameObject(Graphics2D                         graphics,
                              ModelOutlineRenderer               outlineRenderer,
                              TroubleBrewingConfig               config,
                              GameObject                         obj,
                              TroubleBrewingConfig.HighlightType type,
                              Color                              colour)
    {
        if (type == TroubleBrewingConfig.HighlightType.NONE)
        {
            return;
        }
        else if (type == TroubleBrewingConfig.HighlightType.OUTLINE)
        {
            outlineRenderer.drawOutline(obj, config.outlineWidth(), colour, 1);
        }
        else if (type == TroubleBrewingConfig.HighlightType.HULL_OUTLINE)
        {
            graphics.setColor(colour);
            graphics.draw(obj.getConvexHull());
        }
        else if (type == TroubleBrewingConfig.HighlightType.HULL_FILLED)
        {
            graphics.setColor(colour);
            graphics.fill(obj.getConvexHull());
        }
        else if (type == TroubleBrewingConfig.HighlightType.CLICKBOX_OUTLINE)
        {
            graphics.setColor(colour);
            graphics.draw(obj.getClickbox());
        }
        else if (type == TroubleBrewingConfig.HighlightType.CLICKBOX_FILLED)
        {
            graphics.setColor(colour);
            graphics.fill(obj.getClickbox());
        }
    }

    /* Non-static version with fewer parameters */
    public void
    drawHighlightedGameObject(Graphics2D                         graphics,
                              GameObject                         obj,
                              TroubleBrewingConfig.HighlightType type,
                              Color                              colour)
    {
        drawHighlightedGameObject(graphics, modelOutlineRenderer, config, obj, type, colour);
    }

    public void
    configChanged(ConfigChanged event)
    {
        FONT = new Font(FontManager.getRunescapeFont().getName(), Font.PLAIN, config.fontSize());
    }
}
