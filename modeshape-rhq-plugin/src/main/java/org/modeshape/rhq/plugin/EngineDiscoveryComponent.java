/*
 * ModeShape (http://www.modeshape.org)
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * See the AUTHORS.txt file in the distribution for a full listing of 
 * individual contributors.
 *
 * ModeShape is free software. Unless otherwise indicated, all code in ModeShape
 * is licensed to you under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * ModeShape is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.modeshape.rhq.plugin;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.modeshape.rhq.plugin.util.DmrUtil;
import org.modeshape.rhq.plugin.util.PluginConstants;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryComponent;
import org.rhq.core.pluginapi.inventory.ResourceDiscoveryContext;
import org.rhq.modules.plugins.jbossas7.ASConnection;
import org.rhq.modules.plugins.jbossas7.BaseComponent;
import org.rhq.modules.plugins.jbossas7.json.Address;
import org.rhq.modules.plugins.jbossas7.json.ReadResource;
import org.rhq.modules.plugins.jbossas7.json.Result;

/**
 * 
 */
public class EngineDiscoveryComponent implements
		ResourceDiscoveryComponent<BaseComponent<?>> {

	public Set<DiscoveredResourceDetails> discoverResources(
		ResourceDiscoveryContext<BaseComponent<?>> context)
		throws Exception {

	Set<DiscoveredResourceDetails> discoveredResources = new HashSet<DiscoveredResourceDetails>();
	final Log log = LogFactory.getLog(PluginConstants.DEFAULT_LOGGER_CATEGORY);

    BaseComponent<?> parentComponent = context.getParentResourceComponent();
	ASConnection connection = parentComponent.getASConnection();
	Configuration config = context.getDefaultPluginConfiguration();

	Address addr = DmrUtil.getModeShapeAddress();
	Result result = connection.execute(new ReadResource(addr));
	
	if (result.isSuccess()) {	
		
		//TODO: Get version somehow?
		//String version = DmrUtil.stringValue(ModeShapeModuleView.executeManagedOperation(mc, "getVersion", new MetaValue[]{null}));

		/**
		 * 
		 * A discovered resource must have a unique key, that must stay the same
		 * when the resource is discovered the next time
		 */
		DiscoveredResourceDetails detail = new DiscoveredResourceDetails(
				context.getResourceType(), // ResourceType
				PluginConstants.ComponentType.Engine.MODESHAPE_DISPLAYNAME, // Resource Key
				PluginConstants.ComponentType.Engine.MODESHAPE_DISPLAYNAME, // Resource name
				//version,
				"3.1.1",
				PluginConstants.ComponentType.Engine.MODESHAPE_ENGINE, // Description
				context.getDefaultPluginConfiguration(), // Plugin Config
				null // Process info from a process scan
		);

		// Add to return values
		discoveredResources.add(detail);
		log.debug("Discovered ModeShape Engine");
		return discoveredResources;

	}else{

		log.debug("No ModeShape Engine discovered");
		return discoveredResources;
	}
}
}