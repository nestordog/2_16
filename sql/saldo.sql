  select
    `t1`.`DATE_TIME` AS `date_time`,
    `t1`.`TYPE` AS `type`,
    `s1`.`SYMBOL` AS `symbol`,
    `s1`.`POSITION_FK` AS `POSITION_FK`,
    `o1`.`STRIKE` AS `STRIKE`,
    `o1`.`EXPIRATION` AS `expiration`,
    `t1`.`QUANTITY` AS `quantity`,
    `t1`.`PRICE` AS `price`,
    `t1`.`COMMISSION` AS `commission`,
    (
  select
    sum(((-(`t2`.`QUANTITY`) * `t2`.`PRICE`) - `t2`.`COMMISSION`)) AS `sum(-t2.quantity * t2.price - t2.commission)`
  from
    `transaction` `t2`
  where
    (`t2`.`id` <= `t1`.`id`)) AS `saldo`
  from
    ((`transaction` `t1` left join `security` `s1` on((`t1`.`SECURITY_FK` = `s1`.`id`))) left join `stock_option` `o1` on((`o1`.`ID` = `s1`.`id`)))
  order by
    `t1`.`id`;
