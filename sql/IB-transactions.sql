select t.currency, s.symbol, t.date_time, t.quantity, t.price, t.commission, s.security_family_fk
from transaction as t
join security as s on t.security_fk = s.id
order by s.security_family_fk, t.currency, t.date_time desc
