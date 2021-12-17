package com.unascribed.legacycustompublish;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

@Mod(modid="legacycustompublish", name="Legacy Custom Publish", useMetadata=true)
public class LegacyCustomPublish {

	@Mod.ServerStarting
	public void onServerStarting(FMLServerStartingEvent e) {
		if (!e.getServer().isDedicatedServer()) {
			e.registerServerCommand(new CustomPublishCommand());
		}
	}
	
}
