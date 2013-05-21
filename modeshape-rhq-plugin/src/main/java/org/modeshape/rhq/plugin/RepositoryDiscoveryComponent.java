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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.modeshape.rhq.plugin.util.DmrUtil;
import org.modeshape.rhq.plugin.util.PluginConstants;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
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
public class RepositoryDiscoveryComponent implements
		ResourceDiscoveryComponent<BaseComponent<?>> {

	public Set<DiscoveredResourceDetails> discoverResources(
			ResourceDiscoveryContext<BaseComponent<?>> context)
			throws Exception {
		
		Set<DiscoveredResourceDetails> details = new HashSet<DiscoveredResourceDetails>();
		DiscoveredResourceDetails detail = null;
		final Log log = LogFactory
				.getLog(PluginConstants.DEFAULT_LOGGER_CATEGORY);

		BaseComponent<?> parentComponent = context.getParentResourceComponent();
		ASConnection connection = parentComponent.getASConnection();

		Address addr = DmrUtil.getModeShapeAddress();
		Result result = connection.execute(new ReadResource(addr));

		if (result.isSuccess()) {

			Result repoResult = connection.execute(DmrUtil.getRepositories());

			Map<?, ?> repoMap = (LinkedHashMap<?, ?>) repoResult.getResult();
			Iterator<?> repoIterator = repoMap.keySet().iterator();

			while (repoIterator.hasNext()) {
				String repoName = (String) repoIterator.next();
				Map<?, ?> repoValues = (Map<?, ?>) repoMap.get(repoName);

				detail = new DiscoveredResourceDetails(
						context.getResourceType(), // DataType
						repoName, // Key
						repoName, // Name
						//TODO Get ModeShape version
						"3.3", // Version
						context.getResourceType().getDescription(), context.getDefaultPluginConfiguration(),
						null);
				details.add(detail);
				log.debug("Discovered ModeShape Repository: " + repoName);
			}
		} else {
			return details;
		}

		return details;

	}

	
}