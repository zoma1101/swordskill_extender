package com.zoma1101.swordskill_extender.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class WeaponWorkbenchRecipe implements Recipe<SimpleContainer> {
    private final ResourceLocation id;
    private final ItemStack output;
    private final NonNullList<ItemStack> recipeItems; // 個数情報を持つためにIngredientではなくItemStackを使用
    private final ResourceLocation icon; // GUIに表示するアイコンのパス

    public WeaponWorkbenchRecipe(ResourceLocation id, ItemStack output, NonNullList<ItemStack> recipeItems, ResourceLocation icon) {
        this.id = id;
        this.output = output;
        this.recipeItems = recipeItems;
        this.icon = icon;
    }

    // 入力素材リストを取得
    public NonNullList<ItemStack> getInputs() {
        return recipeItems;
    }

    // アイコンのパスを取得
    public ResourceLocation getIcon() {
        return icon;
    }

    @Override
    public boolean matches(SimpleContainer pContainer, Level pLevel) {
        return true;
    }

    @Override
    public ItemStack assemble(SimpleContainer pContainer, RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int pWidth, int pHeight) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess pRegistryAccess) {
        return output.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<WeaponWorkbenchRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "weapon_crafting";
    }

    public static class Serializer implements RecipeSerializer<WeaponWorkbenchRecipe> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "weapon_crafting");

        @Override
        public WeaponWorkbenchRecipe fromJson(ResourceLocation pRecipeId, JsonObject pSerializedRecipe) {
            ItemStack output = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(pSerializedRecipe, "result"));

            // アイコンのパスを取得
            String iconPath = GsonHelper.getAsString(pSerializedRecipe, "icon");
            ResourceLocation icon = ResourceLocation.parse(iconPath);

            // 素材リストの読み込み
            JsonArray ingredients = GsonHelper.getAsJsonArray(pSerializedRecipe, "ingredients");
            NonNullList<ItemStack> inputs = NonNullList.withSize(ingredients.size(), ItemStack.EMPTY);

            for (int i = 0; i < ingredients.size(); i++) {
                JsonObject ingredientObj = ingredients.get(i).getAsJsonObject();
                ItemStack stack = ShapedRecipe.itemStackFromJson(ingredientObj); // itemとcountを読み込む
                inputs.set(i, stack);
            }

            return new WeaponWorkbenchRecipe(pRecipeId, output, inputs, icon);
        }

        @Override
        public @Nullable WeaponWorkbenchRecipe fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf pBuffer) {
            ItemStack output = pBuffer.readItem();
            ResourceLocation icon = pBuffer.readResourceLocation();

            int size = pBuffer.readInt();
            NonNullList<ItemStack> inputs = NonNullList.withSize(size, ItemStack.EMPTY);
            for (int i = 0; i < size; i++) {
                inputs.set(i, pBuffer.readItem());
            }

            return new WeaponWorkbenchRecipe(pRecipeId, output, inputs, icon);
        }

        @Override
        public void toNetwork(FriendlyByteBuf pBuffer, WeaponWorkbenchRecipe pRecipe) {
            pBuffer.writeItem(pRecipe.output);
            pBuffer.writeResourceLocation(pRecipe.icon);

            pBuffer.writeInt(pRecipe.recipeItems.size());
            for (ItemStack stack : pRecipe.recipeItems) {
                pBuffer.writeItem(stack);
            }
        }
    }
}