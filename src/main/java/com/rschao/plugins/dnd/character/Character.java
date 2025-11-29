package com.rschao.plugins.dnd.character;

import com.rschao.plugins.dnd.char_class.DndClass;

public class Character {
    private DndClass dndClass;
    private int level;
    private String name;

    public Character(String name, DndClass dndClass, int level) {
        this.name = name;
        this.dndClass = dndClass;
        this.level = level;
    }

    public DndClass getDndClass() {
        return dndClass;
    }

    public int getLevel() {
        return level;
    }

    public String getName() {
        return name;
    }
}
