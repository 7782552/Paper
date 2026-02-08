package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 (修正版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            // ===== 核心配置 (请确保这里的地址和Key正确) =====
            String apiKey = "sk-g4f-local-agent-2026"; 
            String baseUrl = "https://888888888888.zeabur.app/v1"; 
            // ===========================================
            
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", baseUrl);
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            System.out.println("🗑️ 删除 Telegram Webhook...");
            try {
                URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.getResponseCode();
            } catch (Exception e) { System.out.println("⚠️ Webhook删除跳过: " + e.getMessage()); }

            System.out.println("🧹 清理旧配置...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) { deleteDirectory(openclawDir); }
            openclawDir.mkdirs();

            System.out.println("📝 写入修正后的 openclaw.json...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            // 【关键修改：定义 Provider】
            sb.append("  \"providers\": {\n");
            sb.append("    \"openai\": {\n");
            sb.append("      \"baseUrl\": \"").append(baseUrl).append("\",\n");
            sb.append("      \"apiKey\": \"").append(apiKey).append("\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            // 【定义 Agent 使用该 Provider】
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": {\n");
            sb.append("        \"primary\": \"openai/gpt-4o-mini\"\n");
            sb.append("      },\n");
            sb.append("      \"workspace\": \"/home/container/.openclaw/workspace\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"browser\": {\n");
            sb.append("    \"enabled\": true,\n");
            sb.append("    \"headless\": true\n");
            sb.append("  },\n");
            sb.append("  \"channels\": {\n");
            sb.append("    \"telegram\": {\n");
            sb.append("      \"dmPolicy\": \"open\",\n");
            sb.append("      \"botToken\": \"").append(telegramToken).append("\",\n");
            sb.append("      \"groupPolicy\": \"open\",\n");
            sb.append("      \"streamMode\": \"partial\",\n");
            sb.append("      \"allowFrom\": [\"*\"]\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"gateway\": {\n");
            sb.append("    \"port\": 18789,\n");
            sb.append("    \"mode\": \"local\",\n");
            sb.append("    \"bind\": \"lan\",\n");
            sb.append("    \"auth\": {\n");
            sb.append("      \"mode\": \"token\",\n");
            sb.append("      \"token\": \"").append(gatewayToken).append("\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"plugins\": {\n");
            sb.append("    \"entries\": {\n");
            sb.append("      \"telegram\": {\n");
            sb.append("        \"enabled\": true\n");
            sb.append("      }\n");
            sb.append("    }\n");
            sb.append("  }\n");
            sb.append("}");
            
            Files.write(configFile.toPath(), sb.toString().getBytes());

            System.out.println("📝 创建反向代理脚本...");
            // （此处保持你原有的 proxy.js 构建逻辑不变）
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http = require('http');\n");
            proxy.append("const httpProxy = require('http-proxy');\n");
            proxy.append("const proxy = httpProxy.createProxyServer({ ws: true, xfwd: true });\n");
            proxy.append("proxy.on('error', (err, req, res) => {\n");
            proxy.append("  console.error('Proxy:', err.message);\n");
            proxy.append("  if (res && res.writeHead) { res.writeHead(503); res.end('Service starting...'); }\n");
            proxy.append("});\n");
            proxy.append("const server = http.createServer((req, res) => {\n");
            proxy.append("  const host = req.headers.host || '';\n");
            proxy.append("  if (host.startsWith('5.')) {\n");
            proxy.append("    proxy.web(req, res, { target: 'http://127.0.0.1:18789' });\n");
            proxy.append("  } else {\n");
            proxy.append("    proxy.web(req, res, { target: 'http://127.0.0.1:5678' });\n");
            proxy.append("  }\n");
            proxy.append("});\n");
            proxy.append("server.on('upgrade', (req, socket, head) => {\n");
            proxy.append("  const host = req.headers.host || '';\n");
            proxy.append("  if (host.startsWith('5.')) {\n");
            proxy.append("    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:18789' });\n");
            proxy.append("  } else {\n");
            proxy.append("    proxy.ws(req, socket, head, { target: 'ws://127.0.0.1:5678' });\n");
            proxy.append("  }\n");
            proxy.append("});\n");
            proxy.append("server.listen(30196, '0.0.0.0', () => console.log('Proxy on :30196'));\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            new File(baseDir + "/.openclaw/workspace").mkdirs();
            new File(baseDir + "/.n8n").mkdirs();

            System.out.println("\n🚀 启动 n8n & Gateway...");
            // 启动逻辑保持不变
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "5678");
            n8nPb.environment().put("N8N_USER_FOLDER", baseDir + "/.n8n");
            n8nPb.inheritIO();
            n8nPb.start();

            ProcessBuilder gatewayPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken);
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start();

            Thread.sleep(10000);
            ProcessBuilder proxyPb = new ProcessBuilder(nodeBin, baseDir + "/proxy.js");
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
                if (file.isDirectory()) deleteDirectory(file);
                else file.delete();
            }
        }
        dir.delete();
    }
}
