package com.example.rickregen.network;

import com.example.rickregen.ability.AbilityCooldownManager.Ability;
import com.example.rickregen.ability.AbilityExecutor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class TriggerAbilityPacket {
    private final Ability ability;

    public TriggerAbilityPacket(Ability ability) {
        this.ability = ability;
    }

    public TriggerAbilityPacket(FriendlyByteBuf buf) {
        this.ability = buf.readEnum(Ability.class);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(this.ability);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                AbilityExecutor.execute(player, this.ability);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
