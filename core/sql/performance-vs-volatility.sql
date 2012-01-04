select DATE_FORMAT(s1.date_time, '%Y %m'),
(select s2.saldo
  from saldo as s2
  where DATE_FORMAT(s1.date_time, '%Y %m') = DATE_FORMAT(s2.date_time, '%Y %m')
  limit 1) as start,
(select avg (v.value)
   from vsmi as v
   where DATE_FORMAT(FROM_UNIXTIME(v.date_time / 1000), '%Y %m') = DATE_FORMAT(s1.date_time, '%Y %m')
   group by DATE_FORMAT(FROM_UNIXTIME(v.date_time / 1000), '%Y %m')) as vola
from saldo as s1
group by DATE_FORMAT(s1.date_time, '%Y %m')
