select u.symbol,
sum(o.`CONTRACT_SIZE` * t.`LAST` * t.`OPEN_INTREST`) as market
from stock_option as o
join tick as t on t.`SECURITY_FK` = o.`ID`
join security as s on o.id = s.id
join security as u on s.UNDERLAYING_FK = u.id
group by u.id
order by market desc
