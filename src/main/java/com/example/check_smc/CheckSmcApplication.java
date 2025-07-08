package com.example.check_smc;

import com.example.check_smc.service.SmcTradingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CheckSmcApplication implements CommandLineRunner {

    @Autowired
    private SmcTradingService smcTradingService;

    public static void main(String[] args) {
        SpringApplication.run(CheckSmcApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting SMC Trading Opportunity Analysis...");
        System.out.println("=" + "=".repeat(50) + "=");
        
        // Run the SMC analysis
        smcTradingService.findTradingOpportunities();
        
        System.out.println("\nAnalysis completed successfully!");
    }
}
