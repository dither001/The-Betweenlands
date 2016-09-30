package thebetweenlands.common.world.biome;

import thebetweenlands.common.entity.mobs.*;
import thebetweenlands.common.world.biome.spawning.spawners.CaveSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.SurfaceSpawnEntry;
import thebetweenlands.common.world.biome.spawning.spawners.TreeSpawnEntry;
import thebetweenlands.common.world.gen.biome.decorator.BiomeDecoratorPatchyIslands;
import thebetweenlands.common.world.gen.biome.feature.AlgaeFeature;
import thebetweenlands.common.world.gen.biome.feature.SiltBeachFeature;

public class BiomePatchyIslands extends BiomeBetweenlands {

	public BiomePatchyIslands() {
		super(new BiomeProperties("patchy_islands").setBaseHeight(118.0F).setHeightVariation(4.0F).setWaterColor(0x184220).setTemperature(0.8F).setRainfall(0.9F));
		//this.setWeight(20);
		this.setFogColor(10, 30, 12);
		this.getBiomeGenerator().setDecorator(new BiomeDecoratorPatchyIslands())
		.addFeature(new SiltBeachFeature())
		.addFeature(new AlgaeFeature());
		this.setFoliageColors(0x1FC66D, 0x1FC66D);

		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityDragonFly.class, (short) 35).setGroupSize(1, 2).setSpawnCheckRadius(32.0D).setSpawningInterval(400));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityFirefly.class, (short) 20).setSpawnCheckRadius(32.0D));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityMireSnail.class, (short) 60).setGroupSize(1, 5).setSpawnCheckRadius(32.0D).setSpawningInterval(800));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityFrog.class, (short) 32).setCanSpawnOnWater(true).setGroupSize(1, 3).setSpawnCheckRadius(32.0D).setSpawningInterval(100));
		this.blSpawnEntries.add(new CaveSpawnEntry(EntityBlindCaveFish.class, (short) 30).setGroupSize(3, 5).setSpawnCheckRadius(32.0D));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityGecko.class, (short) 40).setGroupSize(1, 3).setSpawnCheckRadius(32.0D).setSpawningInterval(600));
		this.blSpawnEntries.add(new TreeSpawnEntry(EntitySporeling.class, (short) 80).setGroupSize(2, 5).setSpawnCheckRadius(32.0D));

		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityLurker.class, (short) 35).setHostile(true).setSpawnCheckRadius(16.0D));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityAngler.class, (short) 45).setHostile(true).setGroupSize(1, 3));
		this.blSpawnEntries.add(new CaveSpawnEntry(EntityAngler.class, (short) 35).setHostile(true).setGroupSize(1, 3));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntitySwampHag.class, (short) 50).setHostile(true));
		this.blSpawnEntries.add(new CaveSpawnEntry(EntitySwampHag.class, (short) 140).setHostile(true).setSpawnCheckRadius(6.0D).setGroupSize(1, 3));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityWight.class, (short) 12).setHostile(true).setSpawnCheckRadius(64.0D));
		this.blSpawnEntries.add(new CaveSpawnEntry(EntityWight.class, (short) 18).setHostile(true).setSpawnCheckRadius(64.0D));
//		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntitySiltCrab.class, (short) 50).setHostile(true).setGroupSize(2, 8)); TODO
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityBloodSnail.class, (short) 30).setHostile(true));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityLeech.class, (short) 35).setHostile(true));
		this.blSpawnEntries.add(new SurfaceSpawnEntry(EntityChiromaw.class, (short) 12).setHostile(true).setSpawnCheckRadius(30.0D));
		this.blSpawnEntries.add(new CaveSpawnEntry(EntityChiromaw.class, (short) 40).setHostile(true).setSpawnCheckRadius(20.0D).setGroupSize(1, 3));
	}

}