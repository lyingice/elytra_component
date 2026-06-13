# 🦅 鞘翅组件 (Elytra Component) 完全教程

> **Elytra Component** 是一个 Minecraft NeoForge 模组，允许你将任何注册的鞘翅**安装到胸甲**上，实现鞘翅与胸甲的功能合并。  
> 本教程面向**普通玩家**和**整合包/数据包作者**。

---

## 📋 目录

1. [什么是鞘翅组件？](#-什么是鞘翅组件)
2. [玩家篇：如何使用](#-玩家篇如何使用)
   - [安装鞘翅](#-安装鞘翅)
   - [拆卸鞘翅](#-拆卸鞘翅)
   - [修复耐久](#-修复耐久)
   - [查看信息](#-查看信息)
   - [调试命令](#-调试命令)
   - [JEI 查询](#-jei-查询)
3. [数据包作者篇：注册鞘翅组件](#️-数据包作者篇注册鞘翅组件)
   - [文件位置与格式](#-文件位置与格式)
   - [完整 JSON 字段参考](#-完整-json-字段参考)
   - [完整示例](#-完整示例)
   - [最佳实践与注意事项](#-最佳实践与注意事项)
4. [模组开发者篇：代码注册 API](#-模组开发者篇代码注册-api)

---

## 🧩 什么是鞘翅组件？

本模组引入了一个核心概念——**鞘翅组件 (Elytra Component)**。简单来说：

1. 你有一个**胸甲**（任何胸甲均可，钻石、下界合金、自定义模组胸甲等）。
2. 你有一个**鞘翅**（原版鞘翅或任何其他模组添加的鞘翅，如暮色森林的炽焰鞘翅）。
3. 通过本模组，你可以将鞘翅**安装**到胸甲上，胸甲获得飞行能力。
4. 鞘翅的**耐久、附魔、自定义名称**等数据会被完整保留，拆卸时**100% 还原**。
5. 安装后，胸甲会渲染对应的鞘翅纹理，视觉上也有鞘翅翅膀。

---

## 🎮 玩家篇：如何使用

### 🔧 安装鞘翅

**合成表（手持操作，无合成台）：**

| 条件 | 物品 | 位置 |
|------|------|------|
| 主手 | 鞘翅物品（如 `minecraft:elytra`） | 主手 |
| 副手 | 粘液球 (`minecraft:slime_ball`) | 副手 |
| 身上 | 任意胸甲 | 胸甲槽 |

**步骤：**
1. 确保你穿着胸甲。
2. 主手持鞘翅，副手持粘液球。
3. **右键空气/方块/实体** → 鞘翅会被安装到胸甲上。
4. 你会看到龙息粒子效果，听到粘液挤压音效，并收到成功提示。

> 💡 **创造模式**：粘液球不会被消耗。

### ✂️ 拆卸鞘翅

**方法：**
1. 将安装了鞘翅组件的胸甲放在**副手**。
2. **右键空气**。
3. 鞘翅会被完整拆卸并返还到你的背包（如果背包满则掉落在地上）。

> 拆卸时保留：耐久、附魔、自定义名称、所有原版数据组件。

### 🔨 修复耐久

**方法：**
1. 穿着安装了鞘翅组件的胸甲。
2. **主手持幻翼膜** (`minecraft:phantom_membrane`)。
3. **右键空气/方块/实体**。
4. 每张幻翼膜恢复 **108 点**鞘翅组件耐久。
5. 你会听到幻翼拍打音效，看到灵魂粒子效果。

> 满耐久时右键会提示"不需要修复"。

### ℹ️ 查看信息

将安装了鞘翅组件的胸甲悬停在物品栏中，会在 **Tooltip**（物品提示）中显示：
- ✅ 已安装鞘翅组件标识
- ⚡ 当前耐久 / 最大耐久
- 📦 来源模组信息

### 🖥️ 调试命令

所有命令需要 **OP 权限（等级 2）**，前缀为 `/ecs`：

| 命令 | 说明 | 示例 |
|------|------|------|
| `/ecs attach <鞘翅ID> [玩家]` | 为目标玩家的胸甲安装指定鞘翅 | `/ecs attach minecraft:elytra` |
| `/ecs detach [玩家]` | 从目标玩家胸甲拆卸鞘翅 | `/ecs detach @p` |
| `/ecs info [玩家]` | 查看目标玩家胸甲的组件详细信息 | `/ecs info @p` |
| `/ecs durability <数值> [玩家]` | 设置组件耐久值（不会超过最大值） | `/ecs durability 500 @p` |
| `/ecs give <组件ID> [玩家]` | 给予玩家一个指定组件的测试鞘翅 | `/ecs give twilightforest:fiery_elytra` |
| `/ecs list` | 列出所有已注册的鞘翅组件 | `/ecs list` |
| `/ecs reload` | 提示使用 `/reload` 重新加载数据包 | `/ecs reload` |

> ⚠️ **注意**：修改数据包后请使用 `/reload` 命令，而不是 `/ecs reload`。

### 🔍 JEI 查询

如果安装了 **JEI (Just Enough Items)**，在 JEI 中搜索任意已注册的鞘翅物品：
- 点击信息标签页（"i" 图标）
- 可以看到所有可安装的鞘翅组件列表
- 包含：组件 ID、来源物品、耐久信息、纹理路径、标签、模组依赖

---

## 🛠️ 数据包作者篇：注册鞘翅组件

这是本模组最强大的功能——通过**数据包**注册任意模组中的鞘翅为可用组件，完全无需编写代码！

### 📁 文件位置与格式

```
<数据包根目录>/
└── data/
    └── <你的命名空间>/
        └── elytra_components.json    ← 固定文件名
```

**命名空间说明：**
- `<你的命名空间>` 可以是任何小写字母、数字、下划线组合，如 `mypack`、`config`、`custom`。
- 建议使用你的数据包名称作为命名空间，避免冲突。
- 如果有多个数据包加载了同路径文件，**它们会合并**——所有文件中的 `elytra_components` 数组都会被读取。

### 📝 完整 JSON 字段参考

```json
{
  "elytra_components": [
    {
      "component_id": "必填，唯一标识符",
      "elytra_item": "必填，鞘翅物品的 ResourceLocation",
      "texture": {
        "elytra_layer": "可选，鞘翅纹理路径",
        "elytra_layer_glow": "可选，发光纹理路径",
        "elytra_layer_overlay": "可选，叠加层纹理路径"
      },
      "durability": {
        "base": 432,
        "multiplier": 1.0,
        "max": 0
      },
      "render": {
        "tint_color": "可选，染色颜色 HEX 值",
        "has_glow": false,
        "glow_color": "可选，发光颜色 HEX 值"
      },
      "compatibility": {
        "required_mods": ["可选，需要加载的模组列表"],
        "incompatible_with": ["可选，不兼容的组件 ID 列表"]
      },
      "tags": ["可选，功能标签列表"]
    }
  ]
}
```

---

#### 字段详解

##### `component_id`（必需，字符串）
- 组件唯一标识符，用于在 JEI 和命令中引用。
- 建议格式：`<命名空间>:<名称>`，如 `twilightforest:fiery_elytra_component`。
- 不同数据包之间**允许同名**，后加载的同名定义会**覆盖**先加载的。
- **不过**：模组通过 Java 代码注册的定义（优先级最高）不会被数据包覆盖。

##### `elytra_item`（必需，字符串）
- 鞘翅物品的完整 ID，格式 `<命名空间>:<物品路径>`。
- 常见示例：
  - `minecraft:elytra` — 原版鞘翅
  - `twilightforest:fiery_elytra` — 暮色森林炽焰鞘翅
  - `deeperdarker:soul_elytra` — 更深黑暗灵魂鞘翅
- 如果该物品不存在（模组未安装），该定义会被**安全跳过**，不会导致崩溃。

##### `texture`（可选，对象）

控制鞘翅安装到胸甲后渲染的纹理：

| 字段 | 类型 | 说明 |
|------|------|------|
| `elytra_layer` | 字符串 | 鞘翅主纹理路径，如 `twilightforest:textures/entity/fiery_elytra.png` |
| `elytra_layer_glow` | 字符串 | 发光纹理路径，用于发光的鞘翅纹理层 |
| `elytra_layer_overlay` | 字符串 | 叠加纹理路径，叠加在鞘翅上层的纹理 |

> 💡 **如果留空（不提供 texture）**，模组会自动推算纹理路径：  
> `<namespace>:textures/item/<path>.png`  
> 例如 `twilightforest:fiery_elytra` → `twilightforest:textures/item/fiery_elytra.png`  
> 推算的路径如果不存在，会回退到原版默认鞘翅纹理。

##### `durability`（必需，对象）

控制鞘翅组件的耐久系统：

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `base` | 整数 | `432` | 基础耐久值（默认等于原版鞘翅 432 点） |
| `multiplier` | 浮点数 | `1.0` | 耐久倍率，`base × multiplier` 为计算耐久 |
| `max` | 整数 | `0` | 最大耐久上限。`0` 表示无上限（使用 Integer.MAX_VALUE）。若原鞘翅自身耐久大于计算值，取原鞘翅耐久。 |

**耐久计算公式：**
```
最终耐久 = min(max(base × multiplier, 原鞘翅耐久), max上限)
```

**示例：**
- `base: 432, multiplier: 1.0, max: 0` → 432 点耐久（标准）
- `base: 200, multiplier: 1.5, max: 0` → 300 点耐久
- `base: 500, multiplier: 1.0, max: 1000` → 最多 1000 点
- 如果原鞘翅自身有 800 点耐久，而计算值为 432，则取 800

##### `render`（可选，对象）

控制渲染效果：

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `tint_color` | 字符串 | `null` | 染色颜色，HEX 格式如 `"#FF6600"`（暂未实现染色渲染） |
| `has_glow` | 布尔值 | `false` | 是否渲染发光效果 |
| `glow_color` | 字符串 | `null` | 发光颜色，HEX 格式如 `"#00FF00"` |

##### `compatibility`（可选，对象）

控制兼容性检查：

| 字段 | 类型 | 说明 |
|------|------|------|
| `required_mods` | 字符串数组 | 该组件需要哪些模组加载才能使用。**当前为信息性字段**，主要用于 JEI 显示。如 `["twilightforest"]` |
| `incompatible_with` | 字符串数组 | 与哪些组件 ID 不兼容（暂未实现运行时检查） |

##### `tags`（可选，字符串数组）

功能标签列表，供其他模组或未来功能使用。当前主要用于 JEI 显示。
例如：`["magic", "fire", "soul"]`

---

### 🎯 完整示例

#### 示例 1：注册原版鞘翅

```json
{
  "elytra_components": [
    {
      "component_id": "vanilla:elytra",
      "elytra_item": "minecraft:elytra",
      "durability": {
        "base": 432,
        "multiplier": 1.0
      },
      "render": {
        "has_glow": false
      },
      "tags": ["vanilla"]
    }
  ]
}
```

#### 示例 2：注册暮色森林炽焰鞘翅（带自定义纹理）

```json
{
  "elytra_components": [
    {
      "component_id": "twilightforest:fiery_elytra_component",
      "elytra_item": "twilightforest:fiery_elytra",
      "texture": {
        "elytra_layer": "twilightforest:textures/entity/fiery_elytra.png"
      },
      "durability": {
        "base": 600,
        "multiplier": 1.0,
        "max": 1200
      },
      "render": {
        "has_glow": true,
        "glow_color": "#FF4400"
      },
      "compatibility": {
        "required_mods": ["twilightforest"]
      },
      "tags": ["fire", "magic", "twilight"]
    }
  ]
}
```

#### 示例 3：注册更深黑暗的灵魂鞘翅

```json
{
  "elytra_components": [
    {
      "component_id": "deeperdarker:soul_elytra_component",
      "elytra_item": "deeperdarker:soul_elytra",
      "texture": {
        "elytra_layer": "deeperdarker:textures/entity/soul_elytra.png",
        "elytra_layer_glow": "deeperdarker:textures/entity/soul_elytra_glow.png"
      },
      "durability": {
        "base": 500,
        "multiplier": 1.2,
        "max": 1000
      },
      "render": {
        "has_glow": true,
        "glow_color": "#00AAFF"
      },
      "compatibility": {
        "required_mods": ["deeperdarker"]
      },
      "tags": ["soul", "sculk"]
    }
  ]
}
```

#### 示例 4：完整的多组件注册

```json
{
  "elytra_components": [
    {
      "component_id": "mypack:nether_elytra",
      "elytra_item": "minecraft:elytra",
      "durability": {
        "base": 864,
        "multiplier": 1.0,
        "max": 2000
      },
      "render": {
        "has_glow": true,
        "glow_color": "#FF0000"
      },
      "tags": ["nether", "upgraded"]
    },
    {
      "component_id": "mypack:ender_elytra",
      "elytra_item": "minecraft:elytra",
      "texture": {
        "elytra_layer": "mypack:textures/entity/ender_elytra.png"
      },
      "durability": {
        "base": 648,
        "multiplier": 1.0,
        "max": 1000
      },
      "render": {
        "has_glow": true,
        "glow_color": "#8800FF"
      },
      "tags": ["ender", "rare"]
    }
  ]
}
```

---

### ⚠️ 最佳实践与注意事项

1. **文件路径严格区分大小写**：`elytra_components.json` 必须全小写，没有 s 结尾——是 `elytra_components` 不是 `elytra_component`。

2. **多个数据包可以共存**：不同命名空间下的 `elytra_components.json` 文件都会被加载，所有组件会合并到一个全局注册表中。

3. **优先级规则**：
   - 🥇 **最高**：模组 Java 代码注册（API 注册）
   - 🥈 **中等**：数据包 JSON 文件
   - 同优先级下，后加载的覆盖先加载的

4. **物品不存在不会崩溃**：如果 `elytra_item` 指向一个不存在的物品（模组未安装），该定义会被安全跳过，并在日志中打印警告。

5. **纹理路径**：如果不指定纹理，模组会自动从原鞘翅物品 ID 推算纹理路径 `namespace:textures/item/path.png`。如果推算的纹理不存在，会回退到默认鞘翅纹理。

6. **组件 ID 唯一性**：建议使用 `<模组命名空间>:<描述性名称>` 格式避免冲突。

7. **测试方式**：
   - 放入数据包后，在游戏中执行 `/reload`
   - 然后执行 `/ecs list` 查看是否成功注册
   - 执行 `/ecs give <component_id>` 获取测试物品
   - 穿上胸甲，用测试物品 + 粘液球测试安装

8. **数据包分发**：
   - 将数据包放入存档的 `datapacks/` 目录（仅该存档生效）
   - 或放入服务端的 `world/datapacks/` 目录（服务端生效）
   - 或放入 `.minecraft/datapacks/` 目录（全局生效）

9. **JEI 兼容性**：注册的组件会自动显示在 JEI 信息页面中，无需额外配置。

---

## 👨‍💻 模组开发者篇：代码注册 API

如果你是一个模组开发者，你也可以通过 Java 代码直接注册鞘翅组件定义。  
代码注册的**优先级高于数据包 JSON**，不会被数据包覆盖。

### 核心 API 类

| 类 | 说明 |
|----|------|
| [`ElytraComponentAPI`](src/main/java/net/ec/elytracomponent/api/ElytraComponentAPI.java) | 注册、查询、创建/还原组件的公共 API |
| [`ElytraComponentDefinition`](src/main/java/net/ec/elytracomponent/data/ElytraComponentDefinition.java) | 组件定义 Record，包含所有配置字段 |
| [`ElytraComponent`](src/main/java/net/ec/elytracomponent/component/ElytraComponent.java) | 运行时组件数据，存储在胸甲的 DataComponent 中 |

### 注册示例

```java
import net.ec.elytracomponent.api.ElytraComponentAPI;
import net.ec.elytracomponent.data.ElytraComponentDefinition;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

// 创建纹理信息
var texture = new ElytraComponentDefinition.TextureInfo(
    ResourceLocation.parse("mymod:textures/entity/custom_elytra.png"),  // elytra_layer
    null,  // elytra_layer_glow
    null   // elytra_layer_overlay
);

// 创建耐久信息
var durability = new ElytraComponentDefinition.DurabilityInfo(
    500,   // base
    1.0f,  // multiplier
    2000   // maxDurability (0 = 无上限)
);

// 创建渲染信息
var render = new ElytraComponentDefinition.RenderInfo(
    null,           // tint_color
    true,           // has_glow
    "#FF6600"       // glow_color
);

// 创建兼容性信息
var compatibility = new ElytraComponentDefinition.CompatibilityInfo(
    List.of("mymod"),  // required_mods
    List.of()           // incompatible_with
);

// 创建定义并注册
var def = new ElytraComponentDefinition(
    "mymod:custom_elytra",                    // component_id
    ResourceLocation.parse("mymod:elytra"),    // elytra_item
    texture,                                   // texture (可为 null)
    durability,                                // durability (必需)
    render,                                    // render (可为 null)
    compatibility,                             // compatibility (可为 null)
    List.of("rare", "legendary")               // tags
);

// 注册（优先于数据包 JSON）
ElytraComponentAPI.register("mymod:custom_elytra", def);
```

### 运行时查询

```java
// 检查物品是否是已注册的鞘翅组件源
boolean isRegistered = ElytraComponentAPI.isRegisteredElytra(itemStack);

// 获取胸甲上的组件
ElytraComponent component = ElytraComponentAPI.getComponent(chestplateStack);

// 检查胸甲是否有组件
boolean hasComponent = ElytraComponentAPI.hasComponent(chestplateStack);

// 创建组件实例（在锻造/合成配方中使用）
ElytraComponent newComponent = ElytraComponentAPI.createComponent(elytraStack, def);

// 设置组件到胸甲
ElytraComponentAPI.setComponent(chestplateStack, newComponent);

// 从组件还原鞘翅物品
ItemStack restored = ElytraComponentAPI.restoreElytra(component);

// 移除组件
ElytraComponentAPI.removeComponent(chestplateStack);
```

---

## 📄 许可证

本模组基于 LGPL-3.0-or-later 开源协议发布。

---

*教程版本：v1.0 | 适用模组版本：1.21.1-NeoForge*
