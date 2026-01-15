package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

        // アニメーション再生パケットの登録
        net.messageBuilder(S2CPlayAnimationPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CPlayAnimationPacket::new)
                .encoder(S2CPlayAnimationPacket::encode)
                .consumerMainThread(S2CPlayAnimationPacket::handle)
                .add();

        // ★追加: 熟練度同期パケットの登録
        // これが登録されていないため、ワールドに入った瞬間にエラーで弾かれていました
        net.messageBuilder(ClientboundWeaponProficiencySyncPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(ClientboundWeaponProficiencySyncPacket::new)
                .encoder(ClientboundWeaponProficiencySyncPacket::encode)
                .consumerMainThread(ClientboundWeaponProficiencySyncPacket::handle)
                .add();
    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToTracking(MSG message, Entity entity) {
        INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }
}