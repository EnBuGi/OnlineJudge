#!/bin/bash
# 서버 최초 1회 실행 — /opt/enbuge 환경 세팅 스크립트

set -e

echo "=== EnBuGi 서버 초기 셋업 ==="

# 1. Docker 설치 확인
if ! command -v docker &> /dev/null; then
  echo "Docker 설치 중..."
  curl -fsSL https://get.docker.com | sh
  sudo usermod -aG docker $USER
fi

# 2. 작업 디렉토리 생성
sudo mkdir -p /opt/enbuge/secrets
sudo chown -R $USER:$USER /opt/enbuge

# 3. docker-compose 파일 복사 (이미 clone 되어 있다고 가정)
# cp docker-compose.*.yml /opt/enbuge/

# 4. OCI Private Key 파일 생성 (GitHub Actions가 배포 시 덮어씀 — 아래는 수동 최초 세팅용)
# echo "OCI_PRIVATE_KEY 내용을 붙여넣고 Ctrl+D로 저장:"
# cat > /opt/enbuge/secrets/oci_key.pem
# chmod 600 /opt/enbuge/secrets/oci_key.pem

# 5. 공유 네트워크 생성
docker network create enbuge_shared 2>/dev/null || true

# 6. Redis 기동 (공유, 최초 1회)
cd /opt/enbuge
REDIS_PASSWORD=${REDIS_PASSWORD:-} \
  docker compose -f docker-compose.redis.yml up -d

echo "=== 셋업 완료 ==="
echo "이제 GitHub에 push하면 자동 배포됩니다."
echo ""
echo "수동 배포(dev):  docker compose -f docker-compose.dev.yml --env-file dev.env up -d"
echo "수동 배포(prod): docker compose -f docker-compose.prod.yml --env-file prod.env up -d"
echo "로그(dev):       docker logs -f api-dev"
echo "로그(prod):      docker logs -f api-prod"
