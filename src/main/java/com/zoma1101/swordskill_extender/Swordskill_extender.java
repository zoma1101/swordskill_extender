package com.zoma1101.swordskill_extender;

import com.zoma1101.swordskill_extender.datagen.ModItemModelProvider;
import com.zoma1101.swordskill_extender.datagen.ModItemTagsProvider;
import com.zoma1101.swordskill_extender.datagen.ModRecipeProvider;
import com.zoma1101.swordskill_extender.init.ModCreativeModeTabs;
import com.zoma1101.swordskill_extender.init.ModItems;
import com.zoma1101.swordskill_extender.item.weapon.GreatswordItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.concurrent.CompletableFuture;

@Mod(Swordskill_extender.MODID)
public class Swordskill_extender {

    public static final String MODID = "swordskill_extender";

    public Swordskill_extender(IEventBus modEventBus) {
        ModItems.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        modEventBus.addListener(this::gatherData);

        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.register(GreatswordItem.GreatswordEventHandler.class);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(generator.getPackOutput(), existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(generator.getPackOutput(), lookupProvider));

        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(generator.getPackOutput(), lookupProvider, MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.Provider provider) {
            }
        };
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(generator.getPackOutput(), lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }
}
