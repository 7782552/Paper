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
            
            // ===== G4F 配置 =====
            String g4fApiKey = "你在n8n里用的那个API_Key";  // ← 填真实的
            String g4fBaseUrl = "https://88888888888.zeabur.app/v1";
            // ====================
            
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", g4fApiKey);
            env.put("OPENAI_BASE_URL", g4fBaseUrl);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

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

            // 2. 写入配置 - 使用 openai-compatible
            System.out.println("📝 写入配置...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            // ===== 关键改动：使用 openai-compatible =====
            String config = "{\n" +
                "  \"meta\": {\n" +
                "    \"lastTouchedVersion\": \"2026.2.3-1\",\n" +
                "    \"lastTouchedAt\": \"" + java.time.Instant.now().toString() + "\"\n" +
                "  },\n" +
                "  \"models\": {\n" +
                "    \"mode\": \"replace\",\n" +
                "    \"providers\": {\n" +
                "      \"g4f\": {\n" +                                    // ← 自定义名称
                "        \"type\": \"openai-compatible\",\n" +            // ← 关键！
                "        \"baseUrl\": \"" + g4fBaseUrl + "\",\n" +
                "        \"apiKey\": \"" + g4fApiKey + "\",\n" +
                "        \"models\": [\n" +
                "          { \"id\": \"gpt-4\", \"name\": \"GPT-4\" },\n" +
                "          { \"id\": \"gpt-4o\", \"name\": \"GPT-4o\" },\n" +
                "          { \"id\": \"gpt-3.5-turbo\", \"name\": \"GPT-3.5\" }\n" +
                "        ]\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"agents\": {\n" +
                "    \"defaults\": {\n" +
                "      \"model\": {\n" +
                "        \"primary\": \"g4f/gpt-4\"\n" +                   // ← 使用 g4f/gpt-4
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
                "    \"trustedProxies\": [\"127.0.0.1\", \"::1\", \"172.64.0.0/13\", \"173.245.48.0/20\", \"103.21.244.0/22\", \"103.22.200.0/22\", \"103.31.4.0/22\", \"141.101.64.0/18\", \"108.162.192.0/18\", \"190.93.240.0/20\", \"188.114.96.0/20\", \"197.234.240.0/22\", \"198.41.128.0/17\", \"162.158.0.0/15\", \"104.16.0.0/13\", \"104.24.0.0/14\", \"131.0.72.0/22\"],\n" +
                "    \"auth\": {\n" +
                "      \"mode\": \"token\",\n" +
                "      \"token\": \"" + gatewayToken + "\"\n" +
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

            // 3. 创建反向代理
            System.out.println("📝 创建反向代理...");
            String proxyScript = 
                "const http = require('http');\n" +
                "const httpProxy = require('http-proxy');\n" +
                "\n" +
                "const proxy = httpProxy.createProxyServer({ ws: true, xfwd: true });\n" +
                "\n" +
                "proxy.on('error', (err, req, res) => {\n" +
                "  console.error('Proxy:', err.message);\n" +
                "  if (res && res.writeHead) {\n" +
                "    res.writeHead(503);\n" +
                "    res.end('Service starting...');\n" +
                "  }\n" +
                "});\n" +
                "\n" +
                "const server = http.createServer((req, res) => {\n" +
                "  const host = req.headers.host || '';\n" +
                "  if (host.startsWith('5.')) {\n" +
                "    proxy.web(req, res, { target: 'http://127.0.0.1:18789' });\n" +
                "  } else {\n" +
                "    proxy.web(req, res, { target: 'http://127.0.0.1:5678' });\n" +
                "  }\n" +
                "});\n" +
                "\n" +
                "server.on('upgrade', (req, socket, head) => {\n" +
                "  const host = req.headers.host || '';\n" +
                "  if (host.startsWith('5.')) {\n" +
                "    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:18789' });\n" +
                "  } else {\n" +
                "    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:5678' });\n" +
                "  }\n" +
                "});\n" +
                "\n" +
                "server.listen(30196, '0.0.0.0', () => 
