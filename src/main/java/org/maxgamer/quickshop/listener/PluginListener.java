/*
 * This file is a part of project QuickShop, the name is PluginListener.java
 *  Copyright (C) PotatoCraft Studio and contributors
 *
 *  This program is free software: you can redistribute it and/or modify it
 *  under the terms of the GNU General Public License as published by the
 *  Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 *  FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *  for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.maxgamer.quickshop.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.maxgamer.quickshop.QuickShop;
import org.maxgamer.quickshop.integration.IntegratedPlugin;
import org.maxgamer.quickshop.integration.IntegrationHelper;
import org.maxgamer.quickshop.util.Util;
import org.maxgamer.quickshop.util.compatibility.CompatibilityManager;
import org.maxgamer.quickshop.util.reload.ReloadResult;
import org.maxgamer.quickshop.util.reload.ReloadStatus;

import java.util.Set;

public class PluginListener extends AbstractQSListener {

    private static final Set<String> COMPATIBILITY_MODULE_LIST = CompatibilityManager.getModuleMapping().keySet();
    private static final Set<String> PLUGIN_INTEGRATION_LIST = IntegrationHelper.getIntegrationMapping().keySet();
    private IntegrationHelper integrationHelper;
    private CompatibilityManager compatibilityManager;

    public PluginListener(QuickShop plugin) {
        super(plugin);
        init();
    }

    private void init() {
        integrationHelper = plugin.getIntegrationHelper();
        compatibilityManager = plugin.getCompatibilityTool();
    }


    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginDisabled(PluginDisableEvent event) {
        String pluginName = event.getPlugin().getName();
        if (PLUGIN_INTEGRATION_LIST.contains(pluginName) && plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
            IntegratedPlugin integratedPlugin = integrationHelper.getIntegrationMap().get(pluginName);
            if (integratedPlugin != null) {
                Util.debugLog("[Hot Load] Calling for unloading " + integratedPlugin.getName());
                integratedPlugin.unload();
                integrationHelper.unregister(integratedPlugin);
            }
        }
        if (COMPATIBILITY_MODULE_LIST.contains(pluginName)) {
            compatibilityManager.unregister(pluginName);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPluginEnabled(PluginEnableEvent event) {
        String pluginName = event.getPlugin().getName();
        if (PLUGIN_INTEGRATION_LIST.contains(pluginName) && plugin.getConfig().getBoolean("integration." + pluginName.toLowerCase() + ".enable")) {
            integrationHelper.register(pluginName);
            IntegratedPlugin integratedPlugin = integrationHelper.getIntegrationMap().get(pluginName);
            if (integratedPlugin != null) {
                Util.debugLog("[Hot Load] Calling for loading " + integratedPlugin.getName());
                integratedPlugin.load();
            }
        }
        if (COMPATIBILITY_MODULE_LIST.contains(pluginName)) {
            compatibilityManager.register(pluginName);
        }
    }

    /**
     * Callback for reloading
     *
     * @return Reloading success
     */
    @Override
    public ReloadResult reloadModule() throws Exception {
        return ReloadResult.builder().status(ReloadStatus.SUCCESS).build();
    }
}
