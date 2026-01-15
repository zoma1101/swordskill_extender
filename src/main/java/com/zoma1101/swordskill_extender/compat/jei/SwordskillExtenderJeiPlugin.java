package com.zoma1101.swordskill_extender.compat.jei;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.init.ModBlocks;
import com.zoma1101.swordskill_extender.init.ModMenuTypes; // ★追加: MenuTypeを登録しているクラスをインポート
import com.zoma1101.swordskill_extender.recipe.WeaponWorkbenchRecipe;
import com.zoma1101.swordskill_extender.screen.WeaponWorkbenchMenu;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@JeiPlugin
public class SwordskillExtenderJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new WeaponWorkbenchRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        assert Minecraft.getInstance().level != null;
        List<WeaponWorkbenchRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(WeaponWorkbenchRecipe.Type.INSTANCE);

        registration.addRecipes(WeaponWorkbenchRecipeCategory.RECIPE_TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        // 作業台ブロックをJEIのカタログアイコンとして登録
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.WEAPON_WORKBENCH.get()), WeaponWorkbenchRecipeCategory.RECIPE_TYPE);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        // ★修正: 第2引数に String ではなく MenuType を渡すように変更
        // ModMenuTypes.WEAPON_WORKBENCH_MENU.get() は、あなたがメニュータイプを登録したRegistryObjectに合わせて書き換えてください
        registration.addRecipeTransferHandler(WeaponWorkbenchMenu.class, ModMenuTypes.WEAPON_WORKBENCH_MENU.get(),
                WeaponWorkbenchRecipeCategory.RECIPE_TYPE, 0, 6, 6, 36);
    }

}