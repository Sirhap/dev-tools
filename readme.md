# 组件说明

解决本地开发微服务时面临的一些问题：

1. 本地微服务启动时默认会注册至 dev，但是dev环境很可能被别人正在使用，你本地的代码不是稳定版本，服务注册上去后，可能被其他人调用失败，影响其他人联调 。指定了agent 后
   **默认不注册至dev 环境的Eureka，但仍可调用dev环境的服务**
2. 我们微服务 RPC 调用有可能没有打印请求参数和返回参数，异常时缺少信息排查，以往经验来看，需要debug或者补相关日志。指定了 agent 后
   **会以调用发起方的角度，将请求参数、请求的具体接口地址、响应参数、接口耗时都打印出来，根据这些信息能快速定位错误，另外复制这些信息可直接用POSTMAN再次调用**
3. 本地微服务启动后，如果本地服务存在 rabbit mq consumer，默认会消费 `dev` 环境的消息；如果其他人有 mq 消息相关的功能在 dev 联调，消息可能会被你误消费，会影响其他人联调；指定了
   agent 后
   **默认不会再消费rabbit mq消息； 如有需要，可在配置文件中配置为消费**
4. **可以单独为每个被调用服务指定环境，或者指定具体的地址；**比如 调用sino-island时指定调用dev环境，调用ams-center-server指定调用本地，这样本地可以少启动依赖的微服务实例，也可以进行联调；

ps:
1、指定微服务rpc路由只支持统一注册中心的，对于前台服务通过gateway的方式调用中后台的暂无法支持。
2、rabbitmq需要进行过接入架构组MQ SDK 改造后才可以支持


# 修改记录

|版本号| 修改人    | 日期   | 修改描述 |
|-----|--------| ----| --- |
|1.0.0-SNAPSHOT| Sirhao |2022-2-10 | 初始提交 |
|1.1.0-SNAPSHOT| Sirhao |2022-2-10 | 添加mq消费开关 |

# 使用说明

启动服务的 jvm 参数中指定 -javaagent 为本工程生成的 jar以及配置文件即可：

```
-javaagent:/opt/tmp/dev-tools-agent.jar 
-DdevToolsConf=/opt/tmp/dev-tools-agent.properties
```

配置文件位置：`/opt/tmp/dev-tools-agent.properties`，首次启动时会生成配置文件，如果启动参数devToolsConf不设定，默认会是使用路径/opt/tmp/dev-tools-agent.properties

# 注意事项

此 agent 是基于javassist 工具来修改相关组件的字节码，为了方便使用，工程的输出文件只有一个jar，当相关组件(如Feign, Eureka，RabbitMQ)升级时可能会出现不兼容的情况，需要单独开发来做适配。

# 配置文件

```properties
# ----------------------------------------------------------------------------------------------------------------------
# 以下配置的修改，保存即可生效，无需重启

# 指定服务的实例，以下为示例格式 （不指定的，将走当前连接的注册中心）
# 以下表示：全局默认路由至 dev, 不配置时，默认路由至本地启动服务所注册在的 eureka
service.globalMapping=@dev

# 比如我需要修改： sino-island、ams-center-server、sino-optimization-server 三个服务，调用链路依赖SINO-XRAY服务不修改使用全局dev配置，可以像如下三行配置：
# sino-island 路由至本地
#service.mappings.sino-island=127.0.0.1:9090

# ams-center-server 路由至本地
#service.mappings.ams-center-server=127.0.0.1:8111

# sino-optimization-server 路由至本地
#service.mappings.SINO-OPTIMIZATION-SERVER=127.0.0.1:8098


# feign rpc 日志，提高开发时的排查问题的效率
rpc.printLog=true

# eureka 服务
eureka.server.dev=http://192.168.0.40:7081/
eureka.server.test=http://192.168.0.77:7081/
eureka.server.test2=http://192.168.12.30:7081/
eureka.server.test3=http://192.168.12.33:7081/

# 缓存服务实例的时间（避免每次查询 eureka 服务）
eureka.cacheInstanceMinutes=30

# ----------------------------------------------------------------------------------------------------------------------
# 以下配置的修改，需要重启生效

# 不注册到服务中心，避免被错误调用
registerWithEureka=false
# 禁用mq消费，避免本地环境消费其他环境消息
disableRabbitmqConsumer=true
```