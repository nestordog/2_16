select u.symbol,
sum(ot.last * ot.`VOL` * o.`CONTRACT_SIZE` * r.value) as marketSize
from stock_option as o
join security as s on o.id = s.id
join security as u on s.UNDERLAYING_FK = u.id
join tick as ot on ot.`SECURITY_FK` = o.`ID`
join tick as ut on ut.`SECURITY_FK` = u.`ID`
join rate as r on r.`CURRENCY` = s.CURRENCY
where o.strike <= ut.last
and o.strike > ut.last * 0.9
and o.expiration = '2010-04-01'
group by u.symbol
order by marketSize desc

