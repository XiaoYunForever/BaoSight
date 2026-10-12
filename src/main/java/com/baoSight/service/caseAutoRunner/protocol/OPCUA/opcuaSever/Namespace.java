package com.baoSight.service.caseAutoRunner.protocol.OPCUA.opcuaSever;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespaceWithLifecycle;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.DateTime;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.ULong;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.openqa.selenium.NoSuchSessionException;
import com.baoSight.service.caseAutoRunner.protocol.atomManipulation;

public class Namespace extends ManagedNamespaceWithLifecycle {
    /** 浏览器采集及节点更新任务的固定延迟，单位毫秒。 */
    public static final long NODE_UPDATE_PERIOD_MS = 20L;
    private final SubscriptionModel subscriptionModel;
    private UaFolderNode folder;
    private final Map<String, UaVariableNode> variables = new HashMap<>();
    private final Map<String, NodeId> variableDataTypes = new HashMap<>();
    private ScheduledExecutorService collector;
    private volatile boolean collecting;

    public Namespace(OpcUaServer server, String namespaceUri) {
        super(server, namespaceUri);
        subscriptionModel = new SubscriptionModel(server, this);//订阅对象
        getLifecycleManager().addLifecycle(subscriptionModel);//生命周期管理器管理这个订阅对象
        Runnable startTask = this::createNodes;
        Runnable shutdownTask = this::shutdown;
        getLifecycleManager().addStartupTask(startTask);//调用 namespace.startup() 时执行 createNodes()。
        getLifecycleManager().addShutdownTask(shutdownTask);
    }

    /** 在 Objects/Demo 下挂载浏览器采集的变量。 */
    private void createNodes() {
        // 新建一个文件夹节点
        folder = new UaFolderNode(getNodeContext(), newNodeId("Demo"),
                newQualifiedName("Demo"), LocalizedText.english("Demo"));

        // 加入文件夹节点
        getNodeManager().addNode(folder);

        // 加载到Obejcts节点
        folder.addReference(new Reference(folder.getNodeId(), Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(), false));
    }

