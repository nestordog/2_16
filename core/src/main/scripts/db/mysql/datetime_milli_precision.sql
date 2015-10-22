ALTER TABLE `bar`
    MODIFY `DATE_TIME` datetime(3) NOT NULL;

ALTER TABLE `generic_tick`
    MODIFY `DATE_TIME` datetime(3) NOT NULL;

ALTER TABLE `measurement`
    MODIFY `DATE_TIME` datetime(3) NOT NULL;

ALTER TABLE `order`
    MODIFY `DATE_TIME` datetime(3) NOT NULL,
    MODIFY `TIF_DATE_TIME` datetime(3) DEFAULT NULL;

ALTER TABLE `order_status`
    MODIFY `DATE_TIME` datetime(3) NOT NULL,
    MODIFY `EXT_DATE_TIME` datetime(3) NOT NULL;

ALTER TABLE `portfolio_value`
    MODIFY `DATE_TIME` datetime(3) NOT NULL;

ALTER TABLE `property`
    MODIFY  `DATE_TIME_VALUE` datetime(3) DEFAULT NULL;

ALTER TABLE `tick`
    MODIFY `DATE_TIME` datetime(3) NOT NULL,
    MODIFY `LAST_DATE_TIME` datetime DEFAULT NULL;

ALTER TABLE `transaction`
    MODIFY  `DATE_TIME` datetime(3) NOT NULL;



