package iq.stock_checker;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class StockMonitorService {

    private static final String BOT_TOKEN = "YOUR_BOT_TOKEN";
    private static final String CHAT_ID = "YOUR_CHAT_ID";

    private volatile String buyNowXpath = "div.btn-buynow.btn";

    private final List<String> liveLogs = new CopyOnWriteArrayList<>();
    private final List<String> buyNowLogs = new CopyOnWriteArrayList<>();

    private volatile boolean running = false;
    private ExecutorService pool;

    public List<String> getLiveLogs() { return liveLogs; }
    public List<String> getBuyNowLogs() { return buyNowLogs; }

    public void clearLiveLogs() { liveLogs.clear(); }
    public void clearBuyNowLogs() { buyNowLogs.clear(); }

    public synchronized void start(List<String> urls) {
        if (running) {
            liveLogs.add("⚠️ Already running");
            return;
        }

        running = true;
        liveLogs.clear();
        buyNowLogs.clear();
        liveLogs.add("✅ Started monitoring");

        pool = Executors.newFixedThreadPool(urls.size());

        for (String url : urls) {
            if (!url.trim().isEmpty()) {
                pool.submit(() -> monitor(url.trim()));
            }
        }
    }

    public synchronized void stop() {
        running = false;
        if (pool != null) {
            pool.shutdownNow();
            pool = null;
        }
        liveLogs.add("🛑 Monitoring stopped");
    }

    private void monitor(String url) {
        liveLogs.add("▶️ Checking started: " + url);

        while (running) {
            try {
                Thread.sleep(3000);

                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0")
                        .timeout(5000)
                        .get();

                Element buyNowBtn = doc.selectFirst(buyNowXpath);

                if (buyNowBtn != null) {
                    buyNowLogs.add("🎉 IN STOCK: " + url);
                    liveLogs.add("[" + url + "] ➜ BUY NOW");
                    sendTelegramMessage("IN STOCK: " + url);
                    break;
                } else {
                    liveLogs.add("[" + url + "] ➜ OUT OF STOCK");
                }

                Thread.sleep(2000);

            } catch (Exception e) {
                liveLogs.add("❌ ERROR: " + url);
                liveLogs.add("Reason: " + e.getMessage());
                try { Thread.sleep(2000); } catch (Exception ignored) {}
            }
        }
    }

    private void sendTelegramMessage(String message) {
        try {
            String text = URLEncoder.encode(message, "UTF-8");
            String urlString =
                    "https://api.telegram.org/bot" + BOT_TOKEN +
                    "/sendMessage?chat_id=" + CHAT_ID +
                    "&text=" + text;

            HttpURLConnection conn =
                    (HttpURLConnection) new URL(urlString).openConnection();
            conn.setRequestMethod("GET");
            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception ignored) {}
    }

    public void setBuyNowXpath(String xpath) {
        if (xpath != null && !xpath.trim().isEmpty()) {
            this.buyNowXpath = xpath.trim();
            liveLogs.add("🔧 BuyNow selector updated: " + this.buyNowXpath);
        }
    }

    public String getBuyNowXpath() {
        return buyNowXpath;
    }
}
