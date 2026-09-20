# BaoSight 自动化执行引擎

## 1. 模块定位

自动化执行引擎位于：

```text
src/main/java/com/baoSight/service/caseAutoRunner
```

它负责为测试用例提供浏览器运行环境，并按照测试用例定义的步骤调用原子操作。

当前的执行关系如下：

```text
前端请求
    ↓
taskDispatcher.mainRun
    ↓
根据用例编号获取 Spring Bean
    ↓
Protocol.start()
    ↓
AbstractBrowserCase
    ↓
BrowserSession
    ↓
具体测试用例 executeCase()
    ↓
通用原子操作 / 协议原子操作
```

示例目录结构：

```text
caseAutoRunner
├── BrowserFactory.java
├── BrowserSession.java
└── protocol
    ├── Protocol.java
    ├── AbstractBrowserCase.java
    ├── atomManipulation.java
    └── OPCUA
        ├── opcua_atom.java
        ├── opcua_01.java
        └── opcua_02.java
```

## 2. BrowserFactory：浏览器创建工厂

`BrowserFactory` 只负责创建浏览器，不负责打开业务页面，也不负责执行测试步骤。

支持的浏览器类型：

```java
BrowserFactory.Browser.CHROME
BrowserFactory.Browser.EDGE
```

调用时按照参数顺序表示优先级：

```java
WebDriver driver = BrowserFactory.createBrowser(
        BrowserFactory.Browser.EDGE,
        BrowserFactory.Browser.CHROME
);
```

上面的代码会：

1. 优先尝试启动 Edge；
2. Edge 启动失败时尝试 Chrome；
3. 第一个浏览器启动成功后立即返回；
4. 两个浏览器都失败时抛出异常。

因此，一个测试用例只会获得一个浏览器会话，不会同时启动两个浏览器。

`BrowserFactory` 不应该负责以下工作：

- 打开 Baosky IDE 页面；
- 点击页面元素；
- 等待用户登录；
- 执行 PLC 或 OPC UA 操作；
- 决定测试用例的步骤顺序。

## 3. BrowserSession：一次测试的浏览器会话

`BrowserSession` 封装一次测试用例使用的 `WebDriver`。它负责管理浏览器的生命周期：

```text
创建浏览器
    ↓
最大化窗口
    ↓
打开 IDE 地址
    ↓
把 WebDriver 交给原子操作
    ↓
测试结束后关闭浏览器
```

创建会话：

```java
BrowserSession session = BrowserSession.open(
        ideUrl,
        BrowserFactory.Browser.EDGE,
        BrowserFactory.Browser.CHROME
);
```

原子操作通过会话取得当前浏览器：

```java
opcua_atom actions = new opcua_atom(session.getDriver());
```

关闭会话：

```java
session.close();
```

`BrowserSession` 实现了 `AutoCloseable`，因此也可以使用自动资源管理：

```java
try (BrowserSession session = BrowserSession.open(
        ideUrl,
        BrowserFactory.Browser.EDGE,
        BrowserFactory.Browser.CHROME)) {

    opcua_atom actions = new opcua_atom(session.getDriver());
    actions.initialization();
}
```

代码块结束后会自动调用 `session.close()`。

### BrowserSession 与通用原子操作的区别

两者职责不同：

| 类 | 主要职责 | 关注内容 |
|---|---|---|
| `BrowserSession` | 管理浏览器会话 | 浏览器创建、页面打开、WebDriver 生命周期 |
| `atomManipulation` | 执行页面原子动作 | 点击、输入、等待、读取页面元素等 |

`BrowserSession` 不应该实现“点击程序单元”或“下载程序”等业务动作；`atomManipulation` 也不应该自己创建或关闭浏览器。

可以把它们理解为：

```text
BrowserSession = 操作环境
atomManipulation = 在环境中执行的动作
```

## 4. Protocol：测试用例统一入口

`Protocol` 定义所有测试用例的统一执行入口：

```java
public interface Protocol {
    void start();
}
```

调度器不需要知道具体用例的内部步骤，只需要调用：

```java
protocol.start();
```

