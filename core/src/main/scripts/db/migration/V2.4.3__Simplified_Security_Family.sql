ALTER TABLE `security_family`
  ADD `CLASS` varchar(255),
  ADD `INTREST` double DEFAULT NULL,
  ADD `DIVIDEND` double DEFAULT NULL,
  ADD `EXPIRATION_TYPE` enum('NEXT_3_RD_FRIDAY','NEXT_3_RD_FRIDAY_3_MONTHS','NEXT_3_RD_MONDAY_3_MONTHS','THIRTY_DAYS_BEFORE_NEXT_3_RD_FRIDAY') DEFAULT NULL,
  ADD `EXPIRATION_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') DEFAULT NULL,
  ADD `STRIKE_DISTANCE` double DEFAULT NULL,
  ADD `WEEKLY` bit(1) DEFAULT NULL,
  ADD `MATURITY_DISTANCE` enum('MSEC_1','SEC_1','MIN_1','MIN_2','MIN_5','MIN_15','MIN_30','HOUR_1','HOUR_2','DAY_1','DAY_2','WEEK_1','WEEK_2','MONTH_1','MONTH_2','MONTH_3','MONTH_4','MONTH_5','MONTH_6','MONTH_7','MONTH_8','MONTH_9','MONTH_10','MONTH_11','MONTH_18','YEAR_1','YEAR_2') DEFAULT NULL,
  ADD `LENGTH` int(11) DEFAULT NULL,
  ADD `QUOTATION_STYLE` enum('PRICE','YIELD') DEFAULT NULL;

UPDATE `security_family` sf, `future_family` ff SET
  sf.`CLASS` = 'FutureFamilyImpl',
  sf.`INTREST` = ff.`INTREST`,
  sf.`DIVIDEND` = ff.`DIVIDEND`,
  sf.`EXPIRATION_TYPE` = ff.`EXPIRATION_TYPE`,
  sf.`EXPIRATION_DISTANCE` = ff.`EXPIRATION_DISTANCE`,
  sf.`LENGTH` = ff.`LENGTH`
WHERE sf.`ID` = ff.`ID`;

UPDATE `security_family` sf, `generic_future_family` ff SET
  sf.`CLASS` = 'GenericFutureFamilyImpl',
  sf.`EXPIRATION_TYPE` = ff.`EXPIRATION_TYPE`,
  sf.`EXPIRATION_DISTANCE` = ff.`EXPIRATION_DISTANCE`
WHERE sf.`ID` = ff.`ID`;

UPDATE `security_family` sf, `option_family` of SET
  sf.`CLASS` = 'OptionFamilyImpl',
  sf.`INTREST` = of.`INTREST`,
  sf.`DIVIDEND` = of.`DIVIDEND`,
  sf.`EXPIRATION_TYPE` = of.`EXPIRATION_TYPE`,
  sf.`EXPIRATION_DISTANCE` = of.`EXPIRATION_DISTANCE`,
  sf.`STRIKE_DISTANCE` = of.`STRIKE_DISTANCE`,
  sf.`WEEKLY` = of.`WEEKLY`
WHERE sf.`ID` = of.`ID`;

UPDATE `security_family` sf, `bond_family` bf SET
  sf.`CLASS` = 'BondFamilyImpl',
  sf.`MATURITY_DISTANCE` = bf.`MATURITY_DISTANCE`,
  sf.`LENGTH` = bf.`LENGTH`,
  sf.`QUOTATION_STYLE` = bf.`QUOTATION_STYLE`
WHERE sf.`ID` = bf.`ID`;

UPDATE `security_family` SET
  `CLASS` = 'SecurityFamilyImpl'
WHERE `CLASS` IS NULL;

ALTER TABLE `security_family`
  MODIFY `CLASS` varchar(255) NOT NULL;

DROP TABLE bond_family;
DROP TABLE option_family;
DROP TABLE generic_future_family;
DROP TABLE future_family;