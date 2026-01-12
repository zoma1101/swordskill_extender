package com.zoma1101.swordskill_extender.datagen;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.client.model.generators.loaders.SeparateTransformsModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Swordskill_extender.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {

        generatedItem(ModItems.PROFICIENCY_SCROLL);
        generatedItem(ModItems.SKILL_SCROLL);
        // Iron
        greatswordHandheldItem(ModItems.IRON_GREATSWORD);
        spearHandheldItem(ModItems.IRON_SPEAR);
        handheldItem(ModItems.IRON_MACE);
        handheldItem(ModItems.IRON_RAPIER);
        handheldItem(ModItems.IRON_DAGGER);
        handheldItem(ModItems.IRON_KATANA);
        separatePerspectiveItem(ModItems.IRON_CLAW, "iron_claw_3d");
        scytheHandheldItem(ModItems.IRON_SCYTHE);
        battleaxeHandheldItem(ModItems.IRON_BATTLEAXE);

        // Stone
        greatswordHandheldItem(ModItems.STONE_GREATSWORD);
        spearHandheldItem(ModItems.STONE_SPEAR);
        handheldItem(ModItems.STONE_MACE);
        handheldItem(ModItems.STONE_RAPIER);
        handheldItem(ModItems.STONE_DAGGER);
        handheldItem(ModItems.STONE_KATANA);
        separatePerspectiveItem(ModItems.STONE_CLAW, "stone_claw_3d");
        scytheHandheldItem(ModItems.STONE_SCYTHE);
        battleaxeHandheldItem(ModItems.STONE_BATTLEAXE);

        // Gold
        greatswordHandheldItem(ModItems.GOLD_GREATSWORD);
        spearHandheldItem(ModItems.GOLD_SPEAR);
        handheldItem(ModItems.GOLD_MACE);
        handheldItem(ModItems.GOLD_RAPIER);
        handheldItem(ModItems.GOLD_DAGGER);
        handheldItem(ModItems.GOLD_KATANA);
        separatePerspectiveItem(ModItems.GOLD_CLAW, "gold_claw_3d");
        scytheHandheldItem(ModItems.GOLD_SCYTHE);
        battleaxeHandheldItem(ModItems.GOLD_BATTLEAXE);

        // Diamond
        greatswordHandheldItem(ModItems.DIAMOND_GREATSWORD);
        spearHandheldItem(ModItems.DIAMOND_SPEAR);
        handheldItem(ModItems.DIAMOND_MACE);
        handheldItem(ModItems.DIAMOND_RAPIER);
        handheldItem(ModItems.DIAMOND_DAGGER);
        handheldItem(ModItems.DIAMOND_KATANA);
        separatePerspectiveItem(ModItems.DIAMOND_CLAW, "diamond_claw_3d");
        scytheHandheldItem(ModItems.DIAMOND_SCYTHE);
        battleaxeHandheldItem(ModItems.DIAMOND_BATTLEAXE);

        // Netherite
        greatswordHandheldItem(ModItems.NETHERITE_GREATSWORD);
        spearHandheldItem(ModItems.NETHERITE_SPEAR);
        handheldItem(ModItems.NETHERITE_MACE);
        handheldItem(ModItems.NETHERITE_RAPIER);
        handheldItem(ModItems.NETHERITE_DAGGER);
        handheldItem(ModItems.NETHERITE_KATANA);
        separatePerspectiveItem(ModItems.NETHERITE_CLAW, "netherite_claw_3d");
        scytheHandheldItem(ModItems.NETHERITE_SCYTHE);
        battleaxeHandheldItem(ModItems.NETHERITE_BATTLEAXE);
    }

    // 視点によってモデルを切り替えるヘルパーメソッド (爪など)
    private void separatePerspectiveItem(RegistryObject<Item> item, String model3dName) {
        String itemName = item.getId().getPath();
        ItemModelBuilder guiModel = withExistingParent(itemName + "_gui", "item/generated")
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + itemName));
        ItemModelBuilder model3d = withExistingParent(itemName + "_3d_ref", modLoc("item/" + model3dName));
        withExistingParent(itemName, "item/generated")
                .customLoader(SeparateTransformsModelBuilder::begin)
                .base(guiModel)
                .perspective(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, model3d)
                .perspective(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, model3d)
                .perspective(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, model3d)
                .perspective(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, model3d)
                .end();
    }


    // ★追加: 通常のアイテム（本、素材など）用の生成メソッド
    // 親モデルに "item/generated" を使用します
    private ItemModelBuilder generatedItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()));
    }
    // 通常の武器用
    private ItemModelBuilder handheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()));
    }

    // ★槍 (Spear) - diamond_spear.json の設定を反映
    private ItemModelBuilder spearHandheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 55)
                .translation(0, 9.5F, 1.0F)
                .scale(1.5F, 1.5F, 1.5F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(0, 90, -55)
                .translation(0, 5.5F, 0.5F)
                .scale(1.5F, 1.5F, 1.5F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -85, 40)
                .translation(1.13F, 3.2F, 0.13F)
                .scale(1.2F, 1.2F, 1.2F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(72, -85, 40)
                .translation(1.13F, 3.2F, 1.13F)
                .scale(1.2F, 1.2F, 1.2F)
                .end()
                .end();
    }

    // ★鎌 (Scythe) - diamond_scythe.json の設定を反映
    private ItemModelBuilder scytheHandheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(-23.25F, -90, 0)
                .translation(0, 12.25F, 5.75F)
                .scale(2.0F, 2.0F, 2.0F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(-22.5F, 90, 0)
                .translation(0, 10.5F, 6.0F)
                .scale(2.0F, 2.0F, 2.0F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(1.13F, 3.2F, -1.12F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 90, -25)
                .translation(1.13F, 3.2F, -1.12F)
                .end()
                .end();
    }

    // ★両手斧 (Battleaxe) - diamond_battleaxe.json の設定を反映
    private ItemModelBuilder battleaxeHandheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 55)
                .translation(0, 5.5F, 0.25F)
                .scale(1.5F, 1.5F, 1.5F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(0, 90, -55)
                .translation(0, 5.5F, 0.5F)
                .scale(1.5F, 1.5F, 1.5F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(1.13F, 5.7F, 0.88F)
                .scale(1.25F, 1.25F, 1.25F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(0, 90, -25)
                .translation(1.13F, 4.45F, 0.13F)
                .scale(1.25F, 1.25F, 1.25F)
                .end()
                .end();
    }

    // ★大剣 (Greatsword) - diamond_greatsword.json の設定を反映
    private ItemModelBuilder greatswordHandheldItem(RegistryObject<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "item/" + item.getId().getPath()))
                .transforms()
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
                .rotation(0, -90, 55)
                .translation(0.5F, 6.5F, 0.5F)
                .scale(1.2F, 1.2F, 1.2F)
                .end()
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
                .rotation(0, 90, -55)
                .translation(0, 6.25F, 0.5F)
                .scale(1.2F, 1.2F, 1.2F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(1.13F, 4.45F, -0.12F)
                .end()
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
                .rotation(-1, 90, -25)
                .translation(1.13F, 4.7F, -0.12F)
                .end()
                .end();
    }
}