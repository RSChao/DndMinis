package com.rschao.plugins.dnd.command;

import com.rschao.plugins.dnd.DndMinis;
import com.rschao.plugins.dnd.DndMinis;
import com.rschao.plugins.dnd.char_class.DndClass;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class CharacterCommands {
    // Registra los comandos: createchar, viewchar, setlevel
    public static void register() {
        Plugin plugin = DndMinis.getProvidingPlugin(DndMinis.class);
        if (plugin == null) {
            Bukkit.getLogger().warning("CharacterCommands: plugin 'dndminis' no encontrado, comandos no registrados.");
            return;
        }

        File file = new File(plugin.getDataFolder(), "characters.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            Bukkit.getLogger().warning("CharacterCommands: no se pudo crear characters.yml: " + e.getMessage());
            return;
        }

        // helper: load config each ejecución para reflejar cambios externos
        java.util.function.Supplier<FileConfiguration> loadConfig = () -> YamlConfiguration.loadConfiguration(file);
        java.util.function.Consumer<FileConfiguration> saveConfig = cfg -> {
            try {
                cfg.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().warning("CharacterCommands: error guardando characters.yml: " + e.getMessage());
            }
        };

        // createchar <name> <class> <level> [hp]
        new CommandAPICommand("createchar")
                .withPermission("dndminis.character.create")
                .withArguments(new StringArgument("name"), new StringArgument("class"), new IntegerArgument("level"), new IntegerArgument("hp").setOptional(true))
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    String clazz = (String) args.get("class");
                    int level = (int) args.get("level");
                    int hp = (int) args.getOrDefault("hp", level * 10);

                    for(DndClass c : DndClass.values()){
                        if(c.name().equalsIgnoreCase(clazz)){
                            clazz = c.name();
                            hp = (int) args.getOrDefault("hp", level * c.getPerLevelHp());
                            break;
                        }
                    }

                    FileConfiguration cfg = loadConfig.get();
                    String base = "characters." + name;
                    cfg.set(base + ".class", clazz);
                    cfg.set(base + ".level", level);
                    cfg.set(base + ".hp", hp);
                    saveConfig.accept(cfg);

                    sender.sendMessage("Personaje '" + name + "' creado: clase=" + clazz + " level=" + level + " hp=" + hp);
                }).register("dndminis");

        // viewchar <name>
        new CommandAPICommand("viewchar")
                .withPermission("dndminis.character.view")
                .withArguments(new StringArgument("name"))
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    FileConfiguration cfg = loadConfig.get();
                    String base = "characters." + name;
                    if (!cfg.contains(base)) {
                        sender.sendMessage("Personaje '" + name + "' no encontrado.");
                        return;
                    }
                    int level = cfg.getInt(base + ".level", 0);
                    int hp = cfg.getInt(base + ".hp", level * 10);
                    String clazz = cfg.getString(base + ".class", "Unknown");
                    for(DndClass c : DndClass.values()){
                        if(c.name().equalsIgnoreCase(clazz)){
                            clazz = c.name();
                            hp = level * c.getPerLevelHp();
                            break;
                        }
                    }
                    sender.sendMessage("Personaje: " + name + " | Clase: " + clazz + " | Level: " + level + " | HP: " + hp);
                }).register("dndminis");

        // setlevel <name> <level>  (recalcula hp = level * 10)
        new CommandAPICommand("setlevel")
                .withPermission("dndminis.character.edit")
                .withArguments(new StringArgument("name"), new IntegerArgument("level"))
                .executes((sender, args) -> {
                    String name = (String) args.get("name");
                    int level = (int) args.get("level");

                    FileConfiguration cfg = loadConfig.get();
                    String base = "characters." + name;
                    if (!cfg.contains(base)) {
                        sender.sendMessage("Personaje '" + name + "' no encontrado.");
                        return;
                    }
                    //load class to determine per level hp
                    String clazz = cfg.getString(base + ".class", "Unknown");
                    for(DndClass c : DndClass.values()){
                        if(c.name().equalsIgnoreCase(clazz)){
                            clazz = c.name();

                            break;
                        }
                    }

                    int newHp = DndClass.valueOf(clazz).getPerLevelHp() * level;
                    cfg.set(base + ".level", level);
                    cfg.set(base + ".hp", newHp);
                    saveConfig.accept(cfg);

                    sender.sendMessage("Personaje '" + name + "' actualizado: level=" + level + " hp=" + newHp);
                }).register("dndminis");
    }
}

