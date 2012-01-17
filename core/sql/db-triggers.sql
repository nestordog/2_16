
      /*
      * Script created by Quest Schema Compare at 22.11.2011 12:55:28.
      * Please back up your database before running this script.
      *
      * Synchronizing objects from algotradertest to algotrader.
      */
    USE `algotrader`;


/* Header line. Object: allocation_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `allocation_after_update`
  AFTER UPDATE ON `allocation`
  FOR EACH ROW BEGIN
     IF NOT NEW.QUANTITY = OLD.QUANTITY OR (NEW.QUANTITY IS NULL XOR OLD.QUANTITY IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('allocation', NEW.id, NOW(), 'QUANTITY', NEW.QUANTITY);
     END IF;
END;

/* Header line. Object: cash_balance_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `cash_balance_after_update`
  AFTER UPDATE ON `cash_balance`
  FOR EACH ROW BEGIN
     IF NOT NEW.AMOUNT = OLD.AMOUNT OR (NEW.AMOUNT IS NULL XOR OLD.AMOUNT IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('cash_balance', NEW.id, NOW(), 'AMOUNT', NEW.AMOUNT);
     END IF;
END;

/* Header line. Object: combination_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `combination_after_update`
  AFTER UPDATE ON `combination`
  FOR EACH ROW BEGIN
     IF NOT NEW.EXIT_VALUE = OLD.EXIT_VALUE OR (NEW.EXIT_VALUE IS NULL XOR OLD.EXIT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('combination', NEW.id, NOW(), 'EXIT_VALUE', NEW.EXIT_VALUE);
     END IF;
     IF NOT NEW.PROFIT_TARGET = OLD.PROFIT_TARGET OR (NEW.PROFIT_TARGET IS NULL XOR OLD.PROFIT_TARGET IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('combination', NEW.id, NOW(), 'PROFIT_TARGET', NEW.PROFIT_TARGET);
     END IF;
END;

/* Header line. Object: position_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `position_after_update`
  AFTER UPDATE ON `position`
  FOR EACH ROW BEGIN
     IF NOT NEW.QUANTITY = OLD.QUANTITY OR (NEW.QUANTITY IS NULL XOR OLD.QUANTITY IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'QUANTITY', NEW.QUANTITY);
     END IF;
     IF NOT NEW.EXIT_VALUE = OLD.EXIT_VALUE OR (NEW.EXIT_VALUE IS NULL XOR OLD.EXIT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'EXIT_VALUE', NEW.EXIT_VALUE);
     END IF;
     IF NOT NEW.PROFIT_TARGET = OLD.PROFIT_TARGET OR (NEW.PROFIT_TARGET IS NULL XOR OLD.PROFIT_TARGET IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'PROFIT_TARGET', NEW.PROFIT_TARGET);
     END IF;
     IF NOT NEW.MAINTENANCE_MARGIN = OLD.MAINTENANCE_MARGIN OR (NEW.MAINTENANCE_MARGIN IS NULL XOR OLD.MAINTENANCE_MARGIN IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'MAINTENANCE_MARGIN', NEW.MAINTENANCE_MARGIN);
     END IF;
END;

/* Header line. Object: strategy_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `strategy_after_update`
  AFTER UPDATE ON `strategy`
  FOR EACH ROW BEGIN
     IF NOT NEW.ALLOCATION = OLD.ALLOCATION OR (NEW.ALLOCATION IS NULL XOR OLD.ALLOCATION IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('strategy', NEW.id, NOW(), 'ALLOCATION', NEW.ALLOCATION);
     END IF;
END;

/* Header line. Object: watch_list_item_after_update. Script date: 22.11.2011 12:55:28. */
CREATE TRIGGER `watch_list_item_after_update`
  AFTER UPDATE ON `watch_list_item`
  FOR EACH ROW BEGIN
     IF NOT NEW.UPPER_ALERT_VALUE = OLD.UPPER_ALERT_VALUE OR (NEW.UPPER_ALERT_VALUE IS NULL XOR OLD.UPPER_ALERT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('watch_list_item', NEW.id, NOW(), 'UPPER_ALERT_VALUE', NEW.UPPER_ALERT_VALUE);
     END IF;
     IF NOT NEW.LOWER_ALERT_VALUE = OLD.LOWER_ALERT_VALUE OR (NEW.LOWER_ALERT_VALUE IS NULL XOR OLD.LOWER_ALERT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('watch_list_item', NEW.id, NOW(), 'LOWER_ALERT_VALUE', NEW.LOWER_ALERT_VALUE);
     END IF;
END;
