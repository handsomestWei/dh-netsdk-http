# dh-netsdk-http
大华`netsdk` `http`服务。目前提供门禁相关接口。基于`cursor + vibe coding`模式开发，不再需要关注jna对象内存和指针等使用细节。

## 简介
对厂商原有的设备sdk dll二次封装，暴露http接口，提供给外部系统调用。
+ 屏蔽底层开发语言的差异，上层系统对接大华设备不再需要参照对应的开发语言demo去实现。
+ http服务独立进程部署，而非集成的方式整合到原有业务，避免厂商dll自身原因引发如内存泄漏等问题带崩主业务。

netsdk版本：`General_NetSDK_ChnEng_JAVA_Win64_IS_V3.060.0000000.0.R.250417`

## 接口文档
从启动类[NetSdkApplication](src/main/java/com/netsdk/demo/http/NetSdkApplication.java)启动服务后，访问`http://localhost:8090/dh-netsdk/swagger-ui.html`查看接口文档和在线调试。

## http接口列表
<details>
<summary>点击展开/收起接口列表</summary>

| URL | 接口名称 | 说明 |
|-----|----------|------|
| POST /gate/insertCard | 添加卡 | 支持多门通道 |
| POST /gate/insertCardByService | 服务方式添加卡 | 无需传门禁通道和有效期，人员侧已绑定 |
| POST /gate/findCardList | 查询卡片列表 | 支持按卡号和用户ID查询 |
| GET /gate/getAccessChannelCount | 获取门禁通道数量 | 返回门禁通道数量 |
| POST /gate/getAccessChannelInfo | 获取单个门禁通道信息 | 根据通道号获取门禁通道信息 |
| POST /gate/getAllAccessChannelInfo | 获取所有门禁通道信息 | 根据通道数量获取所有门禁通道信息 |
| POST /gate/getOpenDoorRecords | 分页查询开门记录 | 带分页参数，返回开门记录列表 |
| POST /gate/getOpenDoorRecordCount | 获取开门记录总数 | 根据条件统计开门记录总数 |
| POST /gate/openDoor | 开门 | 门禁控制：开门 |
| POST /gate/closeDoor | 关门 | 门禁控制：关门 |
| POST /gate/alwaysOpenDoor | 常开 | 门禁控制：常开 |
| POST /gate/alwaysCloseDoor | 常闭 | 门禁控制：常闭 |
| POST /gate/addOrUpdateUser | 新增或修改用户 | 新增或修改门禁用户 |
| POST /gate/deleteUser | 删除用户 | 根据用户ID删除门禁用户 |
| POST /gate/clearAllUsers | 清空所有用户 | 清空门禁设备所有用户 |
| POST /gate/getUserRecords | 查询用户信息列表 | 分页查询门禁用户信息 |
| POST /gate/getUserRecordsCount | 查询用户总数 | 查询门禁用户总数 |

</details>

## http调用说明
端口`8090`，url前缀`/dh-netsdk`，可在`application.yml`中调整。
- **请求头参数（所有接口均需）：**
  | Header | 说明 | 必填 |
  |--------|------|------|
  | Dh-Device-Ip | 设备IP | 是 |
  | Dh-Device-Password | 设备登录加密密码串（AES加密，带时间戳，详见下方） | 是 |
  | Dh-Device-Timestamp | 当前请求时间戳（与加密串一致），微秒 | 是 |
  | Dh-Device-User | 设备登录用户名，默认admin | 否 |
  | Dh-Device-Port | 设备端口，默认37777 | 否 |

- **加密说明：**
  - 密码传输采用AES加密，密钥由配置文件`crypto.aes-key`指定，前后端需保持一致。
  - 加密内容为`password|timestamp`，即设备登录明文密码与当前请求时间戳用英文竖线`|`拼接后加密（如：`pwd|1688888888888`），详见`CryptoUtill`类。
  - 每次请求的加密串唯一，服务端用Set防重放。
  - 时间戳有效期由`device.timestamp.expire-ms`配置，默认5分钟。

- **接口响应结构：**
  - 所有接口统一返回`CommonResponse<T>`结构，`success`表示是否成功，`message`为提示信息，`data`为实际数据。

## http服务设计说明
包路径：`com.netsdk.demo.http`

