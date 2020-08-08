package com.resimulators.simukraft.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandStructure {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands.structure.failed"));

    public static void register(CommandDispatcher<CommandSource> dispatcher) {

    }

    private static void save() {

    }

    private static void load() {

    }

    private static void list() {

    }
}
