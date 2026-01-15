package com.zoma1101.swordskill_extender.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.init.ModBlocks;
import com.zoma1101.swordskill_extender.recipe.WeaponWorkbenchRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class WeaponWorkbenchRecipeCategory implements IRecipeCategory<WeaponWorkbenchRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "weapon_workbench");
    public static final RecipeType<WeaponWorkbenchRecipe> RECIPE_TYPE = new RecipeType<>(UID, WeaponWorkbenchRecipe.class);

    // バニラのチェストテクスチャ (スロット描画用)
    private static final ResourceLocation VANILLA_CHEST_TEXTURE = ResourceLocation.parse("textures/gui/container/generic_54.png");

    private final IDrawable background;
    private final IDrawable icon;

    public WeaponWorkbenchRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(160, 110);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.WEAPON_WORKBENCH.get()));
    }

    @Override
    public RecipeType<WeaponWorkbenchRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.swordskill_extender.weapon_workbench");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(WeaponWorkbenchRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        // バニラのスロット枠を描画
        RenderSystem.setShaderTexture(0, VANILLA_CHEST_TEXTURE);

        // 5x5 入力グリッドの描画
        // Menuでの開始位置: x=14, y=18
        int gridStartX = 14;
        int gridStartY = 18;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                // generic_54.png のスロット枠 (7, 17) から 18x18 を切り抜き
                // 描画位置はスロット座標 - 1
                guiGraphics.blit(VANILLA_CHEST_TEXTURE, gridStartX + col * 18 - 1, gridStartY + row * 18 - 1, 7, 17, 18, 18);
            }
        }

        // 出力スロットの描画
        // Menuでの位置: x=128, y=54
        int outputX = 128;
        int outputY = 54;
        guiGraphics.blit(VANILLA_CHEST_TEXTURE, outputX - 1, outputY - 1, 7, 17, 18, 18);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WeaponWorkbenchRecipe recipe, IFocusGroup focuses) {
        // 5x5 グリッドに合わせてアイテムを配置
        int gridStartX = 14;
        int gridStartY = 18;

        var inputs = recipe.getInputs();
        // 入力リストをグリッドにマッピング
        for (int i = 0; i < inputs.size() && i < 25; i++) {
            ItemStack stack = inputs.get(i);
            if (!stack.isEmpty()) {
                int col = i % 5;
                int row = i / 5;
                builder.addSlot(RecipeIngredientRole.INPUT, gridStartX + col * 18, gridStartY + row * 18)
                        .addIngredients(Ingredient.of(stack));
            }
        }

        // 出力アイテム
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 128, 54)
                    .addItemStack(recipe.getResultItem(minecraft.level.registryAccess()));
        }
    }
}