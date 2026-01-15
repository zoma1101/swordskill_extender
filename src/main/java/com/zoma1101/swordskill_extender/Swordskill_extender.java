package com.zoma1101.swordskill_extender;

import com.mojang.logging.LogUtils;
import com.zoma1101.swordskill_extender.datagen.ModItemModelProvider;
import com.zoma1101.swordskill_extender.datagen.ModItemTagsProvider;
import com.zoma1101.swordskill_extender.datagen.ModRecipeProvider;
import com.zoma1101.swordskill_extender.init.*;
import com.zoma1101.swordskill_extender.network.PacketHandler;
import com.zoma1101.swordskill_extender.recipe.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

@Mod(Swordskill_extender.MODID)
public class Swordskill_extender {

    public static final String MODID = "swordskill_extender";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Swordskill_extender(FMLJavaModLoadingContext ctx) {
        IEventBus modEventBus = ctx.getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModCreativeModeTabs.register(modEventBus);
        ModRecipes.register(modEventBus);

        PacketHandler.register();
        MinecraftForge.EVENT_BUS.register(this);

        modEventBus.addListener(this::gatherData);
        modEventBus.addListener(this::commonSetup);
    }

    private void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(packOutput, existingFileHelper));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(packOutput));

        BlockTagsProvider blockTagsProvider = new BlockTagsProvider(packOutput, lookupProvider, MODID, existingFileHelper) {
            @Override
            protected void addTags(HolderLookup.Provider pProvider) {
            }
        };
        generator.addProvider(event.includeServer(), blockTagsProvider);
        generator.addProvider(event.includeServer(), new ModItemTagsProvider(packOutput, lookupProvider, blockTagsProvider.contentsGetter(), existingFileHelper));
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(SkillRequirementInit::register);
    }
}