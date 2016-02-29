ALTER TABLE `account`
    DROP KEY `NAME_UNIQUE`,
    DROP KEY `ACTIVE`,
    DROP KEY `BROKER`,
    DROP KEY `ORDER_SERVICE_TYPE`,
    ADD UNIQUE KEY `ACCOUNT_NAME_UNIQUE` (`NAME`),
    ADD KEY `ACCOUNT_ACTIVE` (`ACTIVE`),
    ADD KEY `ACCOUNT_BROKER` (`BROKER`),
    ADD KEY `ACCOUNT_ORDER_SERVICE_TYPE` (`ORDER_SERVICE_TYPE`);

ALTER TABLE `bar`
    DROP KEY `DATE_TIME_SECURITY_BAR_SIZE_UNIQUE`,
    DROP KEY `DATE_TIME`,
    DROP KEY `SECURITY_FK`,
    DROP KEY `BAR_SIZE`,
    ADD UNIQUE KEY `BAR_DATE_TIME_SECURITY_BAR_SIZE_UNIQUE` (`DATE_TIME`,`SECURITY_FK`,`BAR_SIZE`),
    ADD KEY `BAR_DATE_TIME` (`DATE_TIME`),
    ADD KEY `BAR_SECURITY_FK` (`SECURITY_FK`),
    ADD KEY `BAR_BAR_SIZE` (`BAR_SIZE`),
    ADD KEY `BAR_FEED_TYPE` (`FEED_TYPE`);

ALTER TABLE `broker_parameters`
    ADD KEY `BROKER_PARAMETERS_BROKER` (`BROKER`);

ALTER TABLE `cash_balance`
    DROP KEY `CURRENCY_STRATEGY_UNIQUE`,
    ADD UNIQUE KEY `CASH_BALANCE_CURRENCY_STRATEGY_UNIQUE` (`CURRENCY`,`STRATEGY_FK`),
    ADD KEY `CASH_BALANCE_CURRENCY` (`CURRENCY`);

ALTER TABLE `component`
    DROP KEY `SECURITY_COMBINATION_UNIQUE`,
    ADD UNIQUE KEY `COMPONENT_SECURITY_COMBINATION_UNIQUE` (`SECURITY_FK`,`COMBINATION_FK`);

ALTER TABLE `easy_to_borrow`
    DROP KEY `DATE_BROKER_STOCK_UNIQUE`,
    ADD UNIQUE KEY `EASY_TO_BORROW_DATE_BROKER_STOCK_UNIQUE` (`DATE`,`BROKER`,`STOCK_FK`),
    ADD KEY `EASY_TO_BORROW_DATE` (`DATE`),
    ADD KEY `EASY_TO_BORROW_BROKER` (`BROKER`);

ALTER TABLE `exchange`
    DROP KEY `NAME`,
    ADD UNIQUE KEY `EXCHANGE_NAME_UNIQUE` (`NAME`);

ALTER TABLE `generic_tick`
    ADD KEY `GENERIC_TICK_DATE_TIME` (`DATE_TIME`),
    ADD KEY `GENERIC_TICK_FEED_TYPE` (`FEED_TYPE`),
    ADD KEY `GENERIC_TICK_TICK_TYPE` (`TICK_TYPE`),
    ADD UNIQUE KEY `GENERIC_TICK_DATE_TIME_SECURITY_FEED_TYPE_TICK_TYPE_UNIQUE` (`DATE_TIME`, `SECURITY_FK`, `FEED_TYPE`, `TICK_TYPE`);

ALTER TABLE `holiday`
    ADD KEY `HOLIDAY_DATE` (`DATE`),
    ADD UNIQUE KEY `HOLIDAY_DATE_EXCHANGE_UNIQUE` (`DATE`, `EXCHANGE_FK`);

