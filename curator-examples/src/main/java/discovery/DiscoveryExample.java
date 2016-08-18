/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package discovery;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.ServiceInstance;
import org.apache.curator.x.discovery.ServiceProvider;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.apache.curator.x.discovery.strategies.RandomStrategy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.*;

public class DiscoveryExample
{
    private  final String     PATH = "/services";
    private  CuratorFramework client = null;
    private  ServiceDiscovery<InstanceDetails> serviceDiscovery = null;

    public DiscoveryExample() {
        Map<String, ServiceProvider<InstanceDetails>>   providers = Maps.newHashMap();

        try
        {
            client = CuratorFrameworkFactory.newClient("ce-sandbox-kafka-0001.nm.flipkart.com", 10000, 10000, new ExponentialBackoffRetry(1000, 3));
            client.start();

            JsonInstanceSerializer<InstanceDetails> serializer = new JsonInstanceSerializer<InstanceDetails>(InstanceDetails.class);
            serviceDiscovery = ServiceDiscoveryBuilder.builder(InstanceDetails.class).client(client).basePath(PATH).serializer(serializer).build();
            try {
                serviceDiscovery.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        finally
        {
            for ( ServiceProvider<InstanceDetails> cache : providers.values() )
            {
                CloseableUtils.closeQuietly(cache);
            }

            CloseableUtils.closeQuietly(serviceDiscovery);
            CloseableUtils.closeQuietly(client);
        }

    }

    public Collection<String> listInstances() throws Exception
    {
        // This shows how to query all the instances in service discovery
        List<String> result = new ArrayList<String>();

        try
        {
            Collection<String>  serviceNames = serviceDiscovery.queryForNames();
            System.out.println(serviceNames.size() + " type(s)");
            for ( String serviceName : serviceNames )
            {
                Collection<ServiceInstance<InstanceDetails>> instances = serviceDiscovery.queryForInstances(serviceName);
                System.out.println(serviceName);
                for ( ServiceInstance<InstanceDetails> instance : instances )
                {
                    result.add(instance.getId());
                }
            }
        }
        finally
        {
            CloseableUtils.closeQuietly(serviceDiscovery);
        }
        return result;
    }

    public  void outputInstance(ServiceInstance<InstanceDetails> instance)
    {
        System.out.println("\t" + instance.getPayload().getDescription() + ": " + instance.buildUriSpec());
    }

    public  void deleteInstance(String serviceName, String description) throws Exception {
        // simulate a random instance going down
        // in a real application, this would occur due to normal operation, a crash, maintenance, etc.
        ExampleServer   server = new ExampleServer(client, PATH, serviceName, description);

        CloseableUtils.closeQuietly(server);
    }

    public  void addInstance(String serviceName, String description) throws Exception
    {
        // simulate a new instance coming up
        // in a real application, this would be a separate process

        ExampleServer   server = new ExampleServer(client, PATH, serviceName, description);
        server.start();

        System.out.println(serviceName + " added");
    }

}
