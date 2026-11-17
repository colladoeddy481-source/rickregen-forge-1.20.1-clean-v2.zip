package com.example.rickregen.network;

import com.example.rickregen.RickregenMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(RickregenMod.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetId = 0;

    public static void register() {
        INSTANCE.messageBuilder(TriggerAbilityPacket.class, packetId++, NetworkDirection.PLAY_TO_SERVER)
                .decoder(TriggerAbilityPacket::new)
                .encoder(TriggerAbilityPacket::encode)
                .consumerMainThread(TriggerAbilityPacket::handle)
                .add();
    }
}
