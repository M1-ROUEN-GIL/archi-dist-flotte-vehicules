#!/usr/bin/env bash
set -euo pipefail

# ── Couleurs ──────────────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; NC='\033[0m'

# ── Usage ─────────────────────────────────────────────────────────────────────
usage() {
  echo -e "Usage: $0 [docker|minikube]"
  echo -e "  docker     Lance les tests contre http://localhost:3005 (Docker Compose)"
  echo -e "  minikube   Lance les tests contre http://flotte.local (Minikube)"
  exit 1
}

# ── Arguments ─────────────────────────────────────────────────────────────────
MODE="${1:-}"

case "$MODE" in
  docker)
    BASE_URL="http://localhost:3005"
    ;;
  minikube)
    BASE_URL="http://flotte.local"
    ;;
  *)
    usage
    ;;
esac

# ── Vérification réseau ───────────────────────────────────────────────────────
echo -e "${CYAN}[1/3] Vérification de l'accès à $BASE_URL...${NC}"
if ! curl -sf --max-time 5 "$BASE_URL" > /dev/null; then
  echo -e "${RED}Impossible d'atteindre $BASE_URL. L'environnement $MODE est-il démarré ?${NC}"
  exit 1
fi
echo -e "${GREEN}OK — $BASE_URL est accessible.${NC}"

# ── Lancement des tests ───────────────────────────────────────────────────────
echo -e "${CYAN}[2/3] Lancement des tests E2E (mode : $MODE)...${NC}"
cd "$(dirname "$0")/../frontend"

EXIT_CODE=0
BASE_URL="$BASE_URL" npx playwright test --reporter=list,html || EXIT_CODE=$?

# ── Rapport ───────────────────────────────────────────────────────────────────
echo ""
if [[ $EXIT_CODE -eq 0 ]]; then
  echo -e "${GREEN}[3/3] Tous les tests sont passés !${NC}"
else
  echo -e "${RED}[3/3] Des tests ont échoué (code $EXIT_CODE).${NC}"
fi

echo -e "${CYAN}Ouverture du rapport HTML...${NC}"
npx playwright show-report

exit $EXIT_CODE
