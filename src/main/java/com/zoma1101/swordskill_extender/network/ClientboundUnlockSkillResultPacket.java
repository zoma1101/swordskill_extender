package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill.client.gui.SwordSkillSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientboundUnlockSkillResultPacket {
    private final int skillId;

    public ClientboundUnlockSkillResultPacket(int skillId) {
        this.skillId = skillId;
    }

    public ClientboundUnlockSkillResultPacket(FriendlyByteBuf buf) {
        this.skillId = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.skillId);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            // クライアント側でのみ実行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.screen instanceof SwordSkillSelectionScreen screen) {
                    screen.unlockedSkills.add(this.skillId);
                }
            });
        });
        context.get().setPacketHandled(true);
    }
}