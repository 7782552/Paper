package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 终极配置 (强制 .env 版)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            // ===== ⚠️ 请务必确认这个地址是真实可用的 ⚠️ =====
            // 如果这个地址是瞎编的，你一定会收到 401 或连接错误
            String baseUrl = "https://888888888888.zeabur.app/v1"; 
            String apiKey = "sk-no-key-required"; 
            // =============================================
            
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            // 1. 准备环境变量 Map
            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", baseUrl); 
            env.put("OPENAI_API_BASE", baseUrl); // 双重锁定
            env.put("PLAYWRIGHT_BROWSERS_PATH", baseDir + "/.playwright");
            env.put("TMPDIR", baseDir + "/tmp");
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            // 2. 暴力写入 .env 文件 (这是新加的，防止环境变量不生效)
            System.out.println("📝 强制写入 .env 文件...");
            File envFile = new File(baseDir + "/.env");
            StringBuilder envContent = new StringBuilder();
            envContent.append("OPENAI_API_KEY=").append(apiKey).append("\n");
            envContent.append("OPENAI_BASE_URL=").append(baseUrl).append("\n");
            envContent.append("OPENAI_API_BASE=").append(baseUrl).append("\n");
            envContent.append("OPENCLAW_GATEWAY_TOKEN=").append(gatewayToken).append("\n");
            Files.write(envFile.toPath(), envContent.toString().getBytes());

            System.out.println("🧹 删除旧配置目录...");
            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) { deleteDirectory(openclawDir); }
            openclawDir.mkdirs();

            System.out.println("📝 写入 openclaw.json...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            // 保持这个精简的 JSON，不要加 providers
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": {\n");
            sb.append("        \"primary\": \"openai/gpt-4o-mini\"\n");
            sb.append("      },\n");
            sb.append("      \"workspace\": \"/home/container/.openclaw/workspace\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"channels\": {\n");
            sb.append("    \"telegram\": {\n");
            sb.append("      \"botToken\": \"").append(telegramToken).append("\",\n");
            sb.append("      \"dmPolicy\": \"open\",\n");
            sb.append("      \"allowFrom\": [\"*\"]\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"gateway\": {\n");
            sb.append("    \"port\": 18789,\n");
            sb.append("    \"auth\": { \"mode\": \"token\", \"token\": \"").append(gatewayToken).append("\" }\n");
            sb.append("  }\n");
            sb.append("}");
            Files.write(configFile.toPath(), sb.toString().getBytes());

            System.out.println("📝 创建反向代理...");
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http = require('http');\n");
            proxy.append("const httpProxy = require('http-proxy');\n");
            proxy.append("const proxy = httpProxy.createProxyServer({ ws: true, xfwd: true });\n");
            proxy.append("proxy.on('error', (err, req, res) => {\n");
            proxy.append("  console.error('Proxy:', err.message);\n");
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

            System.out.println("\n🚀 启动 n8n...");
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, "--max-old-space-size=2048", baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.inheritIO();
            n8nPb.start();

            System.out.println("🚀 启动 Gateway...");
            ProcessBuilder gatewayPb = new ProcessBuilder(nodeBin, ocBin, "gateway", "--port", "18789", "--bind", "lan", "--token", gatewayToken);
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start();

            Thread.sleep(5000);
            new ProcessBuilder(nodeBin, baseDir + "/proxy.js").inheritIO().start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) { for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); } }
        dir.delete();
    }
}
