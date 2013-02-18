/*
 * JBoss, Home of Professional Open Source.
 * See the COPYRIGHT.txt file distributed with this work for information
 * regarding copyright ownership.  Some portions may be licensed
 * to Red Hat, Inc. under one or more contributor license agreements.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
 * 02110-1301 USA.
 */
package org.teiid.rhq.plugin;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.core.domain.configuration.Configuration;
import org.rhq.core.domain.configuration.PropertyList;
import org.rhq.core.domain.configuration.PropertyMap;
import org.rhq.core.domain.configuration.PropertySimple;
import org.rhq.core.domain.measurement.MeasurementDataNumeric;
import org.rhq.core.domain.measurement.MeasurementReport;
import org.rhq.core.domain.measurement.MeasurementScheduleRequest;
import org.rhq.core.pluginapi.inventory.ResourceContext;
import org.rhq.core.pluginapi.measurement.MeasurementFacet;
import org.rhq.modules.plugins.jbossas7.ASConnection;
import org.rhq.modules.plugins.jbossas7.json.Address;
import org.rhq.modules.plugins.jbossas7.json.Operation;
import org.rhq.modules.plugins.jbossas7.json.Result;
import org.teiid.rhq.plugin.util.DmrUtil;
import org.teiid.rhq.plugin.util.PluginConstants;
import org.teiid.rhq.plugin.util.PluginConstants.ComponentType.Platform;

/**
 * Component class for the Teiid Translator.
 * 
 */
public class TranslatorComponent extends Facet {
	private final Log LOG = LogFactory.getLog(PluginConstants.DEFAULT_LOGGER_CATEGORY);
	
	public static final String TRANSLATORNAME = "translator-name"; //$NON-NLS-1$
	public static final String DESCRIPTION = "translator-description"; //$NON-NLS-1$
	public static final String MODULENAME = "module-name"; //$NON-NLS-1$
	public static final String PROPERTIES = "properties"; //$NON-NLS-1$
	public static final String PROPERTYNAME = "property-name"; //$NON-NLS-1$
	public static final String PROPERTYVALUE = "property-value"; //$NON-NLS-1$

	@Override
	public void start(ResourceContext context) {
		this.setComponentName(context.getPluginConfiguration().getSimpleValue(	"name", null));
		this.resourceConfiguration=context.getPluginConfiguration();
		try {
		super.start(context);
		}catch (Exception e){
			
		}
	}
	
	/**
	 * @see org.teiid.rhq.plugin.Facet#getComponentType()
	 * @since 1.0
	 */
	@Override
	String getComponentType() {
		return PluginConstants.ComponentType.Translator.NAME;
	}

	/**
	 * The plugin container will call this method when your resource component
	 * has been scheduled to collect some measurements now. It is within this
	 * method that you actually talk to the managed resource and collect the
	 * measurement data that is has emitted.
	 * 
	 * @see MeasurementFacet#getValues(MeasurementReport, Set)
	 */
	public void getValues(MeasurementReport report, Set<MeasurementScheduleRequest> requests) {
		for (MeasurementScheduleRequest request : requests) {
			String name = request.getName();

			// TODO: based on the request information, you must collect the
			// requested measurement(s)
			// you can use the name of the measurement to determine what you
			// actually need to collect
			try {
				Number value = new Integer(1); // dummy measurement value -
				// this should come from the
				// managed resource
				report.addData(new MeasurementDataNumeric(request, value.doubleValue()));
			} catch (Exception e) {
				LOG.error("Failed to obtain measurement [" + name 	+ "]. Cause: " + e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		return;
	}
	
	protected void setOperationArguments(String name,
			Configuration configuration, Map argumentMap) {
		//No operations for translators
	}
	
	@Override
	public Configuration loadResourceConfiguration() {

		Address addr = DmrUtil.getTeiidAddress();
		Operation op = new Operation(Platform.Operations.lIST_TRANSLATORS, addr);
		Result result = getASConnection().execute(op);
		ArrayList<LinkedHashMap<String, Object>> translatorlist = (ArrayList<LinkedHashMap<String, Object>>) result.getResult();
		Configuration configuration = null;

		//Iterate through Translators
		for (LinkedHashMap<String, Object> map : translatorlist) {
			
			String translatorKey = (String) map.get(TranslatorComponent.TRANSLATORNAME);
			String translatorName = translatorKey;
			String moduleType =  (String) map.get(TranslatorComponent.MODULENAME);;
			String description = (String) map.get(TranslatorComponent.DESCRIPTION);

			// Get plugin config map for translators
			configuration = resourceContext.getPluginConfiguration();

			//Set common properties
			configuration.put(new PropertySimple("name", translatorName));//$NON-NLS-1$
			configuration.put(new PropertySimple("moduleName",moduleType));//$NON-NLS-1$	
			configuration.put(new PropertySimple("description",description));//$NON-NLS-1$	
			
			 // Add to return values
			// First get translator specific properties
			ArrayList<Map<String,String>> translatorProps = (ArrayList<Map<String, String>>) map.get(TranslatorComponent.PROPERTIES);
			PropertyList list = new PropertyList("translatorList");//$NON-NLS-1$
			PropertyMap propMap = null;
			getTranslatorValues(translatorProps, propMap, list);
		    configuration.put(list);
			
		}
			
		return configuration;

	}
	
	public static <T> void getTranslatorValues(ArrayList<Map<String,String>> propertyList,
			PropertyMap map, PropertyList list) {
			for (Map propertyMap: propertyList) {
				String key = (String) propertyMap.get(TranslatorComponent.PROPERTYNAME);
				String value = (String) propertyMap.get(TranslatorComponent.PROPERTYVALUE);
				map = new PropertyMap("properties");//$NON-NLS-1$
				map.put(new PropertySimple("name", key));//$NON-NLS-1$
				map.put(new PropertySimple("value", value));//$NON-NLS-1$
				list.add(map);
			}

	}

	@Override
	public ASConnection getASConnection() {
		return ((PlatformComponent)this.resourceContext.getParentResourceComponent()).getASConnection();
	}
	
}