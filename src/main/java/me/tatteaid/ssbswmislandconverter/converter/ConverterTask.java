package me.tatteaid.ssbswmislandconverter.converter;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.fastasyncworldedit.core.FaweAPI;
import com.grinderwolf.swm.api.exceptions.WorldAlreadyExistsException;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.api.world.properties.SlimeProperties;
import com.grinderwolf.swm.api.world.properties.SlimePropertyMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.tatteaid.ssbswmislandconverter.IslandConverterModule;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ConverterTask implements Runnable {

    private final IslandConverterModule instance;

    private final List<Island> islands;

    private final boolean normalEnabled;
    private final boolean netherEnabled;
    private final boolean endEnabled;

    // constant minimum and maximum Y values for the overworld
    private final static int OVERWORLD_MIN_Y = -64;
    private final static int OVERWORLD_MAX_Y = 319;

    // constant minimum and maximum Y values for the nether & end
    private final static int OTHER_MIN_Y = 0;
    private final static int OTHER_MAX_Y = 255;

    public ConverterTask(IslandConverterModule instance) {
        this.instance = instance;
        this.islands = new ArrayList<>(SuperiorSkyblockAPI.getGrid().getIslands());
        this.normalEnabled = SuperiorSkyblockAPI.getProviders().getWorldsProvider().isNormalEnabled();
        this.netherEnabled = SuperiorSkyblockAPI.getProviders().getWorldsProvider().isNetherEnabled();
        this.endEnabled = SuperiorSkyblockAPI.getProviders().getWorldsProvider().isEndEnabled();
    }

    @Override
    public void run() {
        final long startTime = System.nanoTime();

        final Executor syncExecutor = runnable -> Bukkit.getScheduler().runTask(instance.getPlugin(), runnable);

        for (Iterator<Island> islandIterator = islands.iterator(); islandIterator.hasNext(); ) {
            if (instance.getConverterHandler().isTaskStopped()) {
                instance.getLogger().info("Stopped conversion task after " + TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - startTime) + " minutes!");
                return;
            }

            final Island island = islandIterator.next();

            // loop over all the possible environments that an island can have
            // we want to create a slime world for each island environment
            for (World.Environment environment : World.Environment.values()) {
                // skip over the custom environment and skip over any environments that are not enabled
                if (environment == World.Environment.CUSTOM) continue;
                if (!normalEnabled && environment == World.Environment.NORMAL) continue;
                if (!netherEnabled && environment == World.Environment.NETHER) continue;
                if (!endEnabled && environment == World.Environment.THE_END) continue;

                final String islandWorldName = getIslandWorldName(island.getUniqueId(), environment);

                try {
                    // checks if the slime world already exists
                    // if so, we don't have to continue with any logic
                    if (instance.getSlimeLoader().worldExists(islandWorldName)) continue;

                    // create the new empty slime world
                    // a world will be created for each environment that is enabled
                    final SlimeWorld slimeWorld = instance.getSlimePlugin().createEmptyWorld(instance.getSlimeLoader(), islandWorldName, false, createSlimePropertyMap(environment));

                    // block the runnable and forcefully generate the world sync, this must be run sync
                    final CompletableFuture<Void> generationFuture = CompletableFuture.runAsync(() -> instance.getSlimePlugin().generateWorld(slimeWorld), syncExecutor);
                    generationFuture.join();

                    // placeholder variables until we calculate the actual values
                    // these are not world coordinates, these are chunk coordinates
                    // we will handle the bitwise operations to transfer them to world coordinates later
                    int chunkMinX = Integer.MAX_VALUE;
                    int chunkMaxX = Integer.MIN_VALUE;
                    int chunkMinZ = Integer.MAX_VALUE;
                    int chunkMaxZ = Integer.MIN_VALUE;

                    final List<Chunk> chunks = island.getAllChunks(environment, true, true);

                    // the island is completely empty, we don't need to copy anything over
                    if (chunks.size() == 0) continue;

                    List<Integer> xChunkCoords = new ArrayList<>();
                    List<Integer> zChunkCoords = new ArrayList<>();
                    for (Chunk chunk : chunks) {
                        xChunkCoords.add(chunk.getX());
                        zChunkCoords.add(chunk.getZ());
                    }

                    for (int xValue : xChunkCoords) {
                        chunkMinX = Math.min(xValue, chunkMinX);
                        chunkMaxX = Math.max(xValue, chunkMaxX);
                    }

                    for (int zValue : zChunkCoords) {
                        chunkMinZ = Math.min(zValue, chunkMinZ);
                        chunkMaxZ = Math.max(zValue, chunkMaxZ);
                    }

                    // the minimum and maximum corners of all the island chunks
                    // we just need to get the two sides of all the island chunks, one at a max point and one at a min point
                    // this also will account for height differences in the overworld compared to the nether & end
                    final BlockVector3 minimum;
                    final BlockVector3 maximum;

                    if (environment == World.Environment.NORMAL) {
                        minimum = BlockVector3.at((chunkMinX << 4), OVERWORLD_MIN_Y, (chunkMinZ << 4));
                        maximum = BlockVector3.at((chunkMaxX << 4) | 15, OVERWORLD_MAX_Y, (chunkMaxZ << 4) | 15);
                    } else {
                        minimum = BlockVector3.at((chunkMinX << 4), OTHER_MIN_Y, (chunkMinZ << 4));
                        maximum = BlockVector3.at((chunkMaxX << 4) | 15, OTHER_MAX_Y, (chunkMaxZ << 4) | 15);
                    }

                    // get the island grid world and create the cuboid out of the minimum and maximum points
                    final com.sk89q.worldedit.world.World world = FaweAPI.getWorld(SuperiorSkyblockAPI.getProviders().getWorldsProvider().getIslandsWorld(island, environment).getName());
                    final CuboidRegion cuboidRegion = new CuboidRegion(world, minimum, maximum);
                    final Clipboard clipboard = new BlockArrayClipboard(cuboidRegion);

                    // build an EditSession for the target world
                    // this is where we want to paste our island at
                    final EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                            .world(FaweAPI.getWorld(slimeWorld.getName()))
                            .fastMode(true)
                            .changeSetNull()
                            .limitUnlimited()
                            .checkMemory(false)
                            .build();

                    try {
                        // copy the island that is in island grid world
                        clipboard.setOrigin(cuboidRegion.getCenter().toBlockPoint().withY(cuboidRegion.getMinimumY()));
                        ForwardExtentCopy copy = new ForwardExtentCopy(world, cuboidRegion, clipboard, cuboidRegion.getMinimumPoint());
                        copy.setCopyingEntities(true);
                        copy.setCopyingBiomes(true);
                        Operations.complete(copy);

                        // paste the island in the new SlimeWorld
                        Operations.complete(new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(0, environment == World.Environment.NORMAL ? OVERWORLD_MIN_Y : OTHER_MIN_Y, 0))
                                .ignoreAirBlocks(true)
                                .copyEntities(true)
                                .copyBiomes(true)
                                .build());

                        instance.outputInformation("Successfully copy and pasted island: " + islandWorldName);
                    } finally {
                        clipboard.close();
                        editSession.close();
                    }
                } catch (IOException | WorldAlreadyExistsException exception) {
                    instance.getLogger().log(Level.SEVERE, "Could not create an empty world during the conversion task: " + island.getName(), exception);
                } finally {
                    // block the runnable and forcefully unload the slime world to save our precious memory
                    final CompletableFuture<Void> unloadFuture = CompletableFuture.runAsync(() -> {
                        if (Bukkit.unloadWorld(islandWorldName, true))
                            instance.outputInformation("Successfully unloaded world to save memory: " + islandWorldName);
                    }, syncExecutor);
                    unloadFuture.join();
                }
            }

            islandIterator.remove();
        }

        final long endTime = System.nanoTime();
        instance.getLogger().info("Finished conversion task in " + TimeUnit.NANOSECONDS.toMinutes((endTime - startTime)) + " minutes!");
    }

    private String getIslandWorldName(UUID islandUuid, World.Environment environment) {
        return "island_" + islandUuid + "_" + environment.name().toLowerCase();
    }

    private SlimePropertyMap createSlimePropertyMap(World.Environment environment) {
        SlimePropertyMap slimePropertyMap = new SlimePropertyMap();

        slimePropertyMap.setValue(SlimeProperties.DIFFICULTY, "normal");
        slimePropertyMap.setValue(SlimeProperties.ENVIRONMENT, environment.name());

        return slimePropertyMap;
    }
}