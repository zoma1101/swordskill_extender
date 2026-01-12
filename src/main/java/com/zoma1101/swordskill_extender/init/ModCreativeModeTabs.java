package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    // クリエイティブタブのレジストリ
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Swordskill_extender.MODID);

    // タブの登録
    public static final RegistryObject<CreativeModeTab> EXTENDER_TAB = CREATIVE_MODE_TABS.register("extender_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_KATANA.get())) // タブのアイコン
                    .title(Component.translatable("creativetab.swordskill_extender_tab")) // タブ名
                    .displayItems((pParameters, pOutput) -> {
                        // ModItemsに登録されている全てのアイテムを自動で追加
                        // これにより、ModItemsに新しい武器を追加するだけでタブにも反映されます
                        ModItems.ITEMS.getEntries().forEach(item -> pOutput.accept(item.get()));

                        // ブロック（ワークベンチ）の追加
                        // ModItemsに含まれていない場合はここで追加します
                        pOutput.accept(ModBlocks.WEAPON_WORKBENCH.get());

                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}