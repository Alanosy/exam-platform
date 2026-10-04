#!/usr/bin/env bash
#
# 考试相关微服务：打包 + 打 Docker 镜像
#
# 用法：
#   ./build-images.sh                         # 打包全部考试模块并打镜像
#   ./build-images.sh question paper          # 只处理题库/试卷两个（也可写全名 ruoyi-exam-question）
#   SKIP_PACKAGE=1 ./build-images.sh          # 已经有 jar 了，跳过打包直接打镜像
#   TAG=v1.0.0 PREFIX=ruoyi ./build-images.sh # 指定镜像标签与前缀
#   PUSH=1 PREFIX=registry.example.com/exam ./build-images.sh  # 打完并推送到私有仓库
#   PLATFORM=linux/amd64 ./build-images.sh    # 指定目标平台（推 ARM 机器跑的服务时用得上）
#   NO_AGENT=1 ./build-images.sh              # 不打 Python 侧的 AI Agent 镜像
#
# 可用环境变量：
#   TAG         镜像标签，默认 latest
#   PREFIX      镜像命名空间/前缀，默认 ruoyi（最终镜像名 <PREFIX>/<模块>:<TAG>）
#   SKIP_PACKAGE=1  跳过 mvn 打包
#   PUSH=1      打完逐个 docker push
#   PLATFORM    docker build --platform（留空则用本机架构）
#   WITH_AGENT=0/1  是否打 Python Agent，默认 1
#   MVN         maven 命令，默认 mvn（需要 wrapper 就传 ./mvnw）
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
MVN="${MVN:-mvn}"

# 短名:端口:说明（脚本在这里维护，Dockerfile 里的端口要与之一致）
# shellcheck disable=SC2034
ALL_MODULES=(
  "question:9211:题库服务"
  "paper:9212:试卷服务"
  "manage:9213:考试管理服务"
  "answer:9214:答题服务"
  "mark:9215:阅卷服务"
  "stat:9216:考试统计服务"
  "practice:9217:练习服务"
  "cert:9218:证书服务"
  "proctor:9219:防作弊服务"
  "ai:9220:AI 服务"
)

log() { printf '\033[36m[build]\033[0m %s\n' "$*"; }
warn() { printf '\033[33m[warn]\033[0m %s\n' "$*" >&2; }
die() { printf '\033[31m[fail]\033[0m %s\n' "$*" >&2; exit 1; }

# 支持两种写法：question 或 ruoyi-exam-question
normalize() {
  local name="$1"
  name="${name#ruoyi-exam-}"
  printf '%s' "$name"
}

resolve() {
  local want="$1"
  for item in "${ALL_MODULES[@]}"; do
    if [[ "${item%%:*}" == "$want" ]]; then
      printf '%s' "$item"
      return 0
    fi
  done
  return 1
}

usage() {
  cat <<'TXT'
用法：build-images.sh [模块短名...]

模块短名（不带 ruoyi-exam- 前缀也可以）：
TXT
  for item in "${ALL_MODULES[@]}"; do
    IFS=':' read -r short port desc <<<"$item"
    printf '  %-10s %s  %s\n' "$short" "$port" "$desc"
  done
  cat <<'TXT'

环境变量：TAG PREFIX SKIP_PACKAGE PUSH PLATFORM WITH_AGENT MVN
示例：TAG=v1.0.0 PUSH=1 PREFIX=registry.example.com/exam ./build-images.sh manage question
TXT
}

if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
  usage
  exit 0
fi

cd "$ROOT_DIR"

# ---------- 1. 解析要处理的模块 ----------
SELECTED=()
if [[ $# -eq 0 ]]; then
  SELECTED=("${ALL_MODULES[@]}")
else
  for arg in "$@"; do
    short="$(normalize "$arg")"
    item="$(resolve "$short")" || die "未知模块：$arg（可选：$(printf '%s ' "${ALL_MODULES[@]%%:*}")）"
    SELECTED+=("$item")
  done
fi

# ---------- 2. 打包 ----------
if [[ "$SKIP_PACKAGE" == "1" ]]; then
  log "SKIP_PACKAGE=1，跳过 maven 打包"
else
  modules_csv=""
  for item in "${SELECTED[@]}"; do
    modules_csv+=",ruoyi-modules/ruoyi-exam-${item%%:*}"
  done
  modules_csv="${modules_csv:1}"
  log "maven 打包：$modules_csv"
  # -am 会把依赖的 ruoyi-api / ruoyi-common 一起编；这些模块会被打成依赖 jar，不是 fat jar
  "$MVN" -DskipTests -pl "$modules_csv" -am package
fi

# ---------- 3. 逐个打镜像 ----------
build_and_push() {
  local image="$1" dir="$2" dockerfile="${3:-$2/Dockerfile}"
  local -a cmd
  cmd=(docker build)
  if [[ -n "$PLATFORM" ]]; then
    cmd+=(--platform "$PLATFORM")
  fi
  cmd+=(-t "$image" -f "$dockerfile" "$dir")
  log "docker build -> $image"
  "${cmd[@]}"
  if [[ "$PUSH" == "1" ]]; then
    log "docker push $image"
    docker push "$image"
  fi
}

failed=()
for item in "${SELECTED[@]}"; do
  IFS=':' read -r short port desc <<<"$item"
  mod="ruoyi-exam-$short"
  dir="ruoyi-modules/$mod"
  jar="$dir/target/$mod.jar"

  if [[ ! -f "$jar" ]]; then
    warn "跳过 $mod：没有 $jar（先跑一次打包，或确认 mvn 是否成功）"
    failed+=("$mod(无 jar)")
    continue
  fi
  if [[ ! -f "$dir/Dockerfile" ]]; then
    warn "跳过 $mod：没有 $dir/Dockerfile"
    failed+=("$mod(无 Dockerfile)")
    continue
  fi

  image="$PREFIX/$mod:$TAG"
  if ! build_and_push "$image" "$dir"; then
    warn "$mod 构建失败"
    failed+=("$mod(构建失败)")
  fi
done

# ---------- 4. Python 侧 AI Agent ----------
# exam-ai 会回调 9221 的 Python 服务，所以它也是这套东西的一部分
if [[ "$WITH_AGENT" == "1" ]]; then
  agent_dir="ruoyi-agent/ruoyi-exam-agent"
  agent_image="$PREFIX/ruoyi-exam-agent:$TAG"
  if [[ -f "$agent_dir/Dockerfile" ]]; then
    build_and_push "$agent_image" "$agent_dir" || warn "AI Agent 构建失败"
  else
    warn "没有找到 $agent_dir/Dockerfile，跳过 Agent 镜像"
  fi
fi

# ---------- 5. 收尾 ----------
echo
if [[ ${#failed[@]} -gt 0 ]]; then
  die "以下模块没打成功：${failed[*]}"
fi
log "全部完成，镜像列表："
docker images --filter "reference=$PREFIX/ruoyi-exam-*" --format 'table {{.Repository}}\t{{.Tag}}\t{{.Size}}'
