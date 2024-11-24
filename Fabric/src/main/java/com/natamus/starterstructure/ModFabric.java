package com.natamus.starterstructure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import com.natamus.collective.check.RegisterMod;
import com.natamus.collective.check.ShouldLoadCheck;
import com.natamus.collective.fabric.callbacks.*;
import com.natamus.starterstructure.events.StructureCreationEvents;
import com.natamus.starterstructure.events.StructureProtectionEvents;
import com.natamus.starterstructure.events.StructureSpawnPointEvents;
import com.natamus.starterstructure.util.Reference;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ServerLevelData;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;

public class ModFabric implements ModInitializer {
	
	@Override
	public void onInitialize() {
		if (!ShouldLoadCheck.shouldLoad(Reference.MOD_ID)) {
			return;
		}

		setGlobalConstants();
		ModCommon.init();

		loadEvents();

		RegisterMod.register(Reference.NAME, Reference.MOD_ID, Reference.VERSION, Reference.ACCEPTED_VERSIONS);
	}

	private void generateSchematicUsingWorldEdit(File schematicFile, BlockPos structurePos, ServerLevel level) {
    	/*
    	ServerPlayerEntity player = context.getSource().getPlayer();
		SessionManager manager = WorldEdit.getInstance().getSessionManager();
        FabricPlayer owner = FabricAdapter.adaptPlayer(player);
        LocalSession session = manager.get(owner);*/
        
        Clipboard clipboard = null;

		ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
		try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
		    clipboard = reader.read();
		} catch (IOException e) {
			//LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
		
		World worldEditWorld = FabricAdapter.adapt(level);
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(worldEditWorld)) {
		    Operation operation = new ClipboardHolder(clipboard)
		            .createPaste(editSession)
		            .to(BlockVector3.at(structurePos.getX(), structurePos.getY(), structurePos.getZ()))
		            .copyEntities(true)
		            // configure here
		            .build();
		    Operations.complete(operation);
		} catch (WorldEditException e) {
			// TODO Auto-generated catch block
			//LOGGER.error(e.getMessage());
			e.printStackTrace();
		}
    }
	
	private void loadEvents() {
		// StructureCreationEvents
		CollectiveMinecraftServerEvents.WORLD_SET_SPAWN.register((ServerLevel serverLevel, ServerLevelData serverLevelData) -> {
			StructureCreationEvents.onLevelSpawn(serverLevel, serverLevelData, this::generateSchematicUsingWorldEdit);
		});

		ServerWorldEvents.LOAD.register((MinecraftServer server, ServerLevel serverLevel) -> {
			StructureCreationEvents.onLevelLoad(serverLevel);
		});

		// StructureProtectionEvents
		PlayerBlockBreakEvents.BEFORE.register((level, player, pos, state, entity) -> {
			return StructureProtectionEvents.onBlockBreak(level, player, pos, state, entity);
		});

		CollectiveBlockEvents.BLOCK_PLACE.register((Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) -> {
			return StructureProtectionEvents.onBlockPlace(level, blockPos, blockState, livingEntity, itemStack);
		});

		CollectivePistonEvents.PRE_PISTON_ACTIVATE.register((Level level, BlockPos blockPos, Direction direction, boolean isExtending) -> {
			return StructureProtectionEvents.onPistonMove(level, blockPos, direction, isExtending);
		});

		CollectiveExplosionEvents.EXPLOSION_DETONATE.register((Level level, Entity sourceEntity, Explosion explosion) -> {
			StructureProtectionEvents.onTNTExplode(level, sourceEntity, explosion);
		});

		CollectiveEntityEvents.ON_LIVING_ATTACK.register((Level level, Entity entity, DamageSource damageSource, float damageAmount) -> {
			return StructureProtectionEvents.onLivingAttack(level, entity, damageSource, damageAmount);
		});

		// StructureSpawnPointEvents
		ServerPlayerEvents.AFTER_RESPAWN.register((ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) -> {
			StructureSpawnPointEvents.onPlayerRespawn(oldPlayer, newPlayer, alive);
		});
		ServerEntityEvents.ENTITY_LOAD.register((Entity entity, ServerLevel serverLevel) -> {
			StructureSpawnPointEvents.onEntityJoin(serverLevel, entity);
		});
	}

	private static void setGlobalConstants() {

	}
}
