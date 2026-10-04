#!/usr/bin/env bash
#
# 全仓库镜像构建脚本：所有带 Dockerfile 的服务都能打，可单独打也可以分组/全量打
#
# 自动发现：扫描 exam-back 下所有 Dockerfile（含框架自带的 gateway / auth / system / visual 等），
#           新增模块只要放一个 Dockerfile 就会被自动纳入，不需要改脚本。
#           script/docker 下的中间件辅助镜像（如 rabbitmq 插件）已排除。
#
# 用法：
#   ./build-images.sh                        # 全部：22 个 Java 服务 + Python AI Agent
#   ./build-images.sh --list                 # 看看有哪些可以打、端口是多少、jar 齐不齐
#   ./build-images.sh exam                   # 只打考试域 10 个服务
#   ./build-images.sh core                   # 网关 / 认证 / system / gen / job / resource / workflow
#   ./build-images.sh visual                 # monitor / nacos / seata-server / snailjob-server
#   ./build-images.sh agent                  # 只打 Python 侧 AI Agent（9221）
#   ./build-images.sh system question        # 指定若干，支持短名（question 等价 ruoyi-exam-question）
#   DRY_RUN=1 ./build-images.sh all          # 只打印将要执行的命令，不真跑
#
# 环境变量：
#   TAG           镜像标签，默认 latest
#   PREFIX        镜像前缀，默认 ruoyi（最终 <PREFIX>/<模块>:<TAG>）
#   SKIP_PACKAGE=1  跳过 maven 打包（jar 已经打好了）
#   PUSH=1        打完逐个 docker push
#   PLATFORM      docker build --platform（留空用本机架构）
#   WITH_AGENT=0  全量构建时跳过 Python Agent
#   DRY_RUN=1     只打印命令
#   MVN           maven 命令，默认 mvn（本机 wrapper 就传绝对路径）
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../../.." && pwd)"

TAG="${TAG:-latest}"
PREFIX="${PREFIX:-ruoyi}"
SKIP_PACKAGE="${SKIP_PACKAGE:-0}"
PUSH="${PUSH:-0}"
PLATFORM="${PLATFORM:-}"
WITH_AGENT="${WITH_AGENT:-1}"
DRY_RUN="${DRY_RUN:-0}"
MVN="${MVN:-mvn}"

cd "$ROOT_DIR"

log()  { printf '\033[36m[build]\033[0m %s\n' "$*"; }
info() { printf '        %s\n' "$*"; }
warn() { printf '\033[33m[warn]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[31m[fail]\033[0m %s\n' "$*" >&2; exit 1; }

# ---------- 自动发现 ----------
# 只扫这几个源码顶层 + maxdepth 2：全仓库 find 会把 target 里的依赖 jar 一起遍历，
# 又慢又容易撞上无关目录。将来新增顶层源码目录，加到 SOURCE_ROOTS 里即可。
SOURCE_ROOTS=(ruoyi-agent ruoyi-auth ruoyi-gateway ruoyi-gateway-mvc ruoyi-modules ruoyi-visual)

# 目录名就是 jar 名 / 镜像名（与 spring-boot-maven-plugin 的产物一致）
discover_modules() {
  find "${SOURCE_ROOTS[@]}" -maxdepth 2 -type f -name Dockerfile 2>/dev/null \
    | sed 's|/Dockerfile$||' \
    | sort
}

