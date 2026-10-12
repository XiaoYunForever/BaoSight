package com.baosight.client;

import java.time.LocalTime;
import java.util.*;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.QualifiedName;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.NodeClass;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;
import org.eclipse.milo.opcua.stack.core.types.structured.ReferenceDescription;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;

/** 动态发现指定命名空间下的变量，支持主动读取和订阅读取。 */
public final class OpcUaReadDemo {
    private static final String ENDPOINT = "opc.tcp://localhost:60187/milo";
    private static final String NAMESPACE_URI = "urn:baosight:opcua:demo";
    private OpcUaReadDemo() {}

    public static void main(String[] args) throws Exception {
        String endpoint = args.length == 0 ? ENDPOINT : args[0];
        String namespaceUri = args.length < 2 ? NAMESPACE_URI : args[1];
        OpcUaClient client = OpcUaClient.create(endpoint);
        Thread reader = Thread.currentThread();
        Thread hook = new Thread(() -> {
            reader.interrupt();
            try { reader.join(5000); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }, "opcua-client-shutdown");
        Runtime.getRuntime().addShutdownHook(hook);
        try {
            client.connect().get();
            System.out.println("Connected: " + endpoint);
            UShort namespaceIndex = client.readNamespaceTable().getIndex(namespaceUri);
            if (namespaceIndex == null) {
                throw new IllegalArgumentException("服务器未提供命名空间：" + namespaceUri);
            }
            System.out.println("Namespace: " + namespaceUri + "，index=" + namespaceIndex);
            runSubscription(client, namespaceIndex, reader);
        } catch (InterruptedException e) {
            reader.interrupt();
        } finally {
            boolean interrupted = Thread.interrupted();
            try {
                client.disconnect().get(5, java.util.concurrent.TimeUnit.SECONDS);
                System.out.println("Disconnected.");
            } finally {
                if (interrupted) reader.interrupt();
                try { Runtime.getRuntime().removeShutdownHook(hook); }
                catch (IllegalStateException e) { /* JVM 已进入关闭流程。 */ }
            }
        }
    }

    /** 原来的主动轮询方式：客户端每 250ms 主动读取一次。 */
    public static void runPolling(OpcUaClient client, UShort namespaceIndex,
                                  Thread cancellationThread) throws Exception {
        while (!cancellationThread.isInterrupted()) {
            long start = System.nanoTime();
            Map<NodeId, String> nodes = findVariables(client, namespaceIndex);
            System.out.println("--- " + LocalTime.now() + "，变量数：" + nodes.size() + " ---");
            if (!nodes.isEmpty()) {
                List<NodeId> ids = new ArrayList<>(nodes.keySet());
                List<DataValue> values = client.readValues(0.0, TimestampsToReturn.Both, ids).get();
                for (int i = 0; i < ids.size(); i++) {
                    printValue(nodes.get(ids.get(i)), ids.get(i), values.get(i));
                }
            }
            long remaining = 250 - (System.nanoTime() - start) / 1_000_000;
            if (remaining > 0) Thread.sleep(remaining);
        }
    }

    /**
     * 创建一个订阅，并把指定命名空间下当前发现的全部变量加入其中。
     * 服务器值变化后会通过回调主动通知客户端；每秒重新浏览一次以补订阅运行中新增的节点。
     */
    public static void runSubscription(OpcUaClient client, UShort namespaceIndex,
                                       Thread cancellationThread) throws Exception {
        UaSubscription subscription = client.getSubscriptionManager()
                .createSubscription(250.0)
                .get();
        Set<NodeId> subscribedNodes = new HashSet<>();
        System.out.println("Subscription created: publishing interval="
                + subscription.getRevisedPublishingInterval() + " ms");

        while (!cancellationThread.isInterrupted()) {
            Map<NodeId, String> nodes = findVariables(client, namespaceIndex);
            addNewMonitoredItems(subscription, nodes, subscribedNodes);
            Thread.sleep(1000);
        }
    }

    private static void addNewMonitoredItems(UaSubscription subscription,
                                             Map<NodeId, String> nodes,
                                             Set<NodeId> subscribedNodes) throws Exception {
        List<MonitoredItemCreateRequest> requests = new ArrayList<>();
        Map<UInteger, String> namesByHandle = new HashMap<>();
        Map<UInteger, NodeId> idsByHandle = new HashMap<>();

        for (Map.Entry<NodeId, String> entry : nodes.entrySet()) {
            if (!subscribedNodes.add(entry.getKey())) continue;
            UInteger handle = subscription.nextClientHandle();
            namesByHandle.put(handle, entry.getValue());
            idsByHandle.put(handle, entry.getKey());

            ReadValueId readValueId = new ReadValueId(
                    entry.getKey(), AttributeId.Value.uid(), null, QualifiedName.NULL_VALUE);
            MonitoringParameters parameters = new MonitoringParameters(
                    handle, 100.0, null, Unsigned.uint(10), true);
            requests.add(new MonitoredItemCreateRequest(
                    readValueId, MonitoringMode.Reporting, parameters));
        }

        if (requests.isEmpty()) return;
        List<UaMonitoredItem> items = subscription
                .createMonitoredItems(TimestampsToReturn.Both, requests)
                .get();
        for (UaMonitoredItem item : items) {
            UInteger handle = item.getClientHandle();
            String name = namesByHandle.get(handle);
            NodeId nodeId = idsByHandle.get(handle);
            item.setValueConsumer(value -> printValue(name, nodeId, value));
            System.out.println("Subscribed: " + name + " [" + nodeId + "]");
        }
    }

    private static void printValue(String name, NodeId nodeId, DataValue value) {
        Object data = value.getValue() == null ? null : value.getValue().getValue();
        String text = Arrays.deepToString(new Object[]{data});
        System.out.printf("%s [%s] = %s，状态：%s%n", name, nodeId,
                text.substring(1, text.length() - 1), value.getStatusCode());
    }

    /** 跨命名空间浏览层级，但只收集选定命名空间的变量；按 NodeId 去重。 */
    private static Map<NodeId, String> findVariables(OpcUaClient client, UShort namespaceIndex) throws Exception {
        Map<NodeId, String> variables = new LinkedHashMap<>();
        Set<NodeId> visited = new HashSet<>();
        ArrayDeque<NodeId> pending = new ArrayDeque<>();
        pending.add(Identifiers.ObjectsFolder);
        visited.add(Identifiers.ObjectsFolder);
        while (!pending.isEmpty()) {
            if (Thread.currentThread().isInterrupted()) throw new InterruptedException();
            for (ReferenceDescription reference : client.getAddressSpace().browse(pending.remove())) {
                NodeId id = reference.getNodeId().toNodeId(client.getNamespaceTable()).orElse(null);
                if (id == null || !visited.add(id)) continue;
                NodeClass type = reference.getNodeClass();
                if (type == NodeClass.Variable && namespaceIndex.equals(id.getNamespaceIndex())) {
                    variables.put(id, reference.getBrowseName().getName());
                }
                if (type == NodeClass.Object || type == NodeClass.Variable) pending.add(id);
            }
        }
        return variables;
    }
}
