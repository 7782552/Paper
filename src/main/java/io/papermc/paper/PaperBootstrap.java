import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class PaperBootstrap {
    public static void main(String[] args) {
        // 1. 确定 Node 路径（如果您的 node 不在 /usr/local/bin，请改一下这里）
        String fullNodePath = "node"; 

        // 2. 构造启动命令：强制开启 OpenAI 适配器模式
        List<String> command = new ArrayList<>();
        command.add(fullNodePath);
        command.add("dist/index.js");
        command.add("gateway");
        command.add("--force");
        command.add("--port");
        command.add("18789");
        command.add("--openai-adapter"); // 👈 开启 HTTP API 的命门
        command.add("true");
        command.add("--api-prefix");
        command.add("/v1");
        command.add("--token");
        command.add("mytoken123");

        ProcessBuilder clawPb = new ProcessBuilder(command);

        // 3. 强制注入环境变量，双重保险
        clawPb.environment().put("OPENAI_ADAPTER", "true");
        clawPb.environment().put("API_PREFIX", "/v1");
        clawPb.environment().put("TOKEN", "mytoken123");

        try {
            // 4. 合并错误流，让日志更清晰
            clawPb.redirectErrorStream(true);
            Process process = clawPb.start();

            System.out.println("🚀 OpenClaw 正在启动，端口: 18789...");
            
            // 5. 实时打印日志到控制台，爹您就盯着这儿看
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                // 只要看到这一行，说明成功了！
                if (line.contains("openai adapter enabled")) {
                    System.out.println("✅ 【证据】HTTP API 已开启，n8n 可以连接了！");
                }
            }
            
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
