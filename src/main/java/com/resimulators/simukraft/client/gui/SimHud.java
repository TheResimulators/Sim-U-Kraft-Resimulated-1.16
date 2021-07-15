package com.resimulators.simukraft.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.resimulators.simukraft.SimuKraft;
import com.resimulators.simukraft.common.world.Faction;
import com.resimulators.simukraft.common.world.SavedWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;

public class SimHud extends AbstractGui {
    private int num;
    private double credits;

    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Text event) {
        if (Minecraft.getInstance().screen == null) {
            World world = SimuKraft.proxy.getClientWorld();
            PlayerEntity player = SimuKraft.proxy.getClientPlayer();
            if (world != null && player != null) {
                SavedWorldData data = SavedWorldData.get(world);
                Faction faction = data.getFactionWithPlayer(player.getUUID());
                if (faction != null) {
                    num = data.getFactionWithPlayer(player.getUUID()).getAmountOfSims();
                    credits = data.getFactionWithPlayer(player.getUUID()).getCredits();
                    String day = DayOfWeek.of((int) (1 + (Math.floor(world.getDayTime() / 24000f) % 7))).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
                    Minecraft.getInstance().font.draw(new MatrixStack(), "Sims: " + num, 10, 10, 16777215);
                    Minecraft.getInstance().font.draw(new MatrixStack(), "Credits: " + String.format("%.2f", credits), 10, 30, 16777215);
                    Minecraft.getInstance().font.draw(new MatrixStack(), "Day: " + day, 60, 10, 16777215);
                }
            }
        }
    }


}
