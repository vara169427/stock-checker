package iq.stock_checker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StockMonitorService {

    private static final String BOT_TOKEN = "8268301332:AAF6LlMrEVBCkR9FaZ87nr8CIP0cNfynCqM";
    private static final String CHAT_ID = "1456153642";
    private final java.util.List<String> logs =
            new java.util.concurrent.CopyOnWriteArrayList<>();
    public java.util.List<String> getLogs() {
        return logs;
    }

    private volatile boolean running = false;
    private ExecutorService pool;

    public synchronized void start(List<String> urls) {
        if (running) {
            logs.add("⚠️ Already running");
            return;
        }

        // clear old logs and print start log
        logs.clear();
        logs.add("✅ Started monitoring");

        running = true;
        pool = Executors.newFixedThreadPool(urls.size());

        for (String url : urls) {
            String trimmed = url.trim();
            if (!trimmed.isEmpty()) {
                pool.submit(() -> monitor(trimmed));
            }
        }
    }

    public synchronized void stop() {
        running = false;

        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }

        logs.add("🛑 Monitoring stopped");
    }
    
    private void monitor(String url) {
        logs.add("Monitoring started for: " + url);

        while (running) {
            try {
                logs.add("Checking: " + url);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .get();

                Element button = doc.selectFirst("div.btn-buynow.btn");
                String text = button != null ? button.text().trim() : "";

                logs.add("[" + url + "] Status: " + text);

                if ("Buy Now".equalsIgnoreCase(text)) {
                    logs.add("✅ BUY NOW AVAILABLE! URL: " + url);
                    sendTelegramMessage("BUY NOW available at: " + url);
                    break; // stop for this URL
                }

                Thread.sleep(1000); // refresh every 1 second
            } catch (Exception e) {
                logs.add("❌ Error while checking " + url);
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }

        logs.add("Stopped monitoring: " + url);
    }


    private void sendTelegramMessage(String message) {
        try {
            String text = URLEncoder.encode(message, "UTF-8");
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN +
                    "/sendMessage?chat_id=" + CHAT_ID + "&text=" + text;

            HttpURLConnection conn = (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            System.out.println("Telegram response code: " + code);
            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
  

	public void clearLogs() {
		// TODO Auto-generated method stub
		logs.clear();
	}

}
