package de.pxav.kelp.core.inventory;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.pxav.kelp.core.event.kelpevent.KelpInventoryCloseEvent;
import de.pxav.kelp.core.event.kelpevent.KelpInventoryOpenEvent;
import de.pxav.kelp.core.event.kelpevent.KelpInventoryUpdateEvent;
import de.pxav.kelp.core.inventory.listener.KelpListenerRepository;
import de.pxav.kelp.core.inventory.material.MaterialVersionTemplate;
import de.pxav.kelp.core.inventory.type.AnimatedInventory;
import de.pxav.kelp.core.inventory.type.KelpInventory;
import de.pxav.kelp.core.inventory.widget.Pagination;
import de.pxav.kelp.core.player.KelpPlayer;
import de.pxav.kelp.core.reflect.MethodFinder;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.UUID;

/**
 * This repository class stores and handles all kelp
 * inventories. You can open, close and update kelp inventories
 * from here. Some of the methods are also callable
 * from the {@link KelpPlayer} class directly, if you are working
 * with players.
 *
 * @author pxav
 */
@Singleton
public class KelpInventoryRepository {

  private MaterialVersionTemplate materialVersionTemplate;
  private MethodFinder methodFinder;
  private KelpListenerRepository kelpListenerRepository;

  private Map<UUID, KelpInventory> playerInventories = Maps.newHashMap();
  private Map<UUID, Map<Pagination, Integer>> playerPages = Maps.newHashMap();
  private Map<UUID, AnimatedInventory> playerAnimations = Maps.newHashMap();

  @Inject
  public KelpInventoryRepository(MaterialVersionTemplate materialVersionTemplate,
                                 MethodFinder methodFinder,
                                 KelpListenerRepository kelpListenerRepository) {
    this.materialVersionTemplate = materialVersionTemplate;
    this.methodFinder = methodFinder;
    this.kelpListenerRepository = kelpListenerRepository;
  }

  /**
   * Maps all kelp materials with the version specific bukkit materials.
   */
  public void loadMaterials() {
    this.materialVersionTemplate.defineDefaults();
  }

  /**
   * Opens a kelp inventory to the given player. This also adds the
   * player to all required lists.
   *
   * @param inventory The inventory you want to show to the player.
   * @param player    The player who should see the inventory.
   */
  public void openInventory(KelpInventory inventory, KelpPlayer player) {
    Inventory renderedInventory = inventory.render(player);
    player.getBukkitPlayer().openInventory(renderedInventory);
    boolean animated = false;

    if (inventory instanceof AnimatedInventory) {
      animated = true;
      AnimatedInventory animatedInventory = (AnimatedInventory) inventory;
      animatedInventory.scheduleUpdater(player.getBukkitPlayer());
      playerAnimations.put(player.getUUID(), animatedInventory);
    }

    Bukkit.getPluginManager().callEvent(new KelpInventoryOpenEvent(player, inventory, animated));
    playerInventories.put(player.getUUID(), inventory);
  }

  /**
   * This method should be executed when a player closes their
   * kelp inventory as it removes the player from the cache and
   * stops all schedulers.
   *
   * @param player The player you want to remove from the cache/whose inventory
   *               you want to close.
   */
  public void closeInventory(KelpPlayer player) {
    KelpInventory inventory = this.playerInventories.get(player.getUUID());
    boolean animated = false;

    // if his inventory was animated, stop the scheduler
    if (inventory instanceof AnimatedInventory) {
      animated = true;
      AnimatedInventory animatedInventory = (AnimatedInventory) inventory;
      animatedInventory.stopUpdater();
    }

    Bukkit.getPluginManager().callEvent(new KelpInventoryCloseEvent(player, inventory, animated));
    this.playerInventories.remove(player.getUUID());
    this.playerAnimations.remove(player.getUUID());
    this.kelpListenerRepository.unregisterListeners(player.getUUID());
  }

  /**
   * Updates the entire kelp inventory of a player.
   * This includes all widgets, but not the normal
   * inventory of the player.
   *
   * @param player The player whose inventory you want to update.
   */
  public void updateInventory(KelpPlayer player) {
    KelpInventory kelpInventory = playerInventories.get(player.getUUID());
    kelpInventory.update(player);
    Bukkit.getPluginManager().callEvent(new KelpInventoryUpdateEvent(player, kelpInventory));
  }

  public Map<UUID, Map<Pagination, Integer>> getPlayerPages() {
    return playerPages;
  }

}
