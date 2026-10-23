package net.hyze.core.spigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.Lists;
import dev.utils.bukkit.stop.StopManager;
import dev.utils.echo.Echo;
import dev.utils.shared.concurrent.NamedThreadFactory;
import lombok.Getter;
import lombok.Setter;
import net.citizensnpcs.api.CitizensPlugin;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.hyze.core.shared.CoreConstants;
import net.hyze.core.shared.CoreProvider;
import net.hyze.core.shared.echo.packets.user.KickUserPacket;
import net.hyze.core.shared.misc.jackson.LocationDeserializer;
import net.hyze.core.shared.misc.report.ReportManager;
import net.hyze.core.shared.providers.EchoProvider;
import net.hyze.core.shared.user.User;
import net.hyze.core.shared.user.preferences.PreferenceStatus;
import net.hyze.core.spigot.commands.CommandRegistry;
import net.hyze.core.spigot.commands.impl.*;
import net.hyze.core.spigot.commands.impl.basics.*;
import net.hyze.core.spigot.commands.impl.cash.CashCommand;
import net.hyze.core.spigot.commands.impl.group.GroupCommand;
import net.hyze.core.spigot.commands.impl.restart.RestartCommand;
import net.hyze.core.spigot.commands.impl.tell.ReplyCommand;
import net.hyze.core.spigot.commands.impl.tell.TellCommand;
import net.hyze.core.spigot.echo.listeners.EchoListeners;
import net.hyze.core.spigot.echo.listeners.UserEchoListener;
import net.hyze.core.spigot.events.HyzePreStopEvent;
import net.hyze.core.spigot.listeners.*;
import net.hyze.core.spigot.listeners.armor.ArmorListener;
import net.hyze.core.spigot.misc.customitem.listeners.CustomItemListener;
import net.hyze.core.spigot.misc.economy.CoreEconomy;
import net.hyze.core.spigot.misc.enchantments.listeners.CustomEnchantmentsListener;
import net.hyze.core.spigot.misc.enchantments.listeners.EnchantmentsMerchantRecipeListener;
import net.hyze.core.spigot.misc.hiddencuboid.HiddenCuboidManager;
import net.hyze.core.spigot.misc.hologram.HologramArmorStand;
import net.hyze.core.spigot.misc.hologram.HologramListener;
import net.hyze.core.spigot.misc.jackson.MaterialDataDeserializer;
import net.hyze.core.spigot.misc.jackson.PotionEffectTypeDeserializer;
import net.hyze.core.spigot.misc.npc.NPCStorage;
import net.hyze.core.spigot.misc.preference.CorePreference;
import net.hyze.core.spigot.misc.preference.PreferenceIcon;
import net.hyze.core.spigot.misc.preference.PreferenceInventoryRegistry;
import net.hyze.core.spigot.misc.report.AutoReportTask;
import net.hyze.core.spigot.misc.scoreboard.bukkit.GroupScoreboard;
import net.hyze.core.spigot.misc.tpa.commands.TPAcceptCommand;
import net.hyze.core.spigot.misc.utils.CustomPayloadFixer;
import net.hyze.core.spigot.misc.utils.NMS;
import net.hyze.core.spigot.misc.utils.SignMenuFactory;
import net.hyze.core.spigot.voidworld.VoidChunkGenerator;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.EntityLightning;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.entity.Entity;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffectType;
import org.greenrobot.eventbus.EventBus;

