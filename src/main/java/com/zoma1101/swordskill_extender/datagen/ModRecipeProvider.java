package com.zoma1101.swordskill_extender.datagen;

import com.zoma1101.swordskill_extender.init.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        generateWeaponRecipes(output, Ingredient.of(Items.IRON_INGOT), Ingredient.of(Items.GOLD_INGOT), "iron",
                ModItems.IRON_GREATSWORD, ModItems.IRON_MACE,
                ModItems.IRON_RAPIER, ModItems.IRON_DAGGER, ModItems.IRON_KATANA,
                ModItems.IRON_CLAW, ModItems.IRON_SCYTHE);

        generateWeaponRecipes(output, Ingredient.of(ItemTags.STONE_TOOL_MATERIALS), Ingredient.of(Items.IRON_INGOT), "stone",
                ModItems.STONE_GREATSWORD, ModItems.STONE_MACE,
                ModItems.STONE_RAPIER, ModItems.STONE_DAGGER, ModItems.STONE_KATANA,
                ModItems.STONE_CLAW, ModItems.STONE_SCYTHE);

        generateWeaponRecipes(output, Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.DIAMOND), "gold",
                ModItems.GOLD_GREATSWORD, ModItems.GOLD_MACE,
                ModItems.GOLD_RAPIER, ModItems.GOLD_DAGGER, ModItems.GOLD_KATANA,
                ModItems.GOLD_CLAW, ModItems.GOLD_SCYTHE);

        generateWeaponRecipes(output, Ingredient.of(Items.DIAMOND), Ingredient.of(Items.NETHERITE_INGOT), "diamond",
                ModItems.DIAMOND_GREATSWORD, ModItems.DIAMOND_MACE,
                ModItems.DIAMOND_RAPIER, ModItems.DIAMOND_DAGGER, ModItems.DIAMOND_KATANA,
                ModItems.DIAMOND_CLAW, ModItems.DIAMOND_SCYTHE);

        generateNetheriteRecipes(output);
    }

    private void generateWeaponRecipes(RecipeOutput output, Ingredient material, Ingredient centerMaterial, String materialName,
                                       DeferredItem<Item> greatsword, DeferredItem<Item> mace,
                                       DeferredItem<Item> rapier, DeferredItem<Item> dagger, DeferredItem<Item> katana,
                                       DeferredItem<Item> claw, DeferredItem<Item> scythe) {
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, greatsword.get())
                .pattern(" # ")
                .pattern("###")
                .pattern(" / ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, mace.get())
                .pattern(" # ")
                .pattern("#*#")
                .pattern(" / ")
                .define('#', material)
                .define('*', centerMaterial)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, rapier.get())
                .requires(material, 2)
                .requires(Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, dagger.get())
                .pattern(" #")
                .pattern("/ ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, katana.get())
                .pattern("  #")
                .pattern("G# ")
                .pattern("/G ")
                .define('#', material)
                .define('G', Items.GOLD_NUGGET)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, claw.get())
                .pattern(" #")
                .pattern("# ")
                .define('#', material)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, scythe.get())
                .pattern("###")
                .pattern("#/ ")
                .pattern("/  ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(output);
    }

    private void generateNetheriteRecipes(RecipeOutput output) {
        Ingredient template = Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        Ingredient addition = Ingredient.of(Items.NETHERITE_INGOT);

        smithing(output, template, ModItems.DIAMOND_GREATSWORD, addition, ModItems.NETHERITE_GREATSWORD);
        smithing(output, template, ModItems.DIAMOND_MACE, addition, ModItems.NETHERITE_MACE);
        smithing(output, template, ModItems.DIAMOND_RAPIER, addition, ModItems.NETHERITE_RAPIER);
        smithing(output, template, ModItems.DIAMOND_DAGGER, addition, ModItems.NETHERITE_DAGGER);
        smithing(output, template, ModItems.DIAMOND_KATANA, addition, ModItems.NETHERITE_KATANA);
        smithing(output, template, ModItems.DIAMOND_CLAW, addition, ModItems.NETHERITE_CLAW);
        smithing(output, template, ModItems.DIAMOND_SCYTHE, addition, ModItems.NETHERITE_SCYTHE);
    }

    private void smithing(RecipeOutput output, Ingredient template, DeferredItem<Item> base,
                          Ingredient addition, DeferredItem<Item> result) {
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(base.get()),
                        addition,
                        RecipeCategory.COMBAT,
                        result.get())
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(output, getItemName(result.get()) + "_smithing");
    }
}
