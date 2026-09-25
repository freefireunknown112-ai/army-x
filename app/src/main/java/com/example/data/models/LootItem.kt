package com.example.data.models

enum class ItemCategory {
    WEAPON,
    AMMO,
    ARMOR,
    MEDICAL,
    GRENADE,
    ATTACHMENT
}

data class LootItem(
    val id: String,
    val name: String,
    val category: ItemCategory,
    val iconName: String,
    val quantity: Int = 1,
    val maxStack: Int = 5,
    val weaponData: Weapon? = null,
    val ammoType: AmmoType? = null,
    val healAmount: Float = 0f,
    val shieldAmount: Float = 0f,
    val useDurationMs: Long = 0L,
    val armorLevel: Int = 0,
    val description: String = ""
) {
    companion object {
        fun medkit(): LootItem = LootItem(
            id = "med_large",
            name = "Military Medkit",
            category = ItemCategory.MEDICAL,
            iconName = "ic_medkit",
            quantity = 1,
            maxStack = 3,
            healAmount = 100f,
            useDurationMs = 5000L,
            description = "Fully restores health to 100%."
        )

        fun firstAid(): LootItem = LootItem(
            id = "med_small",
            name = "First Aid Kit",
            category = ItemCategory.MEDICAL,
            iconName = "ic_first_aid",
            quantity = 1,
            maxStack = 5,
            healAmount = 50f,
            useDurationMs = 3000L,
            description = "Heals up to 75 HP."
        )

        fun energyDrink(): LootItem = LootItem(
            id = "med_energy",
            name = "Combat Adrenaline",
            category = ItemCategory.MEDICAL,
            iconName = "ic_energy",
            quantity = 1,
            maxStack = 5,
            healAmount = 25f,
            shieldAmount = 25f,
            useDurationMs = 2000L,
            description = "Boosts speed and restores 25 HP and 25 Shield."
        )

        fun armorVest(level: Int): LootItem = LootItem(
            id = "armor_lvl_$level",
            name = when (level) {
                1 -> "Kevlar Vest L1"
                2 -> "Tactical Vest L2"
                else -> "Military Exosuit L3"
            },
            category = ItemCategory.ARMOR,
            iconName = "ic_armor",
            quantity = 1,
            maxStack = 1,
            armorLevel = level,
            shieldAmount = level * 50f,
            description = "Absorbs damage. Shield capacity: ${level * 50}."
        )

        fun fragGrenade(): LootItem = LootItem(
            id = "grenade_frag",
            name = "M67 Frag Grenade",
            category = ItemCategory.GRENADE,
            iconName = "ic_frag",
            quantity = 2,
            maxStack = 4,
            useDurationMs = 800L,
            description = "High-explosive fragmentation grenade with 8m lethal radius."
        )

        fun smokeGrenade(): LootItem = LootItem(
            id = "grenade_smoke",
            name = "Tactical Smoke Screen",
            category = ItemCategory.GRENADE,
            iconName = "ic_smoke",
            quantity = 2,
            maxStack = 4,
            useDurationMs = 800L,
            description = "Creates a dense concealment cloud lasting 20 seconds."
        )

        fun ammoPack(type: AmmoType, count: Int): LootItem = LootItem(
            id = "ammo_${type.name}",
            name = "${type.label} Box",
            category = ItemCategory.AMMO,
            iconName = "ic_ammo",
            quantity = count,
            maxStack = 300,
            ammoType = type,
            description = "Contains $count rounds of ${type.label}."
        )

        fun weaponItem(weapon: Weapon): LootItem = LootItem(
            id = "loot_${weapon.id}",
            name = weapon.name,
            category = ItemCategory.WEAPON,
            iconName = "ic_gun",
            quantity = 1,
            maxStack = 1,
            weaponData = weapon,
            description = weapon.description
        )
    }
}

data class WorldLoot(
    val id: String,
    val item: LootItem,
    var x: Float,
    var y: Float,
    var z: Float,
    var isSpawned: Boolean = true
)
