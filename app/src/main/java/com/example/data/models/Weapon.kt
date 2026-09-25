package com.example.data.models

enum class WeaponType(val displayName: String) {
    PISTOL("9mm Tactical Sidearm"),
    ASSAULT_RIFLE("AR-X Phantom"),
    SMG("Viper-9 Tactical SMG"),
    SHOTGUN("Striker-12 Combat"),
    SNIPER("Valkyrie .50 Cal"),
    LMG("Titan-X Heavy Support")
}

enum class AmmoType(val label: String, val maxCarry: Int) {
    LIGHT_AMMO("9mm Light", 180),
    MEDIUM_AMMO("5.56mm Medium", 240),
    HEAVY_AMMO("7.62mm Heavy", 180),
    SHELLS("12-Gauge Shells", 60),
    SNIPER_AMMO(".50 Cal High Velocity", 40)
}

data class Weapon(
    val id: String,
    val type: WeaponType,
    val name: String,
    val damage: Float,
    val fireRateMs: Long,
    val magazineSize: Int,
    var currentAmmo: Int,
    var reserveAmmo: Int,
    val reloadTimeMs: Long,
    val range: Float,
    val recoil: Float,
    val bulletSpread: Float,
    val bulletSpeed: Float,
    val headshotMultiplier: Float,
    val ammoType: AmmoType,
    val accuracy: Float,
    val description: String = ""
) {
    fun canFire(): Boolean = currentAmmo > 0

    companion object {
        fun createPistol(): Weapon = Weapon(
            id = "wpn_pistol_01",
            type = WeaponType.PISTOL,
            name = "Tactical P9",
            damage = 28f,
            fireRateMs = 220L,
            magazineSize = 15,
            currentAmmo = 15,
            reserveAmmo = 60,
            reloadTimeMs = 1400L,
            range = 45f,
            recoil = 0.025f,
            bulletSpread = 0.035f,
            bulletSpeed = 120f,
            headshotMultiplier = 1.8f,
            ammoType = AmmoType.LIGHT_AMMO,
            accuracy = 0.82f,
            description = "Reliable close-quarters sidearm with rapid trigger response."
        )

        fun createAssaultRifle(): Weapon = Weapon(
            id = "wpn_ar_01",
            type = WeaponType.ASSAULT_RIFLE,
            name = "AR-X Phantom",
            damage = 34f,
            fireRateMs = 110L,
            magazineSize = 30,
            currentAmmo = 30,
            reserveAmmo = 120,
            reloadTimeMs = 2100L,
            range = 110f,
            recoil = 0.04f,
            bulletSpread = 0.028f,
            bulletSpeed = 180f,
            headshotMultiplier = 2.0f,
            ammoType = AmmoType.MEDIUM_AMMO,
            accuracy = 0.88f,
            description = "High-accuracy versatile military rifle with balanced recoil."
        )

        fun createSMG(): Weapon = Weapon(
            id = "wpn_smg_01",
            type = WeaponType.SMG,
            name = "Viper-9",
            damage = 22f,
            fireRateMs = 75L,
            magazineSize = 35,
            currentAmmo = 35,
            reserveAmmo = 140,
            reloadTimeMs = 1600L,
            range = 55f,
            recoil = 0.03f,
            bulletSpread = 0.05f,
            bulletSpeed = 140f,
            headshotMultiplier = 1.7f,
            ammoType = AmmoType.LIGHT_AMMO,
            accuracy = 0.78f,
            description = "Devastating close-range fire rate for aggressive breaching."
        )

        fun createShotgun(): Weapon = Weapon(
            id = "wpn_shotgun_01",
            type = WeaponType.SHOTGUN,
            name = "Striker-12",
            damage = 85f,
            fireRateMs = 600L,
            magazineSize = 8,
            currentAmmo = 8,
            reserveAmmo = 32,
            reloadTimeMs = 2800L,
            range = 25f,
            recoil = 0.12f,
            bulletSpread = 0.14f,
            bulletSpeed = 90f,
            headshotMultiplier = 1.5f,
            ammoType = AmmoType.SHELLS,
            accuracy = 0.65f,
            description = "Semi-automatic combat shotgun delivering lethal point-blank spread."
        )

        fun createSniper(): Weapon = Weapon(
            id = "wpn_sniper_01",
            type = WeaponType.SNIPER,
            name = "Valkyrie .50",
            damage = 110f,
            fireRateMs = 1100L,
            magazineSize = 5,
            currentAmmo = 5,
            reserveAmmo = 20,
            reloadTimeMs = 3200L,
            range = 250f,
            recoil = 0.18f,
            bulletSpread = 0.005f,
            bulletSpeed = 260f,
            headshotMultiplier = 2.5f,
            ammoType = AmmoType.SNIPER_AMMO,
            accuracy = 0.98f,
            description = "Long-range anti-materiel rifle capable of single-shot eliminations."
        )

        fun createLMG(): Weapon = Weapon(
            id = "wpn_lmg_01",
            type = WeaponType.LMG,
            name = "Titan-X",
            damage = 38f,
            fireRateMs = 130L,
            magazineSize = 75,
            currentAmmo = 75,
            reserveAmmo = 150,
            reloadTimeMs = 4200L,
            range = 95f,
            recoil = 0.06f,
            bulletSpread = 0.045f,
            bulletSpeed = 160f,
            headshotMultiplier = 1.9f,
            ammoType = AmmoType.HEAVY_AMMO,
            accuracy = 0.80f,
            description = "High-capacity suppressive squad machine gun for area control."
        )

        fun getAllAvailableWeapons(): List<Weapon> = listOf(
            createPistol(),
            createAssaultRifle(),
            createSMG(),
            createShotgun(),
            createSniper(),
            createLMG()
        )
    }
}