后续如果从配置文件读取浏览器和运行参数，可以将接口扩展为接收运行配置对象，例如：

```java
void start(CaseRunOptions options);
```

## 5. AbstractBrowserCase：测试用例公共模板

`AbstractBrowserCase` 为浏览器自动化测试用例提供统一流程。它实现 `Protocol`，并统一完成：

1. 选择优先浏览器和备用浏览器；
2. 创建 `BrowserSession`；
3. 打开 IDE；
4. 调用具体测试用例的 `executeCase()`；
5. 根据配置决定是否关闭浏览器。

公共入口是：

```java
public final void start()
```

使用 `final` 是为了避免子类绕过统一的浏览器初始化流程。具体用例只需要实现：

```java
protected abstract void executeCase(BrowserSession session);
```

示例：

```java
@Service("case_opcua_01")
public class opcua_01 extends AbstractBrowserCase {
    @Override
    protected void executeCase(BrowserSession session) {
        opcua_atom actions = new opcua_atom(session.getDriver());

        actions.initialization();
        actions.opcuaInitial();
        actions.loadingVriables();
    }
}
```

这样，`opcua_01` 只负责描述自己的测试步骤，不需要重复编写浏览器创建、页面打开和资源关闭代码。

## 6. 原子操作层

`atomManipulation` 是通用原子操作集，放在 `protocol` 包下，供不同测试类型复用。它的构造方法接收当前用例的 `WebDriver`：

```java
public atomManipulation(WebDriver driver) {
    this.driver = Objects.requireNonNull(driver, "driver 不能为空");
}
```

通用原子操作可以包括：

- 点击元素；
- 输入文本；
- 等待元素出现；
- 读取页面状态；
- 选择树节点；
- 上传或下载文件。

`opcua_atom` 继承通用原子操作，并增加 OPC UA 专属动作：

```java
public class opcua_atom extends atomManipulation {
    public opcua_atom(WebDriver driver) {
        super(driver);
    }
}
```

例如端口配置、订阅配置、安全策略和证书配置等，都属于 OPC UA 专属操作，应放在 `opcua_atom` 中。

测试用例类负责组合这些动作：

```text
opcua_01
    ├── 调用通用初始化
    ├── 调用 OPC UA 初始化
    ├── 调用端口配置
    └── 调用订阅配置
```

原子操作类负责“一个动作怎么做”，测试用例类负责“动作按什么顺序组合”。

## 7. 配置文件

默认配置放在：

```text
src/main/resources/application.properties
```

示例：

```properties
# 优先使用的浏览器：CHROME 或 EDGE
plc.browser=CHROME

# 测试结束后是否保持浏览器打开
plc.browser.keepOpen=true

# Baosky IDE 地址
plc.ide.url=http://localhost:8080
```

当前公共用例基类通过 Spring 的 `@Value` 读取这些配置。后续前端可以传入本次运行的浏览器、IDE 地址和保持窗口选项，前端参数应覆盖配置文件中的默认值。

推荐的配置优先级为：

```text
前端本次请求参数 > application.properties > 代码默认值
```

## 8. 新增测试用例

新增 OPC UA 用例时：

1. 在 `protocol/OPCUA` 下创建新的用例类；
2. 继承 `AbstractBrowserCase`；
3. 添加 Spring `@Service` 名称；
4. 在 `executeCase()` 中创建对应的原子操作对象；
5. 按测试步骤组合原子操作。

示例：

```java
@Service("case_opcua_03")
public class opcua_03 extends AbstractBrowserCase {
    @Override
    protected void executeCase(BrowserSession session) {
        opcua_atom actions = new opcua_atom(session.getDriver());
        actions.initialization();
        actions.opcuaInitial();
        // 添加用例 03 的专属步骤
    }
}
```

新增其他测试类型时，在 `protocol` 下创建对应目录，例如：

```text
protocol
├── OPCUA
├── Modbus
└── Profinet
```

不同测试类型可以拥有各自的 `xxx_atom`，同时复用 `atomManipulation`、`BrowserSession` 和 `AbstractBrowserCase`。

