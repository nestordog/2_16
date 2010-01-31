/*
    SQL Manager 2005 for MySQL
    SQL Editor queries
 */

/*  Page 1  */

SELECT t1.date_time,
       t1.type,
       s1.symbol,
       t1.quantity,
       t1.price,
       t1.commission,
       (SELECT sum(-t2.quantity * t2.price - t2.commission) from transaction t2 where t2.date_time <= t1.date_time) as saldo
FROM transaction t1
left outer join security s1 on t1.security_fk = s1.id
order by t1.date_time ASC;


