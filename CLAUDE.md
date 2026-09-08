# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Needle 是各类会员接口充值服务（WeChat 生态的会员订阅/充值系统骨架），含四个 Gradle 模块：

- **Needle-core** — 核心库（plain jar，`bootJar` 已禁用）：DAO 层（MyBatis mapper + Generator 生成的 model）与通用 BO 层，供另外三个工程依赖
- **Needle-api-server** — 公共平台接口服务，供外部厂商调用
- **Needle-bui-server** — 管理后台接口服务，供内部管理平台前端调用
- **Needle-job** — 轮训服务（Spring @Scheduled 定时任务）

**注意：本仓库是从生产系统裁剪出的骨架工程**（见提交记录"去掉不需要的代码"、"更换包名"）：大量 controller/BO/定时任务方法体为空或被注释。遇到空方法体不要当作 bug 去"修复"，如需恢复业务逻辑先与用户确认。

## 构建与运行

**构建环境：Gradle 8.14.2 + JDK 17**（2025-09 从 Gradle 6.7/Boot 2.1.2 迁移而来）。可用 `./gradlew <task>`（wrapper 已同步 8.14.2）或本地 `/Users/bpascal/tools/gradle-8.14.2/bin/gradle`；JDK 17 位于 `/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home`。Java 8 目标（sourceCompatibility 保持 1.8），Spring Boot 2.7.18（最后一个 javax 命名空间版本，支持 JDK 17）。

```bash
./gradlew build                                # 全量构建
./gradlew :Needle-api-server:build             # 单模块构建（其余同理）
./gradlew :Needle-core:mybatisGenerator        # 重新生成 model/mapper（配置：Needle-core/src/main/resources/tools/NeedleGeneratorConfig.xml）
```

- **测试**：所有模块均无 `src/test`，无可运行的测试
- **运行**：三个启动类分别为 `NeedleApiApplication` / `NeedleBuiApplication` / `JobApplication`；配置 profile 为 dev（默认）/uat/ptc；dev 端口：api 8848、bui 8858、job 8089
- **容器**：三个可运行模块均排除 Tomcat、使用 Jetty（`configurations.all { exclude module: "spring-boot-starter-tomcat" }`，版本随 Boot BOM）
- **MySQL 驱动坐标为 `com.mysql:mysql-connector-j`**（Boot 2.7.8+ BOM 不再管理旧坐标 `mysql:mysql-connector-java`）
- 仓库仅依赖 mavenCentral（及 MyBatis Generator 插件仓库），无私有仓库/本地 jar

## 架构

### 模块依赖

```
Needle-core (jar)
  ↑ project 依赖
Needle-api-server   Needle-bui-server   Needle-job   （三个独立部署的 Boot 应用，互不依赖）
```

### 分层与代码约定

- 请求链路：`controller`（`@RestController` + Swagger 注解）→ `service`/BO → core 的 MyBatis mapper。BO 为"接口 + `impl`（@Component）"成对出现（如 `CfgBo`/`CfgBoImpl`）
- **api 与 bui 的 `common` 包（BaseReq/BaseResp、切面、异常处理、常量类）是复制粘贴的镜像代码**，修改一侧时注意同步另一侧
- 请求/响应 bean 继承 `BaseReq`/`BaseResp`（含 `tokenId`、`respCode`/`respMsg`）；返回码是字符串常量（"0000"=成功），定义在 `AppConstans`/`BuiConstans`/core 的 `CommonConstans`，非枚举
- AOP：`RequestLogAspect` 为每个请求打 bizId（用于日志串联）；`LoginAspect` 拦截 `@AreYouLogin` 注解做 token 校验；`WebException`（@ControllerAdvice）做全局异常→BaseResp 转换
- MyBatis 全部为注解 SQL（`@Select` 等 + `*SqlProvider`），无 XML mapper；分页用 PageHelper
- 包名按 3 字母业务域前缀组织（`ord` 订单、`usr` 用户、`opr` 运营、`cfg` 配置）；工具类以 `JB` 为前缀（`JBUtil`、`JBDateUtil`）

### 数据模型（db: needle）

| 表 | 模型 | 含义 |
|---|---|---|
| `t_pub_param` | `PubParam` | 全局 key/value 配置参数 |
| `t_cfg_channel` | `CfgChannel` | 上游充值渠道（含 `orderActionUrl`/`orderQueryUrl` 下单与查询地址、余额） |
| `t_cfg_agent` | `CfgAgent` | 下游代理商/厂商（含 `agentIpAddress` IP 白名单、余额） |
| `t_cfg_offer` | `CfgOffer` | 上游产品/Offer |

### 外部集成

微信公众号/支付（binarywang weixin-java-mp/pay，api 与 bui 用 2.9.0，job 用 3.7.0 starter）、创蓝/SUBMAIL 短信、七牛云存储、上游充值渠道（HttpClientUtil + 渠道配置的 URL）。

### 定时任务（Needle-job）

纯 Spring `@Scheduled`（`ScheduleConfig` 提供 20 线程池），任务集中在 `SubIntScheduling`：每日 00:00:30 生成自动续费订单、每日 12:00 推送即将过期订阅。

## 其他注意事项

- 注释/提交信息为中文，提交信息格式形如 `@bpascal@描述`
- 依赖存在混用版本（core 中 dom4j 声明了两个版本、`log4j:log4j:+` 动态版本等）——除非用户要求，不要主动"升级/统一"依赖版本
- springfox 2.9.2 与 Boot 2.6+ 的兼容依赖 `application.yml` 中的 `spring.mvc.pathmatch.matching-strategy: ant_path_matcher`，勿删除该配置
