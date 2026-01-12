package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill.capability.PlayerSkillsProvider; // ★親ModのCapabilityプロバイダー
import com.zoma1101.swordskill.swordskills.SkillData;
import com.zoma1101.swordskill.swordskills.SwordSkillRegistry;
import com.zoma1101.swordskill_extender.capability.WeaponProficiencyProvider;
import com.zoma1101.swordskill_extender.util.WeaponTypeHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ServerboundUnlockSkillPacket {
    private final int skillId;

    public ServerboundUnlockSkillPacket(int skillId) {
        this.skillId = skillId;
    }

    public ServerboundUnlockSkillPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.skillId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null) return;

            // 1. 習得しようとしているスキルを取得
            SkillData skill = SwordSkillRegistry.SKILLS.get(this.skillId);
            if (skill == null) return;

            // 2. プレイヤーが手に持っている武器種を取得
            String weaponType = WeaponTypeHelper.getWeaponType(player);

            // 3. 熟練度Capabilityを取得してチェック
            player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(cap -> {
                boolean canUnlock = true;
                int requiredXp = 0;

                Map<String, Object> data = skill.getAdditionalData();

                // "proficiency_req" (熟練度要件) が設定されているか確認
                if (data.containsKey("proficiency_req")) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Integer> reqs = (Map<String, Integer>) data.get("proficiency_req");

                        // 現在の武器種に対する要件があるかチェック
                        if (weaponType != null && reqs.containsKey(weaponType)) {
                            requiredXp = reqs.get(weaponType);
                            // 熟練度が足りているか？
                            if (cap.getExperience(weaponType) < requiredXp) {
                                canUnlock = false;
                            }
                        } else {
                            // 要件があるのに、対応する武器を持っていない場合はアンロック不可
                            canUnlock = false;
                        }
                    } catch (Exception e) {
                        canUnlock = false;
                    }
                }

                if (canUnlock) {
                    player.getCapability(PlayerSkillsProvider.PLAYER_SKILLS).ifPresent(skills -> {
                        skills.unlockSkill(skill.getId());
                    });

                    // ★追加: クライアントに習得完了を通知してGUIを更新させる
                    // (ClientboundUnlockSkillResultPacket は別途作成し、PacketHandlerに登録する必要があります)
                    PacketHandler.sendToPlayer(new ClientboundUnlockSkillResultPacket(skill.getId()), player);

                    // 演出: 成功音とメッセージ
                    player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
                    player.sendSystemMessage(Component.translatable("message.swordskill_extender.skill_unlocked", skill.getName())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

                } else {
                    // ★失敗: エラーメッセージを表示
                    String wName = weaponType != null ? weaponType : "None";
                    Component weaponNameComp = Component.translatable("weapon_type.swordskill_extender." + wName.toLowerCase());

                    player.sendSystemMessage(Component.translatable("message.swordskill_extender.not_enough_proficiency", weaponNameComp, requiredXp)
                            .withStyle(ChatFormatting.RED));

                    player.level().playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            });
        });
        context.get().setPacketHandled(true);
    }
}