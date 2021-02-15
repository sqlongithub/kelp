package de.pxav.kelp.core.npc.activity;

import de.pxav.kelp.core.npc.KelpNpc;
import org.bukkit.Location;

import java.util.function.Supplier;

public class LookToActivity extends NpcActivity<LookToActivity> {

  private Supplier<Location> target;

  public static LookToActivity create() {
    return new LookToActivity();
  }

  public LookToActivity target(Supplier<Location> target) {
    this.target = target;
    return this;
  }

  @Override
  public void onTick(KelpNpc npc) {
    npc.lookTo(target.get());
  }
}
