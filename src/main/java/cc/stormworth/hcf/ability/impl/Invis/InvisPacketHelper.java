package cc.stormworth.hcf.ability.impl.Invis;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import net.minecraft.server.v1_7_R4.EntityPlayer;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.PacketPlayOutEntityEquipment;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class InvisPacketHelper {

  private static MethodHandle ENTITY_ID_GETTER;
  private static MethodHandle SLOT_GETTER;
  private static MethodHandle ITEM_STACK_GETTER;

  static {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();

      ENTITY_ID_GETTER = lookup.unreflectGetter(setAccessibleAndGet(PacketPlayOutEntityEquipment.class, "a"));
      SLOT_GETTER = lookup.unreflectGetter(setAccessibleAndGet(PacketPlayOutEntityEquipment.class, "b"));
      ITEM_STACK_GETTER = lookup.unreflectGetter(setAccessibleAndGet(PacketPlayOutEntityEquipment.class, "c"));

    } catch (Throwable t) {
      t.printStackTrace();
    }
  }

  public static PacketPlayOutEntityEquipment createEquipmentPacket(Player player, int slot, boolean remove) {
    EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();

    return new PacketPlayOutEntityEquipment(player.getEntityId(), slot, remove ? null : entityPlayer.inventory.armor[slot - 1]);
  }

  public static int getEntityId(PacketPlayOutEntityEquipment packet) throws Throwable {
    return (int) ENTITY_ID_GETTER.invokeExact(packet);
  }

  public static int getSlot(PacketPlayOutEntityEquipment packet) throws Throwable {
    return (int) SLOT_GETTER.invokeExact(packet);
  }

  public static ItemStack getItemStack(PacketPlayOutEntityEquipment packet) throws Throwable {
    return (ItemStack) ITEM_STACK_GETTER.invokeExact(packet);
  }

  public static Field setAccessibleAndGet(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    Field field = clazz.getDeclaredField(fieldName);
    field.setAccessible(true);

    return field;
  }
}