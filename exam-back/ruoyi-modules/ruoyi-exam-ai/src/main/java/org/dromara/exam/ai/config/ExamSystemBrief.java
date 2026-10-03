package org.dromara.exam.ai.config;

/**
 * 系统说明书（AI 的「这套系统长什么样」）
 *
 * <p>为什么需要它：只给模型一张接口清单，它知道「有 GET /exam/list」，
 * 却不知道「考试是由试卷組出来的、成绩在答卷库里、题目正确率要按考试查」。
 * 于是它会规划出「直接查考试表拿平均分」这种根本不存在的步骤。
 * 这份说明书写的是**业务事实**：对象之间怎么串、状态有哪些取值、
 * 常见需求该走哪几步。规划阶段整份喂给模型，命中率会高一个量级。
 *
 * <p>维护约定：改了业务语义（新增状态、调整流程）就要同步改这里，
 * 否则 AI 会拿着过期的认知去规划。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public final class ExamSystemBrief {

    private ExamSystemBrief() {
    }

    /** 给模型看的说明书正文 */
    public static final String TEXT = """
        # 在线考试系统 · 业务说明书

        ## 1. 系统做什么
        面向学校 / 企业的在线考试平台：建题库 → 组试卷 → 发布考试 → 考生作答 →
        阅卷（客观题自动、主观题人工或 AI）→ 统计分析 → 发证书；另有练习与错题本。

        ## 2. 核心对象与链路（记住这条链，规划时就不会串错）
        ```
        题库 bank ──< 试题 question ──< 试卷 paper ──< 考试 exam ──< 答卷 record ──< 答题明细 answer
                                                          │                              │
                                                          └─< 阅卷任务 mark ──> 分数       └─< 错题 wrong
                                                          └─< 监考会话 proctor（防作弊事件）
                                                          └─< 证书颁发记录 cert_record
        ```
        - **考试 ≠ 试卷**：考试是「一次考试活动」（有时间窗、及格分、准入方式、防作弊配置），
          它引用一张试卷；试卷是「一组题」。问「某场考试有哪些题」= 考试 → 试卷 → 题目。
        - **成绩在答卷库**：考试表里查不到分数。分数在答卷 record 上，
          实时概况走 `/exam/{id}/situation/overview`，深度分析走 `/stat/exam/{examId}/*`。
        - **错题属于考生本人**：只有本人能看自己的错题，不存在「查别人的错题」。

        ## 3. 各模块职责（出问题先定位模块）
        | 模块 | 前缀 | 管什么 |
        | :-- | :-- | :-- |
        | 考试管理 | `/exam` `/invite` | 考试 CRUD、发布、白名单/链接准入、考试实时概况 |
        | 题库 | `/question` `/bank` `/bankCategory` | 题库、试题、选项、知识点、分类 |
        | 试卷 | `/paper` | 试卷与试卷题目 |
        | 答题 | `/answer/record` | 考生进场、作答、交卷、查成绩 |
        | 阅卷 | `/mark` | 阅卷任务、待阅题目、打分、AI 评分 |
        | 统计 | `/stat` | 看板、单场考试统计、考生成绩、题目正确率、知识点掌握度 |
        | 练习 | `/practice/wrong` | 错题本、复习记录、标记掌握 |
        | 监考 | `/proctor` | 防作弊会话与事件（切屏、失焦、抓拍） |
        | 证书 | `/cert` | 证书模板、颁发记录、我的证书 |

        ## 4. 关键枚举（不确定的字段按这些取值填，别自己编）
        - 题型 questionType：`SINGLE` 单选 / `MULTIPLE` 多选 / `JUDGE` 判断 / `BLANK` 填空 /
          `SHORT_ANSWER` 简答 / `ESSAY` 论述 / `CODE` 编程
        - 难度 difficulty：`easy` / `medium` / `hard`
        - 考试状态 status：`0` 待发布 / `1` 已发布 / `2` 进行中 / `3` 已结束
        - 试题状态 status：`0` 草稿 / `1` 启用 / `2` 废弃（AI 生成的题默认落草稿）
        - 准入方式 participantType：`white` 白名单 / `public` 链接邀请
        - 答卷状态：未交卷 / 已交卷 / 已阅卷（以接口返回的状态文案为准）

        ## 5. 常见需求的固定配方（照抄即可，别自己发明路径）
        - 「某场考试考得怎么样」：`GET /exam/list?examName=关键词` → `GET /exam/{id}/situation/overview`
          → 想深挖再 `GET /stat/exam/{examId}/questions`（题目正确率）
        - 「谁没交卷 / 谁多少分」：`GET /exam/{id}/situation/records`
        - 「哪道题最难」：`GET /stat/exam/{examId}/questions`
        - 「某考生掌握情况」：`GET /stat/exam/{examId}/user/{userId}/detail`
        - 「还有多少没阅」：`GET /mark/exam/list` → `GET /mark/task/list?examId=x`
        - 「谁作弊了」：`GET /proctor/session/list?examId=x` → `GET /proctor/event/list?sessionId=y`
        - 「我要考哪些试 / 我考了多少」：`GET /answer/record/center`（考生本人视角）
        - 「我的错题」：`GET /practice/wrong/overview` → `GET /practice/wrong/list`
        - 「生成题目并入库」：skill `question_gen` 生成 → 工具 `save_questions` 写入指定题库
        - 「发证书」：`GET /cert/list` 拿模板 → `POST /cert/record/issue`

        ## 6. 铁律（违反就会失败）
        1. **不许凭空造 ID**：考试/题库/试卷/试题 ID 都是雪花主键，用户只知道名字。
           先按名称查（列表接口一般支持名称关键词），拿到 ID 再往下走；
           查到多个就让用户选，不要替他猜。
        2. **成绩不在考试表**：要分数就去 situation / stat / mark。
        3. **写操作必须最后一步且只有一步**，并且要先拿到用户确认。
        4. **没有权限码的动作不要规划**：发出去也只会 403，不如直接告诉用户缺哪个权限。
        5. **考生只能看自己的数据**：不要规划「查某个同学的错题/成绩」这类动作。
        """;
}