ALTER TABLE `measurement`
    DROP KEY `DATE_NAME_STRATEGY_UNIQUE`,
    ADD UNIQUE KEY `MEASUREMENT_DATE_NAME_STRATEGY_UNIQUE` (`DATE_TIME`,`NAME`,`STRATEGY_FK`),
    ADD KEY `MEASUREMENT_NAME` (`NAME`),
    ADD KEY `MEASUREMENT_DATE_TIME` (`DATE_TIME`);

ALTER TABLE `order`
    ADD KEY `ORDER_INT_ID` (`INT_ID`),
    ADD KEY `ORDER_EXT_ID` (`EXT_ID`),
    ADD KEY `ORDER_DATE_TIME` (`DATE_TIME`),
    ADD KEY `ORDER_SIDE` (`SIDE`),
    ADD KEY `ORDER_TIF` (`TIF`);

ALTER TABLE `order_preference`
	DROP KEY `NAME_UNIQUE`,
	ADD UNIQUE KEY `ORDER_PREFERENCE_NAME_UNIQUE` (`NAME`);

ALTER TABLE `order_property`
    ADD KEY `ORDER_PROPERTY_NAME` (`NAME`),
    ADD UNIQUE KEY `ORDER_PROPERTY_NAME_ORDER_UNIQUE` (`NAME`, `ORDER_FK`);

ALTER TABLE `order_status`
    ADD KEY `ORDER_STATUS_DATE_TIME` (`DATE_TIME`),
    ADD KEY `ORDER_STATUS_EXT_DATE_TIME` (`EXT_DATE_TIME`),
    ADD KEY `ORDER_STATUS_STATUS` (`STATUS`),
    ADD KEY `ORDER_STATUS_INT_ID` (`INT_ID`),
    ADD KEY `ORDER_STATUS_EXT_ID` (`EXT_ID`),
    ADD KEY `ORDER_STATUS_SEQUENCE_NUMBER` (`SEQUENCE_NUMBER`);

ALTER TABLE `portfolio_value`
    DROP KEY `DATE_TIME_STRATEGY_UNIQUE`,
    ADD UNIQUE KEY `PORTFOLIO_VALUE_DATE_TIME_STRATEGY_UNIQUE` (`DATE_TIME`,`STRATEGY_FK`),
    ADD KEY `PORTFOLIO_VALUE_DATE_TIME` (`DATE_TIME`);

ALTER TABLE `property`
    DROP KEY `NAME_PROPERTY_HOLDER_UNIQUE`,
    ADD UNIQUE KEY `PROPERTY_NAME_PROPERTY_HOLDER_UNIQUE` (`NAME`,`PROPERTY_HOLDER_FK`),
    ADD KEY `PROPERTY_NAME` (`NAME`),
    ADD KEY `PROPERTY_PROPERTY_HOLDER_FK` (`PROPERTY_HOLDER_FK`);

ALTER TABLE `position`
    DROP KEY `SECURITY_STRATEGY_UNIQUE`,
    ADD UNIQUE KEY `POSITION_SECURITY_STRATEGY_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`);

