package id.naturalsmp.naturalFun.bloodmoon;

import id.naturalsmp.naturalFun.NaturalFun;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SafezoneManager {

    private static final List<Cuboid> safezones = new ArrayList<>();
    private static NaturalFun plugin;

    public static void init(NaturalFun pl) {
        plugin = pl;
        loadSafezones();
    }
    
    public static void loadSafezones() {
        safezones.clear();
        if (plugin.getConfig().getBoolean("safezone.enabled", true) == false) return;
        List<String> list = plugin.getConfig().getStringList("safezone.zones");
        for (String s : list) {
            try {
                // Format: world: x1,y1,z1,x2,y2,z2
                String[] parts = s.split(":");
                String worldName = parts[0].trim();
                String[] coords = parts[1].trim().split(",");
                
                World world = Bukkit.getWorld(worldName);
                if (world == null) continue;
                
                int x1 = Integer.parseInt(coords[0]);
                int y1 = Integer.parseInt(coords[1]);
                int z1 = Integer.parseInt(coords[2]);
                int x2 = Integer.parseInt(coords[3]);
                int y2 = Integer.parseInt(coords[4]);
                int z2 = Integer.parseInt(coords[5]);
                
                safezones.add(new Cuboid(world, x1, y1, z1, x2, y2, z2));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid safezone format: " + s);
            }
        }
    }

    public static boolean isInSafezone(Player player) {
        for (Cuboid c : safezones) {
            if (c.contains(player.getLocation())) {
                return true;
            }
        }
        return false;
    }

    private static class Cuboid {
        private final World world;
        private final int xMin, xMax, yMin, yMax, zMin, zMax;

        public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
            this.world = world;
            this.xMin = Math.min(x1, x2);
            this.xMax = Math.max(x1, x2);
            this.yMin = Math.min(y1, y2);
            this.yMax = Math.max(y1, y2);
            this.zMin = Math.min(z1, z2);
            this.zMax = Math.max(z1, z2);
        }

        public boolean contains(Location loc) {
            if (!loc.getWorld().equals(world)) return false;
            return loc.getBlockX() >= xMin && loc.getBlockX() <= xMax &&
                   loc.getBlockY() >= yMin && loc.getBlockY() <= yMax &&
                   loc.getBlockZ() >= zMin && loc.getBlockZ() <= zMax;
        }
    }
}
