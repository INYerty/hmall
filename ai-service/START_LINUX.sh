#!/bin/bash
# macOS/Linux 快速启动脚本
# 使用方法: chmod +x START_LINUX.sh && ./START_LINUX.sh

echo ""
echo "========================================"
echo "  AI 服务商品推荐 - 快速启动"
echo "========================================"
echo ""

echo "[1/3] 检查 Redis..."
redis-cli ping > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Redis 已就绪"
else
    echo "⚠️  Redis 未响应，请确保已启动 redis-server"
    echo "启动方式: redis-server"
fi

echo ""
echo "[2/3] 检查 MySQL..."
mysql -u root -p --execute "SELECT 1" > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ MySQL 已就绪"
else
    echo "⚠️  MySQL 未响应，请确保已启动 MySQL"
    echo "启动方式: mysql -u root -p"
fi

echo ""
echo "[3/3] 检查 Nacos..."
curl -s http://localhost:8848/nacos/v1/auth/login > /dev/null 2>&1
if [ $? -eq 0 ]; then
    echo "✓ Nacos 已就绪"
else
    echo "⚠️  Nacos 未响应（可选，但推荐启动）"
    echo "启动方式: nacos/bin/startup.sh -m standalone"
fi

echo ""
echo "========================================"
echo ""
echo "现在将启动 ai-service..."
echo ""
echo "[提示]"
echo "- 确保先启动了依赖服务 (Redis, MySQL)"
echo "- 建议同时启动 Nacos (可选)"
echo "- item-service 应该单独启动"
echo ""
echo "按 Enter 键继续启动 ai-service..."
read

cd ai-service
if [ ! -d "." ]; then
    echo "✗ 错误: 找不到 ai-service 目录"
    echo "请在项目根目录运行此脚本"
    exit 1
fi

echo ""
echo "========================================"
echo "  启动 ai-service..."
echo "========================================"
echo ""

mvn spring-boot:run

if [ $? -ne 0 ]; then
    echo ""
    echo "✗ 启动失败！请查看上面的错误日志"
    echo ""
    echo "常见问题:"
    echo "1. Redis 未启动 - 运行: redis-server"
    echo "2. MySQL 未启动 - 运行: mysql -u root -p"
    echo "3. API Key 未设置 - 设置环境变量 API-KEY"
    echo ""
    exit 1
fi

echo ""
echo "✓ ai-service 启动成功！"
echo ""
echo "推荐接口已就绪:"
echo "  GET http://localhost:8080/recommend/products?memoryId=xxx&userRequest=xxxx"
echo ""

