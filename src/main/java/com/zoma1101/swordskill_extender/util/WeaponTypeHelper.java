package com.zoma1101.swordskill_extender.util;

import com.mojang.logging.LogUtils;
import com.zoma1101.swordskill.data.WeaponTypeDetector;
import com.zoma1101.swordskill.swordskills.SkillData; // ★重要: Enumを使うためにインポート
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Set;

public class WeaponTypeHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static String getWeaponType(Player player) {
        if (player == null) return null;

        ItemStack mainStack = player.getMainHandItem();
        ItemStack offStack = player.getOffhandItem();

        Set<SkillData.WeaponType> mainTypes = getWeaponTypesSafe(mainStack);
        Set<SkillData.WeaponType> offTypes = getWeaponTypesSafe(offStack);


        boolean mainIsSword = mainTypes.contains(SkillData.WeaponType.ONE_HANDED_SWORD);
        boolean offIsSword = offTypes.contains(SkillData.WeaponType.ONE_HANDED_SWORD);


        // 3. 両手とも片手剣属性なら「二刀流」
        if (mainIsSword && offIsSword) {
            return "DualSword";
        }

        // 4. 二刀流でない場合、メインが片手剣属性なら「Sword」を返す
        if (mainIsSword) {
            return "Sword";
        }


        return normalizeName(getWeaponNameSafe(mainStack));
    }

    /**
     * アイテムスタック単体の判定（イベント等で使う用）
     */
    public static String getWeaponType(ItemStack stack) {
        Set<SkillData.WeaponType> types = getWeaponTypesSafe(stack);

        // 片手剣属性を持っていれば Sword
        if (types.contains(SkillData.WeaponType.ONE_HANDED_SWORD)) {
            return "Sword";
        }

        // それ以外は名前で判定
        return normalizeName(getWeaponNameSafe(stack));
    }


    /**
     * 安全にWeaponTypeのSetを取得する
     */
    private static Set<SkillData.WeaponType> getWeaponTypesSafe(ItemStack stack) {
        if (stack.isEmpty()) return Collections.emptySet();
        try {
            return WeaponTypeDetector.detectWeaponTypes(stack);
        } catch (Throwable e) {
            // エラー時はフォールバック: バニラの剣なら ONE_HANDED_SWORD を含むセットを返す
            if (stack.getItem() instanceof SwordItem) {
                return Collections.singleton(SkillData.WeaponType.ONE_HANDED_SWORD);
            }
            return Collections.emptySet();
        }
    }

    /**
     * 安全に武器名を取得する
     */
    private static String getWeaponNameSafe(ItemStack stack) {
        if (stack.isEmpty()) return null;
        try {
            return WeaponTypeDetector.getWeaponName(stack);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 名前を正規化する（表記揺れ対策）
     */
    private static String normalizeName(String name) {
        if (name == null) return null;

        // 念のため名前ベースの判定も残しておく
        if ("One-Handed Sword".equals(name) || "OneHandedSword".equals(name) || "ONE_HANDED_SWORD".equals(name) || "dual_sword".equals(name)) {
            return "Sword";
        }
        if ("DUALSWORD".equals(name)) {
            return "DualSword";
        }

        return name;
    }
}