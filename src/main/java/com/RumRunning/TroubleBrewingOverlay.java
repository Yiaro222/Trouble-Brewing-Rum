package com.RumRunning;

import java.awt.*;

import javax.inject.Inject;

import lombok.Setter;
import net.runelite.api.Point;
import net.runelite.api.*;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.TileObject;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.api.events.*;

public class TroubleBrewingOverlay extends Overlay {
    private final Client client;
    private final TroubleBrewingPlugin plugin;
    private final TroubleBrewingConfig config;
    private final ModelOutlineRenderer modelOutlineRenderer;

    @Setter
    public GameObject renderableJunglePlant;

    @Inject
    private TroubleBrewingOverlay(Client client, TroubleBrewingPlugin plugin, TroubleBrewingConfig config, ModelOutlineRenderer modelOutlineRenderer)
    {
        this.client = client;
        this.plugin = plugin;
        this.config = config;
        this.modelOutlineRenderer = modelOutlineRenderer;
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPriority(OverlayPriority.HIGH);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (renderableJunglePlant != null) {
            modelOutlineRenderer.drawOutline(renderableJunglePlant, 3, Color.ORANGE, 1);
            //graphics.draw(renderableJunglePlant.getConvexHull());

        }else {
            return null;
        }

        Player player = client.getLocalPlayer();
        Point test = player.getCanvasTextLocation(graphics, "", player.getLogicalHeight() + 40);
        modelOutlineRenderer.drawOutline(player, 3, Color.MAGENTA,1);
        OverlayUtil.renderTextLocation(graphics, test, "Certified Rum Runner", Color.MAGENTA);


        return null;
    }
}