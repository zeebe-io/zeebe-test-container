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
package io.zeebe.containers.gateway;

import io.zeebe.containers.ZeebeConfigurable;
import io.zeebe.containers.ZeebeContainer;
import io.zeebe.containers.ZeebeNetworkable;
import io.zeebe.containers.ZeebePort;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.HostPortWaitStrategy;
import org.testcontainers.utility.Base58;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class ZeebeGatewayContainer extends GenericContainer<ZeebeGatewayContainer>
    implements ZeebeContainer<ZeebeGatewayContainer>,
        ZeebeConfigurable<ZeebeGatewayContainer>,
        ZeebeNetworkable {

  protected static final String DEFAULT_CLUSTER_MEMBER_ID = "zeebe-gateway-0";
  protected static final String DEFAULT_HOST = "0.0.0.0";

  protected String internalHost;
  protected boolean monitoringEnabled;

  public ZeebeGatewayContainer() {
    this(DEFAULT_ZEEBE_VERSION);
  }

  public ZeebeGatewayContainer(final String version) {
    this(ZeebeContainer.getDefaultImage(), version);
  }

  public ZeebeGatewayContainer(final String image, final String version) {
    super(image + ":" + version);
    applyDefaultConfiguration();
  }

  @Override
  public void applyDefaultConfiguration() {
    final String internalHost = "zeebe-gateway-" + Base58.randomString(6);

    withHost(DEFAULT_HOST)
        .withPort(ZeebePort.GATEWAY.getPort())
        .withClusterName(DEFAULT_CLUSTER_NAME)
        .withClusterMemberId(DEFAULT_CLUSTER_MEMBER_ID)
        .withClusterPort(ZeebePort.INTERNAL_API.getPort())
        .withClusterHost(internalHost);

    setWaitStrategy(new HostPortWaitStrategy());
    withEnv(ZeebeGatewayEnvironmentVariable.STANDALONE, true);
    withNetwork(Network.newNetwork());
  }

  @Override
  protected void configure() {
    final String name = getInternalHost() + "-" + Base58.randomString(6);
    final Set<ZeebePort> exposedPorts = EnumSet.of(ZeebePort.GATEWAY);
    if (monitoringEnabled) {
      exposedPorts.add(ZeebePort.MONITORING_API);
    }

    super.configure();
    withExposedPorts(exposedPorts.stream().map(ZeebePort::getPort).toArray(Integer[]::new));
    withNetworkAliases(getInternalHost());
    withCreateContainerCmdModifier(createContainerCmd -> createContainerCmd.withName(name));
  }

  @Override
  public Set<Integer> getLivenessCheckPortNumbers() {
    final Set<Integer> ports = new HashSet<>();
    ports.add(getMappedPort(ZeebePort.GATEWAY.getPort()));
    return ports;
  }

  @Override
  public String getInternalHost() {
    return internalHost;
  }

  public ZeebeGatewayContainer withHost(final String host) {
    return withEnv(ZeebeGatewayEnvironmentVariable.HOST, host);
  }

  public ZeebeGatewayContainer withPort(final int port) {
    return withEnv(ZeebeGatewayEnvironmentVariable.PORT, port);
  }

  public ZeebeGatewayContainer withContactPoint(final String contactPoint) {
    return withEnv(ZeebeGatewayEnvironmentVariable.CONTACT_POINT, contactPoint);
  }

  public ZeebeGatewayContainer withTransportBuffer(final int transportBuffer) {
    return withEnv(ZeebeGatewayEnvironmentVariable.TRANSPORT_BUFFER, transportBuffer);
  }

  public ZeebeGatewayContainer withRequestTimeout(final int requestTimeout) {
    return withEnv(ZeebeGatewayEnvironmentVariable.REQUEST_TIMEOUT, requestTimeout);
  }

  public ZeebeGatewayContainer withClusterName(final String clusterName) {
    return withEnv(ZeebeGatewayEnvironmentVariable.CLUSTER_NAME, clusterName);
  }

  public ZeebeGatewayContainer withClusterMemberId(final String clusterMemberId) {
    return withEnv(ZeebeGatewayEnvironmentVariable.CLUSTER_MEMBER_ID, clusterMemberId);
  }

  public ZeebeGatewayContainer withClusterHost(final String clusterHost) {
    internalHost = clusterHost;
    return withEnv(ZeebeGatewayEnvironmentVariable.CLUSTER_HOST, clusterHost);
  }

  public ZeebeGatewayContainer withClusterPort(final int clusterPort) {
    return withEnv(ZeebeGatewayEnvironmentVariable.CLUSTER_PORT, clusterPort);
  }

  public ZeebeGatewayContainer withManagementThreadCount(final int managementThreadCount) {
    return withEnv(ZeebeGatewayEnvironmentVariable.MANAGEMENT_THREAD_COUNT, managementThreadCount);
  }

  public ZeebeGatewayContainer withSecurityEnabled(final boolean securityEnabled) {
    return withEnv(ZeebeGatewayEnvironmentVariable.SECURITY_ENABLED, securityEnabled);
  }

  public ZeebeGatewayContainer withCertificatePath(final String certificatePath) {
    return withEnv(ZeebeGatewayEnvironmentVariable.CERTIFICATE_PATH, certificatePath);
  }

  public ZeebeGatewayContainer withPrivateKeyPath(final String privateKeyPath) {
    return withEnv(ZeebeGatewayEnvironmentVariable.PRIVATE_KEY_PATH, privateKeyPath);
  }

  public ZeebeGatewayContainer withMonitoringEnabled(final boolean monitoringEnabled) {
    this.monitoringEnabled = monitoringEnabled;
    return withEnv(ZeebeGatewayEnvironmentVariable.MONITORING_ENABLED, monitoringEnabled);
  }

  public ZeebeGatewayContainer withMonitoringHost(final String monitoringHost) {
    return withEnv(ZeebeGatewayEnvironmentVariable.MONITORING_HOST, monitoringHost);
  }

  public ZeebeGatewayContainer withMonitoringPort(final int monitoringPort) {
    return withEnv(ZeebeGatewayEnvironmentVariable.MONITORING_PORT, monitoringPort);
  }
}