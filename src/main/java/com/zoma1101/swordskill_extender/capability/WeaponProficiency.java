package com.zoma1101.swordskill_extender.capability;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashMap;
import java.util.Map;

public class WeaponProficiency implements IWeaponProficiency {
    private final Map<String, Integer> proficiencyMap = new HashMap<>();

    @Override
    public void addExperience(String weaponType, int amount) {
        proficiencyMap.put(weaponType, getExperience(weaponType) + amount);
    }

    @Override
    public int getExperience(String weaponType) {
        return proficiencyMap.getOrDefault(weaponType, 0);
    }

    @Override
    public int getLevel(String weaponType) {
        // 例: 100XP = Lv1, 200XP = Lv2...
        // 必要に応じて計算式を調整してください (例: Math.sqrt(xp) など)
        return getExperience(weaponType) / 100;
    }

    @Override
    public Map<String, Integer> getAllData() {
        return proficiencyMap;
    }

    @Override
    public void setExperience(String weaponType, int amount) {
        proficiencyMap.put(weaponType, amount);
    }

    @Override
    public void copyFrom(IWeaponProficiency source) {
        this.proficiencyMap.clear();
        this.proficiencyMap.putAll(source.getAllData());
    }

    @Override
    public CompoundTag saveNBT() {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();

        proficiencyMap.forEach((type, xp) -> {
            CompoundTag entry = new CompoundTag();
            entry.putString("type", type);
            entry.putInt("xp", xp);
            list.add(entry);
        });

        nbt.put("proficiency_data", list);
        return nbt;
    }

    @Override
    public void loadNBT(CompoundTag nbt) {
        proficiencyMap.clear();
        ListTag list = nbt.getList("proficiency_data", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            proficiencyMap.put(entry.getString("type"), entry.getInt("xp"));
        }
    }
}