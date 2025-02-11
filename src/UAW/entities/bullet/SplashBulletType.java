package UAW.entities.bullet;

import UAW.audiovisual.UAWFx;
import UAW.entities.bullet.ModdedVanillaBullet.UAWBulletType;
import arc.audio.Sound;
import arc.graphics.Color;
import arc.math.*;
import arc.util.Nullable;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.Pal;

import static mindustry.Vars.indexer;

/**
 * Damages all enemies caught within its area of effect
 */
public class SplashBulletType extends UAWBulletType {
	/**
	 * Delays in tick between splashes
	 */
	public float splashDelay = 25f;
	/**
	 * How many times splash splashDamage occurs
	 */
	public int splashAmount = 3;
	/** How much rotating triangles does the splash effect has */
	public int pointCount = 0;
	public Sound applySound = Sounds.shotgun;
	/** Adjust circle light color */
	public Color frontColor = Pal.lightishOrange;
	/** Adjust circle dark color */
	public Color backColor = Pal.lightOrange;
	/** Adjust bottom color of the circle splash, will use backColor if its null */
	@Nullable
	public Color bottomColor;
	public Effect particleEffect = Fx.hitBulletColor;

	float splashDuration = (splashDelay * splashAmount);

	public SplashBulletType(float splashDamage, float radius) {
		super(splashDamage, radius);
		this.damage = 0f;
		this.splashDamage = splashDamage;
		this.splashDamageRadius = radius;
		lifetime = splashDuration;
		hitSize = speed = 0;
		smokeEffect = despawnEffect = hitEffect = Fx.none;
		displayAmmoMultiplier = false;
		absorbable = hittable = false;
	}

	@Override
	public void update(Bullet b) {
		if (b.timer(1, splashDelay) && splashAmount > 1) {
			Damage.damage(b.team, b.x, b.y, splashDamageRadius, splashDamage, collidesAir, collidesGround);
			UAWFx.circleSplash(
				splashDamageRadius,
				splashDelay,
				frontColor, backColor, bottomColor == null ? backColor : bottomColor, pointCount
			).at(b.x, b.y);
			applySound.at(b.x, b.y);
			for (int j = 0; j < ((splashAmount) * 15); j++) {
				particleEffect.at(
					b.x + Angles.trnsx(Mathf.random(360), Mathf.random(splashDamageRadius)),
					b.y + Angles.trnsx(Mathf.random(360), Mathf.random(splashDamageRadius))
				);
			}
			if (status != StatusEffects.none) {
				Damage.status(b.team, b.x, b.y, splashDamageRadius, status, statusDuration, collidesAir, collidesGround);
			}
			if (healPercent > 0f) {
				indexer.eachBlock(b.team, b.x, b.y, splashDamageRadius, Building::damaged, other -> {
					Fx.healBlockFull.at(other.x, other.y, other.block.size, Pal.heal);
					other.heal(healPercent / 100f * other.maxHealth());
				});
			}
			if (makeFire) {
				indexer.eachBlock(null, b.x, b.y, splashDamageRadius, other -> other.team != b.team, other -> Fires.create(other.tile));
			}
		}
		super.update(b);
	}
}
