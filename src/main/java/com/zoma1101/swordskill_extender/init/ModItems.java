package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.item.Proficiency.ProficiencyScrollItem;
import com.zoma1101.swordskill_extender.item.weapon.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Swordskill_extender.MODID);


    public static final RegistryObject<Item> IRON_GREATSWORD = ITEMS.register("iron_greatsword",
            () -> new GreatswordItem(Tiers.IRON, 4, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_SPEAR = ITEMS.register("iron_spear",
            () -> new SpearItem(Tiers.IRON, 3, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_MACE = ITEMS.register("iron_mace",
            () -> new MaceItem(Tiers.IRON, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_RAPIER = ITEMS.register("iron_rapier",
            () -> new RapierItem(Tiers.IRON, 2, -1.8F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_DAGGER = ITEMS.register("iron_dagger",
            () -> new DaggerItem(Tiers.IRON, 1, -1.0F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_KATANA = ITEMS.register("iron_katana",
            () -> new KatanaItem(Tiers.IRON, 3, -2.2F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_CLAW = ITEMS.register("iron_claw",
            () -> new ClawItem(Tiers.IRON, 2, -1.5F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_SCYTHE = ITEMS.register("iron_scythe",
            () -> new ScytheItem(Tiers.IRON, 4, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> IRON_BATTLEAXE = ITEMS.register("iron_battleaxe",
            () -> new BattleaxeItem(Tiers.IRON, 5, -3.0F, new Item.Properties()));

    // --- 石 (Stone) ---
    public static final RegistryObject<Item> STONE_GREATSWORD = ITEMS.register("stone_greatsword",
            () -> new GreatswordItem(Tiers.STONE, 4, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_SPEAR = ITEMS.register("stone_spear",
            () -> new SpearItem(Tiers.STONE, 3, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_MACE = ITEMS.register("stone_mace",
            () -> new MaceItem(Tiers.STONE, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_RAPIER = ITEMS.register("stone_rapier",
            () -> new RapierItem(Tiers.STONE, 2, -1.8F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_DAGGER = ITEMS.register("stone_dagger",
            () -> new DaggerItem(Tiers.STONE, 1, -1.0F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_KATANA = ITEMS.register("stone_katana",
            () -> new KatanaItem(Tiers.STONE, 3, -2.2F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_CLAW = ITEMS.register("stone_claw",
            () -> new ClawItem(Tiers.STONE, 2, -1.5F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_SCYTHE = ITEMS.register("stone_scythe",
            () -> new ScytheItem(Tiers.STONE, 4, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> STONE_BATTLEAXE = ITEMS.register("stone_battleaxe",
            () -> new BattleaxeItem(Tiers.STONE, 5, -3.0F, new Item.Properties()));

    // --- 金 (Gold) ---
    public static final RegistryObject<Item> GOLD_GREATSWORD = ITEMS.register("gold_greatsword",
            () -> new GreatswordItem(Tiers.GOLD, 4, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SPEAR = ITEMS.register("gold_spear",
            () -> new SpearItem(Tiers.GOLD, 3, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_MACE = ITEMS.register("gold_mace",
            () -> new MaceItem(Tiers.GOLD, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_RAPIER = ITEMS.register("gold_rapier",
            () -> new RapierItem(Tiers.GOLD, 2, -1.8F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_DAGGER = ITEMS.register("gold_dagger",
            () -> new DaggerItem(Tiers.GOLD, 1, -1.0F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_KATANA = ITEMS.register("gold_katana",
            () -> new KatanaItem(Tiers.GOLD, 3, -2.2F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_CLAW = ITEMS.register("gold_claw",
            () -> new ClawItem(Tiers.GOLD, 2, -1.5F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_SCYTHE = ITEMS.register("gold_scythe",
            () -> new ScytheItem(Tiers.GOLD, 4, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> GOLD_BATTLEAXE = ITEMS.register("gold_battleaxe",
            () -> new BattleaxeItem(Tiers.GOLD, 5, -3.0F, new Item.Properties()));

    // --- ダイヤ (Diamond) ---
    public static final RegistryObject<Item> DIAMOND_GREATSWORD = ITEMS.register("diamond_greatsword",
            () -> new GreatswordItem(Tiers.DIAMOND, 4, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SPEAR = ITEMS.register("diamond_spear",
            () -> new SpearItem(Tiers.DIAMOND, 3, -2.8F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_MACE = ITEMS.register("diamond_mace",
            () -> new MaceItem(Tiers.DIAMOND, 5, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_RAPIER = ITEMS.register("diamond_rapier",
            () -> new RapierItem(Tiers.DIAMOND, 2, -1.8F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_DAGGER = ITEMS.register("diamond_dagger",
            () -> new DaggerItem(Tiers.DIAMOND, 1, -1.0F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_KATANA = ITEMS.register("diamond_katana",
            () -> new KatanaItem(Tiers.DIAMOND, 3, -2.2F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_CLAW = ITEMS.register("diamond_claw",
            () -> new ClawItem(Tiers.DIAMOND, 2, -1.5F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_SCYTHE = ITEMS.register("diamond_scythe",
            () -> new ScytheItem(Tiers.DIAMOND, 4, -3.0F, new Item.Properties()));
    public static final RegistryObject<Item> DIAMOND_BATTLEAXE = ITEMS.register("diamond_battleaxe",
            () -> new BattleaxeItem(Tiers.DIAMOND, 5, -3.0F, new Item.Properties()));

    // --- ネザライト (Netherite) ---
    public static final RegistryObject<Item> NETHERITE_GREATSWORD = ITEMS.register("netherite_greatsword",
            () -> new GreatswordItem(Tiers.NETHERITE, 4, -2.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_SPEAR = ITEMS.register("netherite_spear",
            () -> new SpearItem(Tiers.NETHERITE, 3, -2.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_MACE = ITEMS.register("netherite_mace",
            () -> new MaceItem(Tiers.NETHERITE, 5, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_RAPIER = ITEMS.register("netherite_rapier",
            () -> new RapierItem(Tiers.NETHERITE, 2, -1.8F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_DAGGER = ITEMS.register("netherite_dagger",
            () -> new DaggerItem(Tiers.NETHERITE, 1, -1.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_KATANA = ITEMS.register("netherite_katana",
            () -> new KatanaItem(Tiers.NETHERITE, 3, -2.2F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_CLAW = ITEMS.register("netherite_claw",
            () -> new ClawItem(Tiers.NETHERITE, 2, -1.5F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_SCYTHE = ITEMS.register("netherite_scythe",
            () -> new ScytheItem(Tiers.NETHERITE, 4, -3.0F, new Item.Properties().fireResistant()));
    public static final RegistryObject<Item> NETHERITE_BATTLEAXE = ITEMS.register("netherite_battleaxe",
            () -> new BattleaxeItem(Tiers.NETHERITE, 5, -3.0F, new Item.Properties().fireResistant()));

    public static final RegistryObject<Item> PROFICIENCY_SCROLL = ITEMS.register("proficiency_scroll",
            () -> new ProficiencyScrollItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}