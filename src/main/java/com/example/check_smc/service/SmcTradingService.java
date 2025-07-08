package com.example.check_smc.service;

import com.example.check_smc.model.CryptoCurrency;
import com.example.check_smc.model.TradingOpportunity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Main service for SMC trading opportunity detection and analysis
 */
@Service
public class SmcTradingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SmcTradingService.class);
    
    @Autowired
    private CoinMarketCapScraper coinMarketCapScraper;
    
    @Autowired
    private SmcAnalysisEngine smcAnalysisEngine;
    
    private final ObjectMapper objectMapper;
    
    public SmcTradingService() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    /**
     * Main method to find and analyze trading opportunities
     */
    public List<TradingOpportunity> findTradingOpportunities() {
        logger.info("Starting SMC trading opportunity analysis...");
        
        // Step 1: Scrape cryptocurrency data
        List<CryptoCurrency> cryptocurrencies = scrapeMarketData();
        
        // Step 2: Filter potential candidates
        List<CryptoCurrency> candidates = filterCandidates(cryptocurrencies);
        
        // Step 3: Analyze each candidate for SMC patterns
        List<TradingOpportunity> opportunities = analyzeCandidates(candidates);
        
        // Step 4: Filter and rank opportunities
        List<TradingOpportunity> filteredOpportunities = filterAndRankOpportunities(opportunities);
        
        // Step 5: Output results
        outputResults(filteredOpportunities);
        
        logger.info("SMC analysis completed. Found {} opportunities", filteredOpportunities.size());
        
        return filteredOpportunities;
    }
    
    /**
     * Scrapes market data from multiple sources
     */
    private List<CryptoCurrency> scrapeMarketData() {
        logger.info("Scraping market data...");
        
        List<CryptoCurrency> allCryptocurrencies = new ArrayList<>();
        
        try {
            // Get top cryptocurrencies
            List<CryptoCurrency> topCoins = coinMarketCapScraper.scrapeTopCryptocurrencies(50);
            allCryptocurrencies.addAll(topCoins);
            
            // Get high volume coins
            List<CryptoCurrency> highVolumeCoins = coinMarketCapScraper.scrapeHighVolumeCoins();
            allCryptocurrencies.addAll(highVolumeCoins);
            
            // Get volatile coins
            List<CryptoCurrency> volatileCoins = coinMarketCapScraper.scrapeVolatileCoins();
            allCryptocurrencies.addAll(volatileCoins);
            
            // Remove duplicates (by symbol)
            allCryptocurrencies = allCryptocurrencies.stream()
                    .filter(crypto -> crypto.getSymbol() != null)
                    .collect(Collectors.toMap(
                            CryptoCurrency::getSymbol,
                            crypto -> crypto,
                            (existing, replacement) -> existing))
                    .values()
                    .stream()
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Error scraping market data: {}", e.getMessage());
            // Fallback to sample data for demonstration
            allCryptocurrencies = createSampleData();
        }
        
        logger.info("Scraped {} unique cryptocurrencies", allCryptocurrencies.size());
        return allCryptocurrencies;
    }
    
    /**
     * Filters cryptocurrencies based on SMC criteria
     */
    private List<CryptoCurrency> filterCandidates(List<CryptoCurrency> cryptocurrencies) {
        logger.info("Filtering candidates based on SMC criteria...");
        
        List<CryptoCurrency> candidates = cryptocurrencies.stream()
                .filter(this::meetsSMCCriteria)
                .collect(Collectors.toList());
        
        logger.info("Filtered to {} candidates", candidates.size());
        return candidates;
    }
    
    /**
     * Checks if a cryptocurrency meets SMC criteria
     */
    private boolean meetsSMCCriteria(CryptoCurrency crypto) {
        // Volume criteria - minimum $10M 24h volume
        BigDecimal minVolume = new BigDecimal("10000000");
        if (crypto.getVolume24h() == null || crypto.getVolume24h().compareTo(minVolume) < 0) {
            return false;
        }
        
        // Volatility criteria - minimum 2% change in 24h
        BigDecimal minVolatility = new BigDecimal("2");
        if (crypto.getPercentChange24h() == null || 
            crypto.getPercentChange24h().abs().compareTo(minVolatility) < 0) {
            return false;
        }
        
        // Market cap criteria - minimum $100M market cap
        BigDecimal minMarketCap = new BigDecimal("100000000");
        if (crypto.getMarketCap() == null || crypto.getMarketCap().compareTo(minMarketCap) < 0) {
            return false;
        }
        
        // Price criteria - exclude very low price coins (< $0.01)
        BigDecimal minPrice = new BigDecimal("0.01");
        if (crypto.getCurrentPrice() == null || crypto.getCurrentPrice().compareTo(minPrice) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Analyzes candidates for SMC patterns
     */
    private List<TradingOpportunity> analyzeCandidates(List<CryptoCurrency> candidates) {
        logger.info("Analyzing {} candidates for SMC patterns...", candidates.size());
        
        List<TradingOpportunity> opportunities = new ArrayList<>();
        
        for (CryptoCurrency crypto : candidates) {
            try {
                TradingOpportunity opportunity = smcAnalysisEngine.analyzeForSmcOpportunity(crypto);
                opportunities.add(opportunity);
            } catch (Exception e) {
                logger.warn("Error analyzing {}: {}", crypto.getSymbol(), e.getMessage());
            }
        }
        
        logger.info("Analyzed {} cryptocurrencies, found {} potential opportunities", 
                   candidates.size(), opportunities.size());
        
        return opportunities;
    }
    
    /**
     * Filters and ranks trading opportunities
     */
    private List<TradingOpportunity> filterAndRankOpportunities(List<TradingOpportunity> opportunities) {
        logger.info("Filtering and ranking opportunities...");
        
        List<TradingOpportunity> filteredOpportunities = opportunities.stream()
                .filter(this::isValidOpportunity)
                .sorted(Comparator.comparing(TradingOpportunity::getConfidenceScore).reversed())
                .limit(20) // Top 20 opportunities
                .collect(Collectors.toList());
        
        logger.info("Filtered to {} top opportunities", filteredOpportunities.size());
        return filteredOpportunities;
    }
    
    /**
     * Validates if an opportunity meets minimum criteria
     */
    private boolean isValidOpportunity(TradingOpportunity opportunity) {
        // Minimum confidence threshold
        BigDecimal minConfidence = new BigDecimal("0.3");
        if (opportunity.getConfidenceScore() == null || 
            opportunity.getConfidenceScore().compareTo(minConfidence) < 0) {
            return false;
        }
        
        // Must have at least one SMC signal
        TradingOpportunity.SmcSignals signals = opportunity.getSmcSignals();
        return signals.isHasBOS() || signals.isHasCHOCH() || 
               signals.isHasOrderBlockRetest() || signals.isHasSupplyDemandImbalance();
    }
    
    /**
     * Outputs results to console and JSON file
     */
    private void outputResults(List<TradingOpportunity> opportunities) {
        // Console output
        displayToConsole(opportunities);
        
        // JSON file output
        saveToJsonFile(opportunities);
    }
    
    /**
     * Displays opportunities to console
     */
    private void displayToConsole(List<TradingOpportunity> opportunities) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("SMC TRADING OPPORTUNITIES ANALYSIS RESULTS");
        System.out.println("=".repeat(80));
        System.out.println("Analysis Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Total Opportunities Found: " + opportunities.size());
        System.out.println();
        
        if (opportunities.isEmpty()) {
            System.out.println("No trading opportunities found meeting SMC criteria.");
            return;
        }
        
        for (int i = 0; i < opportunities.size(); i++) {
            TradingOpportunity opp = opportunities.get(i);
            System.out.println(String.format("--- OPPORTUNITY #%d ---", i + 1));
            System.out.println("Coin: " + opp.getCryptocurrency().getName() + " (" + opp.getCryptocurrency().getSymbol() + ")");
            System.out.println("Current Price: $" + opp.getCurrentPrice());
            System.out.println("Opportunity Type: " + opp.getType());
            System.out.println("Confidence Score: " + String.format("%.1f%%", opp.getConfidenceScore().multiply(new BigDecimal("100")).doubleValue()));
            System.out.println("Suggested Entry: $" + opp.getSuggestedEntryPrice());
            System.out.println("Stop Loss: $" + opp.getStopLoss());
            System.out.println("Take Profit: $" + opp.getTakeProfit());
            
            System.out.println("\nSMC Signals:");
            TradingOpportunity.SmcSignals signals = opp.getSmcSignals();
            System.out.println("  - Break of Structure (BOS): " + (signals.isHasBOS() ? "✓" : "✗"));
            System.out.println("  - Change of Character (CHOCH): " + (signals.isHasCHOCH() ? "✓" : "✗"));
            System.out.println("  - Order Block Retest: " + (signals.isHasOrderBlockRetest() ? "✓" : "✗"));
            System.out.println("  - Supply/Demand Imbalance: " + (signals.isHasSupplyDemandImbalance() ? "✓" : "✗"));
            
            if (opp.getKeyLevels() != null && !opp.getKeyLevels().isEmpty()) {
                System.out.println("\nKey Price Levels:");
                opp.getKeyLevels().stream()
                        .limit(3) // Show top 3 levels
                        .forEach(level -> System.out.println(String.format("  - %s: $%s (Strength: %.2f)", 
                                level.getType(), level.getPrice(), level.getStrength())));
            }
            
            System.out.println("\nAnalysis: " + opp.getAnalysis());
            System.out.println("\n" + "-".repeat(50));
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * Saves opportunities to JSON file
     */
    private void saveToJsonFile(List<TradingOpportunity> opportunities) {
        try {
            String filename = "smc_trading_opportunities_" + 
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
            
            File file = new File(filename);
            objectMapper.writeValue(file, opportunities);
            
            logger.info("Results saved to: {}", file.getAbsolutePath());
            System.out.println("\nResults saved to: " + file.getAbsolutePath());
            
        } catch (IOException e) {
            logger.error("Error saving to JSON file: {}", e.getMessage());
        }
    }
    
    /**
     * Creates sample data for demonstration when scraping fails
     */
    private List<CryptoCurrency> createSampleData() {
        List<CryptoCurrency> sampleData = new ArrayList<>();
        
        // Bitcoin
        CryptoCurrency btc = new CryptoCurrency("BTC", "Bitcoin", new BigDecimal("45000"));
        btc.setMarketCap(new BigDecimal("850000000000"));
        btc.setVolume24h(new BigDecimal("25000000000"));
        btc.setPercentChange24h(new BigDecimal("2.5"));
        btc.setPercentChange7d(new BigDecimal("8.2"));
        btc.setLastUpdated(LocalDateTime.now());
        sampleData.add(btc);
        
        // Ethereum
        CryptoCurrency eth = new CryptoCurrency("ETH", "Ethereum", new BigDecimal("2800"));
        eth.setMarketCap(new BigDecimal("320000000000"));
        eth.setVolume24h(new BigDecimal("15000000000"));
        eth.setPercentChange24h(new BigDecimal("-1.8"));
        eth.setPercentChange7d(new BigDecimal("5.4"));
        eth.setLastUpdated(LocalDateTime.now());
        sampleData.add(eth);
        
        // Solana
        CryptoCurrency sol = new CryptoCurrency("SOL", "Solana", new BigDecimal("120"));
        sol.setMarketCap(new BigDecimal("45000000000"));
        sol.setVolume24h(new BigDecimal("2500000000"));
        sol.setPercentChange24h(new BigDecimal("7.2"));
        sol.setPercentChange7d(new BigDecimal("15.8"));
        sol.setLastUpdated(LocalDateTime.now());
        sampleData.add(sol);
        
        // Cardano
        CryptoCurrency ada = new CryptoCurrency("ADA", "Cardano", new BigDecimal("0.85"));
        ada.setMarketCap(new BigDecimal("28000000000"));
        ada.setVolume24h(new BigDecimal("800000000"));
        ada.setPercentChange24h(new BigDecimal("-3.2"));
        ada.setPercentChange7d(new BigDecimal("2.1"));
        ada.setLastUpdated(LocalDateTime.now());
        sampleData.add(ada);
        
        // Polygon
        CryptoCurrency matic = new CryptoCurrency("MATIC", "Polygon", new BigDecimal("1.25"));
        matic.setMarketCap(new BigDecimal("12000000000"));
        matic.setVolume24h(new BigDecimal("600000000"));
        matic.setPercentChange24h(new BigDecimal("4.8"));
        matic.setPercentChange7d(new BigDecimal("12.3"));
        matic.setLastUpdated(LocalDateTime.now());
        sampleData.add(matic);
        
        return sampleData;
    }
}