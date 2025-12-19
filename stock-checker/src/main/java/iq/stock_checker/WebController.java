package iq.stock_checker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class WebController {

    @Autowired
    private StockMonitorService service;

    @PostMapping("/start")
    public void start(@RequestBody(required = false) String body) {
        if (body == null || body.trim().isEmpty()) {
            service.getLiveLogs().add("⚠️ No URLs provided");
            return;
        }
        List<String> urls = List.of(body.split("\\R"));
        service.start(urls);
    }

    @PostMapping("/stop")
    public void stop() {
        service.stop();
    }

    @GetMapping("/logs/live")
    public List<String> liveLogs() {
        return service.getLiveLogs();
    }

    @GetMapping("/logs/buynow")
    public List<String> buyNowLogs() {
        return service.getBuyNowLogs();
    }

    @PostMapping("/logs/live/clear")
    public void clearLive() {
        service.clearLiveLogs();
    }

    @PostMapping("/logs/buynow/clear")
    public void clearBuyNow() {
        service.clearBuyNowLogs();
    }

    @GetMapping("/xpaths")
    public Map<String, String> getXpath() {
        return Map.of("buyNow", service.getBuyNowXpath());
    }

    @PostMapping("/xpaths")
    public void updateXpath(@RequestBody Map<String, String> body) {
        service.setBuyNowXpath(body.get("buyNow"));
    }
}