ALTER TABLE `security`
    DROP KEY `SYMBOL_SECURITY_FAMILY_UNIQUE`,
    DROP KEY `ISIN_UNIQUE`,
    DROP KEY `BBGID_UNIQUE`,
    DROP KEY `RIC_UNIQUE`,
    DROP KEY `CONID_UNIQUE`,
    DROP KEY `LMAXID_UNIQUE`,
    DROP KEY `TTID_UNIQUE`,
    DROP KEY `FUTURE_BK_CONSTRAINT`,
    DROP KEY `UUID_UNIQUE_CONSTRAINT`,
    DROP KEY `MATURITY`,
    DROP KEY `EXPIRATION`,
    DROP KEY `MONTH_YEAR`,
    DROP KEY `FIRST_NOTICE`,
    DROP KEY `DURATION`,
    DROP KEY `STRIKE`,
    DROP KEY `OPTION_TYPE`,
    DROP KEY `GICS`,
    ADD UNIQUE KEY `SECURITY_SYMBOL_SECURITY_FAMILY_UNIQUE` (`SYMBOL`,`SECURITY_FAMILY_FK`),
    ADD UNIQUE KEY `SECURITY_ISIN_UNIQUE` (`ISIN`),
    ADD UNIQUE KEY `SECURITY_BBGID_UNIQUE` (`BBGID`),
    ADD UNIQUE KEY `SECURITY_RIC_UNIQUE` (`RIC`),
    ADD UNIQUE KEY `SECURITY_CONID_UNIQUE` (`CONID`),
    ADD UNIQUE KEY `SECURITY_LMAXID_UNIQUE` (`LMAXID`),
    ADD UNIQUE KEY `SECURITY_TTID_UNIQUE` (`TTID`),
    ADD UNIQUE KEY `SECURITY_CLASS_SECURITY_FAMILY_MATURITY_UNIQUE` (`CLASS`,`SECURITY_FAMILY_FK`,`MATURITY`),
    ADD UNIQUE KEY `SECURITY_CLASS_SECURITY_FAMILY_MONTH_YEAR_UNIQUE` (`CLASS`,`SECURITY_FAMILY_FK`,`MONTH_YEAR`),
    ADD UNIQUE KEY `SECURITY_CLASS_SECURITY_FAMILY_DURATION_UNIQUE` (`CLASS`,`SECURITY_FAMILY_FK`,`DURATION`),
    ADD UNIQUE KEY `SECURITY_CLASS_S_F_EXPIRATION_STRIKE_OPT_TYPE_UNIQUE` (`CLASS`,`SECURITY_FAMILY_FK`,`EXPIRATION`,`STRIKE`,`OPTION_TYPE`),
    ADD UNIQUE KEY `SECURITY_UUID_UNIQUE` (`UUID`),
    ADD KEY `SECURITY_CLASS` (`CLASS`),
    ADD KEY `SECURITY_SYMBOL` (`SYMBOL`),
    ADD KEY `SECURITY_COMBINATION_TYPE` (`COMBINATION_TYPE`),
    ADD KEY `SECURITY_COMMODITY_TYPE` (`COMMODITY_TYPE`),
    ADD KEY `SECURITY_ASSET_CLASS` (`ASSET_CLASS`),
    ADD KEY `SECURITY_BASE_CURRENCY` (`BASE_CURRENCY`),
    ADD KEY `SECURITY_MATURITY` (`MATURITY`),
    ADD KEY `SECURITY_EXPIRATION` (`EXPIRATION`),
    ADD KEY `SECURITY_MONTH_YEAR` (`MONTH_YEAR`),
    ADD KEY `SECURITY_FIRST_NOTICE` (`FIRST_NOTICE`),
    ADD KEY `SECURITY_DURATION` (`DURATION`),
    ADD KEY `SECURITY_STRIKE` (`STRIKE`),
    ADD KEY `SECURITY_OPTION_TYPE` (`OPTION_TYPE`),
    ADD KEY `SECURITY_GICS` (`GICS`);

ALTER TABLE `security_family`
    DROP KEY `NAME_UNIQUE`,
    ADD UNIQUE KEY `SECURITY_FAMILY_NAME_UNIQUE` (`NAME`),
    ADD KEY `SECURITY_FAMILY_CLASS` (`CLASS`),
    ADD KEY `SECURITY_FAMILY_SYMBOL_ROOT` (`SYMBOL_ROOT`),
    ADD KEY `SECURITY_FAMILY_ISIN_ROOT` (`ISIN_ROOT`),
    ADD KEY `SECURITY_FAMILY_RIC_ROOT` (`RIC_ROOT`),
    ADD KEY `SECURITY_FAMILY_CURRENCY` (`CURRENCY`),
    ADD KEY `SECURITY_FAMILY_TRADEABLE` (`TRADEABLE`),
    ADD KEY `SECURITY_FAMILY_SYNTHETIC` (`SYNTHETIC`),
    ADD KEY `SECURITY_FAMILY_PERIODICITY` (`PERIODICITY`),
    ADD KEY `SECURITY_FAMILY_EXPIRATION_TYPE` (`EXPIRATION_TYPE`),
    ADD KEY `SECURITY_FAMILY_EXPIRATION_DISTANCE` (`EXPIRATION_DISTANCE`),
    ADD KEY `SECURITY_FAMILY_WEEKLY` (`WEEKLY`),
    ADD KEY `SECURITY_FAMILY_QUOTATION_STYLE` (`QUOTATION_STYLE`);

