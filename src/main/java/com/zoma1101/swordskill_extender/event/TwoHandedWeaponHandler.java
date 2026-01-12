package com.zoma1101.swordskill_extender.event;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Swordskill_extender.MODID)
public class TwoHandedWeaponHandler {


    public static final TagKey<Item> TWO_HANDED_WEAPONS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "two_handed_weapons"));

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity() instanceof Player player) {
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();

            if (mainHandItem.is(TWO_HANDED_WEAPONS) && !offHandItem.isEmpty()) {
                if (!player.getInventory().add(offHandItem)) {
                    player.drop(offHandItem, true);
                }
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }

            if (offHandItem.is(TWO_HANDED_WEAPONS)) {
                if (!player.getInventory().add(offHandItem)) {
                    player.drop(offHandItem, true);
                }
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }
        }
    }
}