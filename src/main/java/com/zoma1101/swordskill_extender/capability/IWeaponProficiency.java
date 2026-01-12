package com.zoma1101.swordskill_extender.capability;

import net.minecraft.nbt.CompoundTag;

import java.util.Map;

public interface IWeaponProficiency {
    // 経験値を加算
    void addExperience(String weaponType, int amount);

    // 経験値を取得
    int getExperience(String weaponType);

    // レベルを取得 (例: 100XPごとに1レベル)
    int getLevel(String weaponType);

    // 全データを取得 (同期用)
    Map<String, Integer> getAllData();

    // データを上書き (同期/ロード用)
    void setExperience(String weaponType, int amount);

    void copyFrom(IWeaponProficiency source);

    CompoundTag saveNBT();
    void loadNBT(CompoundTag nbt);
}