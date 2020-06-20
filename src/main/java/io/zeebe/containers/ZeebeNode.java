/*
 * Copyright © 2019 camunda services GmbH (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.zeebe.containers;

import java.time.Duration;
import java.util.List;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;

/**
 * Represents common properties of all Zeebe nodes in a cluster, e.g. the monitoring address, the
 * internal address, etc.
 *
 * @param <T> the concrete type of the underlying container
 */
public interface ZeebeNode<T extends GenericContainer<T>> extends Container<T> {

  /**
   * Returns an address accessible from within the container's network for the given port.
   *
   * @param port the target port
   * @return internally accessible address for {@code port}
   */
  default String getInternalAddress(final int port) {
    return getInternalHost() + ":" + port;
  }

  /**
   * Returns an address accessible from outside of the container's network for the given port.
   *
   * @param port the target port
   * @return externally accessible address for {@code port}
   */
  default String getExternalAddress(final int port) {
    return getExternalHost() + ":" + getMappedPort(port);
  }

  /**
   * Returns the address that nodes should use to talk to each other within the docker network. When
   * starting a cluster of containers, this is what you want to use for the initial contact points
   * so the nodes can find each other.
   *
   * @return the internal cluster address
   */
  default String getInternalClusterAddress() {
    return getInternalAddress(ZeebePort.INTERNAL.getPort());
  }

  /**
   * Returns the address that a Zeebe node outside of the docker network can use to talk to this
   * node.
   *
   * @return the external cluster address
   */
  default String getExternalClusterAddress() {
    return getExternalAddress(ZeebePort.INTERNAL.getPort());
  }

  /**
   * Returns the address to access the monitoring API of this node from within the same container
   * network as this node's.
   *
   * @return the internal monitoring address
   */
  default String getInternalMonitoringAddress() {
    return getInternalAddress(ZeebePort.MONITORING.getPort());
  }

  /**
   * Returns the address to access the monitoring API of this node from outside the container
   * network of this node.
   *
   * @return the external monitoring address
   */
  default String getExternalMonitoringAddress() {
    return getExternalAddress(ZeebePort.MONITORING.getPort());
  }

  /** @return the hostname of this node as visible from a host outside of its docker network */
  default String getExternalHost() {
    return self().getContainerIpAddress();
  }

  /**
   * Returns a hostname which is accessible from a host that is within the same docker network as
   * this node. It will attempt to return the first network alias it finds, and if there is none,
   * will return the container name. The network alias is preferable as it typically conveys more
   * meaning than container name, which is often randomly generated.
   *
   * @return the hostname of this node as visible from a host within the same docker network
   */
  default String getInternalHost() {
    final GenericContainer<?> container = self();
    final List<String> aliases = container.getNetworkAliases();
    if (aliases.isEmpty()) {
      return container.getContainerInfo().getName();
    }

    return aliases.get(0);
  }

  /**
   * Attempts to stop the container gracefully. If it times out, the container is abruptly killed.
   * The use case here is that {@link GenericContainer#stop()} actually kills and removes the
   * container, preventing us from:
   *
   * <ul>
   *   <li>shutting it down gracefully
   *   <li>restarting it
   * </ul>
   *
   * <p>There is an issue opened for this
   * https://github.com/testcontainers/testcontainers-java/issues/1000
   *
   * @param timeout must be greater than 1 second
   */
  default void shutdownGracefully(final Duration timeout) {
    getDockerClient()
        .stopContainerCmd(getContainerId())
        .withTimeout((int) timeout.getSeconds())
        .exec();
  }
}
