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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.mc4j.ems.connection.EmsConnection;
import org.modeshape.rhq.plugin.util.DmrUtil;
import org.modeshape.rhq.plugin.util.ModeShapeModuleView;
import org.modeshape.rhq.plugin.util.PluginConstants;
import org.modeshape.rhq.plugin.util.PluginConstants.ComponentType;
import org.modeshape.rhq.plugin.util.PluginConstants.ComponentType.Engine;
import org.modeshape.rhq.plugin.util.PluginConstants.ComponentType.Repository;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.measurement.MeasurementDataTrait;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.CreateResourceReport;
import org.rhq.core.pluginapi.inventory.DiscoveredResourceDetails;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.modules.plugins.jbossas7.ASConnection;
import org.rhq.modules.plugins.jbossas7.BaseComponent;
import org.rhq.modules.plugins.jbossas7.json.Address;
import org.rhq.modules.plugins.jbossas7.json.ReadResource;
import org.rhq.modules.plugins.jbossas7.json.Result;

public class RepositoryComponent extends Facet {

	final String[] PROPERTIES = new String[] {"enable-monitoring","jndi-name", "predefined-workspace-names", "default-initial-content", "default-workspace", "allow-workspace-creation", "workspaces-cache-container"};

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.teiid.rhq.plugin.Facet#start(org.rhq.core.pluginapi.inventory.
	 * ResourceContext)
	 */
	@Override
	public void start(ResourceContext context) {
		this.setComponentName(context.getPluginConfiguration().getSimpleValue(
				"fullName", null));
		this.resourceConfiguration = context.getPluginConfiguration();
		this.componentType = PluginConstants.ComponentType.Repository.NAME;
		
		try {
			super.start(context);
			}catch (Exception e){
				
			}
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @see org.modeshape.rhq.plugin.Facet#getComponentType()
	 */
	@Override
	String getComponentType() {
		return ComponentType.Repository.NAME;
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @see org.modeshape.rhq.plugin.Facet#setOperationArguments(java.lang.String, org.rhq.core.domain.configuration.Configuration, java.util.Map)
	 */
	@Override
	protected void setOperationArguments(String name,
			Configuration configuration, Map<String, Object> valueMap) {
		// Parameter logic for engine Operations
		if (name.equals(Engine.Operations.SHUTDOWN)) {
			//no parms
		} else if (name.equals(Engine.Operations.RESTART)) {
			//no parms
		} 
	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.modeshape.rhq.plugin.Facet#getValues(org.rhq.core.domain.measurement.MeasurementReport, java.util.Set)
	 */
	@Override
	public void getValues(MeasurementReport report,
			Set<MeasurementScheduleRequest> requests) throws Exception {
		
		ModeShapeModuleView view = new ModeShapeModuleView();

		Map<String, Object> valueMap = new HashMap<String, Object>();
		valueMap.put(Repository.Operations.Parameters.REPOSITORY_NAME,
				this.resourceContext.getResourceKey());

		for (MeasurementScheduleRequest request : requests) {
			String name = request.getName();
			LOG.debug("Measurement name = " + name); //$NON-NLS-1$

			Object metricReturnObject = view.getMetric(getASConnection(),
					getComponentType(), this.getComponentIdentifier(), name,
					valueMap);

			try {
				if (request
						.getName()
						.equals(
								PluginConstants.ComponentType.Repository.Metrics.ACTIVESESSIONS)) {
					report.addData(new MeasurementDataTrait(request,
							(String) metricReturnObject));
				}
			} catch (Exception e) {
				LOG.error("Failed to obtain measurement [" + name //$NON-NLS-1$
						+ "]. Cause: " + e); //$NON-NLS-1$
				// throw(e);
			}
		}
	}
	
	@Override
	 public Configuration loadResourceConfiguration() throws Exception {

		
		
		Address addr = DmrUtil.getModeShapeAddress();
		Result result = getASConnection().execute(new ReadResource(addr));
		Result repoResult = null;

		if (result.isSuccess()) {
			repoResult = getASConnection().execute(DmrUtil.getRepositories());
		} else{
			return null;
		}
			
		Map<?, ?> repoMap = (LinkedHashMap<?, ?>) repoResult.getResult();

		String repoName = this.deploymentName;
		Map<?, ?> repoValues = (Map<?, ?>) repoMap.get(repoName);
		getProperties(repoValues, this.resourceConfiguration);
		
		return this.resourceConfiguration; 
	}
	
	private void getProperties(Map<?, ?> repoValues,
			Configuration c) {

		Iterator<?> propertyIterator = repoValues.keySet().iterator();
		
		while (propertyIterator.hasNext()) {

			String propName = (String) propertyIterator.next();
			Object value = repoValues.get(propName);
			if (Arrays.asList(PROPERTIES).contains(propName)){
				if (value instanceof String){
					c.setSimpleValue(propName, (String)value);
				}else if (value instanceof Boolean){
					c.setSimpleValue(propName, ((Boolean)value).toString());
				}else if (value == null) {
					c.setSimpleValue(propName, "Not Defined");
				}
			}
		}

	}

	/**
	 * {@inheritDoc}
	 *
	 * @see org.rhq.core.pluginapi.inventory.CreateChildResourceFacet#createResource(org.rhq.core.pluginapi.inventory.CreateResourceReport)
	 */
	@Override
	public CreateResourceReport createResource(CreateResourceReport arg0) {
		return null;
	}

	@Override
	public ASConnection getASConnection() {
		return ((BaseComponent<BaseComponent<?>>) this.resourceContext.getParentResourceComponent()).getASConnection();
	}

}
