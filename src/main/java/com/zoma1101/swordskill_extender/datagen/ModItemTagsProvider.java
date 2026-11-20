package com.zoma1101.swordskill_extender.datagen;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, Swordskill_extender.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        // 1. 武器種ごとの独自タグを定義
        TagKey<Item> greatswords = createTag("greatswords");
        TagKey<Item> maces = createTag("maces");
        TagKey<Item> rapiers = createTag("rapiers");
        TagKey<Item> daggers = createTag("daggers");
        TagKey<Item> katanas = createTag("katanas");
        TagKey<Item> claws = createTag("claws");
        TagKey<Item> scythes = createTag("scythes");

        // ★追加: 両手持ち武器用のタグ定義
        TagKey<Item> twoHandedWeapons = createTag("two_handed_weapons");

        // 2. 各タグにアイテムを登録
        tag(greatswords).add(
                ModItems.IRON_GREATSWORD.get(), ModItems.STONE_GREATSWORD.get(), ModItems.GOLD_GREATSWORD.get(),
                ModItems.DIAMOND_GREATSWORD.get(), ModItems.NETHERITE_GREATSWORD.get()
        );

        tag(maces).add(
                ModItems.IRON_MACE.get(), ModItems.STONE_MACE.get(), ModItems.GOLD_MACE.get(),
                ModItems.DIAMOND_MACE.get(), ModItems.NETHERITE_MACE.get()
        );

        tag(rapiers).add(
                ModItems.IRON_RAPIER.get(), ModItems.STONE_RAPIER.get(), ModItems.GOLD_RAPIER.get(),
                ModItems.DIAMOND_RAPIER.get(), ModItems.NETHERITE_RAPIER.get()
        );

        tag(daggers).add(
                ModItems.IRON_DAGGER.get(), ModItems.STONE_DAGGER.get(), ModItems.GOLD_DAGGER.get(),
                ModItems.DIAMOND_DAGGER.get(), ModItems.NETHERITE_DAGGER.get()
        );

        tag(katanas).add(
                ModItems.IRON_KATANA.get(), ModItems.STONE_KATANA.get(), ModItems.GOLD_KATANA.get(),
                ModItems.DIAMOND_KATANA.get(), ModItems.NETHERITE_KATANA.get()
        );

        tag(claws).add(
                ModItems.IRON_CLAW.get(), ModItems.STONE_CLAW.get(), ModItems.GOLD_CLAW.get(),
                ModItems.DIAMOND_CLAW.get(), ModItems.NETHERITE_CLAW.get()
        );

        tag(scythes).add(
                ModItems.IRON_SCYTHE.get(), ModItems.STONE_SCYTHE.get(), ModItems.GOLD_SCYTHE.get(),
                ModItems.DIAMOND_SCYTHE.get(), ModItems.NETHERITE_SCYTHE.get()
        );

        // 両手持ちタグに、大剣・鎌のタグを登録
        tag(twoHandedWeapons)
                .addTag(greatswords)
                .addTag(scythes);

        // 3. バニラのタグに統合 (互換性確保)
        tag(ItemTags.SWORDS)
                .addTag(greatswords)
                .addTag(maces)
                .addTag(rapiers)
                .addTag(daggers)
                .addTag(katanas)
                .addTag(claws)
                .addTag(scythes);
    }

    // タグ作成用のヘルパーメソッド
    private TagKey<Item> createTag(String name) {
        return ItemTags.create(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, name));
    }
}