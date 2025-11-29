package com.rschao.plugins.dnd.char_class;

public enum DndClass {
    Mage(6),
    Wizard(6),
    Cavendish(10),
    Barbarian(12),
    Paladin(10),
    Artificer(8),
    Cleric(10),
    Druid(10),
    Warrior(10),
    Bard(8);

    private int levelHp;
    DndClass(int levelHp){
        this.levelHp = levelHp;
    }

    public int getPerLevelHp() {
        return levelHp;
    }
}
