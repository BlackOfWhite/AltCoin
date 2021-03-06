package org.preferences;

public class Constants {

    public static final String MSG_MIN_TRADE_REQUIREMENT_NOT_MET = "MIN_TRADE_REQUIREMENT_NOT_MET";
    public static final String MSG_INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String MSG_INVALID_MARKET = "INVALID_MARKET";
    public static final String MSG_ZERO_OR_NEGATIVE_NOT_ALLOWED = "ZERO_OR_NEGATIVE_NOT_ALLOWED";
    public static final String MSG_REQUEST_PROCESSING_PROBLEM = "There was a problem processing your request.";
    public static final String MSG_APIKEY_INVALID = "APIKEY_INVALID";

    public static final String MSG_REQUEST_TIMEOUT = "Request timeout.";
    public static final int REQUEST_TIMEOUT_SECONDS = 3;

    public static final double BALANCE_MINIMUM = 0.00000001;
    public static final double CHART_SIGNIFICANT_MINIMUM = 0.000001;
    public static final int MAX_INPUT_VALUE = 1000000;
    public static final double VALUE_NOT_SET = -1.012345; // MUST BE LESS THAN 0!

    public static final String ORDER_TYPE_SELL = "LIMIT_SELL";
    public static final String ORDER_TYPE_BUY = "LIMIT_BUY";

    // Sell all transactions for SELL_PRICE_RATIO * last price.
    public static final double SELL_PRICE_RATIO = 0.998;
    public static final double BUY_PRICE_RATIO = 1.002;

    // Dialogs
    public static final String DIALOG_FAILED_TO_LOAD_API_KEYS = "Failed to load one or more API keys! Please go to 'API Setup' section in the 'Settings' menu.";
    public static final String DIALOG_INVALID_API_KEYS = "API keys are invalid. Please go to 'API Setup' section in the 'Settings' menu.";
    public static final String NO_INTERNET_CONNECTION = "Your internet connection is weak or Bittrex server is temporarily not accessible.";

    // Donation addresses
    public static final String BTC_DONATION_ADDRESS = "168zdkYPAqMjwjior2GRyfbo1djBJL9LTH";
    public static final String ETH_DONATION_ADDRESS = "0xE69Cb31ddD790F072E79aC789f0152dF85b16AF4";
    public static final String LTC_DONATION_ADDRESS = "LPBDpTsHd9n6e8ZG4xhUoyUx91CdNmXZWW";
}
