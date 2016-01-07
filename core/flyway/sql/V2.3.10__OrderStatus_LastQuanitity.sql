ALTER TABLE `order_status` ADD `LAST_QUANTITY` bigint(20);
UPDATE `order_status` SET `LAST_QUANTITY` = 0;
ALTER TABLE `order_status` MODIFY COLUMN `LAST_QUANTITY` bigint(20) NOT NULL;
