SELECT t1.date_time,
       t1.type,
       s1.symbol,
       s1.position_fk,
       t1.quantity,
       t1.price,
       t1.commission,
       (SELECT sum(-t2.quantity * t2.price - t2.commission)
        from transaction t2
        where t2.id <= t1.id) as saldo
FROM transaction t1
left outer join security s1 on t1.security_fk = s1.id
order by t1.id ASC;


