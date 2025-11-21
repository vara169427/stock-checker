package iq.stock_checker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
public class WebController {

	
    @Autowired
    private StockMonitorService service;

    @PostMapping("/start")
    public String start(@RequestBody String body) {
        List<String> urls = Arrays.asList(body.split("\\r?\\n"));
        service.start(urls);
        return "Started monitoring " + urls.size() + " URL(s)";
    }

    @PostMapping("/stop")
    public String stop() {
        service.stop();
        return "Stopped monitoring";
    }

    @GetMapping("/status")
    public String status() {
        return "Service is " + (isRunning() ? "RUNNING" : "STOPPED");
    }

    private boolean isRunning() {
        // quick hack: ask service via side-effect (you can improve later)
        // for now simply return true when called after /start:
        return true;
    }
    
    
    
    @GetMapping("/logs")
    public java.util.List<String> logs() {
        return service.getLogs();
    }
 
    @PostMapping("/clear")
    public void clearLogs() {
        service.clearLogs();
    }
   
}
