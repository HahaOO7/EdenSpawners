package at.haha007.edenspawners;

import com.destroystokyo.paper.MaterialTags;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class EdenSpawners extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length != 2) {
            sender.sendMessage("/spawner <target> <type>");
            return false;
        }
        Player player = Bukkit.getPlayerExact(args[0]);
        if (player == null) {
            sender.sendMessage("player not found");
            return true;
        }
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("entity type not found");
            return true;
        }
        ItemStack item = createSpawnerItem(type);

        var remaining = player.getInventory().addItem(item);
        if (!remaining.isEmpty())
            player.getLocation().getWorld().dropItem(player.getLocation(), item);
        return true;
    }

    private static ItemStack createSpawnerItem(EntityType type) {
        ItemStack item = new ItemStack(Material.SPAWNER);
        item.editMeta(meta -> {
            meta.displayName(Component.translatable(type.translationKey(), NamedTextColor.AQUA).append(Component.space()).append(Component.translatable(Material.SPAWNER.translationKey(), NamedTextColor.DARK_AQUA)));
            BlockStateMeta bsm = (BlockStateMeta) meta;
            CreatureSpawner spawner = (CreatureSpawner) bsm.getBlockState();
            spawner.setSpawnedType(type);
            bsm.setBlockState(spawner);
        });
        return item;
    }

    @EventHandler(priority = EventPriority.HIGH)
    void onInteractBlock(PlayerInteractEvent event) {
        Block block = event.getClickedBlock();
        if (block == null) return;
        if (block.getType() != Material.SPAWNER) return;
        ItemStack handItem = event.getItem();
        if (handItem == null) return;
        if (!MaterialTags.SPAWN_EGGS.isTagged(handItem)) return;
        if (event.getPlayer().hasPermission("edenspawners.interactegg.bypass")) return;
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getType() != Material.SPAWNER || event.getItemInHand().getType() != Material.SPAWNER)
            return;
        CreatureSpawner bs = (CreatureSpawner) event.getBlock().getState(false);
        CreatureSpawner is = (CreatureSpawner) ((BlockStateMeta) event.getItemInHand().getItemMeta()).getBlockState();
        bs.setSpawnedType(is.getSpawnedType());
        bs.update();
    }

}
