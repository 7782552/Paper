package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 (官方文档 G4F 推荐模式)...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            
            // ===== 核心配置 =====
            String baseUrl = "https://888888888888.zeabur.app/v1"; 
            String apiKey = "sk-g4f-2026"; 
            // ===================
            
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
            String gatewayToken = "admin123";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", baseDir + "/node-v22/bin:" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            // 依然保留环境变量作为兜底
            env.put("OPENAI_API_KEY", apiKey);
            env.put("OPENAI_BASE_URL", baseUrl);
            env.put("OPENCLAW_GATEWAY_TOKEN", gatewayToken);

            File openclawDir = new File(baseDir + "/.openclaw");
            if (openclawDir.exists()) { deleteDirectory(openclawDir); }
            openclawDir.mkdirs();

            System.out.println("📝 写入官方格式 openclaw.json...");
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            
            /* 注意看这里的结构变化：
               1. 增加一级节点 "providers"
               2. model 命名为 "my-g4f/gpt-4o-mini"
               这样 OpenClaw 绝对不会去连 OpenAI 官方
            */
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"providers\": {\n");
            sb.append("    \"my-g4f\": {\n"); // 自定义名字
            sb.append("      \"type\": \"openai\",\n"); // 告诉它这是 OpenAI 兼容协议
            sb.append("      \"baseUrl\": \"").append(baseUrl).append("\",\n");
            sb.append("      \"apiKey\": \"").append(apiKey).append("\"\n");
            sb.append("    }\n");
            sb.append("  },\n");
            sb.append("  \"agents\": {\n");
            sb.append("    \"defaults\": {\n");
            sb.append("      \"model\": {\n");
            sb.append("        \"primary\": \"my-g4f/gpt-4o-mini\"\n"); // 强制指向上面的 provider
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

            // --- 你的 Proxy 逻辑，原封不动 ---
            StringBuilder proxy = new StringBuilder();
            proxy.append("const http = require('http');\n");
            proxy.append("const httpProxy = require('http-proxy');\n");
            proxy.append("const proxy = httpProxy.createProxyServer({ ws: true, xfwd: true });\n");
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
            proxy.append("server.listen(30196, '0.0.0.0');\n");
            Files.write(new File(baseDir + "/proxy.js").toPath(), proxy.toString().getBytes());

            System.out.println("🚀 启动服务中...");
            
            // 启动 n8n
            ProcessBuilder n8nPb = new ProcessBuilder(nodeBin, baseDir + "/node_modules/.bin/n8n", "start");
            n8nPb.environment().putAll(env);
            n8nPb.start();

            // 启动 Gateway
            ProcessBuilder gatewayPb = new ProcessBuilder(nodeBin, ocBin, "gateway");
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start();

            Thread.sleep(8000);
            new ProcessBuilder(nodeBin, baseDir + "/proxy.js").inheritIO().start().waitFor();

        } catch (Exception e) { e.printStackTrace(); }
    }

    static void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) { for (File f : files) { if (f.isDirectory()) deleteDirectory(f); else f.delete(); } }
        dir.delete();
    }
}