# 分组：按路径归属，方便「只打考试域」这种批量操作
group_of() {
  case "$1" in
    ruoyi-modules/ruoyi-exam-*) echo exam ;;
    ruoyi-visual/*)             echo visual ;;
    ruoyi-agent/*)              echo agent ;;
    ruoyi-gateway*|ruoyi-auth)  echo core ;;
    ruoyi-modules/*)            echo core ;;
    *)                          echo other ;;
  esac
}

# 端口与说明：只用于 --list 展示，来自各模块 application.yml / Dockerfile EXPOSE
meta_of() {
  case "$1" in
    ruoyi-auth)                          echo "9210|认证中心" ;;
    ruoyi-gateway)                       echo "8080|网关（WebFlux）" ;;
    ruoyi-gateway-mvc)                   echo "8080|网关（Spring MVC 版，与 gateway 二选一）" ;;
    ruoyi-modules/ruoyi-system)          echo "9201|系统模块" ;;
    ruoyi-modules/ruoyi-gen)             echo "9202|代码生成" ;;
    ruoyi-modules/ruoyi-job)             echo "9203|定时任务" ;;
    ruoyi-modules/ruoyi-resource)        echo "9204|资源 / 文件服务" ;;
    ruoyi-modules/ruoyi-workflow)        echo "9205|工作流" ;;
    ruoyi-visual/ruoyi-monitor)          echo "9100|服务监控" ;;
    ruoyi-visual/ruoyi-nacos)            echo "8848|注册 / 配置中心镜像" ;;
    ruoyi-visual/ruoyi-seata-server)     echo "8091|分布式事务服务端" ;;
    ruoyi-visual/ruoyi-snailjob-server)  echo "8800|分布式任务调度服务端" ;;
    ruoyi-modules/ruoyi-exam-question)   echo "9211|题库服务" ;;
    ruoyi-modules/ruoyi-exam-paper)      echo "9212|试卷服务" ;;
    ruoyi-modules/ruoyi-exam-manage)     echo "9213|考试管理服务" ;;
    ruoyi-modules/ruoyi-exam-answer)     echo "9214|答题服务" ;;
    ruoyi-modules/ruoyi-exam-mark)       echo "9215|阅卷服务" ;;
    ruoyi-modules/ruoyi-exam-stat)        echo "9216|考试统计服务" ;;
    ruoyi-modules/ruoyi-exam-practice)   echo "9217|练习服务" ;;
    ruoyi-modules/ruoyi-exam-cert)       echo "9218|证书服务" ;;
    ruoyi-modules/ruoyi-exam-proctor)    echo "9219|防作弊服务" ;;
    ruoyi-modules/ruoyi-exam-ai)         echo "9220|AI 服务（会回调 Python Agent）" ;;
    ruoyi-agent/ruoyi-exam-agent)        echo "9221|AI Agent（Python，FastAPI）" ;;
    *)                                   echo "-|" ;;
  esac
}

# jar 路径：优先按 Dockerfile 里写的 ADD 源来，认不出来再用目录名兜底
jar_of() {
  local dir="$1" base="$2"
  local declared
  declared=$(grep -E "^(ADD|COPY)" "$dir/Dockerfile" 2>/dev/null \
    | awk '{print $2}' | grep -E "^\./target/.*\.jar$" | head -1 | sed 's|^\./||')
  echo "${declared:-target/$base.jar}"
}

# 名字匹配：system / ruoyi-system / 完整路径都能命中
name_matches() {
  local path="$1" want="$2" base stripped
  base="${path##*/}"
  [[ "$want" == "$base" || "$want" == "$path" ]] && return 0
  stripped="${base#ruoyi-}"
  stripped="${stripped#exam-}"
  [[ "$want" == "$stripped" ]]
}

# ---------- 列举 ----------
list_modules() {
  printf '%-28s %-6s %-6s %-8s %s\n' "模块" "端口" "分组" "jar" "说明"
  printf '%-28s %-6s %-6s %-8s %s\n' "----" "----" "----" "---" "----"
  while read -r dir; do
    [[ -z "$dir" ]] && continue
    local base meta port desc jar group has_jar
    base="${dir##*/}"
    meta="$(meta_of "$dir")"
    port="${meta%%|*}"
    desc="${meta#*|}"
    group="$(group_of "$dir")"
    if [[ -f "$dir/pom.xml" ]]; then
      jar="$(jar_of "$dir" "$base")"
      if [[ -f "$dir/$jar" ]]; then has_jar="有"; else has_jar="缺"; fi
    else
      has_jar="-" # Python 项目，产物不是一个 fat jar
    fi
    printf '%-28s %-6s %-6s %-8s %s\n' "$base" "$port" "$group" "$has_jar" "$desc"
  done <<<"$(discover_modules)"
  cat <<'TXT'

用法示例：
  ./build-images.sh all|exam|core|visual|agent     # 按组打
  ./build-images.sh system question                # 单独打（短名即可）
  DRY_RUN=1 ./build-images.sh all                  # 只看命令不执行
TXT
}

usage() {
  cat <<'TXT'
用法：build-images.sh [目标...]

目标可以是分组名，也可以是模块名（短名 / 全名 / 路径都认）：
  all          全部（默认）
  exam         考试域 10 个服务
  core         网关 + 认证 + system / gen / job / resource / workflow
  visual       monitor / nacos / seata-server / snailjob-server
  agent        Python 侧 AI Agent
  <模块名>     如 system、question、ruoyi-exam-question

选项：
  --list / -l  列出所有可构建的模块
  --help / -h  显示本帮助

环境变量：TAG PREFIX SKIP_PACKAGE PUSH PLATFORM WITH_AGENT DRY_RUN MVN
TXT
}

# 注意：${arr[@]+"${arr[@]}"} 这种写法是为了兼容 macOS 自带的 bash 3.2——
# 空数组在 set -u 下直接展开会报 unbound variable（bash 4.4 才修）。
list_contains() {
  local needle="$1"
  shift
  local item
  for item in "$@"; do
    [[ "$item" == "$needle" ]] && return 0
  done
  return 1
}

# ---------- 参数解析 ----------
SELECTED=()
for arg in "$@"; do
  case "$arg" in
    -h|--help) usage; exit 0 ;;
    -l|--list) list_modules; exit 0 ;;
    "")        : ;;
    *)         SELECTED+=("$arg") ;;
  esac
done

ALL_DIRS=()
while read -r dir; do
  [[ -n "$dir" ]] && ALL_DIRS+=("$dir")
