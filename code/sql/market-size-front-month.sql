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
group by u.id
order by volVal desc

