package com.example.rickregen.ability;

import com.example.rickregen.ability.AbilityCooldownManager.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class AbilityExecutor {

    // Colores neon definidos: Azul #00D2FF, Rojo #FF2222, Verde #00FF66
    private static final DustParticleOptions BLUE_CABLE = new DustParticleOptions(new Vector3f(0.0f, 0.82f, 1.0f), 1.3f);
    private static final DustParticleOptions RED_LASER = new DustParticleOptions(new Vector3f(1.0f, 0.13f, 0.13f), 1.2f);
    private static final DustParticleOptions GREEN_REGEN = new DustParticleOptions(new Vector3f(0.0f, 1.0f, 0.4f), 1.4f);

    public static void execute(ServerPlayer player, Ability ability) {
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();

        if (!AbilityCooldownManager.isReady(player.getUUID(), ability, now)) {
            return;
        }

        switch (ability) {
            case EVASION_TP -> performEvasionTeleport(player, level);
            case COMBAT_DRONES -> deployCombatDrones(player, level);
            case LASER_BURST -> fireLaserBurst(player, level);
            case CUBIC_REGEN -> triggerCubicRegen(player, level);
        }

        AbilityCooldownManager.triggerCooldown(player.getUUID(), ability, now);
    }

    private static void performEvasionTeleport(ServerPlayer player, ServerLevel level) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(12.0));

        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 targetPos = hit.getType() == HitResult.Type.MISS ? end : hit.getLocation().subtract(look.scale(0.6));

        for (int i = 0; i < 20; i++) {
            double t = i / 20.0;
            Vec3 p = start.lerp(targetPos, t);
            level.sendParticles(BLUE_CABLE, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.01);
        }

        BlockPos safePos = BlockPos.containing(targetPos);
        if (!level.getBlockState(safePos).isSuffocating(level, safePos)) {
            player.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            player.fallDistance = 0.0f;
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 1.35f);
        }
    }

    private static void deployCombatDrones(ServerPlayer player, ServerLevel level) {
        LivingEntity target = player.getLastHurtMob();
        for (int i = 0; i < 3; i++) {
            Bee drone = EntityType.BEE.create(level);
            if (drone != null) {
                double angle = (Math.PI * 2 / 3) * i;
                drone.moveTo(player.getX() + Math.cos(angle) * 1.5, player.getY() + 1.8, player.getZ() + Math.sin(angle) * 1.5, player.getYRot(), 0);
                drone.setCustomName(net.minecraft.network.chat.Component.literal("§bDron Rick Prime"));
                drone.setCustomNameVisible(true);
                drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
                drone.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1));
                if (target != null && target.isAlive()) {
                    drone.setTarget(target);
                }
                level.addFreshEntity(drone);
                level.sendParticles(BLUE_CABLE, drone.getX(), drone.getY(), drone.getZ(), 10, 0.2, 0.2, 0.2, 0.02);
            }
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);
    }

    private static void fireLaserBurst(ServerPlayer player, ServerLevel level) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        double range = 24.0;
        Vec3 end = start.add(look.scale(range));

        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                level, player, start, end, searchBox,
                e -> !e.isSpectator() && e.isPickable() && e != player
        );

        Vec3 impactPoint = end;
        if (entityHit != null) {
            impactPoint = entityHit.getLocation();
            entityHit.getEntity().hurt(level.damageSources().playerAttack(player), 11.0f);
            entityHit.getEntity().setSecondsOnFire(3);
        }

        for (int i = 1; i <= 30; i++) {
            double t = i / 30.0;
            Vec3 p = start.lerp(impactPoint, t);
            level.sendParticles(RED_LASER, p.x, p.y, p.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
        level.playSound(null, player.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 0.9f, 1.9f);
    }

    public static void triggerCubicRegen(ServerPlayer player, ServerLevel level) {
        player.heal(12.0f);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 2));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, 1));
        level.sendParticles(GREEN_REGEN, player.getX(), player.getY() + 1.0, player.getZ(), 35, 0.5, 0.8, 0.5, 0.05);
        level.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.5f);
    }
}
