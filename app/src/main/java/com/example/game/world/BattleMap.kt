package com.example.game.world

import com.example.data.models.AmmoType
import com.example.data.models.LootItem
import com.example.data.models.Vehicle
import com.example.data.models.Weapon
import com.example.data.models.WorldLoot
import com.example.game.engine.AABB
import com.example.game.engine.Vector3

data class MapStructure(
    val id: String,
    val name: String,
    val position: Vector3,
    val size: Vector3,
    val colorR: Float,
    val colorG: Float,
    val colorB: Float,
    val boundingBox: AABB
)

data class PropCover(
    val position: Vector3,
    val size: Vector3,
    val colorR: Float,
    val colorG: Float,
    val colorB: Float,
    val boundingBox: AABB
)

class BattleMap {
    val mapSize: Float = 240f // 240m x 240m battle arena
    val structures = mutableListOf<MapStructure>()
    val coverProps = mutableListOf<PropCover>()
    val initialLoot = mutableListOf<WorldLoot>()
    val vehicles = mutableListOf<Vehicle>()

    init {
        generateMap()
    }

    private fun generateMap() {
        // 1. Military Command Center (Center-North: x=0, z=-40)
        addStructure(
            id = "bld_hq",
            name = "Outpost Command HQ",
            pos = Vector3(0f, 3.5f, -40f),
            size = Vector3(20f, 7f, 16f),
            r = 0.35f, g = 0.38f, b = 0.40f
        )
        // HQ Roof Watchtower
        addStructure(
            id = "bld_hq_tower",
            name = "Radar Communications Tower",
            pos = Vector3(0f, 9.5f, -40f),
            size = Vector3(4f, 5f, 4f),
            r = 0.5f, g = 0.52f, b = 0.55f
        )

        // 2. West Supply Warehouse (x=-45, z=-10)
        addStructure(
            id = "bld_wh_west",
            name = "Logistics Warehouse Alpha",
            pos = Vector3(-45f, 3f, -10f),
            size = Vector3(16f, 6f, 24f),
            r = 0.32f, g = 0.35f, b = 0.36f
        )

        // 3. East Armory Depot (x=45, z=-15)
        addStructure(
            id = "bld_armory_east",
            name = "Weapons Depot Bravo",
            pos = Vector3(45f, 2.5f, -15f),
            size = Vector3(14f, 5f, 14f),
            r = 0.40f, g = 0.38f, b = 0.34f
        )

        // 4. South Bunkers (x=-20, z=50) & (x=25, z=55)
        addStructure(
            id = "bld_bunker_1",
            name = "Reinforced Bunker 01",
            pos = Vector3(-20f, 2f, 50f),
            size = Vector3(10f, 4f, 10f),
            r = 0.28f, g = 0.30f, b = 0.26f
        )
        addStructure(
            id = "bld_bunker_2",
            name = "Reinforced Bunker 02",
            pos = Vector3(25f, 2f, 55f),
            size = Vector3(10f, 4f, 10f),
            r = 0.28f, g = 0.30f, b = 0.26f
        )

        // 5. Watchtowers at Perimeter corners
        addWatchtower("tower_nw", -70f, -70f)
        addWatchtower("tower_ne", 70f, -70f)
        addWatchtower("tower_sw", -70f, 70f)
        addWatchtower("tower_se", 70f, 70f)

        // 6. Tactical Cover Crates & Barriers scattered strategically
        val cratePositions = listOf(
            Vector3(-12f, 0.8f, -28f),
            Vector3(12f, 0.8f, -28f),
            Vector3(0f, 0.8f, -22f),
            Vector3(-32f, 0.8f, -8f),
            Vector3(32f, 0.8f, -12f),
            Vector3(-15f, 0.8f, 25f),
            Vector3(18f, 0.8f, 28f),
            Vector3(-50f, 0.8f, 40f),
            Vector3(45f, 0.8f, 40f),
            Vector3(0f, 0.8f, 20f),
            Vector3(-8f, 0.8f, 70f),
            Vector3(8f, 0.8f, 70f)
        )
        cratePositions.forEachIndexed { i, pos ->
            addCoverCrate("crate_$i", pos, Vector3(2.2f, 1.6f, 2.2f))
        }

        // 7. Tactical Vehicles in map
        vehicles.add(
            Vehicle(
                id = "veh_buggy_01",
                name = "Tactical Buggy",
                x = 10f,
                y = 0.5f,
                z = -10f,
                health = 500f,
                maxHealth = 500f
            )
        )
        vehicles.add(
            Vehicle(
                id = "veh_buggy_02",
                name = "Military Patrol 4x4",
                x = -25f,
                y = 0.5f,
                z = 35f,
                health = 600f,
                maxHealth = 600f
            )
        )

        // 8. Spawn Initial World Loot
        spawnMapLoot()
    }

