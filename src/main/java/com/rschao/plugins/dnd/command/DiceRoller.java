package com.rschao.plugins.dnd.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.util.Random;

public class DiceRoller {
    public static void register() {
        CommandAPICommand summonMiniMobCommand = new CommandAPICommand("rollsingle")
                .withPermission("dndminis.roll")
                .withArguments(new IntegerArgument("max"), new IntegerArgument("bonus"), new BooleanArgument("advantage").setOptional(true), new BooleanArgument("disadvantage").setOptional(true))
                .executes((sender, args) -> {
                    int max = (int) args.get("max");
                    int bonus = (int) args.get("bonus");
                    boolean advantage = (boolean) args.getOrDefault("advantage", false);
                    boolean disadvantage = (boolean) args.getOrDefault("disadvantage", false);

                    if (advantage && disadvantage) {
                        sender.sendMessage("You cannot have both advantage and disadvantage.");
                        return;
                    }

                    int roll1 = (new Random()).nextInt(1, max + 1);
                    int roll2 = (new Random()).nextInt(1, max + 1);
                    int finalRoll;

                    if (advantage) {
                        finalRoll = Math.max(roll1, roll2);
                    } else if (disadvantage) {
                        finalRoll = Math.min(roll1, roll2);
                    } else {
                        finalRoll = roll1;
                    }

                    finalRoll += bonus;

                    sender.sendMessage("You rolled: " + finalRoll + " (Rolls: " + roll1 + (advantage || disadvantage ? ", " + roll2 : "") + ", Bonus: " + bonus + ")");
                });
        summonMiniMobCommand.register("dndminis");

        // Nuevo comando: rolldice
        CommandAPICommand rollMultiCommand = new CommandAPICommand("rollmultiple")
                .withPermission("dndminis.roll.multi")
                .withArguments(
                        new IntegerArgument("count"),
                        new IntegerArgument("max"),
                        new IntegerArgument("bonus").setOptional(true),
                        new BooleanArgument("subtract").setOptional(true),
                        new IntegerArgument("target").setOptional(true)
                )
                .executes((sender, args) -> {
                    int count = (int) args.get("count");
                    int max = (int) args.get("max");
                    int bonus = (int) args.getOrDefault("bonus", 0);
                    boolean subtract = (boolean) args.getOrDefault("subtract", false);
                    Integer target = (Integer) args.getOrDefault("target", 0);

                    if (count <= 0 || max <= 0) {
                        sender.sendMessage("El número de dados y el máximo deben ser mayores que 0.");
                        return;
                    }

                    Random rnd = new Random();
                    int totalRoll = 0;
                    StringBuilder rollsStr = new StringBuilder();
                    for (int i = 0; i < count; i++) {
                        int r = rnd.nextInt(1, max + 1);
                        totalRoll += r;
                        if (i > 0) rollsStr.append(", ");
                        rollsStr.append(r);
                    }
                    totalRoll += bonus;

                    if (target != null) {
                        int result = subtract ? (target - totalRoll) : (target + totalRoll);
                        sender.sendMessage("Dados: [" + rollsStr.toString() + "] + Bonus: " + bonus + " => Total: " + totalRoll);
                        sender.sendMessage((subtract ? "Resta " : "Suma ") + totalRoll + (subtract ? " a " : " a ") + target + " = " + result);
                    } else {
                        sender.sendMessage("Has tirado: " + totalRoll + " (Dados: " + rollsStr.toString() + ", Bonus: " + bonus + ")");
                    }
                });
        rollMultiCommand.register("dndminis");
    }
}
