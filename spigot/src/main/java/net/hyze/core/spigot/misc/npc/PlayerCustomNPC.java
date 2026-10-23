package net.hyze.core.spigot.misc.npc;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.citizensnpcs.api.npc.MetadataStore;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.skin.SkinnableEntity;
import net.hyze.core.spigot.CoreSpigotPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;

public class PlayerCustomNPC extends CustomNPC {

    private final String skinValue;
    private final String skinSign;

    public PlayerCustomNPC(String skinValue, String skinSign, Location npcLocation) {
        this(skinValue, skinSign, npcLocation.clone(), npcLocation.clone());
    }

    public PlayerCustomNPC(String skinValue, String skinSign, Location npcLocation, Location npcLookLocation) {
        super(EntityType.PLAYER, npcLocation, npcLookLocation);

        this.skinValue = skinValue;
        this.skinSign = skinSign;
    }

    @Override
    public double getHologramOffset() {
        return 2.1;
    }

    @Override
    public void spawn() {
        super.spawn();

        MetadataStore data = this.npc.data();

        data.setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA, this.skinValue);
        data.setPersistent(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA, this.skinSign);

        data.setPersistent(NPC.PLAYER_SKIN_USE_LATEST, false);
        data.set(NPC.NAMEPLATE_VISIBLE_METADATA, false);

        Property localData = new Property("textures",
                data.get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_METADATA),
                data.get(NPC.PLAYER_SKIN_TEXTURE_PROPERTIES_SIGN_METADATA));

        try {
            Bukkit.getScheduler().runTaskLater(CoreSpigotPlugin.getInstance(), () -> {

                SkinnableEntity entity = (SkinnableEntity) this.npc.getEntity();

                if (entity == null || entity.getProfile() == null) {
                    return;
                }

                GameProfile profile = entity.getProfile();

                Property current = Iterables.getFirst(profile.getProperties().get("textures"), null);
                if (current != null
                        && current.getValue().equals(localData.getValue())
                        && (current.getSignature() != null && current.getSignature().equals(localData.getSignature()))) {
                    return;
                }

                profile.getProperties().removeAll("textures");
                profile.getProperties().put("textures", localData);

            }, 20L);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}