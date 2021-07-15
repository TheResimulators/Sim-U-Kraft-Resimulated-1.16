package com.resimulators.simukraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

public enum GuiMod {
    ;
    private final Function<PlayerEntity, ItemStack> stackReader;
    private final Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider;

    public static Screen openScreen(Minecraft minecraft, Screen screen) {
        return screen;
    }

    public static Color getColor(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    GuiMod(Function<PlayerEntity, ItemStack> stackReader, Function<ItemStack, Supplier<? extends Screen>> clientScreenProvider) {
        this.stackReader = stackReader;
        this.clientScreenProvider = clientScreenProvider;
    }

    public boolean openScreen(PlayerEntity player) {
        if (clientScreenProvider == null)
            return false;

        ItemStack stack = stackReader.apply(player);
        if (stack == null || stack.isEmpty())
            return false;

        Screen screen = clientScreenProvider.apply(stack).get();
        Minecraft.getInstance().setScreen(screen);
        return screen == null;
    }
}
