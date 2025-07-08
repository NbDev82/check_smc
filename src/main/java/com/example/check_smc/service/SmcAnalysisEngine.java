package com.example.check_smc.service;

import com.example.check_smc.model.CryptoCurrency;
import com.example.check_smc.model.PriceLevel;
import com.example.check_smc.model.TradingOpportunity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Core Smart Money Concept (SMC) analysis engine
 * Implements order block detection, BOS, CHOCH, and supply/demand analysis
 */
@Service
public class SmcAnalysisEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(SmcAnalysisEngine.class);
    
    // SMC parameters
    private static final BigDecimal ORDER_BLOCK_THRESHOLD = new BigDecimal("0.02"); // 2%
    private static final BigDecimal BOS_THRESHOLD = new BigDecimal("0.015"); // 1.5%
    private static final BigDecimal CHOCH_THRESHOLD = new BigDecimal("0.01"); // 1%
    private static final BigDecimal SUPPLY_DEMAND_THRESHOLD = new BigDecimal("0.025"); // 2.5%
    
    /**
     * Analyzes a cryptocurrency for SMC patterns and trading opportunities
     */
    public TradingOpportunity analyzeForSmcOpportunity(CryptoCurrency crypto) {
        logger.info("Analyzing {} for SMC patterns", crypto.getSymbol());
        
        TradingOpportunity opportunity = new TradingOpportunity(crypto, null);
        
        // Generate simulated price data for analysis
        List<BigDecimal> priceHistory = generateSimulatedPriceHistory(crypto);
        
        // Detect key SMC levels
        List<PriceLevel> keyLevels = detectKeyLevels(priceHistory, crypto.getCurrentPrice());
        opportunity.setKeyLevels(keyLevels);
        
        // Analyze SMC signals
        TradingOpportunity.SmcSignals signals = analyzeSmcSignals(priceHistory, crypto.getCurrentPrice());
        opportunity.setSmcSignals(signals);
        
        // Determine opportunity type and confidence
        determineOpportunityTypeAndConfidence(opportunity, keyLevels, signals);
        
        // Calculate entry, stop loss, and take profit levels
        calculateTradingLevels(opportunity, keyLevels);
        
        // Generate analysis text
        generateAnalysisText(opportunity);
        
        return opportunity;
    }
    
    /**
     * Detects key price levels including order blocks and supply/demand zones
     */
    private List<PriceLevel> detectKeyLevels(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        List<PriceLevel> levels = new ArrayList<>();
        
        if (priceHistory.size() < 10) {
            return levels;
        }
        
        // Detect order blocks
        levels.addAll(detectOrderBlocks(priceHistory, currentPrice));
        
        // Detect supply and demand zones
        levels.addAll(detectSupplyDemandZones(priceHistory, currentPrice));
        
        // Detect support and resistance
        levels.addAll(detectSupportResistance(priceHistory, currentPrice));
        
        return levels;
    }
    
    /**
     * Detects order blocks in price data
     */
    private List<PriceLevel> detectOrderBlocks(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        List<PriceLevel> orderBlocks = new ArrayList<>();
        
        for (int i = 2; i < priceHistory.size() - 2; i++) {
            BigDecimal prevPrice = priceHistory.get(i - 1);
            BigDecimal currentBar = priceHistory.get(i);
            BigDecimal nextPrice = priceHistory.get(i + 1);
            
            // Bullish order block detection
            if (isBullishOrderBlock(prevPrice, currentBar, nextPrice)) {
                PriceLevel orderBlock = new PriceLevel(
                    currentBar, 
                    PriceLevel.PriceLevelType.ORDER_BLOCK_BULLISH,
                    LocalDateTime.now().minusHours(priceHistory.size() - i)
                );
                orderBlock.setStrength(calculateOrderBlockStrength(priceHistory, i));
                orderBlocks.add(orderBlock);
            }
            
            // Bearish order block detection
            if (isBearishOrderBlock(prevPrice, currentBar, nextPrice)) {
                PriceLevel orderBlock = new PriceLevel(
                    currentBar,
                    PriceLevel.PriceLevelType.ORDER_BLOCK_BEARISH,
                    LocalDateTime.now().minusHours(priceHistory.size() - i)
                );
                orderBlock.setStrength(calculateOrderBlockStrength(priceHistory, i));
                orderBlocks.add(orderBlock);
            }
        }
        
        return orderBlocks;
    }
    
    /**
     * Detects supply and demand zones
     */
    private List<PriceLevel> detectSupplyDemandZones(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        List<PriceLevel> zones = new ArrayList<>();
        
        // Find significant price swings
        for (int i = 5; i < priceHistory.size() - 5; i++) {
            BigDecimal price = priceHistory.get(i);
            
            // Check if this is a local high (potential supply zone)
            if (isLocalHigh(priceHistory, i)) {
                PriceLevel supplyZone = new PriceLevel(
                    price,
                    PriceLevel.PriceLevelType.SUPPLY_ZONE,
                    LocalDateTime.now().minusHours(priceHistory.size() - i)
                );
                supplyZone.setStrength(calculateZoneStrength(priceHistory, i, true));
                zones.add(supplyZone);
            }
            
            // Check if this is a local low (potential demand zone)
            if (isLocalLow(priceHistory, i)) {
                PriceLevel demandZone = new PriceLevel(
                    price,
                    PriceLevel.PriceLevelType.DEMAND_ZONE,
                    LocalDateTime.now().minusHours(priceHistory.size() - i)
                );
                demandZone.setStrength(calculateZoneStrength(priceHistory, i, false));
                zones.add(demandZone);
            }
        }
        
        return zones;
    }
    
    /**
     * Detects support and resistance levels
     */
    private List<PriceLevel> detectSupportResistance(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        List<PriceLevel> levels = new ArrayList<>();
        
        // Simple support/resistance detection based on price clusters
        for (int i = 1; i < priceHistory.size() - 1; i++) {
            BigDecimal price = priceHistory.get(i);
            int touchCount = countTouches(priceHistory, price, new BigDecimal("0.01")); // 1% tolerance
            
            if (touchCount >= 2) {
                PriceLevel.PriceLevelType type = price.compareTo(currentPrice) > 0 ? 
                    PriceLevel.PriceLevelType.RESISTANCE : PriceLevel.PriceLevelType.SUPPORT;
                
                PriceLevel level = new PriceLevel(
                    price,
                    type,
                    LocalDateTime.now().minusHours(priceHistory.size() - i)
                );
                level.setTouchCount(touchCount);
                level.setStrength(BigDecimal.valueOf(Math.min(touchCount * 0.2, 1.0)));
                levels.add(level);
            }
        }
        
        return levels;
    }
    
    /**
     * Analyzes SMC signals (BOS, CHOCH, etc.)
     */
    private TradingOpportunity.SmcSignals analyzeSmcSignals(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        TradingOpportunity.SmcSignals signals = new TradingOpportunity.SmcSignals();
        
        // Detect Break of Structure (BOS)
        signals.setHasBOS(detectBreakOfStructure(priceHistory, currentPrice));
        
        // Detect Change of Character (CHOCH)
        signals.setHasCHOCH(detectChangeOfCharacter(priceHistory, currentPrice));
        
        // Detect order block retest
        signals.setHasOrderBlockRetest(detectOrderBlockRetest(priceHistory, currentPrice));
        
        // Detect supply/demand imbalance
        signals.setHasSupplyDemandImbalance(detectSupplyDemandImbalance(priceHistory));
        
        // Calculate liquidity level
        signals.setLiquidityLevel(calculateLiquidityLevel(priceHistory, currentPrice));
        
        return signals;
    }
    
    /**
     * Detects Break of Structure
     */
    private boolean detectBreakOfStructure(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        if (priceHistory.size() < 10) return false;
        
        // Find recent high and low
        BigDecimal recentHigh = BigDecimal.ZERO;
        BigDecimal recentLow = new BigDecimal("999999999");
        
        for (int i = priceHistory.size() - 10; i < priceHistory.size(); i++) {
            BigDecimal price = priceHistory.get(i);
            if (price.compareTo(recentHigh) > 0) {
                recentHigh = price;
            }
            if (price.compareTo(recentLow) < 0) {
                recentLow = price;
            }
        }
        
        // Check if current price breaks the structure
        BigDecimal highBreakThreshold = recentHigh.multiply(BigDecimal.ONE.add(BOS_THRESHOLD));
        BigDecimal lowBreakThreshold = recentLow.multiply(BigDecimal.ONE.subtract(BOS_THRESHOLD));
        
        return currentPrice.compareTo(highBreakThreshold) > 0 || 
               currentPrice.compareTo(lowBreakThreshold) < 0;
    }
    
    /**
     * Detects Change of Character
     */
    private boolean detectChangeOfCharacter(List<BigDecimal> priceHistory, BigDecimal currentPrice) {
        if (priceHistory.size() < 20) return false;
        
        // Calculate short-term and medium-term trends
        BigDecimal shortTrend = calculateTrend(priceHistory.subList(priceHistory.size() - 5, priceHistory.size()));
        BigDecimal mediumTrend = calculateTrend(priceHistory.subList(priceHistory.size() - 15, priceHistory.size()));
        
        // CHOCH occurs when short-term trend contradicts medium-term trend significantly
        return shortTrend.multiply(mediumTrend).compareTo(BigDecimal.ZERO) < 0 &&
               shortTrend.abs().compareTo(CHOCH_THRESHOLD) > 0;
    }
    
    /**
     * Helper methods for SMC analysis
     */
    private boolean isBullishOrderBlock(BigDecimal prev, BigDecimal current, BigDecimal next) {
        return current.compareTo(prev) < 0 && next.compareTo(current) > 0 &&
               next.subtract(current).divide(current, 4, RoundingMode.HALF_UP)
                   .compareTo(ORDER_BLOCK_THRESHOLD) > 0;
    }
    
    private boolean isBearishOrderBlock(BigDecimal prev, BigDecimal current, BigDecimal next) {
        return current.compareTo(prev) > 0 && next.compareTo(current) < 0 &&
               current.subtract(next).divide(current, 4, RoundingMode.HALF_UP)
                   .compareTo(ORDER_BLOCK_THRESHOLD) > 0;
    }
    
    private boolean isLocalHigh(List<BigDecimal> prices, int index) {
        if (index < 2 || index >= prices.size() - 2) return false;
        
        BigDecimal price = prices.get(index);
        return price.compareTo(prices.get(index - 1)) > 0 &&
               price.compareTo(prices.get(index - 2)) > 0 &&
               price.compareTo(prices.get(index + 1)) > 0 &&
               price.compareTo(prices.get(index + 2)) > 0;
    }
    
    private boolean isLocalLow(List<BigDecimal> prices, int index) {
        if (index < 2 || index >= prices.size() - 2) return false;
        
        BigDecimal price = prices.get(index);
        return price.compareTo(prices.get(index - 1)) < 0 &&
               price.compareTo(prices.get(index - 2)) < 0 &&
               price.compareTo(prices.get(index + 1)) < 0 &&
               price.compareTo(prices.get(index + 2)) < 0;
    }
    
    private BigDecimal calculateOrderBlockStrength(List<BigDecimal> prices, int index) {
        // Simple strength calculation based on price movement
        if (index < 1 || index >= prices.size()) return BigDecimal.ZERO;
        
        BigDecimal priceMove = prices.get(index + 1).subtract(prices.get(index - 1)).abs();
        BigDecimal avgPrice = prices.get(index);
        
        return priceMove.divide(avgPrice, 4, RoundingMode.HALF_UP);
    }
    
    private BigDecimal calculateZoneStrength(List<BigDecimal> prices, int index, boolean isSupply) {
        // Calculate zone strength based on surrounding price action
        return new BigDecimal("0.5"); // Simplified for demo
    }
    
    private int countTouches(List<BigDecimal> prices, BigDecimal level, BigDecimal tolerance) {
        int count = 0;
        BigDecimal upperBound = level.multiply(BigDecimal.ONE.add(tolerance));
        BigDecimal lowerBound = level.multiply(BigDecimal.ONE.subtract(tolerance));
        
        for (BigDecimal price : prices) {
            if (price.compareTo(lowerBound) >= 0 && price.compareTo(upperBound) <= 0) {
                count++;
            }
        }
        return count;
    }
    
    private boolean detectOrderBlockRetest(List<BigDecimal> prices, BigDecimal currentPrice) {
        // Simplified detection - check if price is near a previously identified level
        return false; // Implementation would check against known order blocks
    }
    
    private boolean detectSupplyDemandImbalance(List<BigDecimal> prices) {
        // Check for significant price gaps or imbalances
        if (prices.size() < 5) return false;
        
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal gap = prices.get(i).subtract(prices.get(i - 1)).abs();
            BigDecimal gapPercent = gap.divide(prices.get(i - 1), 4, RoundingMode.HALF_UP);
            
            if (gapPercent.compareTo(SUPPLY_DEMAND_THRESHOLD) > 0) {
                return true;
            }
        }
        return false;
    }
    
    private BigDecimal calculateLiquidityLevel(List<BigDecimal> prices, BigDecimal currentPrice) {
        // Calculate relative liquidity based on volume and price action
        return new BigDecimal("0.75"); // Simplified for demo
    }
    
    private BigDecimal calculateTrend(List<BigDecimal> prices) {
        if (prices.size() < 2) return BigDecimal.ZERO;
        
        BigDecimal first = prices.get(0);
        BigDecimal last = prices.get(prices.size() - 1);
        
        return last.subtract(first).divide(first, 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Generates simulated price history for demonstration
     */
    private List<BigDecimal> generateSimulatedPriceHistory(CryptoCurrency crypto) {
        List<BigDecimal> history = new ArrayList<>();
        BigDecimal basePrice = crypto.getCurrentPrice();
        
        // Generate 50 periods of simulated price data
        for (int i = 0; i < 50; i++) {
            // Simulate price movement with some volatility
            double randomFactor = 0.95 + (Math.random() * 0.1); // ±5% movement
            BigDecimal newPrice = basePrice.multiply(BigDecimal.valueOf(randomFactor));
            history.add(newPrice);
            basePrice = newPrice;
        }
        
        // Set the last price to current price
        history.set(history.size() - 1, crypto.getCurrentPrice());
        
        return history;
    }
    
    /**
     * Determines opportunity type and confidence score
     */
    private void determineOpportunityTypeAndConfidence(TradingOpportunity opportunity, 
                                                       List<PriceLevel> keyLevels, 
                                                       TradingOpportunity.SmcSignals signals) {
        BigDecimal confidence = BigDecimal.ZERO;
        TradingOpportunity.OpportunityType type = TradingOpportunity.OpportunityType.WAIT_RETEST;
        
        // Analyze signals to determine opportunity type
        if (signals.isHasBOS()) {
            confidence = confidence.add(new BigDecimal("0.3"));
            type = TradingOpportunity.OpportunityType.BREAKOUT_LONG;
        }
        
        if (signals.isHasCHOCH()) {
            confidence = confidence.add(new BigDecimal("0.25"));
        }
        
        if (signals.isHasOrderBlockRetest()) {
            confidence = confidence.add(new BigDecimal("0.2"));
            type = TradingOpportunity.OpportunityType.BUY_LONG;
        }
        
        if (signals.isHasSupplyDemandImbalance()) {
            confidence = confidence.add(new BigDecimal("0.15"));
        }
        
        // Factor in key levels strength
        for (PriceLevel level : keyLevels) {
            confidence = confidence.add(level.getStrength().multiply(new BigDecimal("0.1")));
        }
        
        // Cap confidence at 1.0
        confidence = confidence.min(BigDecimal.ONE);
        
        opportunity.setType(type);
        opportunity.setConfidenceScore(confidence);
    }
    
    /**
     * Calculates trading levels (entry, stop loss, take profit)
     */
    private void calculateTradingLevels(TradingOpportunity opportunity, List<PriceLevel> keyLevels) {
        BigDecimal currentPrice = opportunity.getCurrentPrice();
        
        // Set suggested entry price (slightly below current for long, above for short)
        BigDecimal entryAdjustment = currentPrice.multiply(new BigDecimal("0.005")); // 0.5%
        
        if (opportunity.getType() == TradingOpportunity.OpportunityType.BUY_LONG ||
            opportunity.getType() == TradingOpportunity.OpportunityType.BREAKOUT_LONG) {
            opportunity.setSuggestedEntryPrice(currentPrice.subtract(entryAdjustment));
            opportunity.setStopLoss(currentPrice.multiply(new BigDecimal("0.95")));
            opportunity.setTakeProfit(currentPrice.multiply(new BigDecimal("1.06")));
        } else {
            opportunity.setSuggestedEntryPrice(currentPrice.add(entryAdjustment));
            opportunity.setStopLoss(currentPrice.multiply(new BigDecimal("1.05")));
            opportunity.setTakeProfit(currentPrice.multiply(new BigDecimal("0.94")));
        }
    }
    
    /**
     * Generates analysis text
     */
    private void generateAnalysisText(TradingOpportunity opportunity) {
        StringBuilder analysis = new StringBuilder();
        
        TradingOpportunity.SmcSignals signals = opportunity.getSmcSignals();
        
        if (signals.isHasBOS()) {
            analysis.append("Break of Structure detected. ");
        }
        
        if (signals.isHasCHOCH()) {
            analysis.append("Change of Character identified. ");
        }
        
        if (signals.isHasOrderBlockRetest()) {
            analysis.append("Order block retest opportunity. ");
        }
        
        if (signals.isHasSupplyDemandImbalance()) {
            analysis.append("Supply/demand imbalance present. ");
        }
        
        analysis.append(String.format("Confidence: %.1f%%. ", 
            opportunity.getConfidenceScore().multiply(new BigDecimal("100")).doubleValue()));
        
        analysis.append(String.format("Key levels identified: %d. ", 
            opportunity.getKeyLevels() != null ? opportunity.getKeyLevels().size() : 0));
        
        opportunity.setAnalysis(analysis.toString().trim());
    }
}