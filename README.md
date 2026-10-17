# Observerzone

适用于 **Minecraft 1.21.11 / Fabric** 的侦测器区域管理 Mod。管理员可以用命令划定长方体区域，阻止区域内的侦测器发出新的红石脉冲，适合需要限制侦测器触发的建筑或服务器区域。

逻辑在服务端执行：多人服务器安装后，玩家客户端无需安装；单人游戏可安装到本地 Fabric 实例。

## 下载与安装

| 文件 | 用途 |
| --- | --- |
| [observerzone-1.0.0.jar](releases/observerzone-1.0.0.jar) | 可直接安装的 Mod |
| [observerzone-1.0.0-sources.jar](releases/observerzone-1.0.0-sources.jar) | 源码归档，不用于安装 |

环境要求：Minecraft **1.21.11**、Fabric Loader **0.19.5 或以上**、Java **21 或以上**，并安装适配该 Minecraft 版本的 **Fabric API**。项目使用的 Fabric API 版本为 `0.141.6+1.21.11`。

将 Mod JAR 和 Fabric API 放入服务器或单人游戏实例的 `mods/` 目录，然后启动游戏。使用命令需要 OP 权限等级 2（或以上）；单人游戏需允许作弊。

## 命令

| 命令 | 作用 |
| --- | --- |
| `/observerzone add <名称> <x1> <y1> <z1> <x2> <y2> <z2>` | 使用两个对角点创建禁用区域 |
| `/observerzone remove <名称>` | 删除区域，支持已有名称补全 |
| `/observerzone list` | 列出区域名称、维度及边界坐标 |

名称不含空格，且所有维度之间不能重名。坐标可使用绝对坐标或 `~` 相对坐标；两个角点的顺序不限，范围包含边界和 Y 轴高度。

快速体验：在游戏中依次执行以下命令。第一条会在当前位置周围创建一个 **11 × 11 × 11** 的区域。

```mcfunction
/observerzone add test ~-5 ~-5 ~-5 ~5 ~5 ~5
/observerzone list
/observerzone remove test
```

例如，在当前位置所在维度划定一个固定范围：

```mcfunction
/observerzone add spawn 100 60 100 150 90 150
```

区域属于**执行命令时所在的维度**。在下界或末地创建区域时，应进入对应维度再执行，也可使用 `/execute in <维度> run observerzone add ...`。服务器控制台执行命令时省略开头的 `/`，建议使用绝对坐标；需要明确维度时同样使用 `execute in`。

## 工作方式与数据保存

- 按侦测器自身的位置判断是否处于禁用区域，而不是按它观察的方块位置判断。
- 通过 Mixin 拦截侦测器计划刻中由未充能变为充能的步骤。已经亮起的侦测器仍可正常熄灭，避免卡在常亮状态。
- 仍可放置、拆除侦测器；区域外的侦测器和其他红石元件保持原有行为。
- 创建、删除区域时立即写入存档根目录的 `observer_zones.json`，启动时自动加载，重启后保留。开发环境默认位置为 `run/world/observer_zones.json`。
- 迁移世界时一并保留该 JSON 文件。需要手动修改时，先停止服务器再编辑。

若侦测器仍会触发，先用 `list` 确认区域的维度、坐标和高度，并检查**侦测器本体**是否落在区域内。删除区域后，下一次正常触发会恢复脉冲输出。

## 从源码构建

本目录包含 Gradle Wrapper、Java 源码和 Fabric 资源文件。进入本目录后执行：

```powershell
# Windows
.\gradlew.bat build
```

```sh
# Linux / macOS
chmod +x gradlew
./gradlew build
```

构建目标为 Java 21 字节码；本项目的 Gradle Wrapper 为 9.7.1，本次整理使用 JDK 25。首次构建需要联网下载 Gradle、Fabric Loom 和游戏依赖。产物位于 `build/libs/`：安装不带 `-sources` 的 JAR。

主要实现文件：

- `src/main/java/com/example/observerzone/Observerzone.java`：命令注册和服务端生命周期。
- `src/main/java/com/example/observerzone/mixin/ZoneManager.java`：区域范围、维度判断和 JSON 持久化；该文件声明的 Java 包为 `com.example.observerzone`。
- `src/main/java/com/example/observerzone/mixin/ObserverBlockMixin.java`：拦截区域内侦测器的激活。

## 项目记录

项目基于 Fabric 模板开发。根据原有 Claude `observerzone` 项目对话，开发过程包括区域命令、持久化、侦测器激活拦截、开发服务器运行及打包；2026 年 9 月 28 日，作者在该对话中反馈“测试没有问题”。本次上传结合这些记录及实际源码补充了使用说明。

`releases/` 保留原项目已有的 1.0.0 成品及源码 JAR。构建缓存、开发服务器存档、日志和早期重复的 ZIP/Java 文件不纳入仓库。

## 许可

保留项目原有的 [CC0-1.0 许可](LICENSE)。

## 仓库迁移

本项目于 2026-10-17 从 [Yido404/MC_mod 的 Observerzone](https://github.com/Yido404/MC_mod/tree/main/Observerzone) 拆为独立仓库。此前的提交历史保留在原仓库，后续开发使用本仓库。
