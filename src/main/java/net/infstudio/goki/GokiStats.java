package net.infstudio.goki;

import net.infstudio.goki.api.stat.Stat;
import net.infstudio.goki.api.stat.StatBase;
import net.infstudio.goki.api.stat.Stats;
import net.infstudio.goki.common.config.GokiConfig;
import net.infstudio.goki.common.handlers.TickHandler;
import net.infstudio.goki.common.init.GokiSounds;
import net.infstudio.goki.common.network.GokiPacketHandler;
import net.infstudio.goki.common.stat.StatLootCondition;
import net.infstudio.goki.common.utils.Reference;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Reference.MODID)
public class GokiStats {
    public static GokiStats instance;
    public static final Logger log = LogManager.getLogger(Reference.MODID);

    public GokiStats() {
        instance = this;
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        try {
            Class.forName(Stats.class.getName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot initialize GokiStats Reborn skills", e);
        }
        eventBus.addListener(this::createRegistry);
        eventBus.addListener(this::registerStats);
        GokiSounds.register(eventBus);
        StatLootCondition.CONDITIONS.register(eventBus);
        GokiPacketHandler.registerMessages();
        MinecraftForge.EVENT_BUS.register(new TickHandler());
        initConfig();
        log.info("GokiStats Reborn unofficial Forge 1.20.1 port initialized with {} skills", StatBase.stats.size());
    }

    private void createRegistry(NewRegistryEvent event) {
        event.create(new RegistryBuilder<Stat>().setName(StatBase.REGISTRY_NAME), registry -> StatBase.REGISTRY = registry);
    }

    private void registerStats(RegisterEvent event) {
        event.register(ResourceKey.<Stat>createRegistryKey(StatBase.REGISTRY_NAME), helper -> {
            for (StatBase<?> stat : StatBase.stats) {
                helper.register(stat.getRegistryName(), stat);
            }
        });
    }

    public void initConfig() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GokiConfig.serverSpec, Reference.LEGACY_MODID + "-server.toml");
        for (StatBase<?> stat : StatBase.stats) {
            ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, stat.configSpec, "stat_" + stat.getRegistryName().getPath() + ".toml");
        }
    }
}
