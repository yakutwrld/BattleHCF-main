package cc.stormworth.hcf.util.cooldowntimer.impl;

import cc.stormworth.core.util.time.TimeUtil;
import cc.stormworth.hcf.util.cooldowntimer.Timer;
import cc.stormworth.hcf.util.cooldowntimer.events.TimerActivateEvent;
import cc.stormworth.hcf.util.cooldowntimer.events.TimerCancelEvent;
import cc.stormworth.hcf.util.cooldowntimer.events.TimerExpireEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerTimer extends Timer {

  protected final Map<UUID, ScheduledFuture<?>> players;

  public PlayerTimer(ScheduledExecutorService executor, String name, int delay) {
    this(executor, name, delay, false);
  }

  public PlayerTimer(ScheduledExecutorService executor, String name, int delay,
      boolean persistable) {
    super(executor, name, delay, persistable);

    this.players = new HashMap<>();
  }

  @Override
  public void disable() {
    this.players.values().forEach(future -> future.cancel(true));
    this.players.clear();
  }

  public void reactivate(Player player) {
    this.reactivate(player.getUniqueId());
  }

  public void reactivate(UUID uuid) {
    this.cancel(uuid);
    this.activate(uuid);
  }

  public void activate(Player player) {
    this.activate(player.getUniqueId());
  }

  public void activate(UUID uuid) {
    this.activate(uuid, this.delay);
  }

  public void activate(Player player, int delay) {
    this.activate(player.getUniqueId(), delay);
  }

  public void activate(UUID uuid, int delay) {
    if (delay <= 0 || this.isActive(uuid)) {
      return;
    }

    TimerActivateEvent event = new TimerActivateEvent(uuid, this, delay);
    if (event.isCancelled()) {
      return;
    }

    this.players.put(uuid, this.scheduleExpiry(uuid, delay));
  }

  public void activate(Player player, Callable callable) {
    this.activate(player.getUniqueId(), callable);
  }

  public void activate(UUID uuid, Callable callable) {
    this.activate(uuid, this.delay, callable);
  }

  public void activate(Player player, int delay, Callable callable) {
    this.activate(player.getUniqueId(), delay, callable);
  }

  public void activate(UUID uuid, int delay, Callable callable) {
    if (delay <= 0 || this.isActive(uuid)) {
      return;
    }

    TimerActivateEvent event = new TimerActivateEvent(uuid, this, delay);
    if (event.isCancelled()) {
      return;
    }

    this.players.put(uuid, this.scheduleExpiry(uuid, delay, callable));
  }

  public void cancel(Player player) {
    this.cancel(player.getUniqueId());
  }

  public void cancel(UUID uuid) {
    if (!this.isActive(uuid)) {
      return;
    }

    new TimerCancelEvent(uuid, this);

    this.players.remove(uuid).cancel(true);
  }

  public boolean isActive(Player player) {
    return this.isActive(player.getUniqueId());
  }

  public boolean isActive(UUID uuid) {
    return this.players.containsKey(uuid);
  }

  protected long getCooldown(Player player) {
    return this.getCooldown(player.getUniqueId());
  }

  private long getCooldown(UUID uuid) {
    ScheduledFuture<?> future = this.players.get(uuid);
    return future != null ? future.getDelay(TimeUnit.MILLISECONDS) : 0L;
  }

  public String getTimeLeft(Player player) {
    return TimeUtil.formatTime(this.getCooldown(player), this.format);
  }

  public String getDynamicTimeLeft(Player player) {
    long remaining = this.getCooldown(player);

    if (remaining < 3_600_000L) {
      return TimeUtil.formatTime(remaining, TimeUtil.FormatType.MILLIS_TO_MINUTES);
    } else {
      return TimeUtil.formatTime(remaining, TimeUtil.FormatType.MILLIS_TO_HOURS);
    }
  }

  private void sendMessage(UUID uuid) {
    if (this.expiryMessage == null) {
      return;
    }

    Player player = Bukkit.getPlayer(uuid);
    if (player != null) {
      player.sendMessage(this.expiryMessage);
    }
  }

  private ScheduledFuture<?> scheduleExpiry(UUID uuid, int delay) {
    return this.executor.schedule(() -> {
      try {
        new TimerExpireEvent(uuid, this);

        this.players.remove(uuid);
        this.sendMessage(uuid);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }, delay, TimeUnit.SECONDS);
  }

  private ScheduledFuture<?> scheduleExpiry(UUID uuid, int delay, Callable callable) {
    return this.executor.schedule(() -> {
      try {
        new TimerExpireEvent(uuid, this);

        this.players.remove(uuid);
        callable.call();
        this.sendMessage(uuid);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    }, delay, TimeUnit.SECONDS);
  }
}