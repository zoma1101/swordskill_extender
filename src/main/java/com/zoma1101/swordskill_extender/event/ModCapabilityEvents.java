package com.zoma1101.swordskill_extender.event;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.capability.WeaponProficiencyProvider;
import com.zoma1101.swordskill_extender.item.Proficiency.ProficiencyScrollItem;
import com.zoma1101.swordskill_extender.network.ClientboundWeaponProficiencySyncPacket;
import com.zoma1101.swordskill_extender.network.PacketHandler;
import com.zoma1101.swordskill_extender.util.WeaponTypeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Swordskill_extender.MODID)
public class ModCapabilityEvents {

    // 1. Capabilityの付与
    @SubscribeEvent
    public static void onAttachCapabilitiesPlayer(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player) {
            if (!event.getObject().getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).isPresent()) {
                // 1.20.1 互換のため new ResourceLocation を使用
                event.addCapability(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "weapon_proficiency"), new WeaponProficiencyProvider());
            }
        }
    }

    // 2. データの引き継ぎ
    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(oldStore -> {
                event.getEntity().getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(newStore -> {
                    newStore.copyFrom(oldStore);
                });
            });
        }
    }

    // 3. データの同期
    @SubscribeEvent
    public static void onPlayerJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            if (event.getEntity() instanceof ServerPlayer player) {
                player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(cap -> {
                    PacketHandler.sendToPlayer(new ClientboundWeaponProficiencySyncPacket(cap.getAllData()), player);
                });
            }
        }
    }

    // 4. 経験値の加算
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            String weaponType = WeaponTypeHelper.getWeaponType(player);

            if (weaponType != null) {
                player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(cap -> {
                    cap.addExperience(weaponType, 1);
                    PacketHandler.sendToPlayer(new ClientboundWeaponProficiencySyncPacket(cap.getAllData()), player);
                    // 自動習得チェックは削除
                });
            }
        }
    }

    // 5. 熟練度アップアイテムの使用
    @SubscribeEvent
    public static void onWeaponRightClick(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        if (player.isShiftKeyDown() && event.getHand() == InteractionHand.MAIN_HAND) {

            String weaponType = WeaponTypeHelper.getWeaponType(player);

            if (weaponType != null) {
                ItemStack scroll = ItemStack.EMPTY;
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack s = player.getInventory().getItem(i);
                    if (s.getItem() instanceof ProficiencyScrollItem) {
                        scroll = s;
                        break;
                    }
                }

                if (!scroll.isEmpty()) {
                    int xpAmount = 100;
                    if (scroll.hasTag() && scroll.getTag().contains("xp_amount")) {
                        xpAmount = scroll.getTag().getInt("xp_amount");
                    }
                    final int amountToAdd = xpAmount;

                    player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(cap -> {
                        cap.addExperience(weaponType, amountToAdd);
                        PacketHandler.sendToPlayer(new ClientboundWeaponProficiencySyncPacket(cap.getAllData()), (ServerPlayer) player);

                        Component weaponName = Component.translatable("weapon_type.swordskill_extender." + weaponType.toLowerCase());
                        player.sendSystemMessage(Component.translatable("message.swordskill_extender.proficiency_increased", weaponName, amountToAdd).withStyle(ChatFormatting.GREEN));

                        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1f);

                    });

                    if (!player.getAbilities().instabuild) {
                        scroll.shrink(1);
                    }

                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.SUCCESS);
                }
            }
        }
    }
}