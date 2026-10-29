# 素材制作工作室（全栈笔试题交付）

一个可独立运行的素材制作网站演示：用户浏览模板、上传图片、提交制作任务，后台模拟多步骤异步处理，期间可继续使用网站，最后在历史页查看结果。

**不调用任何真实 AI 服务**：模板、素材与结果均为本地构造/占位；后台用「可控制耗时与成功/失败的模拟生成器」完成业务闭环。重点在于 **Java 后端如何组织多步骤异步处理**，以及 **前端如何管理异步状态**。

---

## 一、技术版本

| 层 | 技术 | 版本 | 说明 |
|---|---|---|---|
| 前端 | Next.js（App Router） | 15.1.0 | React 19、TypeScript 5 |
| 前端 | React / React-DOM | 19.0.0 | |
| 后端 | Spring Boot | 3.5.16 | |
| 后端 | Java | 21（LTS） | `java.version=21`，编译目标 21 |
| 后端 | MyBatis | mybatis-spring-boot-starter 3.0.4 | |
| 数据库 | MySQL | 8.0.39（Docker） | |
| 构建 | Maven Wrapper / npm | — | 后端 Maven Wrapper 免装 Maven；前端 npm |
| 交付 | Docker Compose | — | 前端 / 后端 / MySQL 三服务 |

> 兼容性依据：Spring Boot 3.5 要求 Java ≥17 且官方兼容至 Java 25；Next.js 15 要求 Node ≥18.18。

---

## 二、项目结构

```
.
├── backend/                 # Spring Boot + MyBatis + MySQL
│   ├── pom.xml
│   ├── mvnw / mvnw.cmd       # Maven Wrapper（自举）
│   ├── Dockerfile
│   └── src/
│       ├── main/java/com/example/materialstudio/
│       │   ├── MaterialStudioApplication.java
│       │   ├── config/        # AsyncConfig(虚拟线程池) / MockProperties / StaticResourceConfig(CORS+文件服务)
│       │   ├── domain/        # Template / Task / TaskStatus / TaskStage
│       │   ├── mapper/        # TemplateMapper / TaskMapper (+ XML)
│       │   ├── service/       # TemplateService / TaskService / TaskExecutor(核心状态机) / MockResultWriter
│       │   └── web/           # Controllers / 请求 DTO / 全局异常处理
│       ├── main/resources/
│       │   ├── application.yml
│       │   ├── schema.sql      # 建表（每次启动）
│       │   ├── data.sql        # 种子模板
│       │   ├── mapper/*.xml
│       │   └── static/demo/*.svg  # 本地封面图（可访问文件）
│       └── test/.../service/   # TaskExecutorTest / TaskServiceTest
├── frontend/                # Next.js 15 App Router + TS
│   ├── app/
│   │   ├── layout.tsx
│   │   ├── page.tsx           # 模板列表（UI-01）
│   │   ├── templates/[id]/page.tsx  # 详情 + 上传位（UI-02）+ 制作中（UI-03）
│   │   └── history/page.tsx   # 历史结果（UI-03）
│   ├── components/           # Nav / TemplateCard / UploadSlot / TaskStatusBadge
│   ├── lib/                  # types.ts / api.ts / useTasks.ts(轮询 hook)
│   ├── Dockerfile
│   ├── package.json
│   ├── package-lock.json
│   └── vitest.config.ts
├── db/init.sql               # MySQL 容器初始化（建库+建账号）
├── docker-compose.yml        # 一键编排
└── .github/workflows/ci.yml  # CI：前端 lint+test+tsc+build；后端 test+build
```

---

## 三、构建 / 测试 / 启动

### 方式 A：Docker Compose（推荐，一键全栈）

```bash
docker compose up --build
```

- 前端：http://localhost:3000
- 后端 API：http://localhost:8080
- MySQL：localhost:3306（账号 material / material123）

### 方式 B：本机分步运行（需本地 MySQL 8.0）

1. 准备 MySQL（任选其一）：
   ```bash
   docker run --name ms-mysql -d -p 3306:3306 \
     -e MYSQL_ROOT_PASSWORD=root123 \
     -e MYSQL_DATABASE=material_studio \
     -e MYSQL_USER=material -e MYSQL_PASSWORD=material123 \
     mysql:8.0.39
   ```
   或手动执行 `db/init.sql` 创建库与账号。

