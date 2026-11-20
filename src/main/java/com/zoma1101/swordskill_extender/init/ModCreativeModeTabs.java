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
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Swordskill_extender.MODID);

    public static final RegistryObject<CreativeModeTab> EXTENDER_TAB = CREATIVE_MODE_TABS.register("extender_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_KATANA.get()))
                    .title(Component.translatable("creativetab.swordskill_extender_tab"))
                    .displayItems((pParameters, pOutput) ->
                            ModItems.ITEMS.getEntries().forEach(item -> pOutput.accept(item.get())))
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