import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public final class CoreSpigotPlugin extends CustomPlugin {

    public static int STOP_COUNTDOWN = 30;

    @Getter
    private final StopManager stopManager = new StopManager(this, sender -> new HyzePreStopEvent(sender, STOP_COUNTDOWN));

    @Getter
    private static CoreSpigotPlugin instance;

    @Getter
    private static SignMenuFactory signFactory;

    @Getter
    private final ExecutorService echoExecutorService = Executors.newFixedThreadPool(5, new NamedThreadFactory("Echo"));

    @Setter
    @Getter
    private static CoreEconomy economy;

    @Getter
    private NPCRegistry npcRegistry;

    public CoreSpigotPlugin() {
        super(true);

        instance = this;

        CoreSpigotWrapper.setWrapper(new CoreSpigotWrapper());

        CoreProvider.Redis.ECHO = new EchoProvider(
                "",
                () -> CoreProvider.Redis.REDIS_MAIN,
                EventBus.builder()
                        .logNoSubscriberMessages(false)
                        .throwSubscriberException(true)
                        .executorService(Executors.newCachedThreadPool(new NamedThreadFactory(
                                "Echo Background", true
                        )))
                        .build()
        );

        try {
            System.out.println("MXBean name: " + ManagementFactory.getRuntimeMXBean().getName());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        TPAcceptCommand.registerFilter(
                "t",
                "Aceitar todos os pedidos",
                (u, i) -> true
        );
    }

    @Override
    public void onEnable() {
        super.onEnable();

        CitizensPlugin citizensPlugin = (CitizensPlugin) Bukkit.getPluginManager().getPlugin("Citizens");

        this.npcRegistry = citizensPlugin.createNamedNPCRegistry("MINECRAFT_DEV", new NPCStorage());

        try {
            Class<PacketContainer> clazz = (Class<PacketContainer>) Class.forName("com.comphenix.protocol.events.PacketContainer");
            System.out.println(clazz.getSimpleName() + " found");
        } catch (ClassNotFoundException e) {
            System.out.println("PacketContainer not found");
            e.printStackTrace();
            Bukkit.shutdown();
        }

        // test.
        NMS.registerCustomEntity(HologramArmorStand.class, EntityArmorStand.class);

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Location.class, new LocationDeserializer<>(Location.class, CoreSpigotConstants.LOCATION_PARSER));
        module.addDeserializer(PotionEffectType.class, new PotionEffectTypeDeserializer());
        module.addDeserializer(MaterialData.class, new MaterialDataDeserializer());
        CoreConstants.JACKSON.registerModule(module);

        HiddenCuboidManager hiddenCuboidManager = new HiddenCuboidManager();

        PluginManager pluginManager = Bukkit.getPluginManager();

        pluginManager.registerEvents(new InventoryListener(), this);
        pluginManager.registerEvents(new PlayerConnectionListener(), this);
        pluginManager.registerEvents(new EntityListener(), this);
        pluginManager.registerEvents(new ArmorListener(), this);
        pluginManager.registerEvents(hiddenCuboidManager, this);

        pluginManager.registerEvents(new ServerListener(), this);
        pluginManager.registerEvents(new WorldListener(), this);
        pluginManager.registerEvents(new PlayerListener(), this);
        pluginManager.registerEvents(new CustomItemListener(), this);
        pluginManager.registerEvents(new EnchantmentsMerchantRecipeListener(), this);
        pluginManager.registerEvents(new CustomEnchantmentsListener(), this);
        pluginManager.registerEvents(new HologramListener(), this);

        /*
         * Registro de comandos.
         */
        CommandRegistry.unregisterAllCommands();

        CommandRegistry.registerCommand(new StaffCommand());
        CommandRegistry.registerCommand(new GodCommand());
        //CommandRegistry.registerCommand(new YoutubeCommand());
        CommandRegistry.registerCommand(new LocationCommand());
        CommandRegistry.registerCommand(new FlyCommand());
        CommandRegistry.registerCommand(new GameModeCommand());
        CommandRegistry.registerCommand(new SpeedCommand());
        CommandRegistry.registerCommand(new PluginsCommand());
        CommandRegistry.registerCommand(new HealCommad());
        CommandRegistry.registerCommand(new NBTCommand());
        CommandRegistry.registerCommand(new PreferenceCommand());
        CommandRegistry.registerCommand(new FireballCommand());
        CommandRegistry.registerCommand(new AdminCommand());
        CommandRegistry.registerCommand(new ChangePassCommand());
        CommandRegistry.registerCommand(new TellCommand());
        CommandRegistry.registerCommand(new ReplyCommand());
        CommandRegistry.registerCommand(new TPCommand());
        CommandRegistry.registerCommand(new TPPosCommand());
        CommandRegistry.registerCommand(new TPWorldCommand());
        CommandRegistry.registerCommand(new GroupCommand());
        CommandRegistry.registerCommand(new CashCommand());
        CommandRegistry.registerCommand(new ClearCommand());
        CommandRegistry.registerCommand(new EnchantCommand());
        CommandRegistry.registerCommand(new HatCommand());
        CommandRegistry.registerCommand(new InvseeCommand());
        CommandRegistry.registerCommand(new SignCommand());
        CommandRegistry.registerCommand(new ThorCommand());
        CommandRegistry.registerCommand(new TpAllCommand());

        CommandRegistry.registerCommand(new TitleClearCommand());
        CommandRegistry.registerCommand(new OnlineCommand());
        CommandRegistry.registerCommand(new TPHereCommand());
        CommandRegistry.registerCommand(new RestartCommand());
        CommandRegistry.registerCommand(new ColorsCommand());
        CommandRegistry.registerCommand(new OpCommand());
        CommandRegistry.registerCommand(new GiveCommand());
        CommandRegistry.registerCommand(new HeadCommand());
        CommandRegistry.registerCommand(new ExceptionCommand());
        CommandRegistry.registerCommand(new SoundCommand());
        CommandRegistry.registerCommand(new StopCommand());
        CommandRegistry.registerCommand(new MaintenanceCommand());
//        CommandRegistry.registerCommand(new EnchantmentsCommand());

        // TODO remover
        CommandRegistry.registerCommand(new ShopTest());

        /*
         * Registrando eventos do Echo
         */
        Echo echo = CoreProvider.Redis.ECHO.provide();

        List<String> channels = Lists.newArrayList(
                Echo.BASE_CHANNEL,
                "dev.utils.minigames.proxies"
        );

        if (CoreProvider.getApp() != null && CoreProvider.getApp().getServer() != null) {
            channels.add(Echo.BASE_CHANNEL + "." + CoreProvider.getApp().getServer().getId());
        }


        echo.subscribe(channels, (packet, runnable) -> {
            Bukkit.getScheduler().runTask(CoreSpigotPlugin.this, () -> {
                runnable.run();
            });
        });

        echo.registerListener(new UserEchoListener());
        echo.registerListener(new EchoListeners());

        Bukkit.getScheduler().runTask(this, () -> {
            // NÃO REMOVER!
            World world = Bukkit.getWorlds().get(0);

            EntityLightning lightning = new EntityLightning(((CraftWorld) world).getHandle(), 0, 50, 0, true);
            ((CraftWorld) world).getHandle().strikeLightning(lightning);
        });

        /*
         * Registrando preferência no inventário
         */
        PreferenceInventoryRegistry.registry(
                CorePreference.TELL.name(),
                new PreferenceIcon(
                        "Mensagens Privadas",
                        new String[]{"Habilite suas mensagens privadas."},
                        new ItemStack(Material.BOOK_AND_QUILL)
                ),
                PreferenceStatus.ON
        );

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                CustomPayloadFixer.checkCustomPayload(event);
            }
        });

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(this, PacketType.Play.Client.BLOCK_PLACE) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                CustomPayloadFixer.checkPlace(event);
            }
        });

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            CustomPayloadFixer.PACKET_USAGE.keySet().removeIf(player -> !player.isOnline() || !player.isValid());
        }, 20L, 20L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            CustomPayloadFixer.PACKET_USAGE_PLACE.entrySet().stream()
                    .filter(entry -> entry.getValue() == -2)
                    .map(entry -> Bukkit.getPlayerExact(entry.getKey()))
                    .filter(Objects::nonNull)
                    .forEach(player -> {
                        User user = CoreProvider.Cache.Local.USERS.provide().get(player.getName());

                        KickUserPacket packet = new KickUserPacket(user.getId(), ChatColor.RED + "The books on the table");

                        CoreProvider.Redis.ECHO.provide().publish(packet);
                    });
        }, 20L, 20L);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            CustomPayloadFixer.PACKET_USAGE_PLACE.keySet().removeIf(name -> Bukkit.getPlayerExact(name) == null);
        }, 20L, 20L);

        CoreProvider.Repositories.REPORTS.provide().fetchCategories().forEach(ReportManager::registerCategory);

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, new AutoReportTask(), 20L, 20L);

        signFactory = new SignMenuFactory(this);
    }

    public boolean isNPC(Entity entity) {
        return getNpcRegistry().isNPC(entity);
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new VoidChunkGenerator();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Setter
    @Getter
    private static Function<User, GroupScoreboard> scoreboardCreator;
}