    private UaVariableNode addVariable(UaFolderNode folder, String name, ConvertedValue value) {
        UaVariableNode node = UaVariableNode.builder(getNodeContext())
                .setNodeId(newNodeId("Variables/" + name))
                .setBrowseName(newQualifiedName(name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(value.dataType)
                .setTypeDefinition(Identifiers.BaseDataVariableType)
                .setAccessLevel(AccessLevel.toValue(AccessLevel.CurrentRead))
                .setUserAccessLevel(AccessLevel.toValue(AccessLevel.CurrentRead))
                .build();
        node.setValue(new DataValue(new Variant(value.value)));
        getNodeManager().addNode(node);
        folder.addOrganizes(node);
        variableDataTypes.put(name, value.dataType);
        return node;
    }

    /** 导入和开启监控只执行一次，之后后台采集，不重复点击开关。 */
    public synchronized void startCollecting(atomManipulation actions, String caseName) {
        java.util.Objects.requireNonNull(actions, "actions 不能为空");
        if (collector != null) return;
        actions.observeVariables(caseName);
        updateValues(actions.getWatchValues());
        collecting = true;
        // 定时任务执行器
        collector = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "opcua-watch-collector");
            thread.setDaemon(true);
            return thread;
        });
        collector.scheduleWithFixedDelay(() -> {
            try {
                Map<String, String> values = actions.getWatchValues();
                synchronized (Namespace.this) {
                    if (collecting) updateValues(values);
                }
            } catch (NoSuchSessionException e) {
                stopCollecting();
                System.err.println("浏览器会话已关闭，停止 OPC UA 数据采集。");
            } catch (RuntimeException e) {
                // 保留最后一次成功值；本次读取失败不会终止后续调度。
                System.err.println("OPC UA 数据采集失败：" + e.getMessage());
            }
        }, NODE_UPDATE_PERIOD_MS, NODE_UPDATE_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    private void updateValues(Map<String, String> values) {
        Map<String, ConvertedValue> convertedValues = new java.util.LinkedHashMap<>();
        values.replaceAll((name, rawValue) -> {
            ConvertedValue converted = convertValue(rawValue);
            convertedValues.put(name, converted);
            return converted.normalizedValue;
        });
        convertedValues.forEach((name, value) -> {
            UaVariableNode node = variables.get(name);
            if (node == null) {
                variables.put(name, addVariable(folder, name, value));
            } else {
                node.setValue(new DataValue(new Variant(value.value)));
            }
        });
    }

    /**
     * 将监控表的 TYPE/VALUE 转成 OPC UA 可读的 Java 值。
     * 数字统一输出十进制文本，同时保留 Variant 的实际类型供节点使用。
     */
    private ConvertedValue convertValue(String typeAndValue) {
        int slash = typeAndValue.indexOf('/');
        if (slash <= 0 || slash == typeAndValue.length() - 1) {
            throw new IllegalArgumentException("监控值格式必须是 TYPE/VALUE：" + typeAndValue);
        }
        String type = typeAndValue.substring(0, slash).trim().toUpperCase();
        String raw = typeAndValue.substring(slash + 1).trim();
        switch (type) {
            case "DWORD": {
                long number = parseUnsigned(raw, 32);
                return new ConvertedValue(UInteger.valueOf(number), Identifiers.UInt32,
                        Long.toString(number));
            }
            case "BYTE": {
                long number = parseUnsigned(raw, 8);
                return new ConvertedValue(UByte.valueOf(number), Identifiers.Byte,
                        Long.toString(number));
            }
            case "WORD": {
                long number = parseUnsigned(raw, 16);
                return new ConvertedValue(UShort.valueOf(Math.toIntExact(number)), Identifiers.UInt16,
                        Long.toString(number));
            }
            case "LWORD": {
                BigInteger number = parseUnsignedBig(raw, 64);
                return new ConvertedValue(ULong.valueOf(number), Identifiers.UInt64,
                        number.toString());
            }
            case "BOOL": {
                boolean value = Boolean.parseBoolean(raw);
                return new ConvertedValue(value, Identifiers.Boolean, Boolean.toString(value));
            }
            case "SINT": {
                byte value = (byte) parseSigned(raw, 8);
                return new ConvertedValue(value, Identifiers.SByte, Byte.toString(value));
            }
            case "INT": {
                short value = (short) parseSigned(raw, 16);
                return new ConvertedValue(value, Identifiers.Int16, Short.toString(value));
            }
            case "REAL": {
                float value = Float.parseFloat(raw);
                return new ConvertedValue(value, Identifiers.Float, Float.toString(value));
            }
            case "CHAR": {
                String value = stripQuotes(raw);
                return new ConvertedValue(value, Identifiers.String, value);
            }
            case "STRING[80]": {
                String value = stripQuotes(raw);
                return new ConvertedValue(value, Identifiers.String, value);
            }
            case "TIME": {
                double milliseconds = parseTimeMillis(raw);
                return new ConvertedValue(milliseconds, Identifiers.Double,
                        Double.toString(milliseconds));
            }
            case "DATE": {
                DateTime value = new DateTime(LocalDate.parse(raw.replaceFirst("^D#", ""))
                        .atStartOfDay(ZoneId.systemDefault()).toInstant());
                return new ConvertedValue(value, Identifiers.DateTime, value.getJavaInstant().toString());
            }
            case "DT": {
                DateTime value = new DateTime(parseDateTime(raw));
                return new ConvertedValue(value, Identifiers.DateTime, value.getJavaInstant().toString());
            }
            default:
                throw new IllegalArgumentException("暂不支持的 PLC 数据类型：" + type);
        }
    }

    private long parseSigned(String raw, int bits) {
        BigInteger value = parseNumber(raw, 10);
        // PLC 的有符号十六进制值按补码解释，例如 SINT 的 16#FF 是 -1。
        if (isHex(raw) && value.testBit(bits - 1)) {
            value = value.subtract(BigInteger.ONE.shiftLeft(bits));
        }
        BigInteger min = BigInteger.ONE.shiftLeft(bits - 1).negate();
        BigInteger max = BigInteger.ONE.shiftLeft(bits - 1).subtract(BigInteger.ONE);
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new IllegalArgumentException("有符号值超出 " + bits + " 位范围：" + raw);
        }
        return value.longValue();
    }

