package com.rschao.plugins.dnd.command;

import com.rschao.plugins.dnd.DndMinis;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpellCommands {
    // Registra los comandos: createSpell, viewSpell, castSpell
    public static void register() {
        Plugin plugin = DndMinis.getProvidingPlugin(DndMinis.class);
        if (plugin == null) {
            Bukkit.getLogger().warning("SpellCommands: plugin no encontrado, comandos no registrados.");
            return;
        }

        File file = new File(plugin.getDataFolder(), "spells.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        try {
            if (!file.exists()) file.createNewFile();
        } catch (IOException e) {
            Bukkit.getLogger().warning("SpellCommands: no se pudo crear spells.yml: " + e.getMessage());
            return;
        }

        java.util.function.Supplier<FileConfiguration> loadConfig = () -> YamlConfiguration.loadConfiguration(file);
        java.util.function.Consumer<FileConfiguration> saveConfig = cfg -> {
            try {
                cfg.save(file);
            } catch (IOException e) {
                Bukkit.getLogger().warning("SpellCommands: error guardando spells.yml: " + e.getMessage());
            }
        };

        // createSpell <id> <displayName> <damage>
        new CommandAPICommand("createSpell")
                .withPermission("dndminis.spell.create")
                .withArguments(new StringArgument("id"), new StringArgument("displayName"), new StringArgument("damage"))
                .executes((sender, args) -> {
                    String id = ((String) args.get("id")).toLowerCase();
                    String displayName = (String) args.get("displayName");
                    String damage = (String) args.get("damage");

                    FileConfiguration cfg = loadConfig.get();
                    String base = "spells." + id;
                    cfg.set(base + ".displayName", displayName);
                    cfg.set(base + ".damage", damage);
                    saveConfig.accept(cfg);

                    sender.sendMessage("Hechizo '" + id + "' creado: " + displayName + " -> " + damage);
                }).register("dndminis");

        // viewSpell <id>
        new CommandAPICommand("viewSpell")
                .withPermission("dndminis.spell.view")
                .withArguments(new StringArgument("id"))
                .executes((sender, args) -> {
                    String id = ((String) args.get("id")).toLowerCase();
                    FileConfiguration cfg = loadConfig.get();
                    String base = "spells." + id;
                    if (!cfg.contains(base)) {
                        sender.sendMessage("Hechizo '" + id + "' no encontrado.");
                        return;
                    }
                    String displayName = cfg.getString(base + ".displayName", id);
                    String damage = cfg.getString(base + ".damage", "0");
                    sender.sendMessage("Hechizo: " + id + " | Nombre: " + displayName + " | Daño: " + damage);
                }).register("dndminis");

        // castSpell <id> [target] [subtract]
        new CommandAPICommand("castSpell")
                .withPermission("dndminis.spell.cast")
                .withArguments(new StringArgument("id"), new IntegerArgument("target").setOptional(true), new BooleanArgument("subtract").setOptional(true))
                .executes((sender, args) -> {
                    String id = ((String) args.get("id")).toLowerCase();
                    Integer target = (Integer) args.getOrDefault("target", 0);
                    boolean subtract = (boolean) args.getOrDefault("subtract", false);

                    FileConfiguration cfg = loadConfig.get();
                    String base = "spells." + id;
                    if (!cfg.contains(base)) {
                        sender.sendMessage("Hechizo '" + id + "' no encontrado.");
                        return;
                    }
                    String displayName = cfg.getString(base + ".displayName", id);
                    String damageSpec = cfg.getString(base + ".damage", "0");

                    RollResult rr = parseAndRoll(damageSpec);
                    if (!rr.valid) {
                        sender.sendMessage("Cadena de daño inválida: " + damageSpec);
                        return;
                    }

                    sender.sendMessage("Lanzando '" + displayName + "' (" + damageSpec + ")");
                    sender.sendMessage("Detalle: " + rr.details);
                    sender.sendMessage("Daño total: " + rr.total);

                    if (target != null) {
                        int result = subtract ? (target - rr.total) : (target + rr.total);
                        sender.sendMessage((subtract ? "Resta " : "Suma ") + rr.total + (" a ") + target + " = " + result);
                    }
                }).register("dndminis");
    }

    // Resultado del parseo y tiradas
    private static class RollResult {
        boolean valid;
        int total;
        String details;

        RollResult(boolean valid, int total, String details) {
            this.valid = valid;
            this.total = total;
            this.details = details;
        }
    }

    // Parsear expresiones como: 4d8+1d6+3d4+5 (soporta + y - entre términos)
    private static RollResult parseAndRoll(String spec) {
        if (spec == null) return new RollResult(false, 0, "");
        String expr = spec.replaceAll("\\s+", "");
        if (expr.isEmpty()) return new RollResult(false, 0, "");

        Pattern p = Pattern.compile("([+-]?)(\\d+)(?:d(\\d+))?", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(expr);

        StringBuilder matchedConcat = new StringBuilder();
        List<String> groups = new ArrayList<>();
        Random rnd = new Random();
        int total = 0;

        while (m.find()) {
            String sign = m.group(1);
            if (sign == null || sign.isEmpty()) sign = "+";
            String numStr = m.group(2);
            String sidesStr = m.group(3);

            matchedConcat.append(m.group(0));
            if (sidesStr != null) {
                int count;
                int sides;
                try {
                    count = Integer.parseInt(numStr);
                    sides = Integer.parseInt(sidesStr);
                } catch (NumberFormatException e) {
                    return new RollResult(false, 0, "");
                }
                if (count < 0 || sides <= 0) return new RollResult(false, 0, "");
                List<Integer> rolls = new ArrayList<>();
                int sumGroup = 0;
                for (int i = 0; i < count; i++) {
                    int r = rnd.nextInt(sides) + 1;
                    rolls.add(r);
                    sumGroup += r;
                }
                int signed = sign.equals("-") ? -sumGroup : sumGroup;
                total += signed;
                groups.add((sign.equals("-") ? "-" : "+") + count + "d" + sides + ": " + rolls.toString() + " => " + (sign.equals("-") ? "-" : "") + sumGroup);
            } else {
                int val;
                try {
                    val = Integer.parseInt(numStr);
                } catch (NumberFormatException e) {
                    return new RollResult(false, 0, "");
                }
                int signed = sign.equals("-") ? -val : val;
                total += signed;
                groups.add((sign.equals("-") ? "-" : "+") + val);
            }
        }

        // Verificar que todo el string fue consumido por matches
        if (!matchedConcat.toString().equals(expr)) {
            return new RollResult(false, 0, "");
        }

        StringBuilder details = new StringBuilder();
        for (int i = 0; i < groups.size(); i++) {
            if (i > 0) details.append(" | ");
            details.append(groups.get(i));
        }

        return new RollResult(true, total, details.toString());
    }
}

