package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static SimpleChannel INSTANCE;
    private static int packetId = 0;
    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();

        INSTANCE = net;

        // 既存のパケット
        net.messageBuilder(ClientboundWeaponProficiencySyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundWeaponProficiencySyncPacket::new)
                .encoder(ClientboundWeaponProficiencySyncPacket::encode)
                .consumerMainThread(ClientboundWeaponProficiencySyncPacket::handle)
                .add();

        // サーバーへのアンロック要求パケット
        net.messageBuilder(ServerboundUnlockSkillPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ServerboundUnlockSkillPacket::new)
                .encoder(ServerboundUnlockSkillPacket::encode)
                .consumerMainThread(ServerboundUnlockSkillPacket::handle)
                .add();

        // ★新規追加: クライアントへのアンロック結果通知パケット
        net.messageBuilder(ClientboundUnlockSkillResultPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundUnlockSkillResultPacket::new)
                .encoder(ClientboundUnlockSkillResultPacket::encode)
                .consumerMainThread(ClientboundUnlockSkillResultPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }
}