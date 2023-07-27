package cc.stormworth.hcf.misc.lunarclient.cooldown;

import com.lunarclient.bukkitapi.cooldown.LCCooldown;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;

import java.util.concurrent.TimeUnit;

@Getter
@Setter
@AllArgsConstructor
public class LunarClientCooldown {

    private String name;
    private Material material;

    public LCCooldown createCooldown(int duration) {
        return new LCCooldown(this.name, duration, TimeUnit.SECONDS, this.material);
    }
}