done <<<"$(discover_modules)"

# 没指定就全量；注意 a ll → all 需要显式写 all 或留空
TARGET_DIRS=()
if [[ ${#SELECTED[@]} -eq 0 ]]; then
  TARGET_DIRS=("${ALL_DIRS[@]}")
elif [[ ${#SELECTED[@]} -gt 0 ]] && list_contains "all" "${SELECTED[@]}"; then
  TARGET_DIRS=("${ALL_DIRS[@]}")
else
  for want in "${SELECTED[@]}"; do
    hit=0
    for dir in "${ALL_DIRS[@]}"; do
      if [[ "$want" == "$(group_of "$dir")" ]] || name_matches "$dir" "$want"; then
        TARGET_DIRS+=("$dir")
        hit=1
      fi
    done
    if [[ $hit -eq 0 ]]; then
      die "没有匹配到目标 ${want} —— 用 --list 看看有哪些服务。"
    fi
  done
fi

# agent 组受 WITH_AGENT 控制；但显式写了 agent 就照打不误
FILTERED=()
for dir in "${TARGET_DIRS[@]}"; do
  if [[ "$(group_of "$dir")" == "agent" && "$WITH_AGENT" != "1" ]]; then
    if [[ ${#SELECTED[@]} -gt 0 ]] && list_contains "agent" "${SELECTED[@]}"; then
      : # 显式指定了，保留
    else
      continue
    fi
  fi
  FILTERED+=("$dir")
done
TARGET_DIRS=("${FILTERED[@]}")

# 去重（一个目标可能被多个条件同时命中）
UNIQ=()
if [[ ${#TARGET_DIRS[@]} -gt 0 ]]; then
  for dir in "${TARGET_DIRS[@]}"; do
    if [[ ${#UNIQ[@]} -eq 0 ]]; then
      UNIQ+=("$dir")
    else
      list_contains "$dir" "${UNIQ[@]}" || UNIQ+=("$dir")
    fi
  done
fi
TARGET_DIRS=("${UNIQ[@]}")

[[ ${#TARGET_DIRS[@]} -eq 0 ]] && die "没有可构建的目标"

# ---------- 打包 ----------
if [[ "$SKIP_PACKAGE" == "1" ]]; then
  log "SKIP_PACKAGE=1，跳过 maven 打包"
else
  modules_csv=""
  for dir in "${TARGET_DIRS[@]}"; do
    # Python 项目没有 pom.xml，不进 maven 列表
    if [[ -f "$dir/pom.xml" ]]; then
      modules_csv+=",$dir"
    fi
  done
  if [[ -n "$modules_csv" ]]; then
    modules_csv="${modules_csv:1}"
    log "maven 打包（${modules_csv}）"
    if [[ "$DRY_RUN" == "1" ]]; then
      info "$MVN -DskipTests -pl $modules_csv -am package"
    else
      "$MVN" -DskipTests -pl "$modules_csv" -am package
    fi
  else
    log "目标里没有 Maven 模块，跳过打包"
  fi
fi

# ---------- 打镜像 ----------
build_one() {
  local dir="$1" base image jar
  base="${dir##*/}"
  image="$PREFIX/$base:$TAG"

  if [[ -f "$dir/pom.xml" ]]; then
    jar="$(jar_of "$dir" "$base")"
    if [[ ! -f "$dir/$jar" ]]; then
      warn "跳过 $base：缺少 $jar（先跑一次打包）"
      return 1
    fi
  fi

  local -a cmd=(docker build)
  [[ -n "$PLATFORM" ]] && cmd+=(--platform "$PLATFORM")
  cmd+=(-t "$image" -f "$dir/Dockerfile" "$dir")

  log "$image"
  if [[ "$DRY_RUN" == "1" ]]; then
    info "${cmd[*]}"
    if [[ "$PUSH" == "1" ]]; then
      info "docker push $image"
    fi
  else
    "${cmd[@]}"
    if [[ "$PUSH" == "1" ]]; then
      log "push $image"
      docker push "$image"
    fi
  fi
}

failed=()
ok=()
for dir in "${TARGET_DIRS[@]}"; do
  if build_one "$dir"; then
    ok+=("${dir##*/}")
  else
    failed+=("${dir##*/}")
  fi
done

# ---------- 收尾 ----------
echo
if [[ ${#ok[@]} -gt 0 ]]; then
  log "成功 ${#ok[@]} 个：${ok[*]}"
else
  log "成功 0 个"
fi
if [[ ${#failed[@]} -gt 0 ]]; then
  warn "未成功 ${#failed[@]} 个：${failed[*]}"
fi
if [[ "$DRY_RUN" != "1" && ${#ok[@]} -gt 0 ]]; then
  echo
  docker images --filter "reference=$PREFIX/*:$TAG" --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedSince}}'
fi
if [[ ${#failed[@]} -gt 0 ]]; then
  exit 1
fi
exit 0
