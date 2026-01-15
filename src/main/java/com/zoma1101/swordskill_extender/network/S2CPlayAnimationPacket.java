package com.zoma1101.swordskill_extender.network;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity; // ★インポート追加
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CPlayAnimationPacket {
    private final int playerId;
    private final ResourceLocation animationId;

    public S2CPlayAnimationPacket(int playerId, ResourceLocation animationId) {
        this.playerId = playerId;
        this.animationId = animationId;
    }

    public S2CPlayAnimationPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readInt();
        this.animationId = buf.readResourceLocation();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerId);
        buf.writeResourceLocation(animationId);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> handleClient(playerId, animationId));
        });
        ctx.get().setPacketHandled(true);
    }

    // ★修正: クライアント側の処理をログ強化版に置き換え
    private static void handleClient(int playerId, ResourceLocation animId) {
        try {
            System.out.println("[AnimPacket] Received packet for player ID: " + playerId + ", anim: " + animId);
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                System.out.println("[AnimPacket] ERROR: Minecraft.level is null. Aborting.");
                return;
            }

            Entity entity = mc.level.getEntity(playerId);
            if (!(entity instanceof AbstractClientPlayer player)) {
                System.out.println("[AnimPacket] ERROR: Entity is not a player or not found. Entity: " + entity);
                return;
            }
            System.out.println("[AnimPacket] Player found: " + player.getName().getString());

            KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(animId);
            if (animation == null) {
                System.out.println("[AnimPacket] ERROR: Animation NOT FOUND: " + animId);
                System.out.println("[AnimPacket] Available animations: " + PlayerAnimationRegistry.getAnimations().keySet());
                return;
            }
            System.out.println("[AnimPacket] Animation data found.");

            var data = PlayerAnimationAccess.getPlayerAssociatedData(player);

            var layerObj = data.get(ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "animation_layer"));
            if (!(layerObj instanceof ModifierLayer)) {
                System.out.println("[AnimPacket] ERROR: Animation layer NOT FOUND or is wrong type. Layer object: " + layerObj);
                return;
            }
            System.out.println("[AnimPacket] Animation layer found. Playing animation...");

            @SuppressWarnings("unchecked")
            ModifierLayer<IAnimation> layer = (ModifierLayer<IAnimation>) layerObj;
            layer.setAnimation(new KeyframeAnimationPlayer(animation));
            System.out.println("[AnimPacket] Animation set successfully.");

        } catch (Throwable t) {
            System.err.println("[AnimPacket] CRITICAL ERROR handling animation packet.");
            t.printStackTrace();
        }
    }
}