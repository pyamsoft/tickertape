package com.pyamsoft.tickertape.portfolio.dig

enum class PortfolioDigSections(val display: String) {
  POSITIONS("Positions"),
  SPLITS("Stock Splits"),
  CHART("Chart"),
  STATISTICS("Details"),
  PRICE_ALERTS("Price Alerts"),
  NEWS("News"),
  RECOMMENDATIONS("Recommendations"),
  OPTIONS_CHAIN("Options Chain"),
}
