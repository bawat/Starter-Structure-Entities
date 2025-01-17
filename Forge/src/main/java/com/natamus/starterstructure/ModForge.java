package com.natamus.starterstructure;

import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.check.ShouldLoadCheck;
import com.natamus.starterstructure.forge.config.IntegrateForgeConfig;
import com.natamus.starterstructure.forge.events.ForgeStructureCreationEvents;
import com.natamus.starterstructure.forge.events.ForgeStructureProtectionEvents;
import com.natamus.starterstructure.forge.events.ForgeStructureSpawnPointEvents;
import com.natamus.starterstructure.util.Reference;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Reference.MOD_ID)
public class ModForge {
	
	public ModForge(FMLJavaModLoadingContext modLoadingContext) {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		IEventBus modEventBus = modLoadingContext.getModEventBus();
		modEventBus.addListener(this::loadComplete);

		setGlobalConstants();
		ModCommon.init();

		IntegrateForgeConfig.registerScreen(modLoadingContext);

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void loadComplete(final FMLLoadCompleteEvent event) {
        MinecraftForge.EVENT_BUS.register(new ForgeStructureProtectionEvents());
    	MinecraftForge.EVENT_BUS.register(new ForgeStructureCreationEvents());
        MinecraftForge.EVENT_BUS.register(new ForgeStructureSpawnPointEvents());
	}

	private static void setGlobalConstants() {

	}
}