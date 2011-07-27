
USE `algotrader`;

/* Header line. Object: history. Script date: 15.06.2011 18:32:11. */
CREATE TABLE `algotrader`.`history` (
    `id` int(11) NOT NULL auto_increment,
    `TBL` varchar(255) NOT NULL,
    `REF_ID` int(11) NOT NULL,
    `TIME` datetime NOT NULL,
    `COL` varchar(255) default NULL,
    `VALUE` varchar(255) default NULL,
    PRIMARY KEY  ( `id` )
)
ENGINE = InnoDB
CHARACTER SET = latin1
AUTO_INCREMENT = 232
ROW_FORMAT = Compact
;

/* Header line. Object: position_after_update. Script date: 15.06.2011 18:32:11. */
CREATE TRIGGER `position_after_update`
  AFTER UPDATE ON `position`
  FOR EACH ROWBEGIN
     IF NOT NEW.QUANTITY = OLD.QUANTITY OR (NEW.QUANTITY IS NULL XOR OLD.QUANTITY IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'QUANTITY', NEW.QUANTITY);
     END IF;
     IF NOT NEW.EXIT_VALUE = OLD.EXIT_VALUE OR (NEW.EXIT_VALUE IS NULL XOR OLD.EXIT_VALUE IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'EXIT_VALUE', NEW.EXIT_VALUE);
     END IF;
     IF NOT NEW.MAINTENANCE_MARGIN = OLD.MAINTENANCE_MARGIN OR (NEW.MAINTENANCE_MARGIN IS NULL XOR OLD.MAINTENANCE_MARGIN IS NULL) THEN
        INSERT INTO history (TBL, REF_ID, TIME, COL, VALUE)
        VALUES ('position', NEW.id, NOW(), 'MAINTENANCE_MARGIN', NEW.MAINTENANCE_MARGIN);
     END IF;
END;

