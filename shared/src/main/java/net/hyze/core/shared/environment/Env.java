package net.hyze.core.shared.environment;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Setter;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Env {

    private static final Logger LOGGER = Logger.getLogger("global");

    private static final HashMap<String, JsonElement> ENVIRONMENT_MAP =
            Maps.newHashMap();

    private static final Gson GSON = new Gson();

    @Setter
    private static File primaryEnvironmentFile;

    private static JsonObject PRIMARY_ENVIRONMENT_JSON = new JsonObject();
    private static JsonObject SECONDARY_ENVIRONMENT_JSON = new JsonObject();
    private static JsonObject ENVIRONMENT_JSON = new JsonObject();

    private static boolean INITIALIZED = false;

    public static synchronized void fetchEnvironment() {
        String envPath = System.getProperty("env");

        if (envPath == null || !new File(envPath).isFile()) {
            envPath = "/home/minecraft/cloud/environment.json";
        }

        primaryEnvironmentFile = new File(envPath);
        PRIMARY_ENVIRONMENT_JSON = new JsonObject();

        if (primaryEnvironmentFile.isFile()) {
            try {
                String content = FileUtils.readFileToString(
                        primaryEnvironmentFile,
                        Charsets.UTF_8
                );

                JsonObject loaded = GSON.fromJson(content, JsonObject.class);

                if (loaded != null) {
                    PRIMARY_ENVIRONMENT_JSON = loaded;
                }
            } catch (IOException | JsonParseException ex) {
                LOGGER.log(
                        Level.SEVERE,
                        "Não foi possível ler o arquivo de ambiente: " + envPath,
                        ex
                );
            }
        } else {
            LOGGER.log(
                    Level.WARNING,
                    "Arquivo de ambiente não encontrado: " + envPath
            );
        }

        ENVIRONMENT_JSON = mergeJsonObjects(
                SECONDARY_ENVIRONMENT_JSON,
                PRIMARY_ENVIRONMENT_JSON
        );

        ENVIRONMENT_MAP.clear();
        mapJsonObject("", ENVIRONMENT_JSON, ENVIRONMENT_MAP);

        INITIALIZED = true;

        LOGGER.log(
                Level.INFO,
                "[Environment] Loaded from " + envPath
        );
    }

    public static JsonElement get(String key) {
        if (!INITIALIZED) {
            fetchEnvironment();
        }

        return ENVIRONMENT_MAP.get(key);
    }

    private static boolean isMissing(JsonElement element) {
        return element == null || element.isJsonNull();
    }

    public static String getString(String key, String defaultValue) {
        JsonElement element = get(key);

        if (isMissing(element)) {
            return defaultValue;
        }

        try {
            if (element.isJsonPrimitive()) {
                return element.getAsString();
            }
        } catch (UnsupportedOperationException | IllegalStateException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Valor inválido para String na configuração: " + key,
                    ex
            );
        }

        LOGGER.log(
                Level.WARNING,
                "Valor não é uma String válida: " + key
        );

        return defaultValue;
    }

    public static int getInt(String key, int defaultValue) {
        JsonElement element = get(key);

        if (isMissing(element) || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsInt();
        } catch (UnsupportedOperationException | IllegalStateException
                 | NumberFormatException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Valor inteiro inválido na configuração: " + key,
                    ex
            );
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        JsonElement element = get(key);

        if (isMissing(element) || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsBoolean();
        } catch (UnsupportedOperationException | IllegalStateException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Valor booleano inválido na configuração: " + key,
                    ex
            );
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        JsonElement element = get(key);

        if (isMissing(element) || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsLong();
        } catch (UnsupportedOperationException | IllegalStateException
                 | NumberFormatException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Valor long inválido na configuração: " + key,
                    ex
            );
            return defaultValue;
        }
    }

    public static double getDouble(String key, double defaultValue) {
        JsonElement element = get(key);

        if (isMissing(element) || !element.isJsonPrimitive()) {
            return defaultValue;
        }

        try {
            return element.getAsDouble();
        } catch (UnsupportedOperationException | IllegalStateException
                 | NumberFormatException ex) {
            LOGGER.log(
                    Level.WARNING,
                    "Valor double inválido na configuração: " + key,
                    ex
            );
            return defaultValue;
        }
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static long getLong(String key) {
        return getLong(key, 0L);
    }

    public static double getDouble(String key) {
        return getDouble(key, 0.0D);
    }

    private static void mapJsonObject(
            String key,
            JsonObject jsonObject,
            HashMap<String, JsonElement> map
    ) {
        if (jsonObject == null) {
            return;
        }

        for (Map.Entry<String, JsonElement> entry
                : jsonObject.entrySet()) {

            String fullKey = key.isEmpty()
                    ? entry.getKey()
                    : key + "." + entry.getKey();

            JsonElement value = entry.getValue();

            if (value != null && value.isJsonObject()) {
                mapJsonObject(
                        fullKey,
                        value.getAsJsonObject(),
                        map
                );
            } else {
                map.put(fullKey, value);
            }
        }
    }

    private static JsonObject mergeJsonObjects(
            JsonObject o1,
            JsonObject o2
    ) {
        JsonObject out = new JsonObject();

        if (o1 != null) {
            for (Map.Entry<String, JsonElement> entry
                    : o1.entrySet()) {
                out.add(entry.getKey(), entry.getValue());
            }
        }

        if (o2 != null) {
            for (Map.Entry<String, JsonElement> entry
                    : o2.entrySet()) {

                String key = entry.getKey();
                JsonElement value = entry.getValue();

                if (value != null
                        && value.isJsonObject()
                        && out.has(key)
                        && out.get(key) != null
                        && out.get(key).isJsonObject()) {

                    out.add(
                            key,
                            mergeJsonObjects(
                                    out.get(key).getAsJsonObject(),
                                    value.getAsJsonObject()
                            )
                    );
                } else {
                    // O arquivo principal sobrescreve o secundário,
                    // inclusive quando o valor é explicitamente null.
                    out.add(key, value);
                }
            }
        }

        return out;
    }
}