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

package org.modeshape.rhq.plugin.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rhq.modules.plugins.jbossas7.json.Address;
import org.rhq.modules.plugins.jbossas7.json.Operation;
import org.modeshape.rhq.plugin.util.PluginConstants;

public class DmrUtil implements PluginConstants {

	protected final static Log LOG = LogFactory
			.getLog(PluginConstants.DEFAULT_LOGGER_CATEGORY); 
	public static String MODESHAPE_SUBSYSTEM = "modeshape";
	
	
	public static Address getModeShapeAddress() {
		Address addr = new Address(MODESHAPE_SUBSYSTEM);
		addr.add("subsystem", MODESHAPE_SUBSYSTEM);
		return addr;
	}
	
	public static Operation getRepositories() {
		Address addr = new Address(MODESHAPE_SUBSYSTEM);
		addr.add("subsystem", MODESHAPE_SUBSYSTEM);
		Operation op = new Operation(ComponentType.Engine.Operations.READ_CHILDREN_RESOURCES,addr);
		op.addAdditionalProperty(ComponentType.Engine.Operations.Parameters.CHILD_TYPE, ComponentType.Engine.Operations.Parameters.REPOSITORIES);
		return op;
	}
	
}
