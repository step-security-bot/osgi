/*
 * Copyright (c) IBM Corporation (2009). All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.osgi.test.cases.jndi.secure.objectFactory2;

import java.util.Hashtable;

import javax.naming.spi.ObjectFactory;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.cases.jndi.secure.provider.CTObjectFactory;

/**
 * @version $Revision$ $Date$
 */
public class ObjectFactory2Activator implements BundleActivator {

	private ServiceRegistration sr1;
	private ServiceRegistration sr2;
	
	public void start(BundleContext context) throws Exception {
		Hashtable props1 = new Hashtable();
		Hashtable props2 = new Hashtable();	
		String[] interfaces = {CTObjectFactory.class.getName(), ObjectFactory.class.getName()};		
		
		props1.put("osgi.jndi.serviceName", "CTObjectFactory");
		props1.put(Constants.SERVICE_RANKING, new Integer(3));
		props2.put("osgi.jndi.serviceName", "CTObjectFactory");
		props2.put(Constants.SERVICE_RANKING, new Integer(2));
		
		Hashtable env1 = new Hashtable();
		env1.put("test1", "test1");
		Hashtable env2 = new Hashtable();
		env2.put("test2", "test2");
		
		CTObjectFactory of1 = new CTObjectFactory(env1);
		CTObjectFactory of2 = new CTObjectFactory(env2);
		
		sr1 = context.registerService(interfaces, of1, props1);
		sr2 = context.registerService(interfaces, of2, props2);
	}

	public void stop(BundleContext context) throws Exception {
		sr1.unregister();
		sr2.unregister();
	}
}