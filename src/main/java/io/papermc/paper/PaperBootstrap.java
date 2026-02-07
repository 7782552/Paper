package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class PaperBootstrap {
    
    // ========== 改这里 ==========
    static String geminiApiKey = "换成你的新KEY";
    static String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";
    static String model = "google/gemini-1.5-flash";
    // ============================
    
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw + N8N] 启动中...");
        
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GEMINI_API_KEY", geminiApiKey);

            // 删除 Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 检查配置
            File configFile = new File(baseDir + "/.openclaw/openclaw.json");
            if (!configFile.exists()) {
                System.out.println("📝 首次运行 onboard...");
                File openclawDir = new File(baseDir + "/.openclaw");
                if (openclawDir.exists()) deleteDirectory(openclawDir);
                Thread.sleep(500);
                
                ProcessBuilder onboardPb = new ProcessBuilder(
                    nodeBin, ocBin, "onboard",
                    "--non-interactive", "--accept-risk",
                    "--mode", "local",
                    "--auth-choice", "gemini-api-key",
                    "--gemini-api-key", geminiApiKey,
                    "--gateway-port", "18789",
                    "--gateway-bind", "lan",
                    "--gateway-auth", "token",
                    "--gateway-token", "admin123",
                    "--skip-daemon", "--skip-channels",
                    "--skip-skills", "--skip-health", "--skip-ui"
                );
                onboardPb.environment().putAll(env);
                onboardPb.directory(new File(baseDir));
                onboardPb.inheritIO();
                onboardPb.start().waitFor();
                Thread.sleep(2000);
                
                String config = createConfig(model, telegramToken);
                Files.write(configFile.toPath(), config.getBytes());
            } else {
                System.out.println("✅ 使用现有配置");
            }

            // 创建反向代理脚本
            System.out.println("📝 创建反向代理...");
            File proxyFile = new File(baseDir + "/proxy.mjs");
            Files.write(proxyFile.toPath(), createProxyScript().getBytes());

            // 启动 n8n
            System.out.println("🚀 启动 n8n (内部端口 5678)...");
            File n8nDir = new File(baseDir + "/.n8n");
            if (!n8nDir.exists()) n8nDir.mkdirs();

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

            // 启动 OpenClaw Gateway
            System.out.println("🚀 启动 OpenClaw Gateway (内部端口 18789)...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.directory(new File(baseDir));
            gatewayPb.inheritIO();
            gatewayPb.start();

            System.out.println("⏳ 等待服务启动...");
            Thread.sleep(10000);

            System.out.println("");
            System.out.println("═".repeat(55));
            System.out.println("🎉 所有服务已启动！");
            System.out.println("═".repeat(55));
            System.out.println("📌 模型: " + model);
            System.out.println("🤖 Telegram Bot: 已启动");
            System.out.println("");
            System.out.println("🌐 访问地址:");
            System.out.println("   n8n:      http://你的IP:30196/");
            System.out.println("   Canvas:   http://你的IP:30196/oc/");
            System.out.println("═".repeat(55));

            ProcessBuilder proxyPb = new ProcessBuilder(nodeBin, proxyFile.getAbsolutePath());
            proxyPb.environment().putAll(env);
            proxyPb.directory(new File(baseDir));
            proxyPb.inheritIO();
            proxyPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static String createProxyScript() {
        return "import http from 'http';\n" +
            "import net from 'net';\n" +
            "\n" +
            "const N8N_PORT = 5678;\n" +
            "const OC_PORT = 18789;\n" +
            "const LISTEN_PORT = 30196;\n" +
            "\n" +
            "function proxy(req, res, targetPort, rewritePath) {\n" +
            "    const path = rewritePath || req.url;\n" +
            "    \n" +
            "    const opts = {\n" +
            "        hostname: '127.0.0.1',\n" +
            "        port: targetPort,\n" +
            "        path: path,\n" +
            "        method: req.method,\n" +
            "        headers: { ...req.headers, host: '127.0.0.1:' + targetPort }\n" +
            "    };\n" +
            "    \n" +
            "    const proxyReq = http.request(opts, (proxyRes) => {\n" +
            "        // 修改响应中的重定向路径\n" +
            "        let headers = { ...proxyRes.headers };\n" +
            "        if (headers.location && targetPort === OC_PORT) {\n" +
            "            headers.location = headers.location.replace('/__openclaw__/canvas', '/oc');\n" +
            "        }\n" +
            "        res.writeHead(proxyRes.statusCode, headers);\n" +
            "        proxyRes.pipe(res);\n" +
            "    });\n" +
            "    \n" +
            "    proxyReq.on('error', (e) => {\n" +
            "        console.error('Proxy error to port ' + targetPort + ':', e.message);\n" +
            "        res.writeHead(502);\n" +
            "        res.end('Bad Gateway: ' + e.message + ' (target port: ' + targetPort + ')');\n" +
            "    });\n" +
            "    \n" +
            "    req.pipe(proxyReq);\n" +
            "}\n" +
            "\n" +
            "function proxyWebSocket(req, socket, head, targetPort) {\n" +
            "    const target = net.connect(targetPort, '127.0.0.1', () => {\n" +
            "        target.write(\n" +
            "            req.method + ' ' + req.url + ' HTTP/1.1\\r\\n' +\n" +
            "            Object.entries(req.headers).map(([k,v]) => k + ': ' + v).join('\\r\\n') +\n" +
            "            '\\r\\n\\r\\n'\n" +
            "        );\n" +
            "        if (head && head.length) target.write(head);\n" +
            "        socket.pipe(target).pipe(socket);\n" +
            "    });\n" +
            "    \n" +
            "    target.on('error', (e) => {\n" +
            "        console.error('WS proxy error:', e.message);\n" +
            "        socket.end();\n" +
            "    });\n" +
            "    socket.on('error', () => target.end());\n" +
            "}\n" +
            "\n" +
            "const server = http.createServer((req, res) => {\n" +
            "    const url = req.url || '/';\n" +
            "    console.log('[proxy] ' + req.method + ' ' + url);\n" +
            "    \n" +
            "    // /oc 或 /oc/* -> OpenClaw Canvas\n" +
            "    if (url === '/oc' || url.startsWith('/oc/') || url.startsWith('/oc?')) {\n" +
            "        let newPath = url.replace(/^\\/oc/, '/__openclaw__/canvas');\n" +
            "        if (newPath === '/__openclaw__/canvas') newPath = '/__openclaw__/canvas/';\n" +
            "        console.log('[proxy] -> OpenClaw: ' + newPath);\n" +
            "        proxy(req, res, OC_PORT, newPath);\n" +
            "    }\n" +
            "    // /__openclaw__/* -> OpenClaw (直接访问)\n" +
            "    else if (url.startsWith('/__openclaw__')) {\n" +
            "        console.log('[proxy] -> OpenClaw: ' + url);\n" +
            "        proxy(req, res, OC_PORT, url);\n" +
            "    }\n" +
            "    // 其他所有 -> n8n\n" +
            "    else {\n" +
            "        console.log('[proxy] -> n8n: ' + url);\n" +
            "        proxy(req, res, N8N_PORT, url);\n" +
            "    }\n" +
            "});\n" +
            "\n" +
            "server.on('upgrade', (req, socket, head) => {\n" +
            "    const url = req.url || '/';\n" +
            "    let targetPort = N8N_PORT;\n" +
            "    \n" +
            "    if (url.startsWith('/oc') || url.startsWith('/__openclaw__')) {\n" +
            "        targetPort = OC_PORT;\n" +
            "        if (url.startsWith('/oc')) {\n" +
            "            req.url = url.replace(/^\\/oc/, '/__openclaw__/canvas');\n" +
            "        }\n" +
            "    }\n" +
            "    \n" +
            "    console.log('[ws] ' + url + ' -> port ' + targetPort);\n" +
            "    proxyWebSocket(req, socket, head, targetPort);\n" +
            "});\n" +
            "\n" +
            "server.listen(LISTEN_PORT, '0.0.0.0', () => {\n" +
            "    console.log('');\n" +
            "    console.log('🌐 反向代理已启动: http://0.0.0.0:' + LISTEN_PORT);\n" +
            "    console.log('   /       -> n8n (port 5678)');\n" +
            "    console.log('   /oc/    -> OpenClaw Canvas (port 18789)');\n" +
            "    console.log('');\n" +
            "});\n";
    }

    static String createConfig(String modelName, String botToken) {
        return "{\n" +
            "  \"auth\": { \"profiles\": { \"google:default\": { \"provider\": \"google\", \"mode\": \"api_key\" } } },\n" +
            "  \"agents\": { \"defaults\": { \"model\": { \"primary\": \"" + modelName + "\" }, \"workspace\": \"/home/container/.openclaw/workspace\" } },\n" +
            "  \"channels\": { \"telegram\": { \"dmPolicy\": \"open\", \"botToken\": \"" + botToken + "\", \"groupPolicy\": \"open\", \"streamMode\": \"partial\", \"allowFrom\": [\"*\"] } },\n" +
            "  \"gateway\": { \"port\": 18789, \"bind\": \"lan\", \"auth\": { \"mode\": \"token\", \"token\": \"admin123\" } },\n" +
            "  \"plugins\": { \"entries\": { \"telegram\": { \"enabled\": true } } }\n" +
            "}";
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
