package net.hyze.core.shared;

import com.google.common.collect.Lists;
import dev.utils.echo.Echo;
import io.sentry.Sentry;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.hyze.core.shared.apps.App;
import net.hyze.core.shared.apps.cache.local.AppLocalCache;
import net.hyze.core.shared.apps.cache.local.AppStatusLocalCache;
import net.hyze.core.shared.apps.cache.redis.AppStatusRedisCache;
import net.hyze.core.shared.apps.storage.AppsRepository;
import net.hyze.core.shared.cache.local.UserGroupLocalCache;
import net.hyze.core.shared.cache.local.UserPreferenceLocalCache;
import net.hyze.core.shared.cache.redis.LobbyRedisCache;
import net.hyze.core.shared.cache.redis.TellRedisCache;
import net.hyze.core.shared.config.cache.ConfigLocalCache;
import net.hyze.core.shared.config.storage.ConfigRepository;
import net.hyze.core.shared.contracts.Provider;
import net.hyze.core.shared.echo.packets.app.AppStoppedPacket;
import net.hyze.core.shared.environment.Env;
import net.hyze.core.shared.exceptions.ApplicationAlreadyPreparedException;
import net.hyze.core.shared.exceptions.InvalidApplicationException;
import net.hyze.core.shared.group.due.storage.GroupDueRepository;
import net.hyze.core.shared.group.storage.AbstractGroupRepository;
import net.hyze.core.shared.group.storage.DefaultGroupRepository;
import net.hyze.core.shared.log.cash.CashLogRepository;
import net.hyze.core.shared.misc.cooldowns.redis.cache.local.UserRedisCooldownsLocalCache;
import net.hyze.core.shared.misc.cooldowns.redis.cache.redis.UserRedisCooldownsRedisCache;
import net.hyze.core.shared.misc.mojang.cache.MojangLocalCache;
import net.hyze.core.shared.misc.purchases.storage.PurchaseRepository;
import net.hyze.core.shared.misc.report.cache.redis.ReportRedisCache;
import net.hyze.core.shared.misc.report.storage.ReportRepository;
import net.hyze.core.shared.misc.youtube.storage.YoutubeRepository;
import net.hyze.core.shared.providers.*;
import net.hyze.core.shared.punishments.cache.PunishmentLocalCache;
import net.hyze.core.shared.punishments.storage.PunishmentRepository;
import net.hyze.core.shared.queue.cache.redis.QueueRedisCache;
import net.hyze.core.shared.reconnections.cache.redis.ReconnectionRedisCache;
import net.hyze.core.shared.servers.Server;
import net.hyze.core.shared.servers.cache.local.ServersLocalCache;
import net.hyze.core.shared.servers.cache.redis.ServersRedisCache;
import net.hyze.core.shared.servers.storage.ServersRepository;
import net.hyze.core.shared.sessions.storage.UserSessionRepository;
import net.hyze.core.shared.settings.cache.redis.SettingsRedisCache;
import net.hyze.core.shared.skins.storage.SkinRepository;
import net.hyze.core.shared.user.cache.local.UserLocalCache;
import net.hyze.core.shared.user.cache.local.UserSkinRecordLocalCache;
import net.hyze.core.shared.user.cache.local.UserStatusLocalCache;
import net.hyze.core.shared.user.cache.redis.UserStatusRedisCache;
import net.hyze.core.shared.user.preferences.storage.PreferenceRepository;
import net.hyze.core.shared.user.storage.AbstractUserRepository;
import net.hyze.core.shared.user.storage.HyzeUserRepository;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreProvider {

    private static final List<Supplier<Provider<?>>> PRIMARY_PROVIDERS = Lists.newLinkedList();
    private static final List<Supplier<Provider<?>>> PROVIDERS = Lists.newLinkedList();

    private static boolean providerPopulated = false;

    public static void populateProviders() {

        if (providerPopulated) {
            return;
        }

        providerPopulated = true;

        // Setup echo base
        Echo.BASE_CHANNEL += ".hyze";

        /*
         * Registry database providers
         *
         * a mysql mains esta sendo registrada aqui pois precisa ser prepada
         * antes das demais
         */

        PRIMARY_PROVIDERS.add(() -> Database.MYSQL_MAIN);
        PRIMARY_PROVIDERS.add(() -> Database.Mongo.MAIN);
        PRIMARY_PROVIDERS.add(() -> Repositories.APPS);

        /*
         * Registry redis providers
         */
        PROVIDERS.add(() -> Redis.REDIS_MAIN);
        PROVIDERS.add(() -> Redis.REDIS_ECHO);
        PROVIDERS.add(() -> Redis.ECHO);

        /*
         * Registry repositories providers
         *
         * O repositório de aplicações não esta sendo registrado aqui pois
         * precisa ser prepado antes dos demais
         */
        PROVIDERS.add(() -> Repositories.GROUPS);
        PROVIDERS.add(() -> Repositories.USERS);
        PROVIDERS.add(() -> Repositories.USERS_PREFERENCES);
        PROVIDERS.add(() -> Repositories.CASH_LOG);
        PROVIDERS.add(() -> Repositories.YOUTUBERS);
        PROVIDERS.add(() -> Repositories.SERVERS);
        PROVIDERS.add(() -> Repositories.PUNISHMENTS);
        PROVIDERS.add(() -> Repositories.SKINS);
        PROVIDERS.add(() -> Repositories.SESSIONS);
        PROVIDERS.add(() -> Repositories.CONFIG);
        PROVIDERS.add(() -> Repositories.PURCHASES);
        PROVIDERS.add(() -> Repositories.GROUPS_DUE);
        PROVIDERS.add(() -> Repositories.REPORTS);

        /*
         * Registry local cache
         *
         * Registrando LocalCacheProvider
         */
        PROVIDERS.add(() -> Cache.Local.APPS);
        PROVIDERS.add(() -> Cache.Local.PUNISHMENTS);
        PROVIDERS.add(() -> Cache.Local.USERS);
        PROVIDERS.add(() -> Cache.Local.USERS_GROUPS);
        PROVIDERS.add(() -> Cache.Local.USERS_PREFERENCES);
        PROVIDERS.add(() -> Cache.Local.MOJANG);
        PROVIDERS.add(() -> Cache.Local.CONFIG);
        PROVIDERS.add(() -> Cache.Local.APPS_STATUS);
        PROVIDERS.add(() -> Cache.Local.USERS_STATUS);
        PROVIDERS.add(() -> Cache.Local.SERVERS);
        PROVIDERS.add(() -> Cache.Local.SKINS);
    }

    private static boolean primaryPrepared = false;
    private static boolean prepared = false;

    @Getter
    private static App app;

    @Getter
    @Setter
    private static Server[] domainServers = null;

    public static App prepare(int applicationPort) throws InvalidApplicationException, ApplicationAlreadyPreparedException {

        if (primaryPrepared) {
            throw new ApplicationAlreadyPreparedException("the application has already been prepared");
        }

        populateProviders();
        preparePrimaryProviders();

        app = Repositories.APPS.provide().fetchByPort(applicationPort);

        if (app == null) {
            throw new InvalidApplicationException(String.format("Invalid application port %s", applicationPort));
        }

        prepare(app);

        return app;
    }

    public static void prepare(@NonNull App app) throws ApplicationAlreadyPreparedException {
        if (prepared) {
            throw new ApplicationAlreadyPreparedException("the application has already been prepared");
        }

        CoreProvider.app = app;

        prepared = true;

        populateProviders();
        preparePrimaryProviders();

        prepareProviders();

        /*
         * Baixando todos os apps e adicionando no cache local
         */
        Repositories.APPS.provide().fetchAll().forEach(Cache.Local.APPS.provide()::put);

        /*
         * Baixando todos os servidores e atualizando enum Server
         */
        Repositories.SERVERS.provide().fetchAll();

        domainServers = Server.values();

        Logger.getGlobal().log(Level.INFO, String.format(
                "Application defined as %s (%s)",
                app.getDisplayName(),
                app.getId()
        ));
    }

    public static void shut() {
        if (prepared) {
            Redis.ECHO.provide().publish(new AppStoppedPacket(app));

            PROVIDERS.stream()
                    .map(Supplier::get)
                    .filter(Objects::nonNull)
                    .forEach(Provider::shut);

            PRIMARY_PROVIDERS.stream()
                    .map(Supplier::get)
                    .filter(Objects::nonNull)
                    .forEach(Provider::shut);

            prepared = false;
        }
    }

    private static void preparePrimaryProviders() {
        if (!primaryPrepared) {
            primaryPrepared = true;

            PRIMARY_PROVIDERS.stream()
                    .map(Supplier::get)
                    .filter(Objects::nonNull)
                    .forEach(Provider::prepare);
        }
    }

    private static void prepareProviders() {
        PROVIDERS.stream()
                .map(Supplier::get)
                .filter(Objects::nonNull)
                .forEach(Provider::prepare);
    }

    public static class Database {

        public static MysqlDatabaseProvider MYSQL_MAIN = new MysqlDatabaseProvider(
                new InetSocketAddress(
                        Env.getString("global.mysql.host"),
                        Env.getInt("global.mysql.port")
                ),
                Env.getString("global.mysql.user"),
                Env.getString("global.mysql.password"),
                Env.getString("global.mysql.database")
        );

        public static class Mongo {

            public static MongoDatabaseProvider MAIN = new MongoDatabaseProvider(
                    Env.getString("global.mongo.host"),
                    Env.getInt("global.mongo.port"),
                    Env.getString("global.mongo.database"),
                    Env.getString("global.mongo.auth.user"),
                    Env.getString("global.mongo.auth.password"),
                    Env.getString("global.mongo.auth.database")
            );
        }
    }

    public static class Redis {

        public static RedisProvider REDIS_MAIN = new RedisProvider(
                new InetSocketAddress(
                        Env.getString("global.redis.host"),
                        Env.getInt("global.redis.port")
                ),
                Env.getString("global.redis.auth"),
                Env.getInt("global.redis.database")
        );

        public static RedisProvider REDIS_ECHO = new RedisProvider(
                new InetSocketAddress(
                        Env.getString("global.redis.host"),
                        Env.getInt("global.redis.port")
                ),
                Env.getString("global.redis.auth"),
                Env.getInt("global.redis.database")
        );

        public static EchoProvider ECHO;
    }

    public static class Repositories {

        public static MysqlRepositoryProvider<AbstractGroupRepository> GROUPS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                DefaultGroupRepository.class
        );

        public static final MysqlRepositoryProvider<AbstractUserRepository> USERS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                HyzeUserRepository.class
        );

        public static final MysqlRepositoryProvider<AppsRepository> APPS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                AppsRepository.class
        );

        public static final MysqlRepositoryProvider<ServersRepository> SERVERS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                ServersRepository.class
        );

        public static final MysqlRepositoryProvider<PreferenceRepository> USERS_PREFERENCES = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                PreferenceRepository.class
        );

        public static final MysqlRepositoryProvider<CashLogRepository> CASH_LOG = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                CashLogRepository.class
        );

        public static final MysqlRepositoryProvider<YoutubeRepository> YOUTUBERS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                YoutubeRepository.class
        );

        public static final MysqlRepositoryProvider<PunishmentRepository> PUNISHMENTS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                PunishmentRepository.class
        );

        public static final MysqlRepositoryProvider<SkinRepository> SKINS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                SkinRepository.class
        );

        public static final MysqlRepositoryProvider<UserSessionRepository> SESSIONS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                UserSessionRepository.class
        );

        public static final MysqlRepositoryProvider<ConfigRepository> CONFIG = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                ConfigRepository.class
        );

        public static final MysqlRepositoryProvider<PurchaseRepository> PURCHASES = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                PurchaseRepository.class
        );

        public static final MysqlRepositoryProvider<GroupDueRepository> GROUPS_DUE = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                GroupDueRepository.class
        );

        public static final MysqlRepositoryProvider<ReportRepository> REPORTS = new MysqlRepositoryProvider<>(
                () -> Database.MYSQL_MAIN,
                ReportRepository.class
        );

    }

    public static class Cache {

        public static class Local {

            public static final LocalCacheProvider<UserSkinRecordLocalCache> SKINS = new LocalCacheProvider<>(
                    new UserSkinRecordLocalCache()
            );

            public static LocalCacheProvider<UserLocalCache> USERS = new LocalCacheProvider<>(
                    new UserLocalCache()
            );

            public static final LocalCacheProvider<UserGroupLocalCache> USERS_GROUPS = new LocalCacheProvider<>(
                    new UserGroupLocalCache()
            );

            public static final LocalCacheProvider<AppLocalCache> APPS = new LocalCacheProvider<>(
                    new AppLocalCache()
            );

            public static final LocalCacheProvider<UserPreferenceLocalCache> USERS_PREFERENCES = new LocalCacheProvider<>(
                    new UserPreferenceLocalCache()
            );

            public static final LocalCacheProvider<PunishmentLocalCache> PUNISHMENTS = new LocalCacheProvider<>(
                    new PunishmentLocalCache()
            );

            public static final LocalCacheProvider<MojangLocalCache> MOJANG = new LocalCacheProvider<>(
                    new MojangLocalCache()
            );

            public static final LocalCacheProvider<ConfigLocalCache> CONFIG = new LocalCacheProvider<>(
                    new ConfigLocalCache()
            );

            public static final LocalCacheProvider<AppStatusLocalCache> APPS_STATUS = new LocalCacheProvider<>(
                    new AppStatusLocalCache()
            );

            public static final LocalCacheProvider<ServersLocalCache> SERVERS = new LocalCacheProvider<>(
                    new ServersLocalCache()
            );

            public static final LocalCacheProvider<UserStatusLocalCache> USERS_STATUS = new LocalCacheProvider<>(
                    new UserStatusLocalCache()
            );

            public static final LocalCacheProvider<UserRedisCooldownsLocalCache> USER_REDIS_COOLDOWNS = new LocalCacheProvider<>(
                    new UserRedisCooldownsLocalCache()
            );
        }

        public static class Redis {

            public static final RedisCacheProvider<AppStatusRedisCache> APPS_STATUS = new RedisCacheProvider<>(
                    new AppStatusRedisCache()
            );

            public static final RedisCacheProvider<UserStatusRedisCache> USERS_STATUS = new RedisCacheProvider<>(
                    new UserStatusRedisCache()
            );


            public static final RedisCacheProvider<ServersRedisCache> SERVERS = new RedisCacheProvider<>(
                    new ServersRedisCache()
            );

            public static final RedisCacheProvider<TellRedisCache> TELL = new RedisCacheProvider<>(
                    new TellRedisCache()
            );

            public static final RedisCacheProvider<ReconnectionRedisCache> RECONNECTIONS = new RedisCacheProvider<>(
                    new ReconnectionRedisCache()
            );

            public static final RedisCacheProvider<ReportRedisCache> REPORTS = new RedisCacheProvider<>(
                    new ReportRedisCache()
            );

            public static final RedisCacheProvider<QueueRedisCache> QUEUES = new RedisCacheProvider<>(
                    new QueueRedisCache()
            );

            public static final RedisCacheProvider<SettingsRedisCache> SETTINGS = new RedisCacheProvider<>(
                    new SettingsRedisCache()
            );

            public static final RedisCacheProvider<LobbyRedisCache> LOBBIES = new RedisCacheProvider<>(
                    new LobbyRedisCache()
            );

            public static final RedisCacheProvider<UserRedisCooldownsRedisCache> USER_REDIS_COOLDOWNS = new RedisCacheProvider<>(
                    new UserRedisCooldownsRedisCache()
            );
//	    public static final RedisCacheProvider<UserSessionRedisCache> USERS_SESSION = new RedisCacheProvider(
//		    new UserSessionRedisCache()
//	    );
        }
    }
}