2. 启动后端（需 JDK 21，设置 `JAVA_HOME`）：
   ```bash
   cd backend
   ./mvnw spring-boot:run        # 或本机有 mvn 时直接用 mvn spring-boot:run
   ```
   后端启动时会自动执行 `schema.sql` 建表、`data.sql` 灌入种子模板。

3. 启动前端：
   ```bash
   cd frontend
   npm install
   npm run dev                    # http://localhost:3000
   ```
   前端默认调用 `http://localhost:8080`（由 `NEXT_PUBLIC_API_BASE` 控制）。

### 测试

```bash
cd backend && ./mvnw test        # 后端单测：步骤顺序 / 失败中断 / 终态守卫 / 查询只读
cd frontend && npm run test      # 前端行为测试：多任务互不串扰 / 查询异常与失败区分 / 轮询生命周期
cd frontend && npm run lint && npm run build   # 前端 lint + 类型检查 + 构建
```

---

## 四、前后端如何连接

- 前端通过 `lib/api.ts` 调用后端 REST：`NEXT_PUBLIC_API_BASE` 默认 `http://localhost:8080`。
- 后端 `StaticResourceConfig` 已开启 CORS（`/api/**` 允许任意来源、携带 `X-User-Id` 头），本地跨端口调试与容器内访问均可用。
- 业务执行与持久化全部在 Java 后端完成；Next.js 仅做展示与状态管理，不持有业务状态。

---

## 五、配置项（无真实凭据，均为本地/演示值）

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `DB_HOST/DB_PORT/DB_NAME` | localhost / 3306 / material_studio | 数据库连接 |
| `DB_USERNAME/DB_PASSWORD` | material / material123 | 演示账号，无真实凭据 |
| `material.upload-dir` | ./uploads | 素材与结果落盘目录（容器挂卷） |
| `material.base-url` | http://localhost:8080 | 资源外链基地址 |
| `mock.check-delay-ms` | 1500 | 步骤一「素材检查」模拟耗时 |
| `mock.generate-delay-ms` | 3000 | 步骤二「模拟生成」模拟耗时 |
| `mock.finalize-delay-ms` | 1500 | 步骤三「结果整理」模拟耗时 |
| `mock.fail-stage` | NONE | 指定失败步骤：NONE/CHECK/GENERATE/FINALIZE |
| `mock.fail-reason` | 模拟生成失败（演示用） | 失败原因文案 |

结果地址由 `material.base-url` + `/files/results/{taskId}.svg` 拼出（`/files/results/**` 映射到 `{upload-dir}/results/`）。

以上均可通过环境变量覆盖（如 `MOCK_FAIL_STAGE=GENERATE`），或运行时经开发接口修改（见下）。

---

## 六、数据库初始化 / 模拟数据 / 素材

- **表结构**：`backend/src/main/resources/schema.sql`（template、task 两张表，启动时自动执行）。
- **种子模板**：`backend/src/main/resources/data.sql`，覆盖 创意图片 / 宠物 / 婚礼 / 节日 四个分类、不同素材数量要求（1~2 张）。
- **封面素材**：`backend/src/main/resources/static/demo/*.jpg`，来自 Unsplash（免费授权、可商用），已下载为本地文件由 Spring 静态资源直接服务（`/demo/xxx.jpg`），运行时不依赖外部网络。
- **结果素材**：任务成功后由 `MockResultWriter` 在 `uploads/results/{taskId}.svg` 生成占位 SVG，经 `/files/results/**` 预览。

> 注意：开发环境 `spring.sql.init.mode=always` 会在每次启动重建表（清空历史任务）。页面刷新不会重启后端，历史正常保留；重启后端会重置演示数据。

---

## 七、主要接口

