package com.rschao.plugins.dnd.command;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.EntityTypeArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class SummonMiniMob {
    public static void register() {
        CommandAPICommand summonMiniMobCommand = new CommandAPICommand("summonminimob")
                .withPermission("dndminis.summonmini")
                .withArguments(new EntityTypeArgument("mobType"), new StringArgument("name").setOptional(true), new IntegerArgument("health").setOptional(true))
                .executesPlayer((sender, args) -> {
                    EntityType mobType = (EntityType) args.get(0);
                    Entity entity = sender.getLocation().getWorld().spawnEntity(sender.getLocation(), mobType);
                    if(entity instanceof LivingEntity) {
                        LivingEntity livingEntity = (LivingEntity) entity;
                        livingEntity.setAI(false);
                        livingEntity.setPersistent(true);
                        livingEntity.setRemoveWhenFarAway(false);
                        if (args.args().length > 1) {
                            String name = (String) args.get("name");
                            livingEntity.setCustomName(name);
                            livingEntity.setCustomNameVisible(true);
                            if (args.args().length > 2) {
                                int health = (int) args.get("health");
                                livingEntity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(health);
                                livingEntity.setHealth(health);

                            }
                        }
                    }
                });
        summonMiniMobCommand.register("dndminis");
    }
}
