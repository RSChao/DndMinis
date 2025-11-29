package com.rschao.plugins.dnd;

import com.rschao.plugins.dnd.command.CharacterCommands;
import com.rschao.plugins.dnd.command.DiceRoller;
import com.rschao.plugins.dnd.command.SpellCommands;
import com.rschao.plugins.dnd.command.SummonMiniMob;
import org.bukkit.plugin.java.JavaPlugin;

public final class DndMinis extends JavaPlugin {

    @Override
    public void onEnable() {
        SummonMiniMob.register();
        DiceRoller.register();
        CharacterCommands.register();
        SpellCommands.register();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
