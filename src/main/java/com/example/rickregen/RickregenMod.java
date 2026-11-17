package com.example.rickregen;

import com.example.rickregen.network.PacketHandler;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(RickregenMod.MOD_ID)
public class RickregenMod {
    public static final String MOD_ID = "rickregen";
    public static final Logger LOGGER = LogUtils.getLogger();

    public RickregenMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::register);
        LOGGER.info("Rickregen Cybernetics (Rick Prime Edition) 2.0.0 inicializado correctamente.");
    }
}
