package com.zoma1101.swordskill_extender.init;

import com.zoma1101.swordskill.swordskills.SkillData;
import com.zoma1101.swordskill.swordskills.SwordSkillRegistry;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    // クリエイティブタブのレジストリ
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Swordskill_extender.MODID);

    // タブの登録
    public static final RegistryObject<CreativeModeTab> EXTENDER_TAB = CREATIVE_MODE_TABS.register("extender_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.IRON_KATANA.get())) // タブのアイコン
                    .title(Component.translatable("creativetab.swordskill_extender_tab")) // タブ名
                    .displayItems((pParameters, pOutput) -> {
                        // ModItemsに登録されている全てのアイテムを自動で追加
                        // これにより、ModItemsに新しい武器を追加するだけでタブにも反映されます
                        ModItems.ITEMS.getEntries().forEach(item -> pOutput.accept(item.get()));

                        // ブロック（ワークベンチ）の追加
                        // ModItemsに含まれていない場合はここで追加します
                        pOutput.accept(ModBlocks.WEAPON_WORKBENCH.get());

                    })
                    .build());


    public static final RegistryObject<CreativeModeTab> SCROLL_TAB = CREATIVE_MODE_TABS.register("scroll_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.SKILL_SCROLL.get())) // タブのアイコン
                    .title(Component.translatable("creativetab.swordskill_extender.scroll_tab"))
                    .displayItems((pParameters, pOutput) -> {
                        // 全スキルをループして、それぞれのIDを持ったスクロールを追加
                        for (int i = 1; i < SwordSkillRegistry.SKILLS.size(); i++) {
                            SkillData skill = SwordSkillRegistry.SKILLS.get(i);
                            if (skill != null) {
                                ItemStack scroll = new ItemStack(ModItems.SKILL_SCROLL.get());
                                CompoundTag tag = new CompoundTag();
                                tag.putInt("SkillID", skill.getId());
                                scroll.setTag(tag);
                                pOutput.accept(scroll);
                            }
                        }
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}