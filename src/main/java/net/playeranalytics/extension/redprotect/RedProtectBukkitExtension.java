/*
    Copyright(c) 2019 AuroraLS3

    The MIT License(MIT)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files(the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions :
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
*/
package net.playeranalytics.extension.redprotect;

import br.net.fabiozumbi12.RedProtect.Bukkit.RedProtect;
import br.net.fabiozumbi12.RedProtect.Bukkit.Region;
import com.djrapitops.plan.extension.CallEvents;
import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.annotation.NumberProvider;
import com.djrapitops.plan.extension.annotation.PluginInfo;
import com.djrapitops.plan.extension.annotation.TableProvider;
import com.djrapitops.plan.extension.icon.Color;
import com.djrapitops.plan.extension.icon.Family;
import com.djrapitops.plan.extension.icon.Icon;
import com.djrapitops.plan.extension.table.Table;
import org.bukkit.Location;

import java.util.Set;
import java.util.UUID;

/**
 * DataExtension for RedProtect Bukkit.
 *
 * @author AuroraLS3
 */
@PluginInfo(name = "RedProtect", iconName = "shield-alt", iconFamily = Family.SOLID, color = Color.RED)
public class RedProtectBukkitExtension implements DataExtension {

    public RedProtectBukkitExtension() {
    }

    @Override
    public CallEvents[] callExtensionMethodsOn() {
        return new CallEvents[]{
                CallEvents.PLAYER_LEAVE,
                CallEvents.SERVER_PERIODICAL
        };
    }

    @TableProvider(tableColor = Color.RED)
    public Table regionTable(UUID playerUUID) {
        return createRegionTable(RedProtect.get().getAPI().getPlayerRegions(playerUUID.toString()));
    }

    @NumberProvider(
            text = "Total Area",
            description = "Total area the player has claimed",
            iconName = "map",
            iconFamily = Family.REGULAR,
            iconColor = Color.RED,
            showInPlayerTable = true
    )
    public long totalArea(UUID playerUUID) {
        return RedProtect.get().getAPI().getPlayerRegions(playerUUID.toString()).stream().mapToLong(this::getArea).sum();
    }

    private int getArea(Region region) {
        Location maxLocation = region.getMaxLocation();
        Location minLocation = region.getMinLocation();
        return Math.abs(maxLocation.getBlockX() - minLocation.getBlockX())
                * Math.abs(maxLocation.getBlockZ() - minLocation.getBlockZ());
    }

    @TableProvider(tableColor = Color.RED)
    public Table regionTable() {
        return createRegionTable(RedProtect.get().getAPI().getAllRegions());
    }

    private Table createRegionTable(Set<Region> regions) {
        Table.Factory table = Table.builder()
                .columnOne("Region", Icon.called("map-marker").build())
                .columnTwo("World", Icon.called("map").build())
                .columnThree("Area", Icon.called("map").of(Family.REGULAR).build());

        regions.stream()
                .sorted((one, two) -> Integer.compare(getArea(two), getArea(one)))
                .forEach(region -> {
                    int area = getArea(region);
                    Location center = region.getCenterLoc();
                    String location = "x: " + center.getBlockX() + ", z: " + center.getBlockZ();
                    String world = region.getCenterLoc().getWorld().getName();
                    table.addRow(location, world, area);
                });

        return table.build();
    }
}