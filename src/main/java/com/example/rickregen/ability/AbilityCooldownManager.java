package com.example.rickregen.ability;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AbilityCooldownManager {

    public enum Ability {
        EVASION_TP(120),      // 6s (20 ticks/s)
        COMBAT_DRONES(600),   // 30s
        LASER_BURST(80),      // 4s
        CUBIC_REGEN(1200);    // 60s

        private final int cooldownTicks;

        Ability(int cooldownTicks) {
            this.cooldownTicks = cooldownTicks;
        }

        public int getCooldownTicks() {
            return cooldownTicks;
        }
    }

    private static final Map<UUID, Map<Ability, Long>> COOLDOWNS = new HashMap<>();
    private static long clientTickCounter = 0;

    public static void tickClient() {
        clientTickCounter++;
    }

    public static long getCurrentTime(boolean isClient) {
        return isClient ? clientTickCounter : System.currentTimeMillis() / 50L;
    }

    public static boolean isReady(UUID playerId, Ability ability, long currentTick) {
        Map<Ability, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null || !playerMap.containsKey(ability)) {
            return true;
        }
        return currentTick >= playerMap.get(ability);
    }

    public static void triggerCooldown(UUID playerId, Ability ability, long currentTick) {
        COOLDOWNS.computeIfAbsent(playerId, k -> new EnumMap<>(Ability.class))
                 .put(ability, currentTick + ability.getCooldownTicks());
    }

    public static float getRemainingSeconds(UUID playerId, Ability ability, long currentTick) {
        Map<Ability, Long> playerMap = COOLDOWNS.get(playerId);
        if (playerMap == null || !playerMap.containsKey(ability)) {
            return 0.0f;
        }
        long diff = playerMap.get(ability) - currentTick;
        return diff <= 0 ? 0.0f : diff / 20.0f;
    }
}