- **双模式（守护进程/worker进程）机制简介**

  为提升服务高可用性，系统支持“守护进程+业务进程”双模式启动：

  - **守护进程模式**（推荐）：通过 `java -jar xxx.jar guard` 启动，主进程自动拉起并监控业务进程（worker），worker异常退出时自动重启。

  - **业务进程模式**：通过 `java -jar xxx.jar` 启动，仅运行业务逻辑，无自恢复能力。

  守护进程模式适用于JNA/JNI DLL崩溃等场景，可防止服务假死、线程挂起，实现自动自愈。Docker部署时建议ENTRYPOINT为 `java -jar xxx.jar guard`，确保容器内服务可持续运行。

- **控制器基类**：
  - 统一处理设备登录、接口安全。
  - 自动从请求头获取设备信息，自动登录并释放资源，子类只需专注业务。
  - 登录句柄通过`ThreadLocal`线程上下文隔离，每个请求独立，彻底线程安全。
- **接口安全**：
  - 避免密码明文传输，采用AES加密+时间戳，防止重放攻击。
  - 所有接口需带唯一加密串，服务端防重放。
- **线程安全**：
  - 所有SDK操作均用本线程独立登录句柄，支持高并发。

## 服务打包与启动说明
参考工程目录下的`package.bat`脚本。
- **打包桌面版（GUI）**：
  ```sh
  mvn clean package -Pgui -DskipTests
  ```
  生成主类为`com.netsdk.demo.frame.Main`的`dh-netsdk-gui.jar`。

- **打包 Spring Boot 服务版（HTTP）**：
  ```sh
  mvn clean package -Phttp -DskipTests
  ```
  生成主类为`com.netsdk.demo.http.NetSdkApplication`的`dh-netsdk-http.jar`。

- **启动示例**：
参考工程目录下的`run_gui_win.bat`和`run_http_win.bat`脚本。

## 其他扩展
### 窗体功能扩展
包路径：`com.netsdk.demo.frame.Gate.ext`
+ 扩展：门禁通道信息
+ 扩展：门禁用户管理
+ 扩展：门禁卡管理
+ 扩展：开门记录查询

### 模块扩展
包路径：`com.netsdk.demo.module.ext`

## 附：sdk调用常见问题
### 接口调用异常：无效内存访问Invalid memory access
- 原因：JNA的.size()只根据Java结构体推算，但如果结构体有指针字段（如 Pointer），JNA只分配指针本身的大小，不会递归分配指针指向的内容。C端如果期望对象是“全内存展开”，而你只分配了指针本身，native端访问指针内容时会发生“Invalid memory access”
- 解决方案：所有指针字段必须置为 NULL，不能让 native 端去解引用
- 示例代码：
  ```java
  com.sun.jna.Memory mem = new com.sun.jna.Memory(userInfoSize * thisBatch);
  mem.clear(); // 清零，确保所有指针字段为0
  outDo.pstuInfo = mem;
  inDo.write();
  outDo.write();
  boolean ret = com.netsdk.demo.module.LoginModule.netsdk.CLIENT_DoFindUserInfo(findHandle, inDo, outDo, 5000);
  ```
### 接口调用失败：(0x80000000|7) 用户参数不合法
- 原因：部分接口调用较严格，除了填充接口入参外，还需要完整的定义接口出参去接返回数据，否则调用失败。
- 示例代码：
  ```java
  outParam.nMaxRetNum = inParam.nInfoNum;
  NetSDKLib.FAIL_CODE[] failCodes = (NetSDKLib.FAIL_CODE[]) new NetSDKLib.FAIL_CODE().toArray(inParam.nInfoNum);
  outParam.pFailCode = failCodes[0].getPointer();
  for (int i = 0; i < inParam.nInfoNum; i++) {
      failCodes[i].write();
  }
  // write写入内存之前，需要定义完整的出参outParam，不能偷懒
  outParam.write();
  boolean ret = LoginModule.netsdk.CLIENT_OperateAccessUserService(loginHandle, NetSDKLib.NET_EM_ACCESS_CTL_USER_SERVICE.NET_EM_ACCESS_CTL_USER_SERVICE_INSERT, inParam.getPointer(), outParam.getPointer(), 5000);
  ```

## 第三方依赖与License
本项目依赖的第三方库及其License详见`doc/`目录下的License文件：
- [Open Source Software Licenses-NetSDK_Java.txt](doc/Open%20Source%20Software%20Licenses-NetSDK_Java.txt)
- [Open Source Software Licenses-PlaySDK.txt](doc/Open%20Source%20Software%20Licenses-PlaySDK.txt)
- [Open Source Software Licenses-StreamConvertor.txt](doc/Open%20Source%20Software%20Licenses-StreamConvertor.txt)

本项目仅调用/链接上述第三方库，未对其源码进行修改。所有第三方库的版权声明和License均已完整保留。
