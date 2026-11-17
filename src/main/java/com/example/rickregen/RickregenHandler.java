package com.example.rickregen;

import com.example.rickregen.ability.AbilityCooldownManager;
import com.example.rickregen.ability.AbilityCooldownManager.Ability;
import com.example.rickregen.ability.AbilityExecutor;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Vector3f;

import java.util.List;

@Mod.EventBusSubscriber(modid = RickregenMod.MOD_ID)
public class RickregenHandler {

    private static final DustParticleOptions WIRE_BLUE = new DustParticleOptions(new Vector3f(0.0f, 0.82f, 1.0f), 0.9f);
    private static final DustParticleOptions WIRE_RED = new DustParticleOptions(new Vector3f(1.0f, 0.13f, 0.13f), 0.9f);
    private static final DustParticleOptions WIRE_GREEN = new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.4f), 0.9f);

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            long now = level.getGameTime();

            // Chispas de cables ciberneticos de 3 colores al recibir impacto
            level.sendParticles(WIRE_BLUE, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.4, 0.3, 0.02);
            level.sendParticles(WIRE_RED, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.4, 0.3, 0.02);
            level.sendParticles(WIRE_GREEN, player.getX(), player.getY() + 1.0, player.getZ(), 5, 0.3, 0.4, 0.3, 0.02);

            // Supervivencia a caidas severas dejandote a 1 corazon (2.0f)
            if (event.getSource().is(DamageTypes.FALL) && event.getAmount() >= 15.0f) {
                player.setHealth(2.0f);
                event.setCanceled(true);
                if (AbilityCooldownManager.isReady(player.getUUID(), Ability.CUBIC_REGEN, now)) {
                    AbilityExecutor.triggerCubicRegen(player, level);
                    AbilityCooldownManager.triggerCooldown(player.getUUID(), Ability.CUBIC_REGEN, now);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
            // Cuando el jugador esta caido (<= 1 corazon), los mobs hostiles pierden el objetivo y retroceden
            if (player.getHealth() <= 2.0f) {
                AABB searchArea = player.getBoundingBox().inflate(10.0D);
                List<Mob> nearbyMobs = player.level().getEntitiesOfClass(Mob.class, searchArea);

                for (Mob mob : nearbyMobs) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null);
                        mob.getNavigation().moveTo(
                            player.getX() - (mob.getX() - player.getX()),
                            player.getY(),
                            player.getZ() - (mob.getZ() - player.getZ()),
                            1.25D
                        );
                    }
                }
            }
        }
    }
}
