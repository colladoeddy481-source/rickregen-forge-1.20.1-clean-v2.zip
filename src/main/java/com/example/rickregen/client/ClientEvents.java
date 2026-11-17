package com.example.rickregen.client;

import com.example.rickregen.RickregenMod;
import com.example.rickregen.ability.AbilityCooldownManager;
import com.example.rickregen.ability.AbilityCooldownManager.Ability;
import com.example.rickregen.network.PacketHandler;
import com.example.rickregen.network.TriggerAbilityPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ClientEvents {

    @Mod.EventBusSubscriber(modid = RickregenMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModBusEvents {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(KeyBindings.EVASION_TP_KEY);
            event.register(KeyBindings.COMBAT_DRONES_KEY);
            event.register(KeyBindings.LASER_BURST_KEY);
            event.register(KeyBindings.CUBIC_REGEN_KEY);
        }

        @SubscribeEvent
        public static void registerOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("rickregen_hud", (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null || mc.options.hideGui) return;
                renderCyberHud(guiGraphics, mc);
            });
        }

        private static void renderCyberHud(GuiGraphics gfx, Minecraft mc) {
            long now = AbilityCooldownManager.getCurrentTime(true);
            int x = 8;
            int y = 8;

            gfx.fill(x - 4, y - 4, x + 132, y + 54, 0x880A0F18);
            gfx.drawString(mc.font, "§b[RICK PRIME OS]", x, y, 0x00D2FF, true);

            drawAbilityLine(gfx, mc, "[V] Evasión", Ability.EVASION_TP, now, x, y + 12, 0x00D2FF);
            drawAbilityLine(gfx, mc, "[G] Drones", Ability.COMBAT_DRONES, now, x, y + 22, 0x00D2FF);
            drawAbilityLine(gfx, mc, "[R] Láser", Ability.LASER_BURST, now, x, y + 32, 0xFF2222);
            drawAbilityLine(gfx, mc, "[H] Regen", Ability.CUBIC_REGEN, now, x, y + 42, 0x00FF66);
        }

        private static void drawAbilityLine(GuiGraphics gfx, Minecraft mc, String label, Ability ability, long now, int x, int y, int activeColor) {
            float rem = AbilityCooldownManager.getRemainingSeconds(mc.player.getUUID(), ability, now);
            if (rem <= 0.0f) {
                gfx.drawString(mc.font, label + ": §aLISTO", x, y, activeColor, true);
            } else {
                gfx.drawString(mc.font, String.format("%s: §c%.1fs", label, rem), x, y, 0xAAAAAA, true);
            }
        }
    }

    @Mod.EventBusSubscriber(modid = RickregenMod.MOD_ID, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;

            AbilityCooldownManager.tickClient();
            long now = AbilityCooldownManager.getCurrentTime(true);

            checkAndTrigger(mc, KeyBindings.EVASION_TP_KEY.consumeClick(), Ability.EVASION_TP, now);
            checkAndTrigger(mc, KeyBindings.COMBAT_DRONES_KEY.consumeClick(), Ability.COMBAT_DRONES, now);
            checkAndTrigger(mc, KeyBindings.LASER_BURST_KEY.consumeClick(), Ability.LASER_BURST, now);
            checkAndTrigger(mc, KeyBindings.CUBIC_REGEN_KEY.consumeClick(), Ability.CUBIC_REGEN, now);
        }

        private static void checkAndTrigger(Minecraft mc, boolean pressed, Ability ability, long now) {
            if (pressed && AbilityCooldownManager.isReady(mc.player.getUUID(), ability, now)) {
                AbilityCooldownManager.triggerCooldown(mc.player.getUUID(), ability, now);
                PacketHandler.INSTANCE.sendToServer(new TriggerAbilityPacket(ability));
            }
        }
    }
}
