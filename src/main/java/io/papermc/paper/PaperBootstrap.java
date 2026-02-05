package io.papermc.paper;

import java.io.*;
import java.net.*;
import java.util.*;

public class PaperBootstrap {
    public static void main(String[] args) {
        System.out.println("🦞 [OpenClaw] 正在配置 Telegram + Gemini 2.0...");
        try {
            String baseDir = "/home/container";
            String nodeBin = baseDir + "/node-v22/bin/node";
            String ocBin = baseDir + "/node_modules/.bin/openclaw";
            String geminiKey = "AIzaSyBzv_a-Q9u2TF1FVh58DT0yOJQPEMfJtqQ";
            String telegramToken = "8538523017:AAEHAyOSnY0n7dFN8YRWePk8pFzU0rQhmlM";

            Map<String, String> env = new HashMap<>();
            env.put("PATH", new File(nodeBin).getParent() + ":" + System.getenv("PATH"));
            env.put("HOME", baseDir);
            env.put("GOOGLE_API_KEY", geminiKey);
            env.put("DEBUG", "*");  // 开启调试模式

            // 0. 先测试 API Key 是否有效
            System.out.println("🧪 测试 Gemini API Key...");
            testGeminiKey(geminiKey);

            // 1. 删除 Telegram Webhook
            System.out.println("🗑️ 删除 Telegram Webhook...");
            URL url = new URL("https://api.telegram.org/bot" + telegramToken + "/deleteWebhook");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();

            // 2. 清除旧配置
            System.out.println("🗑️ 清除旧配置...");
            File configDir = new File(baseDir + "/.openclaw");
            if (configDir.exists()) {
                deleteDirectory(configDir);
            }

            // 3. 重新 onboard
            System.out.println("📝 运行 OpenClaw onboard (Gemini 2.0)...");
            ProcessBuilder onboardPb = new ProcessBuilder(
                nodeBin, ocBin, "onboard",
                "--non-interactive",
                "--accept-risk",
                "--reset",
                "--mode", "local",
                "--auth-choice", "gemini-api-key",
                "--gemini-api-key", geminiKey,
                "--gateway-port", "18789",
                "--gateway-bind", "lan",
                "--gateway-auth", "token",
                "--gateway-token", "admin123",
                "--skip-daemon",
                "--skip-skills",
                "--skip-health",
                "--skip-ui"
            );
            onboardPb.environment().putAll(env);
            onboardPb.inheritIO();
            onboardPb.start().waitFor();

            // 4. 配置 Telegram
            System.out.println("📝 配置 Telegram Bot...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "channels.telegram.botToken", telegramToken);

            // 5. 设置模型
            System.out.println("📝 设置模型为 gemini-2.0-flash...");
            runCommand(env, nodeBin, ocBin, "config", "set", 
                "agents.defaults.model.primary", "google/gemini-2.0-flash");

            // 6. 运行 doctor --fix
            System.out.println("🔧 运行 doctor --fix...");
            runCommand(env, nodeBin, ocBin, "doctor", "--fix");

            // 7. 启动 n8n
            System.out.println("🚀 启动 n8n (端口 30196)...");
            ProcessBuilder n8nPb = new ProcessBuilder(
                nodeBin, baseDir + "/node_modules/.bin/n8n", "start"
            );
            n8nPb.environment().putAll(env);
            n8nPb.environment().put("N8N_PORT", "30196");
            n8nPb.inheritIO();
            n8nPb.start();

            Thread.sleep(3000);

            // 8. 启动 Gateway（带调试）
            System.out.println("🚀 启动 OpenClaw Gateway + Telegram...");
            ProcessBuilder gatewayPb = new ProcessBuilder(
                nodeBin, ocBin, "gateway",
                "--port", "18789",
                "--bind", "lan",
                "--token", "admin123",
                "--verbose"
            );
            gatewayPb.environment().putAll(env);
            gatewayPb.inheritIO();
            gatewayPb.start().waitFor();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void testGeminiKey(String key) {
        try {
            URL url = new URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + key);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            String body = "{\"contents\":[{\"parts\":[{\"text\":\"Hi\"}]}]}";
            conn.getOutputStream().write(body.getBytes());
            
            int code = conn.getResponseCode();
            System.out.println("   API 响应码: " + code);
            
            InputStream is = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();
            if (is != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                String resp = response.toString();
                if (resp.length() > 200) {
                    resp = resp.substring(0, 200) + "...";
                }
                System.out.println("   API 响应: " + resp);
            }
        } catch (Exception e) {
            System.out.println("   API 测试失败: " + e.getMessage());
        }
    }

    static void runCommand(Map<String, String> env, String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.environment().putAll(env);
        pb.inheritIO();
        pb.start().waitFor();
    }

    static void deleteDirectory(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }
}
