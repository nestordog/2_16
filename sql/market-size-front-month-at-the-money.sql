select u.id,
u.symbol,
u.market,
sum(t.`VOL`) as vol,
sum(t.`OPEN_INTREST`) as openIntrest,
sum(o.`CONTRACT_SIZE` * t.`LAST` * t.`VOL` * r.value) as volVal,
sum(o.`CONTRACT_SIZE` * t.`LAST` * t.`OPEN_INTREST` * r.value) as openIntrestVal
from stock_option as o
join tick as t on t.`SECURITY_FK` = o.`ID`
join security as s on o.id = s.id
join security as u on s.UNDERLAYING_FK = u.id
join rate as r on r.`CURRENCY` = s.CURRENCY
where o.expiration = '2010-07-01'
#and u.id = 9972
and o.strike >= (
    select o1.strike
    from stock_option as o1
    join security as s1 on o1.id = s1.id
    join security as u1 on s1.UNDERLAYING_FK = u1.id
    join tick as ut1 on ut1.`SECURITY_FK` = u1.`ID`
    where u1.id = u.id
    and o1.expiration = o.expiration
    and o1.strike < ut1.last
    and o1.type = 'PUT'
    order by o1.strike desc
    limit 1, 1
) and o.strike <= (
  select o2.strike
  from stock_option as o2
  join security as s2 on o2.id = s2.id
  join security as u2 on s2.UNDERLAYING_FK = u2.id
  join tick as ut2 on ut2.`SECURITY_FK` = u2.`ID`
  where u2.id = u.id
  and o2.expiration = o.expiration
  and o2.strike > ut2.last
  and o2.type = 'PUT'
  order by o2.strike asc
  limit 1, 1)
group by u.id
order by volVal desc

