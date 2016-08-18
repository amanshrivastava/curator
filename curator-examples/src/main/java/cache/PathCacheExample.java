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
package cache;

import com.google.common.collect.Lists;
import discovery.DiscoveryExample;
import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.curator.utils.ZKPaths;
import discovery.ExampleServer;
import org.apache.zookeeper.KeeperException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

/**
 * An example of the PathChildrenCache. The example "harness" is a command processor
 * that allows adding/updating/removed nodes in a path. A PathChildrenCache keeps a
 * cache of these changes and outputs when updates occurs.
 */
public class PathCacheExample
{
    private static final String     PATH = "/services/xmpp";
    private static DiscoveryExample discoveryExample = null;
    CuratorFramework    client = null;
    PathChildrenCache   cache = null;

    public PathCacheExample() throws Exception {
        try
        {
            client = CuratorFrameworkFactory.newClient("ce-sandbox-kafka-0001.nm.flipkart.com", 10000, 10000, new ExponentialBackoffRetry(1000, 3));
            client.start();
            discoveryExample = new DiscoveryExample(client);
            // in this example we will cache data. Notice that this is optional.
            cache = new PathChildrenCache(client, PATH, true);
            cache.start();
            addListener();
        }
        finally
        {
            CloseableUtils.closeQuietly(cache);
            CloseableUtils.closeQuietly(client);
        }
    }

    public void add() throws Exception {
        discoveryExample.addInstance(client, "xmpp", InetAddress.getLocalHost().getHostAddress());
    }

    public void remove() throws Exception {
        discoveryExample.deleteInstance(client, "xmpp", InetAddress.getLocalHost().getHostAddress());
    }



    public void addListener()
    {
        // a PathChildrenCacheListener is optional. Here, it's used just to log changes
        PathChildrenCacheListener listener = new PathChildrenCacheListener()
        {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                System.out.println("Total number of instance = " + discoveryExample.listInstances().size());
            }
        };
        cache.getListenable().addListener(listener);
    }


}
