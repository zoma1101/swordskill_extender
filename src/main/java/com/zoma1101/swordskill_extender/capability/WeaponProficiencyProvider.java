package com.zoma1101.swordskill_extender.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WeaponProficiencyProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<IWeaponProficiency> WEAPON_PROFICIENCY = CapabilityManager.get(new CapabilityToken<>() {});

    private IWeaponProficiency backend = null;
    private final LazyOptional<IWeaponProficiency> optional = LazyOptional.of(this::createWeaponProficiency);

    private IWeaponProficiency createWeaponProficiency() {
        if (backend == null) {
            backend = new WeaponProficiency();
        }
        return backend;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == WEAPON_PROFICIENCY) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return createWeaponProficiency().saveNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createWeaponProficiency().loadNBT(nbt);
    }
}