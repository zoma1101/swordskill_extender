package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill_extender.capability.WeaponProficiencyProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ClientboundWeaponProficiencySyncPacket {
    private final Map<String, Integer> proficiencyData;

    public ClientboundWeaponProficiencySyncPacket(Map<String, Integer> proficiencyData) {
        this.proficiencyData = proficiencyData;
    }

    public ClientboundWeaponProficiencySyncPacket(FriendlyByteBuf buffer) {
        this.proficiencyData = buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt);
    }

    public void encode(FriendlyByteBuf buffer) {
        // マップの書き込み
        buffer.writeMap(this.proficiencyData, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
    }

    public boolean handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            Player player = Minecraft.getInstance().player;
            if (player != null) {
                player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).ifPresent(cap -> {
                    // 受け取ったデータをすべてセット
                    proficiencyData.forEach(cap::setExperience);
                });
            }
        });
        return true;
    }
}