    private fun addStructure(id: String, name: String, pos: Vector3, size: Vector3, r: Float, g: Float, b: Float) {
        val halfW = size.x / 2f
        val halfH = size.y / 2f
        val halfD = size.z / 2f
        val aabb = AABB(
            pos.x - halfW, pos.y - halfH, pos.z - halfD,
            pos.x + halfW, pos.y + halfH, pos.z + halfD
        )
        structures.add(MapStructure(id, name, pos, size, r, g, b, aabb))
    }

    private fun addWatchtower(id: String, x: Float, z: Float) {
        addStructure(
            id = id,
            name = "Sniper Watchtower",
            pos = Vector3(x, 4f, z),
            size = Vector3(4f, 8f, 4f),
            r = 0.35f, g = 0.33f, b = 0.30f
        )
    }

    private fun addCoverCrate(id: String, pos: Vector3, size: Vector3) {
        val halfW = size.x / 2f
        val halfH = size.y / 2f
        val halfD = size.z / 2f
        val aabb = AABB(
            pos.x - halfW, pos.y - halfH, pos.z - halfD,
            pos.x + halfW, pos.y + halfH, pos.z + halfD
        )
        coverProps.add(PropCover(pos, size, 0.45f, 0.38f, 0.22f, aabb))
    }

    private fun spawnMapLoot() {
        val lootSpots = listOf(
            Pair(Vector3(0f, 0.5f, -32f), LootItem.weaponItem(Weapon.createAssaultRifle())),
            Pair(Vector3(-5f, 0.5f, -32f), LootItem.ammoPack(AmmoType.MEDIUM_AMMO, 60)),
            Pair(Vector3(5f, 0.5f, -32f), LootItem.armorVest(2)),

            Pair(Vector3(-42f, 0.5f, -8f), LootItem.weaponItem(Weapon.createSMG())),
            Pair(Vector3(-45f, 0.5f, -6f), LootItem.ammoPack(AmmoType.LIGHT_AMMO, 70)),
            Pair(Vector3(-40f, 0.5f, -12f), LootItem.medkit()),

            Pair(Vector3(42f, 0.5f, -14f), LootItem.weaponItem(Weapon.createShotgun())),
            Pair(Vector3(45f, 0.5f, -12f), LootItem.ammoPack(AmmoType.SHELLS, 24)),
            Pair(Vector3(47f, 0.5f, -16f), LootItem.fragGrenade()),

            Pair(Vector3(-70f, 8.2f, -70f), LootItem.weaponItem(Weapon.createSniper())),
            Pair(Vector3(-71f, 8.2f, -71f), LootItem.ammoPack(AmmoType.SNIPER_AMMO, 15)),

            Pair(Vector3(70f, 8.2f, -70f), LootItem.weaponItem(Weapon.createLMG())),
            Pair(Vector3(71f, 8.2f, -71f), LootItem.ammoPack(AmmoType.HEAVY_AMMO, 100)),

            Pair(Vector3(-18f, 0.5f, 48f), LootItem.weaponItem(Weapon.createAssaultRifle())),
            Pair(Vector3(-22f, 0.5f, 52f), LootItem.energyDrink()),
            Pair(Vector3(23f, 0.5f, 53f), LootItem.firstAid()),
            Pair(Vector3(26f, 0.5f, 57f), LootItem.smokeGrenade()),

            Pair(Vector3(0f, 0.5f, 0f), LootItem.armorVest(3)),
            Pair(Vector3(-3f, 0.5f, 3f), LootItem.weaponItem(Weapon.createPistol())),
            Pair(Vector3(3f, 0.5f, -3f), LootItem.medkit())
        )

        lootSpots.forEachIndexed { idx, pair ->
            initialLoot.add(
                WorldLoot(
                    id = "loot_$idx",
                    item = pair.second,
                    x = pair.first.x,
                    y = pair.first.y,
                    z = pair.first.z
                )
            )
        }
    }

    fun checkCollision(position: Vector3, radius: Float): Boolean {
        // Collision with structures
        for (s in structures) {
            if (s.boundingBox.collidesWithSphere(position, radius)) {
                return true
            }
        }
        // Collision with crates
        for (c in coverProps) {
            if (c.boundingBox.collidesWithSphere(position, radius)) {
                return true
            }
        }
        // Map boundary limits
        val half = mapSize / 2f - 2f
        if (position.x < -half || position.x > half || position.z < -half || position.z > half) {
            return true
        }
        return false
    }
}
