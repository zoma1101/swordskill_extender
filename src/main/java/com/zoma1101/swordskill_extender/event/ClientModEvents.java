package com.zoma1101.swordskill_extender.event;

import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.init.ModMenuTypes;
import com.zoma1101.swordskill_extender.screen.WeaponWorkbenchScreen;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Swordskill_extender.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MenuScreens.register(ModMenuTypes.WEAPON_WORKBENCH_MENU.get(), WeaponWorkbenchScreen::new);
    }
}