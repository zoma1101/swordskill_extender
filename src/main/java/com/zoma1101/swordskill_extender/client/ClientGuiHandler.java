package com.zoma1101.swordskill_extender.client;

import com.zoma1101.swordskill.client.gui.SwordSkillSelectionScreen;
import com.zoma1101.swordskill.swordskills.SkillData;
import com.zoma1101.swordskill.swordskills.SwordSkillRegistry;
import com.zoma1101.swordskill_extender.Swordskill_extender;
import com.zoma1101.swordskill_extender.capability.WeaponProficiencyProvider;
import com.zoma1101.swordskill_extender.network.PacketHandler;
import com.zoma1101.swordskill_extender.network.ServerboundUnlockSkillPacket;
import com.zoma1101.swordskill_extender.util.WeaponTypeHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = Swordskill_extender.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientGuiHandler {


    private static final ResourceLocation WIDGETS_LOCATION = ResourceLocation.parse("textures/gui/widgets.png");

    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();

        if (shouldRenderOverlay(screen)) {
            renderProficiencyOverlay(event.getGuiGraphics());
        }

        if (screen instanceof SwordSkillSelectionScreen skillScreen) {
            renderSkillRequirements(event.getGuiGraphics(), skillScreen);
            renderProficiencyUnlockButton(event.getGuiGraphics(), skillScreen, event.getMouseX(), event.getMouseY());
        }
    }

    @SubscribeEvent
    public static void onScreenMouseClick(ScreenEvent.MouseButtonPressed.Pre event) {
        if (event.getScreen() instanceof SwordSkillSelectionScreen skillScreen) {
            if (tryClickUnlockButton(skillScreen, event.getMouseX(), event.getMouseY(), event.getButton())) {
                event.setCanceled(true);
            }
        }
    }

    private static boolean shouldRenderOverlay(Screen screen) {
        String className = screen.getClass().getName();
        String title = screen.getTitle().getString();
        return className.contains("Skill") || title.contains("Skill") || title.contains("スキル");
    }

    private static void renderProficiencyOverlay(GuiGraphics graphics) {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        int startX = 10;
        int startY = 10;
        int lineHeight = 12;
        int width = 120;
        int height = 0;

        var capOpt = player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY);
        if (capOpt.isPresent()) {
            Map<String, Integer> data = capOpt.orElse(null).getAllData();

            if (!data.isEmpty()) {
                height = (data.size() + 1) * lineHeight + 5;
                graphics.fill(startX - 5, startY - 5, startX + width, startY + height, 0x80000000);
                graphics.drawString(Minecraft.getInstance().font, "§6[Weapon Proficiency]", startX, startY, 0xFFFFFF);
                startY += lineHeight;

                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    String weaponType = entry.getKey();
                    int xp = entry.getValue();

                    Component nameComp = Component.translatable("weapon_type.swordskill_extender." + weaponType.toLowerCase());
                    String text = nameComp.getString() + ": §a" + xp + " XP";

                    graphics.drawString(Minecraft.getInstance().font, text, startX, startY, 0xFFFFFF);
                    startY += lineHeight;
                }
            }
        }
    }

    private static int getTargetSkillId(SwordSkillSelectionScreen screen) {
        int selectedIndex = screen.selectedSkillIndex;

        if (!screen.unlockedSkills.contains(selectedIndex)) {
            return selectedIndex;
        }

        SkillData skill = SwordSkillRegistry.SKILLS.get(selectedIndex);
        if (skill != null && skill.getType() == SkillData.SkillType.TRANSFORM) {
            int checkIndex = selectedIndex;
            while (true) {
                int nextIndex = checkIndex + 1;
                if (nextIndex >= SwordSkillRegistry.SKILLS.size()) break;

                SkillData nextSkill = SwordSkillRegistry.SKILLS.get(nextIndex);
                if (nextSkill == null) break;

                if (!screen.unlockedSkills.contains(nextIndex)) {
                    return nextIndex;
                }

                if (nextSkill.getType() == SkillData.SkillType.TRANSFORM_FINISH) break;

                checkIndex = nextIndex;
            }
        }

        return -1;
    }

    private static void renderSkillRequirements(GuiGraphics guiGraphics, SwordSkillSelectionScreen screen) {
        int targetId = getTargetSkillId(screen);
        if (targetId == -1) return;

        SkillData skill = SwordSkillRegistry.SKILLS.get(targetId);

        if (skill != null) {
            Map<String, Object> additionalData = skill.getAdditionalData();
            if (additionalData.containsKey("proficiency_req")) {
                renderProficiencyRequirements(guiGraphics, screen, (Map<String, Integer>) additionalData.get("proficiency_req"));
            }
        }
    }

    private static void renderProficiencyRequirements(GuiGraphics guiGraphics, SwordSkillSelectionScreen screen, Map<String, Integer> reqs) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        int width = screen.width;
        int height = screen.height;
        int centerX = width / 2;
        int startY = height - 85;

        String heldWeaponType = WeaponTypeHelper.getWeaponType(player);

        if (heldWeaponType == null || !reqs.containsKey(heldWeaponType)) {
            return;
        }

        guiGraphics.drawCenteredString(mc.font, Component.translatable("gui.swordskill_extender.req_proficiency"), centerX, startY, 0xFF5555);
        startY += 10;

        for (Map.Entry<String, Integer> entry : reqs.entrySet()) {
            String weaponType = entry.getKey();
            int requiredXp = entry.getValue();

            if (!weaponType.equals(heldWeaponType)) {
                continue;
            }

            int currentXp = 0;
            if (player != null) {
                var cap = player.getCapability(WeaponProficiencyProvider.WEAPON_PROFICIENCY).orElse(null);
                if (cap != null) {
                    currentXp = cap.getExperience(weaponType);
                }
            }

            int color = (currentXp >= requiredXp) ? 0x55FF55 : 0xFF5555;

            Component weaponName = Component.translatable("weapon_type.swordskill_extender." + weaponType.toLowerCase());
            Component text = Component.translatable("gui.swordskill_extender.req_proficiency_format", weaponName, requiredXp);

            guiGraphics.drawCenteredString(mc.font, text, centerX, startY, color);
            startY += 10;
        }
    }

    private static void renderProficiencyUnlockButton(GuiGraphics graphics, SwordSkillSelectionScreen screen, int mouseX, int mouseY) {
        int targetId = getTargetSkillId(screen);
        if (targetId == -1) return;

        SkillData skill = SwordSkillRegistry.SKILLS.get(targetId);
        if (skill == null) return;

        Map<String, Object> data = skill.getAdditionalData();
        if (!data.containsKey("proficiency_req")) return;

        int btnX = screen.width / 2 + 85;
        int btnY = screen.height - 40;
        int btnW = 20;
        int btnH = 20;

        boolean hovered = mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH;

        int v = 46 + (hovered ? 2 : 1) * 20;

        graphics.blit(WIDGETS_LOCATION, btnX, btnY, 0, v, btnW / 2, btnH);
        graphics.blit(WIDGETS_LOCATION, btnX + btnW / 2, btnY, 200 - btnW / 2, v, btnW / 2, btnH);

        int color = 0x80FF80;
        if (!hovered) color = 0xFFFFFF;

        graphics.drawCenteredString(Minecraft.getInstance().font, "XP", btnX + btnW / 2, btnY + (btnH - 8) / 2, color);

        if (hovered) {
            graphics.renderTooltip(Minecraft.getInstance().font, Component.translatable("gui.swordskill_extender.unlock_proficiency"), mouseX, mouseY);
        }
    }

    private static boolean tryClickUnlockButton(SwordSkillSelectionScreen screen, double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int targetId = getTargetSkillId(screen);
        if (targetId == -1) return false;

        SkillData skill = SwordSkillRegistry.SKILLS.get(targetId);
        if (skill == null) return false;

        Map<String, Object> data = skill.getAdditionalData();
        if (!data.containsKey("proficiency_req")) return false;

        int btnX = screen.width / 2 + 85;
        int btnY = screen.height - 40;
        int btnW = 20;
        int btnH = 20;

        if (mouseX >= btnX && mouseX < btnX + btnW && mouseY >= btnY && mouseY < btnY + btnH) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            PacketHandler.sendToServer(new ServerboundUnlockSkillPacket(targetId));
            return true;
        }
        return false;
    }
}