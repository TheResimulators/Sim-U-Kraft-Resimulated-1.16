package com.resimulators.simukraft.client.data;

import com.google.common.collect.Iterables;
import com.mojang.authlib.AuthenticationService;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.resimulators.simukraft.SimuKraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.SkullTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

import java.io.File;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SkinCacher {
    private static final Map<String, ResourceLocation> playerSkinMap = new HashMap<>();
    private static PlayerProfileCache profileCache;
    private static MinecraftSessionService sessionService;
    GameProfile profile = null;

    public static ResourceLocation getSkinForSim(String name) {
        if (playerSkinMap.containsKey(name)) {
            return playerSkinMap.get(name);
        }
        return null;
    }

    public void registerSpecialSkins() {
        for (String s : SimuKraft.config.getSims().specialSimNames.get()) {
            playerSkinMap.put(s, getPlayerSkin(s));
        }
    }

    private ResourceLocation getPlayerSkin(String playerName) {
        ResourceLocation playerSkin = DefaultPlayerSkin.getDefaultSkin();
        GameProfile profileDirty = new GameProfile(null, playerName);
        this.profile = updateGameprofile(profileDirty);
        if (this.profile != null) {
            Minecraft minecraft = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(this.profile);
            if (map.containsKey(MinecraftProfileTexture.Type.SKIN))
                playerSkin = minecraft.getSkinManager().registerTexture(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
            else {
                UUID uuid = PlayerEntity.createPlayerUUID(this.profile);
                playerSkin = DefaultPlayerSkin.getDefaultSkin(uuid);
            }
        }
        return playerSkin;
    }

    public static GameProfile updateGameprofile(GameProfile input) {
        if (input != null && !StringUtils.isNullOrEmpty(input.getName())) {
            if (input.isComplete() && input.getProperties().containsKey("textures")) {
                return input;
            } else if (profileCache != null && sessionService != null) {
                GameProfile gameprofile = profileCache.get(input.getName());

                if (gameprofile == null) {
                    return input;
                } else {
                    Property property = (Property) Iterables.getFirst(gameprofile.getProperties().get("textures"), (Object) null);

                    if (property == null) {
                        gameprofile = sessionService.fillProfileProperties(gameprofile, true);
                    }

                    return gameprofile;
                }
            } else {
                return input;
            }
        } else {
            return input;
        }
    }

    public void initSkinService() {
        Proxy proxy = Minecraft.getInstance().getProxy();
        AuthenticationService authenticationservice = new YggdrasilAuthenticationService(proxy, UUID.randomUUID().toString());
        MinecraftSessionService minecraftsessionservice = authenticationservice.createMinecraftSessionService();
        GameProfileRepository gameprofilerepository = authenticationservice.createProfileRepository();
        PlayerProfileCache playerprofilecache = new PlayerProfileCache(gameprofilerepository, new File(Minecraft.getInstance().gameDirectory, MinecraftServer.USERID_CACHE_FILE.getName()));
        SkullTileEntity.setProfileCache(playerprofilecache);
        SkullTileEntity.setSessionService(minecraftsessionservice);
        PlayerProfileCache.setUsesAuthentication(false);
        setProfileCache(playerprofilecache);
        setSessionService(minecraftsessionservice);
    }

    public void setProfileCache(PlayerProfileCache profileCacheIn) {
        profileCache = profileCacheIn;
    }

    public void setSessionService(MinecraftSessionService sessionServiceIn) {
        sessionService = sessionServiceIn;
    }
}
