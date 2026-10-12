# OPC UA Client Demo

这是一个独立的 Eclipse Milo Java 客户端，用来连接同目录下的 `opcua-simulator` 并读取数据。

## 使用方法

先启动模拟服务器：

```powershell
cd "..\opcua-simulator"
mvn exec:java
```

再打开另一个终端运行客户端：

```powershell
cd "..\opcua-client-demo"
mvn exec:java
```

客户端会连接 `opc.tcp://localhost:12686/milo`，按 URI `urn:baosight:opcua:demo` 找到自定义命名空间，然后读取 `Objects/DemoDevice` 下的四个自定义节点和 XML 注册的 `aa` 节点，并把结果打印到控制台。
