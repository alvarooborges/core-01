package net.hyze.core.spigot.misc.mining;

import com.google.common.collect.Sets;
import lombok.Getter;
import net.hyze.core.shared.group.Group;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;

@Getter
public class MiningDrop extends Drop {

    protected final MiningTool tool;
    protected final int maximumHeight;
    protected final MiningXP xp;
    protected final HashSet<Biome> biomes = Sets.newHashSet();

    public MiningDrop(Material drop, int amount, int maximumHeight, MiningXP xp, MiningTool tool, int dropWeight) {
        this(drop, amount, maximumHeight, xp, tool, dropWeight, Group.DEFAULT);
    }

    public MiningDrop(Material drop, int amount, int maximumHeight, MiningXP xp, MiningTool tool, int dropWeight, Group group) {
        this(new ItemStack(drop, amount), maximumHeight, xp, tool, dropWeight, null, group);
    }

    public MiningDrop(ItemStack is, int maximumHeight, MiningXP xp, MiningTool tool, int dropWeight) {
        this(is, maximumHeight, xp, tool, dropWeight, null, Group.DEFAULT);
    }

    public MiningDrop(ItemStack is, int maximumHeight, MiningXP xp, MiningTool tool, int dropWeight, HashSet<Biome> biomes, Group group) {
        super(is.getType(), is.getAmount(), group, dropWeight);

        this.xp = xp;
        this.tool = tool;
        this.itemStack = is;
        this.maximumHeight = maximumHeight;

        if (biomes != null) {
            this.biomes.addAll(biomes);
        }
    }

    public int getXP() {
        return xp.getXP();
    }
}