| 方法 | 路径 | 说明 | 主要字段 |
|---|---|---|---|
| GET | `/api/templates` | 模板列表（支持 `?category=`） | 返回 Template[] |
| GET | `/api/templates/categories` | 分类列表 | 返回 string[] |
| GET | `/api/templates/{id}` | 模板详情 | 返回 Template |
| POST | `/api/upload` | 上传素材（multipart `file`，可选 `templateId` 校验类型/大小） | 返回 `{url,name,size,contentType}` |
| POST | `/api/templates/{id}/tasks` | 提交制作（头 `X-User-Id`，体 `{fileUrls:[...]}`） | 返回 Task（含 id，**立即返回，不等待结束**） |
| GET | `/api/tasks` | 个人历史（头 `X-User-Id`） | 返回 Task[] |
| GET | `/api/tasks/{id}` | 单任务查询（**只读**，不驱动推进） | 返回 Task |
| GET/POST | `/api/dev/mock-config` | 开发接口：查看/修改模拟耗时与失败阶段（R-06） | 见 mock.* 配置 |

### Task 字段

```jsonc
{
  "id": "uuid", "userId": "u-xxx", "templateId": "...", "templateTitle": "...",
  "requiredImageCount": 2,
  "status": "PENDING|PROCESSING|SUCCESS|FAILED",   // 整体状态
  "stage":  "NONE|CHECK|GENERATE|FINALIZE|DONE",   // 当前阶段
  "stageDetail": "素材检查中",                       // 前端直接展示的中文阶段说明
  "resultUrl": "/files/results/{id}.svg",            // 成功结果预览地址
  "failReason": "模拟生成失败（演示用）",            // 失败原因
  "createdAt": "...", "updatedAt": "..."
}
```

---

## 八、任务状态、阶段与后台执行方式

- **受理（R-04）**：`TaskService.submit` 完成参数校验与落库（状态 `PENDING`）后，**立即返回任务 ID**，不等待结束；随后 `TaskExecutor.execute` 经 `@Async` 在不同线程（虚拟线程池）继续，**关闭页面不中断**。
- **多步骤异步（R-05）**：后台按 `CHECK → GENERATE → FINALIZE → DONE` 顺序推进，每步独立写库 + 模拟耗时，阶段可见。`TaskExecutor.processTask` 为可单测的同步状态机；`execute` 仅负责异步调度。
- **失败与中断（R-06）**：某步失败立即停止后续步骤，保存失败阶段（`stage`）与原因（`failReason`）；进入终态（`SUCCESS`/`FAILED`）后由「终态守卫」保证不再回退为处理中。
- **查询只读（R-05/R-07）**：`GET /api/tasks/{id}` 仅 `SELECT`，**绝不驱动任务前进**；查询接口不可用时抛异常，但前端保留已有任务信息、不误标失败。

---

## 九、演示步骤

1. **成功**：默认 `fail-stage=NONE`。进入任意模板 → 上传所需数量图片 → 提交 → 观察「处理中」脉冲 → 变为「成功」并可预览 SVG 结果。
2. **指定阶段失败**：
   ```bash
   curl -X POST http://localhost:8080/api/dev/mock-config \
     -H 'Content-Type: application/json' -d '{"failStage":"GENERATE"}'
   ```
   再次提交，任务会在「模拟生成」阶段失败，历史页显示失败原因。改回 `{"failStage":"NONE"}` 即恢复。
3. **调整处理耗时**（便于观察多任务并发）：
   ```bash
   curl -X POST http://localhost:8080/api/dev/mock-config \
     -H 'Content-Type: application/json' -d '{"generateDelayMs":8000}'
   ```
4. **多任务**：连续提交多个任务，各自状态/阶段/错误互不串扰；前端只在存在未完成任务时轮询，全部完成后停止轮询。
5. **刷新恢复**：提交后刷新浏览器，历史页从后端恢复记录，未结束任务继续被跟踪直至完成。

---

## 十、用户身份与个人记录（R-10）

- **方案**：不做登录系统（题面不要求真实鉴权）。前端在 `localStorage` 生成一个稳定 UUID 作为用户标识，每次请求通过 `X-User-Id` 头带给后端；后端据此过滤 `Task`（个人历史）。
- **演示**：
  - 同一浏览器：刷新/重开均看到自己的历史（标识持久）。
  - 切换用户：在浏览器清除 `localStorage` 的 `ms_user_id`（或换无痕窗口）即视为另一用户，历史互不串扰。
  - 后端对所有 `userId` 一视同仁，无需预注册。

---

## 十一、已完成 / 未完成项与已知限制

