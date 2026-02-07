package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            String kimiApiKey = "sk-Wm6jTDvJ9lVN06MlEpT2b3mOa0upU9z9NHPniIY0308dWHx8";  // ← 换成真实的
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("MOONSHOT_API_KEY", kimiApiKey);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");

            // 0. 删除 Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 1. 删除旧配置
            System.out.println("🧹 删除旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) {
                deleteDirectory(openclawDir);
            }
            openclawDir.mkdirs();

            // 2. 写入配置
            System.out.println("📝 写入配置...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            String config = "{\n" +
                "  \"meta\": {\n" +
                "    \"lastTouchedVersion\": \"2026.2.3-1\",\n" +
                "    \"lastTouchedAt\": \"" + java.time.Instant.now().toString() + "\"\n" +
                "  },\n" +
                "  \"models\": {\n" +
                "    \"mode\": \"merge\",\n" +
                "    \"providers\": {\n" +
                "      \"moonshot\": {\n" +
                "        \"baseUrl\": \"https://api.moonshot.cn/v1\",\n" +
                "        \"apiKey\": \"" + kimiApiKey + "\",\n" +
                "        \"models\": [\n" +
                "          { \"id\": \"kimi-k2.5\", \"name\": \"Kimi K2.5\" }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" +
                "        \"primary\": \"moonshot/kimi-k2.5\"\n" +
                "      },\n" +
                "      \"workspace\": \"/home/container/.openclaw/workspace\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"browser\": {\n" +
                "    \"enabled\": true,\n" +
                "    \"headless\": true\n" +
                "  },\n" +
                "  \"channels\": {\n" +
                "    \"telegram\": {\n" +
                "      \"dmPolicy\": \"open\",\n" +
                "      \"botToken\": \"" + telegramToken + "\",\n" +
                "      \"groupPolicy\": \"open\",\n" +
                "      \"streamMode\": \"partial\",\n" +
                "      \"allowFrom\": [\"*\"]\n" +
                "    }\n" +
                "  },\n" +
                "  \"gateway\": {\n" +
                "    \"port\": 18789,\n" +
                "    \"mode\": \"local\",\n" +
                "    \"bind\": \"lan\",\n" +
                "    \"auth\": {\n" +
                "      \"mode\": \"token\",\n" +
                "      \"token\": \"admin123\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"plugins\": {\n" +
                "    \"entries\": {\n" +
                "      \"telegram\": {\n" +
                "        \"enabled\": true\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
            
            Files.write(configFile.toPath(), config.getBytes());

            // 3. 创建反向代理脚本
            System.out.println("📝 创建反向代理...");
            String proxyScript = 
                "const http = require('http');\n" +
                "const httpProxy = require('http-proxy');\n" +
                "\n" +
                "const proxy = httpProxy.createProxyServer({});\n" +
                "\n" +
                "const server = http.createServer((req, res) => {\n" +
                "  // /claw 路径 -> OpenClaw Gateway\n" +
                "  if (req.url.startsWith('/claw')) {\n" +
                "    req.url = req.url.replace('/claw', '');\n" +
                "    proxy.web(req, res, { target: 'http://127.0.0.1:18789' });\n" +
                "  } else {\n" +
                "    // 其他 -> n8n\n" +
                "    proxy.web(req, res, { target: 'http://127.0.0.1:5678' });\n" +
                "  }\n" +
                "});\n" +
                "\n" +
                "// WebSocket 支持\n" +
                "server.on('upgrade', (req, socket, head) => {\n" +
                "  if (req.url.startsWith('/claw')) {\n" +
                "    req.url = req.url.replace('/claw', '');\n" +
                "    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:18789' });\n" +
                "  } else {\n" +
                "    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:5678' });\n" +
                "  }\n" +
                "});\n" +
                "\n" +
                "server.listen(30196, '0.0.0.0', () => {\n" +
                "  console.log('🔀 代理服务器运行在 :30196');\n" +
                "  console.log('   /claw/* -> OpenClaw');\n" +
                "  console.log('   /*      -> n8n');\n" +
                "});\n";
            
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxyScript.getBytes());

            // 4. 安装 http-proxy（如果没有）
            System.out.println("📦 检查 http-proxy...");
            File httpProxyDir = new File(baseDir + "/node_modules/http-proxy");
            if (!httpProxyDir.exists()) {
                System.out.println("📦 安装 http-proxy...");
                ProcessBuilder npmPb = new ProcessBuilder(
                    baseDir + "/node-v22/bin/npm", "install", "http-proxy"
                );
                npmPb.environment().putAll(env);
                npmPb.directory(new File(baseDir));
                npmPb.inheritIO();
                npmPb.start().waitFor();
            }

            // 5. 创建目录
            new File(baseDir + "/.openclaw/workspace").mkdirs();
            new File(baseDir + "/.n8n").mkdirs();

            System.out.println("\n📋 模型: moonshot/kimi-k2.5");
            System.out.println("📋 浏览器: Chromium ✅");

            // 6. 启动 n8n（内部端口 5678）
            System.out.println("\n🚀 启动 n8n (内部端口 5678)...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, "--max-old-space-size=2048",
                baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "5678");
            n8nPb.environment().put("N8N_HOST", "127.0.0.1");
            n8nPb.environment().put("N8N_SECURE_COOKIE", "false");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.environment().put("N8N_DIAGNOSTICS_ENABLED", "false");
            n8nPb.environment().put("N8N_VERSION_NOTIFICATIONS_ENABLED", "false");
            n8nPb.environment().put("N8N_HIRING_BANNER_ENABLED", "false");
            n8nPb.directory(new File(baseDir));
            n8nPb.inheritIO();
            n8nPb.start();
            Thread.sleep(5000);

            // 7. 启动 OpenClaw Gateway（内部端口 18789）
            System.out.println("\n🚀 启动 Gateway (内部端口 18789)...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "127.0.0.1",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start();
            Thread.sleep(3000);

            // 8. 启动反向代理（公网端口 30196）
            System.out.println("\n🚀 启动反向代理 (公网端口 30196)...");
            ProcessBuilder proxyPb = new ProcessBuilder(
                nodeBin, baseDir + "/proxy.js"
            );
            proxyPb.environment().putAll(env);
            proxyPb.directory(new File(baseDir));
            proxyPb.inheritIO();
            proxyPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