    private long parseUnsigned(String raw, int bits) {
        BigInteger value = parseUnsignedBig(raw, bits);
        return value.longValueExact();
    }

    private BigInteger parseUnsignedBig(String raw, int bits) {
        BigInteger value = parseNumber(raw, 10);
        if (value.signum() < 0 || value.bitLength() > bits) {
            throw new IllegalArgumentException("无符号值超出 " + bits + " 位范围：" + raw);
        }
        return value;
    }

    private BigInteger parseNumber(String raw, int radix) {
        String value = raw.replace("_", "").trim();
        if (value.regionMatches(true, 0, "16#", 0, 3)) {
            return new BigInteger(value.substring(3), 16);
        }
        return new BigInteger(value, radix);
    }

    private double parseTimeMillis(String raw) {
        String value = raw.toUpperCase().replace("_", "").replaceFirst("^T#", "").trim();
        double result = 0;
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d+(?:\\.\\d+)?)(MS|S|M|H|D)").matcher(value);
        while (matcher.find()) {
            double number = Double.parseDouble(matcher.group(1));
            switch (matcher.group(2)) {
                case "MS": result += number; break;
                case "S": result += number * 1_000; break;
                case "M": result += number * 60_000; break;
                case "H": result += number * 3_600_000; break;
                case "D": result += number * 86_400_000; break;
                default: throw new IllegalArgumentException("无法解析 TIME：" + raw);
            }
        }
        if (result == 0 && !value.equals("0MS")) throw new IllegalArgumentException("无法解析 TIME：" + raw);
        return result;
    }

    private Instant parseDateTime(String raw) {
        String value = raw.replaceFirst("^DT#", "").replace(' ', 'T');
        // PLC 常见格式为 DT#yyyy-MM-dd-HH:mm:ss，日期和时间之间也是 '-'.
        if (value.matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}:\\d{2}:\\d{2}")) {
            value = value.substring(0, 10) + "T" + value.substring(11);
        }
        return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
    }

    private boolean isHex(String raw) {
        return raw.trim().regionMatches(true, 0, "16#", 0, 3);
    }

    private String stripQuotes(String value) {
        if (value.length() >= 2 && ((value.startsWith("'") && value.endsWith("'"))
                || (value.startsWith("\"") && value.endsWith("\"")))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    private static final class ConvertedValue {
        private final Object value;
        private final NodeId dataType;
        private final String normalizedValue;

        private ConvertedValue(Object value, NodeId dataType, String normalizedValue) {
            this.value = value;
            this.dataType = dataType;
            this.normalizedValue = normalizedValue;
        }
    }

    public synchronized void stopCollecting() {
        collecting = false;
        if (collector != null) {
            collector.shutdownNow();
            collector = null;
        }
    }

    @Override
    public void onDataItemsCreated(List<DataItem> list) {
        subscriptionModel.onDataItemsCreated(list);
    }

    @Override
    public void onDataItemsModified(List<DataItem> list) {
        subscriptionModel.onDataItemsModified(list);
    }

    @Override
    public void onDataItemsDeleted(List<DataItem> list) {
        subscriptionModel.onDataItemsDeleted(list);
    }

    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> list) {
        subscriptionModel.onMonitoringModeChanged(list);
    }
}
