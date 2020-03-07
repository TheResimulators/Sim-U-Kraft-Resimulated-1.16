package com.resimulators.simukraft.client.gui;

import com.resimulators.simukraft.common.world.SavedWorldData;
import com.resimulators.simukraft.common.events.world.NewDayEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.time.format.TextStyle;
import java.util.Locale;

public class SimHud extends AbstractGui {
    private int num;
    private double credits;
    @SubscribeEvent
    public void render(RenderGameOverlayEvent.Text event){
        if (Minecraft.getInstance().currentScreen == null) {
           SavedWorldData data = SavedWorldData.get(Minecraft.getInstance().world);
            num = data.getFactionWithPlayer(Minecraft.getInstance().player.getUniqueID()).getAmountOfSims();
            credits = data.getFactionWithPlayer(Minecraft.getInstance().player.getUniqueID()).getCredits();
            String day = NewDayEvent.getDay().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
            Minecraft.getInstance().fontRenderer.drawString("Sims: " + num, 10, 10, 16777215);
            Minecraft.getInstance().fontRenderer.drawString("Credits: " + String.format("%.2f", credits), 10, 30, 16777215);
            Minecraft.getInstance().fontRenderer.drawString("Day: " + day, 60, 10, 16777215);

        }
    }


}
