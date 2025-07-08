package com.example.check_smc.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a significant price level in SMC analysis
 */
public class PriceLevel {
    private BigDecimal price;
    private LocalDateTime timestamp;
    private PriceLevelType type;
    private BigDecimal strength; // 0.0 to 1.0
    private boolean isTested;
    private int touchCount;
    
    public enum PriceLevelType {
        SUPPORT,
        RESISTANCE,
        ORDER_BLOCK_BULLISH,
        ORDER_BLOCK_BEARISH,
        SUPPLY_ZONE,
        DEMAND_ZONE
    }
    
    // Constructors
    public PriceLevel() {}
    
    public PriceLevel(BigDecimal price, PriceLevelType type, LocalDateTime timestamp) {
        this.price = price;
        this.type = type;
        this.timestamp = timestamp;
        this.strength = BigDecimal.ZERO;
        this.isTested = false;
        this.touchCount = 0;
    }
    
    // Getters and Setters
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public PriceLevelType getType() {
        return type;
    }
    
    public void setType(PriceLevelType type) {
        this.type = type;
    }
    
    public BigDecimal getStrength() {
        return strength;
    }
    
    public void setStrength(BigDecimal strength) {
        this.strength = strength;
    }
    
    public boolean isTested() {
        return isTested;
    }
    
    public void setTested(boolean tested) {
        isTested = tested;
    }
    
    public int getTouchCount() {
        return touchCount;
    }
    
    public void setTouchCount(int touchCount) {
        this.touchCount = touchCount;
    }
    
    public void incrementTouchCount() {
        this.touchCount++;
        if (this.touchCount > 0) {
            this.isTested = true;
        }
    }
    
    @Override
    public String toString() {
        return String.format("PriceLevel{price=%s, type=%s, strength=%s, touches=%d}",
                price, type, strength, touchCount);
    }
}