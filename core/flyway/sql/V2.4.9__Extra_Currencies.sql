ALTER TABLE `cash_balance`
    MODIFY `CURRENCY`
    ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR', 'MYR', 'SGD', 'CNH', 'CZK', 'DKK', 'HUF', 'ILS', 'MXN') NOT NULL;
ALTER TABLE `security`
    MODIFY `BASE_CURRENCY`
    ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR', 'MYR', 'SGD', 'CNH', 'CZK', 'DKK', 'HUF', 'ILS', 'MXN');
ALTER TABLE `security_family`
    MODIFY `CURRENCY`
    ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR', 'MYR', 'SGD', 'CNH', 'CZK', 'DKK', 'HUF', 'ILS', 'MXN') NOT NULL;
ALTER TABLE `transaction`
    MODIFY `CURRENCY`
    ENUM('AUD','BRL','CAD','CHF','EUR','GBP','HKD','INR','JPY','KRW','NOK','NZD','PLN','RUB','SEK','THB','TRY','TWD','USD','ZAR', 'MYR', 'SGD', 'CNH', 'CZK', 'DKK', 'HUF', 'ILS', 'MXN') NOT NULL;
