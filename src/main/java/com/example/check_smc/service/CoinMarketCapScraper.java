package com.example.check_smc.service;

import com.example.check_smc.model.CryptoCurrency;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for scraping cryptocurrency data from CoinMarketCap
 */
@Service
public class CoinMarketCapScraper {
    
    private static final Logger logger = LoggerFactory.getLogger(CoinMarketCapScraper.class);
    private static final String BASE_URL = "https://coinmarketcap.com";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    
    /**
     * Scrapes top cryptocurrencies from CoinMarketCap
     * @param limit Number of cryptocurrencies to fetch (max 100)
     * @return List of CryptoCurrency objects
     */
    public List<CryptoCurrency> scrapeTopCryptocurrencies(int limit) {
        List<CryptoCurrency> cryptocurrencies = new ArrayList<>();
        
        try {
            String url = BASE_URL + "/";
            logger.info("Scraping CoinMarketCap: {}", url);
            
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();
            
            // Select the cryptocurrency table rows
            Elements rows = document.select("table tbody tr");
            
            for (int i = 0; i < Math.min(rows.size(), limit); i++) {
                Element row = rows.get(i);
                CryptoCurrency crypto = parseTableRow(row);
                if (crypto != null) {
                    cryptocurrencies.add(crypto);
                }
            }
            
            logger.info("Successfully scraped {} cryptocurrencies", cryptocurrencies.size());
            
        } catch (IOException e) {
            logger.error("Error scraping CoinMarketCap: {}", e.getMessage());
        }
        
        return cryptocurrencies;
    }
    
    /**
     * Scrapes cryptocurrencies with high volume
     * @return List of high-volume cryptocurrencies
     */
    public List<CryptoCurrency> scrapeHighVolumeCoins() {
        List<CryptoCurrency> highVolumeCoins = new ArrayList<>();
        
        try {
            String url = BASE_URL + "/rankings/exchanges/";
            logger.info("Scraping high volume coins from: {}", url);
            
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();
            
            // This would be implemented based on the actual structure of the volume page
            // For now, we'll filter from the main list
            List<CryptoCurrency> allCoins = scrapeTopCryptocurrencies(50);
            
            // Filter coins with high 24h volume (> $100M)
            BigDecimal volumeThreshold = new BigDecimal("100000000");
            for (CryptoCurrency coin : allCoins) {
                if (coin.getVolume24h() != null && 
                    coin.getVolume24h().compareTo(volumeThreshold) > 0) {
                    highVolumeCoins.add(coin);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error scraping high volume coins: {}", e.getMessage());
        }
        
        return highVolumeCoins;
    }
    
    /**
     * Scrapes cryptocurrencies with high volatility (based on 24h change)
     * @return List of volatile cryptocurrencies
     */
    public List<CryptoCurrency> scrapeVolatileCoins() {
        List<CryptoCurrency> volatileCoins = new ArrayList<>();
        
        try {
            List<CryptoCurrency> allCoins = scrapeTopCryptocurrencies(100);
            
            // Filter coins with high volatility (>5% change in 24h)
            BigDecimal volatilityThreshold = new BigDecimal("5");
            for (CryptoCurrency coin : allCoins) {
                if (coin.getPercentChange24h() != null) {
                    BigDecimal absChange = coin.getPercentChange24h().abs();
                    if (absChange.compareTo(volatilityThreshold) > 0) {
                        volatileCoins.add(coin);
                    }
                }
            }
            
            logger.info("Found {} volatile coins", volatileCoins.size());
            
        } catch (Exception e) {
            logger.error("Error scraping volatile coins: {}", e.getMessage());
        }
        
        return volatileCoins;
    }
    
    /**
     * Parses a table row element to extract cryptocurrency data
     */
    private CryptoCurrency parseTableRow(Element row) {
        try {
            Elements cells = row.select("td");
            if (cells.size() < 7) {
                return null;
            }
            
            CryptoCurrency crypto = new CryptoCurrency();
            
            // Extract symbol and name
            Element nameCell = cells.get(2);
            Elements symbolElements = nameCell.select("p");
            if (!symbolElements.isEmpty()) {
                crypto.setSymbol(symbolElements.get(0).text());
            }
            
            Elements nameElements = nameCell.select("a");
            if (!nameElements.isEmpty()) {
                crypto.setName(nameElements.get(0).text());
            }
            
            // Extract price
            Element priceCell = cells.get(3);
            String priceText = priceCell.text().replaceAll("[^0-9.]", "");
            if (!priceText.isEmpty()) {
                crypto.setCurrentPrice(new BigDecimal(priceText));
            }
            
            // Extract 24h change
            if (cells.size() > 4) {
                Element changeCell = cells.get(4);
                String changeText = changeCell.text().replaceAll("[^0-9.-]", "");
                if (!changeText.isEmpty()) {
                    crypto.setPercentChange24h(new BigDecimal(changeText));
                }
            }
            
            // Extract 7d change
            if (cells.size() > 5) {
                Element change7dCell = cells.get(5);
                String change7dText = change7dCell.text().replaceAll("[^0-9.-]", "");
                if (!change7dText.isEmpty()) {
                    crypto.setPercentChange7d(new BigDecimal(change7dText));
                }
            }
            
            // Extract market cap
            if (cells.size() > 6) {
                Element marketCapCell = cells.get(6);
                String marketCapText = marketCapCell.text().replaceAll("[^0-9]", "");
                if (!marketCapText.isEmpty()) {
                    crypto.setMarketCap(new BigDecimal(marketCapText));
                }
            }
            
            // Extract volume
            if (cells.size() > 7) {
                Element volumeCell = cells.get(7);
                String volumeText = volumeCell.text().replaceAll("[^0-9]", "");
                if (!volumeText.isEmpty()) {
                    crypto.setVolume24h(new BigDecimal(volumeText));
                }
            }
            
            crypto.setLastUpdated(LocalDateTime.now());
            
            return crypto;
            
        } catch (Exception e) {
            logger.warn("Error parsing table row: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets specific cryptocurrency data by symbol
     */
    public CryptoCurrency getCryptocurrencyBySymbol(String symbol) {
        try {
            String url = BASE_URL + "/currencies/" + symbol.toLowerCase() + "/";
            logger.info("Fetching {} data from: {}", symbol, url);
            
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(10000)
                    .get();
            
            // This would need to be implemented based on the specific page structure
            // For now, we'll return null and fall back to the list method
            
        } catch (IOException e) {
            logger.warn("Error fetching {} data: {}", symbol, e.getMessage());
        }
        
        return null;
    }
}