
package com.RumRunning;

import com.google.inject.Inject;

import java.awt.*;

import net.runelite.api.*;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;



public class TroubleBrewingInterfaceOverlay
extends      OverlayPanel
{
	private final Client client;
	
	private final TroubleBrewingPlugin plugin;
	
	
	
	@Inject
	private
	TroubleBrewingInterfaceOverlay(Client client, TroubleBrewingPlugin plugin)
	{
		super(plugin);
		
		this.client = client;
		this.plugin = plugin;
		
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority(OverlayPriority.LOW);
	}
	
	@Override
	public Dimension
	render(Graphics2D graphics)
	{
		panelComponent.getChildren().clear();
		panelComponent.getChildren().add(LineComponent.builder()
		                            .left("example: ")
		                            .right("" + client.getGameCycle())
		                            .build());
		
		return(super.render(graphics));
	}
}



