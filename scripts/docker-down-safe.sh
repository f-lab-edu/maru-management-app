#!/usr/bin/env bash
set -euo pipefail

# Gracefully stop and remove the stack defined in docker-compose.yml
# Usage:
#   scripts/docker-down-safe.sh           # stop containers and remove network
#   scripts/docker-down-safe.sh -v        # also remove named volumes
#   scripts/docker-down-safe.sh --orphan  # also remove orphans

REMOVE_VOLUMES=false
REMOVE_ORPHANS=false
TIMEOUT=60

for arg in "$@"; do
  case "$arg" in
    -v|--volumes)
      REMOVE_VOLUMES=true
      ;;
    --orphan|--remove-orphans)
      REMOVE_ORPHANS=true
      ;;
    --timeout=*)
      TIMEOUT="${arg#*=}"
      ;;
    *)
      echo "Unknown option: $arg" >&2
      echo "Usage: $0 [-v|--volumes] [--orphan|--remove-orphans] [--timeout=N]" >&2
      exit 2
      ;;
  esac
done

# Ensure we run from repo root so compose picks the right file
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR/.."

compose_cmd() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  else
    docker-compose "$@"
  fi
}

echo "Stopping containers (timeout=${TIMEOUT}s) ..."
compose_cmd stop --timeout "$TIMEOUT" || true

DOWN_ARGS=(down --timeout "$TIMEOUT")
if [ "$REMOVE_VOLUMES" = true ]; then
  DOWN_ARGS+=(--volumes)
fi
if [ "$REMOVE_ORPHANS" = true ]; then
  DOWN_ARGS+=(--remove-orphans)
fi

echo "Bringing stack down: ${DOWN_ARGS[*]}"
compose_cmd "${DOWN_ARGS[@]}"

echo "Done."

