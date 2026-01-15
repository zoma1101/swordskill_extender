package com.zoma1101.swordskill_extender.client;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Swordskill_extender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // プレイヤーのアニメーションシステムにレイヤーを登録するイベント
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.register((player, animationStack) -> {
            try {
                var layer = new ModifierLayer<IAnimation>() {
                    @Override
                    public Vec3f get3DTransform(String modelName, TransformType type, float tickDelta, Vec3f value) {
                        // 元のアニメーションの変形を取得
                        Vec3f transform = super.get3DTransform(modelName, type, tickDelta, value);

                        // rightArm (右腕) の場合
                        if ("rightArm".equals(modelName)) {
                            // プレイヤーの視点（ピッチ：上下の向き）を取得
                            float pitch = player.getViewXRot(tickDelta);
                            float pitchRad = (float) Math.toRadians(pitch);

                            if (type == TransformType.ROTATION) {
                                // 回転: X軸回転にピッチを加算して、腕の向きを視点に合わせる
                                return new Vec3f(transform.getX() + pitchRad, transform.getY(), transform.getZ());
                            } else if (type == TransformType.POSITION) {

                                float x = transform.getX();
                                float y = transform.getY();
                                float z = transform.getZ();

                                float cos = (float) Math.cos(pitchRad);
                                float sin = (float) Math.sin(pitchRad);

                                float newY = y * cos - z * sin;
                                float newZ = y * sin + z * cos;

                                return new Vec3f(x, newY, newZ);
                            }
                        }
                        return transform;
                    }
                };

                // レイヤーに優先度を付けて登録
                animationStack.addAnimLayer(1000, layer);

                // データの取得
                var data = PlayerAnimationAccess.getPlayerAssociatedData(player);

                ResourceLocation layerId = ResourceLocation.fromNamespaceAndPath(Swordskill_extender.MODID, "animation_layer");
                data.set(layerId, layer);

            } catch (Throwable t) {
                System.err.println("ClientModEvents: Error occurred during animation registration. Player creation will continue.");
                t.printStackTrace();
            }
        });
    }
}