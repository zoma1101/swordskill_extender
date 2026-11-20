package com.zoma1101.swordskill_extender.datagen;

import com.zoma1101.swordskill_extender.init.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput pOutput) {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> pWriter) {
        // 1. 鉄 (Iron)
        generateWeaponRecipes(pWriter, Ingredient.of(Items.IRON_INGOT), Ingredient.of(Items.GOLD_INGOT), "iron",
                ModItems.IRON_GREATSWORD, ModItems.IRON_MACE,
                ModItems.IRON_RAPIER, ModItems.IRON_DAGGER, ModItems.IRON_KATANA,
                ModItems.IRON_CLAW, ModItems.IRON_SCYTHE);

        // 2. 石 (Stone)
        generateWeaponRecipes(pWriter, Ingredient.of(ItemTags.STONE_TOOL_MATERIALS), Ingredient.of(Items.IRON_INGOT), "stone",
                ModItems.STONE_GREATSWORD, ModItems.STONE_MACE,
                ModItems.STONE_RAPIER, ModItems.STONE_DAGGER, ModItems.STONE_KATANA,
                ModItems.STONE_CLAW, ModItems.STONE_SCYTHE);

        // 3. 金 (Gold)
        generateWeaponRecipes(pWriter, Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.DIAMOND), "gold",
                ModItems.GOLD_GREATSWORD, ModItems.GOLD_MACE,
                ModItems.GOLD_RAPIER, ModItems.GOLD_DAGGER, ModItems.GOLD_KATANA,
                ModItems.GOLD_CLAW, ModItems.GOLD_SCYTHE);

        // 4. ダイヤモンド (Diamond)
        generateWeaponRecipes(pWriter, Ingredient.of(Items.DIAMOND), Ingredient.of(Items.NETHERITE_INGOT), "diamond",
                ModItems.DIAMOND_GREATSWORD, ModItems.DIAMOND_MACE,
                ModItems.DIAMOND_RAPIER, ModItems.DIAMOND_DAGGER, ModItems.DIAMOND_KATANA,
                ModItems.DIAMOND_CLAW, ModItems.DIAMOND_SCYTHE);

        generateNetheriteRecipes(pWriter);
    }

    private void generateWeaponRecipes(Consumer<FinishedRecipe> pWriter, Ingredient material, Ingredient centerMaterial, String materialName,
                                       RegistryObject<Item> greatsword, RegistryObject<Item> mace,
                                       RegistryObject<Item> rapier, RegistryObject<Item> dagger, RegistryObject<Item> katana,
                                       RegistryObject<Item> claw, RegistryObject<Item> scythe) {
        // (省略: 以前の内容と同じ)
        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, greatsword.get())
                .pattern(" # ")
                .pattern("###")
                .pattern(" / ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, mace.get())
                .pattern(" # ")
                .pattern("#*#")
                .pattern(" / ")
                .define('#', material)
                .define('*', centerMaterial)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.COMBAT, rapier.get())
                .requires(material, 2)
                .requires(Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, dagger.get())
                .pattern(" #")
                .pattern("/ ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, katana.get())
                .pattern("  #")
                .pattern("G# ")
                .pattern("/G ")
                .define('#', material)
                .define('G', Items.GOLD_NUGGET)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, claw.get())
                .pattern(" #")
                .pattern("# ")
                .define('#', material)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, scythe.get())
                .pattern("###")
                .pattern("#/ ")
                .pattern("/  ")
                .define('#', material)
                .define('/', Items.STICK)
                .unlockedBy("has_" + materialName, has(Items.STICK))
                .save(pWriter);

    }

    // ★修正: 引数を削除し、メソッド内で直接 ModItems の変数を参照する
    private void generateNetheriteRecipes(Consumer<FinishedRecipe> pWriter) {
        Ingredient template = Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE);
        Ingredient addition = Ingredient.of(Items.NETHERITE_INGOT);

        smithing(pWriter, template, ModItems.DIAMOND_GREATSWORD, addition, ModItems.NETHERITE_GREATSWORD);
        smithing(pWriter, template, ModItems.DIAMOND_MACE, addition, ModItems.NETHERITE_MACE);
        smithing(pWriter, template, ModItems.DIAMOND_RAPIER, addition, ModItems.NETHERITE_RAPIER);
        smithing(pWriter, template, ModItems.DIAMOND_DAGGER, addition, ModItems.NETHERITE_DAGGER);
        smithing(pWriter, template, ModItems.DIAMOND_KATANA, addition, ModItems.NETHERITE_KATANA);
        smithing(pWriter, template, ModItems.DIAMOND_CLAW, addition, ModItems.NETHERITE_CLAW);
        smithing(pWriter, template, ModItems.DIAMOND_SCYTHE, addition, ModItems.NETHERITE_SCYTHE);
    }

    // 個別の鍛冶台レシピ生成メソッド
    private void smithing(Consumer<FinishedRecipe> pWriter, Ingredient template, RegistryObject<Item> base, Ingredient addition, RegistryObject<Item> result) {
        SmithingTransformRecipeBuilder.smithing(
                        template,
                        Ingredient.of(base.get()),
                        addition,
                        RecipeCategory.COMBAT,
                        result.get()
                )
                .unlocks("has_netherite_ingot", has(Items.NETHERITE_INGOT))
                .save(pWriter, getItemName(result.get()) + "_smithing");
    }
}