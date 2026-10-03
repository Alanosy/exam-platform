package org.dromara.exam.ai.config;

import org.dromara.exam.ai.domain.vo.ApiEntryVo;

import java.util.List;

/**
 * 考试域接口目录（AI 的「系统说明书」）
 *
 * <p>为什么要有这份清单：大模型不知道我们系统长什么样，只给它一堆裸接口
 * 它既不敢用也不会用。这里把各模块**对 AI 有意义**的接口整理成人话，
 * 规划阶段整份喂给模型，模型就能自己判断「这件事系统里有没有能力做到」。
 *
 * <p>只列业务接口：**不列** 用户密码、租户、菜单、字典维护、监控、代码生成
 * 这类基础设施接口，它们不在 allow 前缀里，列了也调不通。
 *
 * <p><b>audience 是角色隔离的第一道闸</b>：{@code student} 的条目只给考生，
 * {@code admin} 的条目只给有后台权限的人。清单会按当前用户过滤后再下发给模型
 * —— 看不见的能力就不会被规划出来。真正的拦截仍然在网关。
 *
 * @author ruoyi
 * @date 2026-10-03
 */
public final class ExamApiCatalog {

    private ExamApiCatalog() {
    }

    public static final List<ApiEntryVo> ENTRIES = List.of(
        // ---------------------------------------------------------- 考试（管理端）
        new ApiEntryVo("exam_list", "考试列表", "GET", "/exam/list", "分页查询考试，可按名称、状态筛选")
            .param("examName: 考试名称关键词")
            .param("status: 状态（0待发布 1已发布 2进行中 3已结束，可不传）")
            .param("pageNum/pageSize: 分页")
            .permission("system:exam:list").admin(),
        new ApiEntryVo("exam_detail", "考试详情", "GET", "/exam/{id}", "考试基本信息、时间窗、及格分、防作弊配置")
            .param("id: 考试ID（路径参数）")
            .permission("system:exam:query").admin(),
        new ApiEntryVo("exam_situation_overview", "考试概况统计", "GET", "/exam/{id}/situation/overview", "实时统计：参考人数、交卷数、平均分、及格率、分数分布")
            .param("id: 考试ID")
            .permission("system:exam:query").admin(),
        new ApiEntryVo("exam_situation_records", "考试答卷名单", "GET", "/exam/{id}/situation/records", "谁参加了、交没交卷、得了多少分（分页）")
            .param("id: 考试ID")
            .param("pageNum/pageSize: 分页")
            .permission("system:exam:query").admin(),
        new ApiEntryVo("exam_white_users", "考试白名单", "GET", "/exam/{id}/whiteUsers", "白名单准入方式的考试，查看被授权人员")
            .param("id: 考试ID")
            .permission("system:exam:query").admin(),
        new ApiEntryVo("exam_join_code_refresh", "刷新考试邀请码", "POST", "/exam/{id}/joinCode/refresh", "重新生成链接准入的邀请码（写操作，需确认）")
            .param("id: 考试ID")
            .permission("system:exam:edit").write().admin(),
        new ApiEntryVo("invite_list", "考试邀请记录", "GET", "/invite/list", "链接准入方式下，谁通过链接进入了考试")
            .param("examId: 考试ID（可选）")
            .permission("system:invite:list").admin(),

        // ---------------------------------------------------------- 题库与试题（管理端）
        new ApiEntryVo("question_list", "试题列表", "GET", "/question/list", "分页查询试题，可按题库、题型、难度、关键词筛选")
            .param("bankId: 题库ID")
            .param("questionType: SINGLE/MULTIPLE/JUDGE/BLANK/SHORT_ANSWER/ESSAY/CODE")
            .param("difficulty: easy/medium/hard")
            .param("keyword: 题干关键词")
            .permission("system:question:list").admin(),
        new ApiEntryVo("question_detail", "试题详情", "GET", "/question/{id}", "试题题干、选项、答案、解析与知识点")
            .param("id: 试题ID")
            .permission("system:question:query").admin(),
        new ApiEntryVo("bank_list", "题库列表", "GET", "/question/bank/list", "查询题库，可按名称模糊匹配")
            .param("name: 题库名称关键词")
            .permission("system:bank:list").admin(),
        new ApiEntryVo("bank_category_tree", "题库分类树", "GET", "/question/bankCategory/tree", "题库的分类层级")
            .permission("system:bankCategory:list").admin(),
        new ApiEntryVo("knowledge_tree", "知识点树", "GET", "/question/knowledge/tree", "两级知识点（章节 -> 知识点）")
            .permission("system:knowledge:list").admin(),
        new ApiEntryVo("knowledge_by_questions", "试题的知识点", "GET", "/question/knowledge/byQuestions", "批量查一批试题关联了哪些知识点")
            .param("questionIds: 试题ID列表")
            .permission("system:knowledge:list").admin(),
        new ApiEntryVo("question_create", "新增试题", "POST", "/question/create", "写入一道试题（含选项与知识点，写操作，需确认）")
            .param("bankId: 题库ID")
            .param("stem: 题干")
            .param("questionType/difficulty/score/analysis/answer")
            .permission("system:question:add").write().admin(),
        new ApiEntryVo("question_random", "随机抽题", "GET", "/question/random", "按条件随机抽题，组卷辅助")
            .param("bankId/questionType/difficulty/count")
            .permission("system:question:query").admin(),

        // ---------------------------------------------------------- 试卷（管理端）
        new ApiEntryVo("paper_list", "试卷列表", "GET", "/paper/list", "分页查询试卷")
            .param("paperName: 试卷名称关键词")
            .permission("system:paper:list").admin(),
        new ApiEntryVo("paper_detail", "试卷详情", "GET", "/paper/{id}", "试卷结构与题目")
            .param("id: 试卷ID")
            .permission("system:paper:query").admin(),
        new ApiEntryVo("paper_question_list", "试卷题目列表", "GET", "/paper/question/list", "某张试卷下的题目")
            .param("paperId: 试卷ID")
            .permission("system:question:list").admin(),

        // ---------------------------------------------------------- 答题（考生本人视角）
        new ApiEntryVo("answer_center", "我的考试", "GET", "/answer/record/center", "**考生视角**：我可参加 / 已参加的考试")
            .student(),
        new ApiEntryVo("answer_records", "我的答卷列表", "GET", "/answer/record/records", "**考生视角**：我的历史答卷")
            .student(),
        new ApiEntryVo("answer_result", "我的成绩", "GET", "/answer/record/{recordId}/result", "**考生视角**：某次作答的成绩与解析")
            .param("recordId: 答卷ID")
            .student(),
        new ApiEntryVo("answer_paper", "我的试卷", "GET", "/answer/record/{recordId}/paper", "**考生视角**：某次作答的试卷与题目")
            .param("recordId: 答卷ID")
            .student(),

        // ---------------------------------------------------------- 阅卷（管理端）
        new ApiEntryVo("mark_exam_list", "待阅考试", "GET", "/mark/exam/list", "有待阅任务的考试列表")
            .permission("exam:mark:list").admin(),
        new ApiEntryVo("mark_task_list", "阅卷任务", "GET", "/mark/task/list", "阅卷任务列表与进度")
            .param("examId: 考试ID（可选）")
            .permission("exam:mark:list").admin(),
        new ApiEntryVo("mark_task_questions", "待阅题目", "GET", "/mark/task/{taskId}/questions", "某个阅卷任务下的待阅题目")
            .param("taskId: 任务ID")
            .permission("exam:mark:list").admin(),
        new ApiEntryVo("mark_task_logs", "阅卷日志", "GET", "/mark/task/{taskId}/logs", "某个阅卷任务的打分流水")
            .param("taskId: 任务ID")
            .permission("exam:mark:list").admin(),
        new ApiEntryVo("mark_score", "提交评分", "POST", "/mark/score", "给某道题打分（写操作，需确认）")
            .param("recordId/questionId/score/comment")
            .permission("exam:mark:edit").write().admin(),
        new ApiEntryVo("mark_task_finish", "完成阅卷任务", "POST", "/mark/task/{taskId}/finish", "结束一个阅卷任务（写操作，需确认）")
            .param("taskId: 任务ID")
            .permission("exam:mark:edit").write().admin(),

        // ---------------------------------------------------------- 统计（管理端）
        new ApiEntryVo("stat_home_overview", "首页总览", "GET", "/stat/home/overview", "考试数、进行中、待阅、参考人次等概览")
            .permission("system:exam:list").admin(),
        new ApiEntryVo("stat_dashboard", "统计看板", "GET", "/stat/dashboard", "考试维度的统计看板")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_page", "考试统计列表", "GET", "/stat/exam/page", "分页列出各场考试的统计摘要")
            .param("pageNum/pageSize: 分页")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_overview", "单场考试统计", "GET", "/stat/exam/{examId}/overview", "分数分布、及格率、题目正确率")
            .param("examId: 考试ID")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_segment", "分数段统计", "GET", "/stat/exam/{examId}/segment", "各分数段人数与占比")
            .param("examId: 考试ID")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_users", "考生成绩名单", "GET", "/stat/exam/{examId}/users", "每个考生的得分与排名")
            .param("examId: 考试ID")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_user_detail", "单个考生详情", "GET", "/stat/exam/{examId}/user/{userId}/detail", "某考生在这场考试里的逐题作答")
            .param("examId: 考试ID")
            .param("userId: 用户ID")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_questions", "题目正确率", "GET", "/stat/exam/{examId}/questions", "每道题的答对率、区分度")
            .param("examId: 考试ID")
            .permission("exam:stat:list").admin(),
        new ApiEntryVo("stat_exam_knowledge", "知识点掌握度", "GET", "/stat/exam/{examId}/knowledge", "按知识点统计掌握情况")
            .param("examId: 考试ID")
            .permission("exam:stat:list").admin(),

        // ---------------------------------------------------------- 练习 / 错题（考生本人视角）
        new ApiEntryVo("wrong_overview", "错题概览", "GET", "/practice/wrong/overview", "**考生视角**：我的错题数量与掌握情况")
            .student(),
        new ApiEntryVo("wrong_sources", "错题来源", "GET", "/practice/wrong/sources", "**考生视角**：我的错题来自哪些考试/练习")
            .student(),
        new ApiEntryVo("wrong_list", "错题列表", "GET", "/practice/wrong/list", "**考生视角**：我的错题（可按来源筛选）")
            .param("sourceId: 来源ID（可选）")
            .student(),
        new ApiEntryVo("wrong_detail", "错题详情", "GET", "/practice/wrong/{id}", "错题内容、我的作答、解析")
            .param("id: 错题ID")
            .student(),
        new ApiEntryVo("wrong_reviews", "错题复习记录", "GET", "/practice/wrong/{id}/reviews", "这道错题我复习过几次")
            .param("id: 错题ID")
            .student(),
        new ApiEntryVo("wrong_master", "标记已掌握", "PUT", "/practice/wrong/{id}/master", "把错题标记为已掌握（写操作）")
            .param("id: 错题ID")
            .write().student(),

        // ---------------------------------------------------------- 监考 / 防作弊（管理端）
        new ApiEntryVo("proctor_exam_list", "监考考试列表", "GET", "/proctor/exam/list", "开启防作弊的考试")
            .permission("exam:proctor:list").admin(),
        new ApiEntryVo("proctor_session_list", "监考会话", "GET", "/proctor/session/list", "考生的监考会话（谁在考试）")
            .param("examId: 考试ID（可选）")
            .permission("exam:proctor:list").admin(),
        new ApiEntryVo("proctor_session_detail", "监考会话详情", "GET", "/proctor/session/{sessionId}", "单个监考会话的汇总")
            .param("sessionId: 会话ID")
            .permission("exam:proctor:list").admin(),
        new ApiEntryVo("proctor_event_list", "作弊事件", "GET", "/proctor/event/list", "切屏、失焦等防作弊事件")
            .param("sessionId: 会话ID（可选）")
            .param("examId: 考试ID（可选）")
            .permission("exam:proctor:list").admin(),
        new ApiEntryVo("proctor_snapshot_list", "监考抓拍", "GET", "/proctor/snapshot/list", "摄像头抓拍记录")
            .param("sessionId: 会话ID（可选）")
            .permission("exam:proctor:list").admin(),
        new ApiEntryVo("proctor_overview", "监考总览", "GET", "/proctor/overview", "监考整体态势")
            .permission("exam:proctor:list").admin(),

        // ---------------------------------------------------------- 证书
        new ApiEntryVo("cert_mine", "我的证书", "GET", "/cert/mine/list", "**考生视角**：我获得的证书")
            .student(),
        new ApiEntryVo("cert_mine_record", "我的证书记录", "GET", "/cert/mine/record/{recordId}", "**考生视角**：某次考试对应的证书")
            .param("recordId: 答卷ID")
            .student(),
        new ApiEntryVo("cert_list", "证书模板列表", "GET", "/cert/list", "证书模板")
            .permission("exam:cert:list").admin(),
        new ApiEntryVo("cert_option_list", "证书下拉选项", "GET", "/cert/option/list", "证书模板的精简列表，用于选择")
            .permission("exam:cert:list").admin(),
        new ApiEntryVo("cert_record_list", "证书颁发记录", "GET", "/cert/record/list", "谁被颁发了证书")
            .param("certId: 证书ID（可选）")
            .permission("exam:cert:list").admin(),
        new ApiEntryVo("cert_record_count", "证书颁发数量", "GET", "/cert/record/count", "各证书已颁发多少份")
            .permission("exam:cert:list").admin(),
        new ApiEntryVo("cert_issue", "颁发证书", "POST", "/cert/record/issue", "给考生颁发证书（写操作，需确认）")
            .param("certId/userId/recordId")
            .permission("exam:cert:issue").write().admin(),

        // ---------------------------------------------------------- 字典（辅助）
        new ApiEntryVo("dict_data", "字典数据", "GET", "/system/dict/data/list", "按字典类型取字典项，例如题型、难度、考试状态")
            .param("dictType: 字典类型")
    );
}
