package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.block.entity.WeaponWorkbenchBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Swordskill_extender.MODID);

    public static final RegistryObject<BlockEntityType<WeaponWorkbenchBlockEntity>> WEAPON_WORKBENCH_BE =
            BLOCK_ENTITIES.register("weapon_workbench_be", () ->
                    BlockEntityType.Builder.of(WeaponWorkbenchBlockEntity::new,
                            ModBlocks.WEAPON_WORKBENCH.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}