**已完成**
- 模板列表 + 分类筛选 + 详情 + 上传位 + 提交前校验（数量/类型/大小）。
- 后端多步骤异步任务引擎（素材检查→模拟生成→结果整理），失败中断、终态守卫、查询只读。
- 前端异步状态管理（轮询 hook、未完成任务才轮询、查询异常不误标、卸载清理）。
- 个人历史 + 刷新恢复；Docker Compose 三服务；GitHub Actions CI **已实际运行并通过**（前端 lint/test/tsc/build、后端 test/build 全绿，见仓库 Actions 页）。
- 关键测试：后端单测（步骤顺序、失败中断、终态守卫、查询只读）+ 前端行为测试（多任务互不串扰、查询异常与任务失败区分、轮询生命周期；Vitest + Testing Library，5 例）。

**已知限制 / 实际投入**
- 模拟生成，非真实 AI（符合题面要求）。
- 开发环境每次启动重建表，重启后端会清空历史（页面刷新不影响）。
- 未实现：失败任务自动重试、服务重启后断点续跑、积分/会员、第三方账号（均属题面「不在范围内」或「不强制」）。
- 验证方式：本机用 Docker 起全栈手动走查；后端单测覆盖核心行为；前端经 `next build` 类型检查。未能在此环境的隔离容器中实跑的部分，按题面记为「未验证」而非「未实现」。

---

## 十二、部署与回滚（基本步骤，非生产）

- **部署**：`docker compose up --build --detach`；如需更新，改代码后重新 `up --build`。
- **回滚**：保留上一个可用镜像/commit；`git checkout <验收commit>` 后重新构建，或 `docker compose down` 后拉起旧镜像。
- 本题不要求公网地址或复杂发布平台。

---

## 十三、AI 工作流说明（R-13）

本项目使用 AI 编程助手（WorkBuddy）辅助完成。以下为实际工作流：

1. **任务拆解**：按职责边界拆为后端（模板读取、上传落盘、任务受理、异步执行器、查询/历史、账户/身份）、前端（列表、详情上传、历史）、工程交付（CI、Compose、README），先定数据模型与状态枚举，再写执行器，最后做前端状态机。
2. **提供上下文**：每次生成前把题面 R-01~R-14、技术栈硬约束（Next.js+TS / Java+SB+MyBatis+MySQL / 不调真实 AI）、参考界面（题目附带截图作 UI-01/02/03 布局参考）、数据模型与「禁止项」（查询不得驱动推进、终态不回退、查询异常不误标失败）一次性给全。
3. **生成与修改**：先产出骨架与接口契约，逐文件确认分层；核心业务逻辑（三步推进、失败中断、状态守卫）由我描述规则、AI 写实现，我再读代码核对边界。
4. **结果验证**：后端单测钉死「步骤顺序」与「失败中断」及「查询只读」；前端走 `lint + tsc + build`；手动跑通成功/失败/调耗时/多任务/刷新恢复四个场景。
5. **问题修正（具体例子）**：多步骤异步引擎第一版曾把「阶段推进」逻辑写在查询接口里（每次 GET 顺势推进一步），违反 R-05/R-07。修正方式：明确「推进权只属后台执行器，查询接口只读」，将推进逻辑收敛到 `TaskExecutor.processTask`（`@Async` 调度），并新增终态守卫；单测强制某步失败后断言后续未执行、失败阶段与原因已落库，且查询接口异常时不写任何状态。通过「读产出→对照 R 条款找越界→重写越界部分→单测钉死行为」的闭环，避免状态被查询请求意外驱动。

**脚手架/素材来源**：Spring Initializr 风格的标准骨架由 AI 生成；首页封面图来自 Unsplash（免费授权，已本地化存储，运行时不依赖外部网络）；生成结果为自绘 SVG 占位；未使用其它第三方源码。

---

## 仓库与验收 commit

- 仓库地址：https://github.com/CuberLuo/xiaoying_project（public，`main` 分支）
- 验收 commit：`3ec6326`（fix: 结果图地址拼接错误，包含此前全部交付内容）
- CI：push 后自动执行前端 lint/test/tsc/build 与后端 test/build，运行记录见仓库 Actions 页
- 压缩包不包含 `node_modules/`、`.next/`、后端 `target/`、`uploads/`
