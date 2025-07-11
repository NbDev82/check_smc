# SMC Trading Opportunity Finder

An automated Java application that finds cryptocurrency trading opportunities using Smart Money Concept (SMC) analysis by scraping data from CoinMarketCap and analyzing price patterns.

## Features

- **Data Collection**: Scrapes cryptocurrency data from CoinMarketCap
- **SMC Analysis**: Implements Smart Money Concept patterns including:
  - Order Block Detection (Bullish/Bearish)
  - Break of Structure (BOS) identification
  - Change of Character (CHOCH) detection
  - Supply and Demand Zone analysis
  - Support and Resistance levels
- **Filtering & Ranking**: Filters coins based on volume, volatility, and market cap criteria
- **Output**: Generates console reports and JSON files with trading opportunities

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                    SMC Trading Flow Diagram                     │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   CoinMarketCap │    │  High Volume    │    │  Volatile Coins │
│   Top 50 Coins  │    │     Coins       │    │     Coins       │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                                 ▼
                    ┌─────────────────────┐
                    │   Data Aggregation  │
                    │   & Deduplication   │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   SMC Criteria      │
                    │   Filtering:        │
                    │   • Volume > $10M   │
                    │   • Volatility > 2% │
                    │   • Market Cap >    │
                    │     $100M           │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   SMC Analysis      │
                    │   Engine:           │
                    │   • Order Blocks    │
                    │   • BOS Detection   │
                    │   • CHOCH Analysis  │
                    │   • Supply/Demand   │
                    │   • S/R Levels      │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   Opportunity       │
                    │   Scoring &         │
                    │   Ranking           │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │   Output Generation │
                    │   • Console Report  │
                    │   • JSON File       │
                    │   • Trading Levels  │
                    └─────────────────────┘
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Internet connection (for CoinMarketCap scraping)

## Quick Start

1. **Clone and build the project:**
   ```bash
   git clone <repository-url>
   cd check_smc
   mvn clean install
   ```

2. **Run the application:**
   ```bash
   mvn spring-boot:run
   ```

3. **View results:**
   - Console output shows detailed analysis
   - JSON file saved with timestamp: `smc_trading_opportunities_YYYYMMDD_HHMMSS.json`

## SMC Concepts Implemented

### Order Blocks
- **Bullish Order Block**: Price drops then rebounds strongly (>2% threshold)
- **Bearish Order Block**: Price rises then drops significantly

### Break of Structure (BOS)
- Identifies when price breaks above recent highs or below recent lows
- Threshold: 1.5% beyond recent structure points

### Change of Character (CHOCH)
- Detects trend reversals by comparing short-term vs medium-term trends
- Identifies when market sentiment shifts

### Supply & Demand Zones
- **Supply Zones**: Areas where selling pressure overwhelms buying
- **Demand Zones**: Areas where buying pressure overwhelms selling

## Sample Output

```
================================================================================
SMC TRADING OPPORTUNITIES ANALYSIS RESULTS
================================================================================
Analysis Time: 2024-01-15 14:30:25
Total Opportunities Found: 5

--- OPPORTUNITY #1 ---
Coin: Solana (SOL)
Current Price: $120.00
Opportunity Type: BREAKOUT_LONG
Confidence Score: 75.5%
Suggested Entry: $119.40
Stop Loss: $114.00
Take Profit: $127.20

SMC Signals:
  - Break of Structure (BOS): ✓
  - Change of Character (CHOCH): ✓
  - Order Block Retest: ✗
  - Supply/Demand Imbalance: ✓

Key Price Levels:
  - ORDER_BLOCK_BULLISH: $115.50 (Strength: 0.65)
  - DEMAND_ZONE: $112.00 (Strength: 0.58)
  - SUPPORT: $108.75 (Strength: 0.42)

Analysis: Break of Structure detected. Change of Character identified. 
Supply/demand imbalance present. Confidence: 75.5%. Key levels identified: 8.
```

## Configuration

### SMC Parameters (in SmcAnalysisEngine.java)
```java
ORDER_BLOCK_THRESHOLD = 0.02;     // 2% price movement
BOS_THRESHOLD = 0.015;            // 1.5% structure break
CHOCH_THRESHOLD = 0.01;           // 1% trend change
SUPPLY_DEMAND_THRESHOLD = 0.025;  // 2.5% imbalance
```

### Filtering Criteria (in SmcTradingService.java)
```java
MIN_VOLUME = $10,000,000;         // 24h volume
MIN_VOLATILITY = 2%;              // 24h price change
MIN_MARKET_CAP = $100,000,000;    // Market capitalization
MIN_PRICE = $0.01;                // Minimum coin price
MIN_CONFIDENCE = 30%;             // Minimum opportunity confidence
```

## Architecture Components

### Models (`src/main/java/com/example/check_smc/model/`)
- `CryptoCurrency.java`: Represents coin data
- `PriceLevel.java`: Represents significant price levels
- `TradingOpportunity.java`: Represents identified opportunities

### Services (`src/main/java/com/example/check_smc/service/`)
- `CoinMarketCapScraper.java`: Web scraping for market data
- `SmcAnalysisEngine.java`: Core SMC pattern detection
- `SmcTradingService.java`: Main orchestration service

## Advanced Features

### Real-time Data Integration
To integrate with TradingView or other APIs, modify the `SmcAnalysisEngine.generateSimulatedPriceHistory()` method to fetch real historical data.

### Custom Screening Criteria
Modify the `meetsSMCCriteria()` method in `SmcTradingService.java` to add custom filtering logic.

### Extended SMC Patterns
Add more SMC concepts by extending the `SmcAnalysisEngine.java`:
- Fair Value Gaps (FVG)
- Liquidity Sweeps
- Market Structure Shifts
- Institutional Order Flow

## Limitations & Disclaimers

⚠️ **Important Disclaimers:**
- This is for educational purposes only
- Not financial advice
- Past performance doesn't guarantee future results
- Cryptocurrency trading involves significant risk
- Always do your own research (DYOR)
- Consider consulting with financial advisors

### Current Limitations:
- Uses simulated price data for SMC analysis (can be replaced with real data)
- CoinMarketCap scraping may be rate-limited
- SMC patterns are based on simplified algorithms
- No backtesting or performance validation included

## Future Enhancements

1. **Real-time Data Sources**
   - TradingView API integration
   - Binance API for real-time prices
   - WebSocket feeds for live updates

2. **Advanced SMC Features**
   - Multi-timeframe analysis
   - Volume profile integration
   - Market structure scoring
   - Backtesting framework

3. **Risk Management**
   - Position sizing calculators
   - Portfolio allocation suggestions
   - Risk/reward optimization

4. **Notifications**
   - Email/SMS alerts for opportunities
   - Discord/Telegram bot integration
   - Real-time monitoring dashboard

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For questions or issues:
1. Check the existing issues in the repository
2. Create a new issue with detailed description
3. Include system information and error logs

---

**Remember: This tool is for educational purposes. Always conduct your own research and consider the risks before making any trading decisions.**