ALTER TABLE `security_reference`
    ADD KEY `SECURITY_REFERENCE_NAME` (`NAME`),
    ADD UNIQUE KEY `SECURITY_REFERENCE_NAME_OWNER_TARGET_UNIQUE` (`NAME`, `OWNER_FK`, `TARGET_FK`);

ALTER TABLE `strategy`
    DROP KEY `NAME_UNIQUE`,
    ADD UNIQUE KEY `STRATEGY_NAME_UNIQUE` (`NAME`);

ALTER TABLE `subscription`
    DROP KEY `STRATEGY_SECURITY_FEED_TYPE_UNIQUE`,
    DROP KEY `PERSISTENT`,
    DROP KEY `FEED_TYPE`,
    ADD UNIQUE KEY `SUBSCRIPTION_STRATEGY_SECURITY_FEED_TYPE_UNIQUE` (`SECURITY_FK`,`STRATEGY_FK`,`FEED_TYPE`),
    ADD KEY `SUBSCRIPTION_PERSISTENT` (`PERSISTENT`),
    ADD KEY `SUBSCRIPTION_FEED_TYPE` (`FEED_TYPE`);

ALTER TABLE `tick`
    DROP KEY `DATE_TIME_SECURITY_FEED_TYPE_UNIQUE`,
    DROP KEY `DATE_TIME`,
    DROP KEY `SECURITY_FK`,
    DROP KEY `FEED_TYPE`,
    ADD UNIQUE KEY `TICK_DATE_TIME_SECURITY_FEED_TYPE_UNIQUE` (`DATE_TIME`,`SECURITY_FK`,`FEED_TYPE`),
    ADD KEY `TICK_DATE_TIME` (`DATE_TIME`),
    ADD KEY `TICK_SECURITY_FK` (`SECURITY_FK`),
    ADD KEY `TICK_FEED_TYPE` (`FEED_TYPE`);

ALTER TABLE `transaction`
    DROP KEY `EXT_ID_UNIQUE`,
    DROP KEY `DATE_TIME`,
    DROP KEY `SETTLEMENT_DATE`,
    DROP KEY `CURRENCY`,
    ADD UNIQUE KEY `TRANSACTION_EXT_ID_UNIQUE` (`EXT_ID`),
    ADD KEY `TRANSACTION_DATE_TIME` (`DATE_TIME`),
    ADD KEY `TRANSACTION_SETTLEMENT_DATE` (`SETTLEMENT_DATE`),
    ADD KEY `TRANSACTION_CURRENCY` (`CURRENCY`),
    ADD KEY `TRANSACTION_INT_ORDER_ID` (`INT_ORDER_ID`),
    ADD KEY `TRANSACTION_EXT_ORDER_ID` (`EXT_ORDER_ID`),
    ADD KEY `TRANSACTION_TYPE` (`TYPE`);

delimiter $$

CREATE PROCEDURE `validate_security_family`(
    IN `CLASS` varchar(255),
    IN `INTREST` double,
    IN `DIVIDEND` double,
    IN `EXPIRATION_TYPE` varchar(255),
    IN `EXPIRATION_DISTANCE` varchar(255),
    IN `STRIKE_DISTANCE` double,
    IN `WEEKLY` bit(1),
    IN `MATURITY_DISTANCE` varchar(255),
    IN `LENGTH` int(11),
    IN `QUOTATION_STYLE` varchar(255))
