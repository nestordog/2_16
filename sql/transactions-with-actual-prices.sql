select t.date_time,
t.type,
s2.symbol,
t.quantity,
--t.price, as simulatedPrice
(case t.type when 'SELL' then ti.bid when 'BUY' then ti.ask when 'EXPIRATION' then 0 end) as actualPrice,
t.commission
--o1.type, o2.type,
--o1.strike, o2.strike,
--o1.expiration, o2.expiration
from algotrader.transaction as t
join algotrader.stock_option as o1 on t.security_fk = o1.id
join algotraderfull.stock_option as o2 on (o1.type = o2.type and o1.strike = o2.strike and o1.expiration = o2.expiration)
join algotraderfull.security as s2 on o2.id = s2.id
left join algotraderfull.tick as ti on (ti.security_fk = o2.id and ti.date_time = t.date_time)
order by t.date_time
