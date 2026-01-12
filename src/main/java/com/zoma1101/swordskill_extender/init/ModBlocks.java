package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.block.custom.WeaponWorkbenchBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Swordskill_extender.MODID);

    // 作業台ブロックの登録
    // 鉄ブロックのような硬さと挙動に設定
    public static final RegistryObject<Block> WEAPON_WORKBENCH = registerBlock("weapon_workbench",
            () -> new WeaponWorkbenchBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).strength(5f).requiresCorrectToolForDrops().noOcclusion()));

    // ブロックを登録し、同時にBlockItemも登録するヘルパーメソッド
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // BlockItemをModItemsに登録する
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}