select avg(
DATEDIFF(t1.date_time,
(select t2.date_time
  from transaction as t2
  where t2.security_fk = t1.security_fk
  and type = 'SELL'
  and t2.date_time < t1.date_time
  order by t2.date_time desc
  limit 1))) as diff
from transaction as t1
where (type = 'BUY' or type = 'EXPIRATION')
order by diff desc
