package com.zoma1101.swordskill_extender.recipe;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Swordskill_extender.MODID);

    public static final RegistryObject<RecipeSerializer<WeaponWorkbenchRecipe>> WEAPON_SERIALIZER =
            SERIALIZERS.register("weapon_crafting", () -> WeaponWorkbenchRecipe.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}