/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2014 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.plugins.ws;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.io.Resources;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.server.plugins.UpdateCenterMatrixFactory;
import org.sonar.updatecenter.common.PluginUpdate;

import java.util.Collection;

import static org.sonar.server.plugins.ws.PluginWSCommons.NAME_KEY_PLUGIN_UPDATE_ORDERING;

public class AvailablePluginsWsAction implements PluginsWsAction {

  private static final boolean DO_NOT_FORCE_REFRESH = false;
  private static final String ARRAY_PLUGINS = "plugins";

  private final UpdateCenterMatrixFactory updateCenterFactory;
  private final PluginWSCommons pluginWSCommons;

  public AvailablePluginsWsAction(UpdateCenterMatrixFactory updateCenterFactory, PluginWSCommons pluginWSCommons) {
    this.updateCenterFactory = updateCenterFactory;
    this.pluginWSCommons = pluginWSCommons;
  }

  @Override
  public void define(WebService.NewController controller) {
    controller.createAction("available")
      .setDescription("Get the list of all the plugins available for installation on the SonarQube instance, sorted by plugin name." +
        "<br/>" +
        "Update status values are: [COMPATIBLE, INCOMPATIBLE, REQUIRES_UPGRADE, DEPS_REQUIRE_UPGRADE]")
      .setSince("5.2")
      .setHandler(this)
      .setResponseExample(Resources.getResource(this.getClass(), "example-available_plugins.json"));
  }

  @Override
  public void handle(Request request, Response response) throws Exception {
    JsonWriter jsonWriter = response.newJsonWriter();
    jsonWriter.beginObject();

    writePlugins(jsonWriter);

    jsonWriter.close();
  }

  private void writePlugins(JsonWriter jsonWriter) {
    jsonWriter.name(ARRAY_PLUGINS);
    jsonWriter.beginArray();
    for (PluginUpdate pluginUpdate : retrieveAvailablePlugins()) {
      pluginWSCommons.writePluginUpdate(jsonWriter, pluginUpdate);
    }
    jsonWriter.endArray();
    jsonWriter.endObject();
  }

  private Collection<PluginUpdate> retrieveAvailablePlugins() {
    return ImmutableSortedSet.copyOf(
        NAME_KEY_PLUGIN_UPDATE_ORDERING,
        updateCenterFactory.getUpdateCenter(DO_NOT_FORCE_REFRESH).findAvailablePlugins()
    );
  }
}
