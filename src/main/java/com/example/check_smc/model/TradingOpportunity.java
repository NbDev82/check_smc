package com.example.check_smc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Represents a trading opportunity identified through SMC analysis
 */
public class TradingOpportunity {
    private CryptoCurrency cryptocurrency;
    private BigDecimal currentPrice;
    private BigDecimal suggestedEntryPrice;
    private BigDecimal stopLoss;
    private BigDecimal takeProfit;
    private OpportunityType type;
    private BigDecimal confidenceScore; // 0.0 to 1.0
    private List<PriceLevel> keyLevels;
    private String analysis;
    private LocalDateTime identifiedAt;
    private SmcSignals smcSignals;
    
    public enum OpportunityType {
        BUY_LONG,
        SELL_SHORT,
        WAIT_RETEST,
        BREAKOUT_LONG,
        BREAKOUT_SHORT
    }
    
    public static class SmcSignals {
        private boolean hasBOS; // Break of Structure
        private boolean hasCHOCH; // Change of Character
        private boolean hasOrderBlockRetest;
        private boolean hasSupplyDemandImbalance;
        private BigDecimal liquidityLevel;
        
        // Getters and Setters
        public boolean isHasBOS() {
            return hasBOS;
        }
        
        public void setHasBOS(boolean hasBOS) {
            this.hasBOS = hasBOS;
        }
        
        public boolean isHasCHOCH() {
            return hasCHOCH;
        }
        
        public void setHasCHOCH(boolean hasCHOCH) {
            this.hasCHOCH = hasCHOCH;
        }
        
        public boolean isHasOrderBlockRetest() {
            return hasOrderBlockRetest;
        }
        
        public void setHasOrderBlockRetest(boolean hasOrderBlockRetest) {
            this.hasOrderBlockRetest = hasOrderBlockRetest;
        }
        
        public boolean isHasSupplyDemandImbalance() {
            return hasSupplyDemandImbalance;
        }
        
        public void setHasSupplyDemandImbalance(boolean hasSupplyDemandImbalance) {
            this.hasSupplyDemandImbalance = hasSupplyDemandImbalance;
        }
        
        public BigDecimal getLiquidityLevel() {
            return liquidityLevel;
        }
        
        public void setLiquidityLevel(BigDecimal liquidityLevel) {
            this.liquidityLevel = liquidityLevel;
        }
    }
    
    // Constructors
    public TradingOpportunity() {
        this.smcSignals = new SmcSignals();
        this.identifiedAt = LocalDateTime.now();
    }
    
    public TradingOpportunity(CryptoCurrency cryptocurrency, OpportunityType type) {
        this();
        this.cryptocurrency = cryptocurrency;
        this.type = type;
        this.currentPrice = cryptocurrency.getCurrentPrice();
    }
    
    // Getters and Setters
    public CryptoCurrency getCryptocurrency() {
        return cryptocurrency;
    }
    
    public void setCryptocurrency(CryptoCurrency cryptocurrency) {
        this.cryptocurrency = cryptocurrency;
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }
    
    public BigDecimal getSuggestedEntryPrice() {
        return suggestedEntryPrice;
    }
    
    public void setSuggestedEntryPrice(BigDecimal suggestedEntryPrice) {
        this.suggestedEntryPrice = suggestedEntryPrice;
    }
    
    public BigDecimal getStopLoss() {
        return stopLoss;
    }
    
    public void setStopLoss(BigDecimal stopLoss) {
        this.stopLoss = stopLoss;
    }
    
    public BigDecimal getTakeProfit() {
        return takeProfit;
    }
    
    public void setTakeProfit(BigDecimal takeProfit) {
        this.takeProfit = takeProfit;
    }
    
    public OpportunityType getType() {
        return type;
    }
    
    public void setType(OpportunityType type) {
        this.type = type;
    }
    
    public BigDecimal getConfidenceScore() {
        return confidenceScore;
    }
    
    public void setConfidenceScore(BigDecimal confidenceScore) {
        this.confidenceScore = confidenceScore;
    }
    
    public List<PriceLevel> getKeyLevels() {
        return keyLevels;
    }
    
    public void setKeyLevels(List<PriceLevel> keyLevels) {
        this.keyLevels = keyLevels;
    }
    
    public String getAnalysis() {
        return analysis;
    }
    
    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }
    
    public LocalDateTime getIdentifiedAt() {
        return identifiedAt;
    }
    
    public void setIdentifiedAt(LocalDateTime identifiedAt) {
        this.identifiedAt = identifiedAt;
    }
    
    public SmcSignals getSmcSignals() {
        return smcSignals;
    }
    
    public void setSmcSignals(SmcSignals smcSignals) {
        this.smcSignals = smcSignals;
    }
    
    @Override
    public String toString() {
        return String.format("TradingOpportunity{%s %s, entry=%s, confidence=%s, analysis='%s'}",
                cryptocurrency.getSymbol(), type, suggestedEntryPrice, confidenceScore, analysis);
    }
}