BEGIN
    DECLARE null_not_allowed CONDITION FOR SQLSTATE '23000';
    IF `CLASS` = 'FutureFamilyImpl' THEN
        IF `INTREST` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''INTEREST'' cannot be null for future family';
        ELSEIF `DIVIDEND` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''DIVIDEND'' cannot be null for future family';
        ELSEIF `EXPIRATION_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_TYPE'' cannot be null for future family';
        ELSEIF `EXPIRATION_DISTANCE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_DISTANCE'' cannot be null for future family';
        ELSEIF `LENGTH` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''LENGTH'' cannot be null for future family';
        END IF;
    ELSEIF `CLASS` = 'GenericFutureFamilyImpl' THEN
        IF `EXPIRATION_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_TYPE'' cannot be null for generic future family';
        ELSEIF `EXPIRATION_DISTANCE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_DISTANCE'' cannot be null for generic future family';
        END IF;
    ELSEIF `CLASS` = 'OptionFamilyImpl' THEN
        IF `INTREST` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''INTEREST'' cannot be null for option family';
        ELSEIF `DIVIDEND` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''DIVIDEND'' cannot be null for option family';
        ELSEIF `EXPIRATION_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_TYPE'' cannot be null for option family';
        ELSEIF `EXPIRATION_DISTANCE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION_DISTANCE'' cannot be null for option family';
        ELSEIF `STRIKE_DISTANCE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''STRIKE_DISTANCE'' cannot be null for option family';
        ELSEIF `WEEKLY` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''WEEKLY'' cannot be null for option family';
        END IF;
    ELSEIF `CLASS` = 'BondFamilyImpl' THEN
        IF `MATURITY_DISTANCE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''MATURITY_DISTANCE'' cannot be null for bond family';
        ELSEIF `LENGTH` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''LENGTH'' cannot be null for bond family';
        ELSEIF `QUOTATION_STYLE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''QUOTATION_STYLE'' cannot be null for bond family';
        END IF;
    END IF;
END;
$$

CREATE TRIGGER `security_family_on_insert`
BEFORE INSERT ON `security_family`
FOR EACH ROW
BEGIN
    CALL `validate_security_family`(NEW.`CLASS`, NEW. `INTREST`, NEW.`DIVIDEND`,  NEW.`EXPIRATION_TYPE`, NEW.`EXPIRATION_DISTANCE`,
    NEW.`STRIKE_DISTANCE`, NEW.`WEEKLY`, NEW.`MATURITY_DISTANCE`, NEW.`LENGTH`, NEW.`QUOTATION_STYLE`);
END;
$$

CREATE TRIGGER `security_family_on_update`
BEFORE UPDATE ON `security_family`
FOR EACH ROW
BEGIN
    CALL `validate_security_family`(NEW.`CLASS`, NEW. `INTREST`, NEW.`DIVIDEND`,  NEW.`EXPIRATION_TYPE`, NEW.`EXPIRATION_DISTANCE`,
    NEW.`STRIKE_DISTANCE`, NEW.`WEEKLY`, NEW.`MATURITY_DISTANCE`, NEW.`LENGTH`, NEW.`QUOTATION_STYLE`);
END;
$$

CREATE PROCEDURE `validate_security`(
    IN `CLASS` varchar(255),
    IN `MATURITY` date,
    IN `COUPON` double,
    IN `COMMODITY_TYPE` varchar(255),
    IN `BASE_CURRENCY` varchar(255),
    IN `EXPIRATION` date,
    IN `MONTH_YEAR` char(6),
    IN `FIRST_NOTICE` date,
    IN `DURATION` varchar(255),
    IN `ASSET_CLASS` varchar(255),
    IN `STRIKE` decimal(12,5),
    IN `OPTION_TYPE` varchar(255),
    IN `GICS` char(8),
    IN `UUID` char(36),
    IN `COMBINATION_TYPE` varchar(255),
    IN `PERSISTENT` bit(1))
BEGIN
    DECLARE null_not_allowed CONDITION FOR SQLSTATE '23000';
    IF `CLASS` = 'CombinationImpl' THEN
        IF `UUID` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''UUID'' cannot be null for combination';
        ELSEIF `COMBINATION_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''COMBINATION_TYPE'' cannot be null for combination';
        ELSEIF `PERSISTENT` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''PERSISTENT'' cannot be null for combination';
        END IF;
    ELSEIF `CLASS` = 'CommodityImpl' THEN
        IF `COMMODITY_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''COMMODITY_TYPE'' cannot be null for commodity';
        END IF;
    ELSEIF `CLASS` = 'BondImpl' THEN
        IF `MATURITY` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''MATURITY'' cannot be null for bond';
        ELSEIF `COUPON` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''COUPON'' cannot be null for bond';
        END IF;
    ELSEIF `CLASS` = 'FutureImpl' THEN
        IF `EXPIRATION` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION'' cannot be null for future';
        ELSEIF `MONTH_YEAR` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''MONTH_YEAR'' cannot be null for future';
        ELSEIF `FIRST_NOTICE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''FIRST_NOTICE'' cannot be null for future';
        END IF;
    ELSEIF `CLASS` = 'IntrestRateImpl' THEN
        IF `DURATION` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''DURATION'' cannot be null for interest rate';
        END IF;
    ELSEIF `CLASS` = 'GenericFutureImpl' THEN
        IF `DURATION` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''DURATION'' cannot be null for generic future';
        ELSEIF `ASSET_CLASS` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''ASSET_CLASS'' cannot be null for generic future';
        END IF;
    ELSEIF `CLASS` = 'OptionImpl' THEN
        IF `STRIKE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''STRIKE'' cannot be null for option';
        ELSEIF `EXPIRATION` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''EXPIRATION'' cannot be null for option';
        ELSEIF `OPTION_TYPE` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''OPTION_TYPE'' cannot be null for option';
        END IF;
    ELSEIF `CLASS` = 'IndexImpl' THEN
        IF `ASSET_CLASS` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''ASSET_CLASS'' cannot be null for index';
        END IF;
    ELSEIF `CLASS` = 'ForexImpl' THEN
        IF `BASE_CURRENCY` IS NULL THEN
            SIGNAL null_not_allowed set message_text = 'Column ''BASE_CURRENCY'' cannot be null for forex';
        END IF;
    END IF;
END;
$$

CREATE TRIGGER `security_on_insert`
BEFORE INSERT ON `security`
FOR EACH ROW
BEGIN
    CALL `validate_security`(NEW.`CLASS`, NEW. `MATURITY`, NEW.`COUPON`,  NEW.`COMMODITY_TYPE`, NEW.`BASE_CURRENCY`,
    NEW.`EXPIRATION`, NEW.`MONTH_YEAR`, NEW.`FIRST_NOTICE`, NEW.`DURATION`, NEW.`ASSET_CLASS`, NEW.`STRIKE`,
    NEW.`OPTION_TYPE`, NEW.`GICS`, NEW.`UUID`, NEW.`COMBINATION_TYPE`, NEW.`PERSISTENT`);
END;
$$

CREATE TRIGGER `security_on_update`
BEFORE UPDATE ON `security`
FOR EACH ROW
BEGIN
    CALL `validate_security`(NEW.`CLASS`, NEW. `MATURITY`, NEW.`COUPON`,  NEW.`COMMODITY_TYPE`, NEW.`BASE_CURRENCY`,
    NEW.`EXPIRATION`, NEW.`MONTH_YEAR`, NEW.`FIRST_NOTICE`, NEW.`DURATION`, NEW.`ASSET_CLASS`, NEW.`STRIKE`,
    NEW.`OPTION_TYPE`, NEW.`GICS`, NEW.`UUID`, NEW.`COMBINATION_TYPE`, NEW.`PERSISTENT`);
END;
$$

delimiter ;
