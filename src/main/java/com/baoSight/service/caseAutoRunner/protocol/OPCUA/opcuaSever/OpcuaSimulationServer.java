package com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfigLimits;
import org.eclipse.milo.opcua.sdk.server.identity.AnonymousIdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.IdentityValidator;
import org.eclipse.milo.opcua.sdk.server.identity.UsernameIdentityValidator;
import org.eclipse.milo.opcua.stack.core.security.CertificateManager;
import org.eclipse.milo.opcua.stack.core.security.DefaultCertificateManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateBuilder;
import org.eclipse.milo.opcua.stack.core.util.SelfSignedCertificateGenerator;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import java.util.Collections;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.BindException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;

import java.util.concurrent.ExecutionException;

public class OpcuaSimulationServer  {

    private OpcUaServer server;
    private Namespace demoNamespace;
    private int port;
    private boolean started;
    private boolean shutdownHookRegistered;
    private final boolean usernamePasswordAccess;
    public static final String NAMESPACE_URI = "urn:baosight:opcua:demo";
    private static final String USER_PASSWORD = "admin123";
    /** 服务器接受订阅监视项时允许的最小采样间隔，单位毫秒。 */
    public static final double MIN_SAMPLING_INTERVAL_MS = 250.0;
    /** 服务器接受订阅请求时允许的最小发布间隔，单位毫秒。 */
    public static final double MIN_PUBLISHING_INTERVAL_MS = 500.0;

    public OpcuaSimulationServer() {
        this(false);
    }

    /**
     * @param usernamePasswordAccess true 使用用户名密码登录，false 使用匿名登录
     */
    public OpcuaSimulationServer(boolean usernamePasswordAccess) {
        this.usernamePasswordAccess = usernamePasswordAccess;
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
            EndpointConfiguration.Builder endpointBuilder = EndpointConfiguration.newBuilder()
                    .setBindAddress("127.0.0.1")
                    .setHostname("localhost")
                    .setBindPort(port)
                    .setPath("/milo")
                    .setSecurityPolicy(SecurityPolicy.None)
                    .setSecurityMode(MessageSecurityMode.None);
            if (usernamePasswordAccess) {
                System.out.println("用户登录成功");
                UserTokenPolicy USER_TOKEN_POLICY_USERNAME_None = new UserTokenPolicy("username", UserTokenType.UserName, (String)null, (String)null, SecurityPolicy.None.getUri());
                endpointBuilder.addTokenPolicy(USER_TOKEN_POLICY_USERNAME_None);
            } else {
                endpointBuilder.addTokenPolicy(OpcUaServerConfig.USER_TOKEN_POLICY_ANONYMOUS);
            }

            // 用户名密码令牌需要使用服务器证书加密。即使测试端点仍使用 None，
            // 也必须把证书放进 endpoint 和 CertificateManager 中。
//            CertificateManager certificateManager = createCertificateManager(endpointBuilder);
            EndpointConfiguration endpoint = endpointBuilder.build();

            IdentityValidator<?> identityValidator = usernamePasswordAccess
                    ? new UsernameIdentityValidator(false, challenge ->
                    (isValidUser(challenge.getUsername())
                            && USER_PASSWORD.equals(challenge.getPassword())))
                    : AnonymousIdentityValidator.INSTANCE;
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
                   // .setCertificateManager()
                    .setIdentityValidator(identityValidator)
                    .build());
        demoNamespace = new Namespace(server,NAMESPACE_URI);
    }

    private CertificateManager createCertificateManager(EndpointConfiguration.Builder endpointBuilder)
            throws IOException {
        if (!usernamePasswordAccess) {
            return new DefaultCertificateManager();
        }
        try {
            KeyPair keyPair = SelfSignedCertificateGenerator.generateRsaKeyPair(2048);
            X509Certificate certificate = new SelfSignedCertificateBuilder(keyPair)
                    .setCommonName("localhost")
                    .setOrganization("BaoSight")
                    .setApplicationUri("urn:baosight:opcua:simulator")
                    .addDnsName("localhost")
                    .addIpAddress("127.0.0.1")
                    .build();
            endpointBuilder.setCertificate(certificate);
            return new DefaultCertificateManager(keyPair, certificate);
        } catch (Exception e) {
            throw new IOException("无法生成 OPC UA 用户认证所需的服务器证书", e);
        }
    }

    private boolean isValidUser(String username) {
        return "admin1".equals(username) || "admin2".equals(username);
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
