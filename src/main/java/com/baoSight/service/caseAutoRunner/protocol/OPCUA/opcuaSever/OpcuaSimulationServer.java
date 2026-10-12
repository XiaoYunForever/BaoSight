package com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigLimits;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import java.util.Collections;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.BindException;

import java.util.concurrent.ExecutionException;

public class OpcuaSimulationServer  {

    private OpcUaServer server;
    private Namespace demoNamespace;
    private int port;
    private boolean started;
    private boolean shutdownHookRegistered;
    public static final String NAMESPACE_URI = "urn:baosight:opcua:demo";
    /** 服务器接受订阅监视项时允许的最小采样间隔，单位毫秒。 */
    public static final double MIN_SAMPLING_INTERVAL_MS = 250.0;
    /** 服务器接受订阅请求时允许的最小发布间隔，单位毫秒。 */
    public static final double MIN_PUBLISHING_INTERVAL_MS = 500.0;

    public OpcuaSimulationServer() {
        try {
            initialize();
        } catch (IOException e) {
            throw new IllegalStateException("无法分配 OPC UA 仿真端口", e);
        }
    }

    private void initialize() throws IOException {
            // 由系统分配本机空闲端口；最终是否可用由 setup 中的实际绑定确认。
            try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))) {
                // 用socket临时分配一个端口，然后知道这个端口是空闲的，就释放掉给milo使用
                // ServerSocket 实现了Autoclose的接口，try结束之后，自动释放
                port = socket.getLocalPort();
            }
            EndpointConfiguration endpoint = EndpointConfiguration.newBuilder()
                    .setBindAddress("127.0.0.1")
                    .setHostname("localhost")
                    .setBindPort(port)
                    .setPath("/milo")
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None)
                    .addTokenPolicy(OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS)
                    .build();
            // 直接创建 SDK 服务器，不创建启动模拟事件线程的 ExampleNamespace。
            server = new OpcUaServer(OpcUaServerConfig.builder()
                    .setApplicationName(LocalizedText.english("BaoSight OPC UA Simulator"))
                    .setApplicationUri("urn:baosight:opcua:simulator")
                    .setProductUri("urn:baosight:opcua")
                    .setEndpoints(Collections.singleton(endpoint))
                    .setLimits(new OpcUaServerConfigLimits() {
                        @Override
                        public Double getMinPublishingInterval() {
                            return MIN_PUBLISHING_INTERVAL_MS;
                        }

                        @Override
                        public Double getDefaultPublishingInterval() {
                            return MIN_PUBLISHING_INTERVAL_MS;
                        }

                        @Override
                        public Double getMinSupportedSampleRate() {
                            return MIN_SAMPLING_INTERVAL_MS;
                        }
                    })
                    .setCertificateManager(new DefaultCertificateManager())
                    .setIdentityValidator(AnonymousIdentityValidator.INSTANCE)
                    .build());
        demoNamespace = new Namespace(server,NAMESPACE_URI);
    }

    /** 构造只初始化，调用此方法才挂载节点并开始监听。 */
    public synchronized void setup() throws ExecutionException, InterruptedException {
        if (started) return;
        for (int attempt = 0; attempt < 5; attempt++) {
            demoNamespace.startup();
            try {
                server.startup().get();
                if (server.getStackServer().getBoundEndpoints().isEmpty()) {
                    throw new ExecutionException(new BindException("端口绑定未成功：" + port));
                }
                started = true;
                break;
            } catch (ExecutionException e) {
                demoNamespace.shutdown();
                server.shutdown().join();
                Throwable cause = e;
                while (cause.getCause() != null) cause = cause.getCause();
                if (!(cause instanceof BindException) || attempt == 4) throw e;
                try {
                    initialize();
                } catch (IOException allocationError) {
                    throw new ExecutionException(allocationError);
                }
            } catch (InterruptedException e) {
                demoNamespace.shutdown();
                server.shutdown().join();
                throw e;
            }
        }
        System.out.println("OPC UA simulator started: " + getEndpointUrl());
        if (!shutdownHookRegistered) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    close();
                    System.out.println("OPC UA simulator stopped.");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            shutdownHookRegistered = true;
        }
    }

    /** 浏览器准备完成后调用：启动服务器并导入监控表，随后后台采集。 */
    public synchronized void setup(atomManipulation actions, String caseName)
            throws ExecutionException, InterruptedException {
        java.util.Objects.requireNonNull(actions, "actions 不能为空");
        setup();
        try {
            demoNamespace.startCollecting(actions, caseName);
        } catch (RuntimeException e) {
            try { close(); } catch (Exception cleanup) { e.addSuppressed(cleanup); }
            throw e;
        }
    }


    public synchronized int getPort() {
        if (!started) throw new IllegalStateException("请先调用 setup() 启动服务器");
        return port;
    }
    // 给客户端接口提供url
    public synchronized String getEndpointUrl() {
        return "opc.tcp://localhost:" + getPort() + "/milo";
    }

    public synchronized void close() throws Exception {
        if (!started) return;
        demoNamespace.stopCollecting();
        try {
            server.shutdown().get();
        } finally {
            demoNamespace.shutdown();
            started = false;
        }
    }
}
