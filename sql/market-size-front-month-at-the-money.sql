select * from (
  select u.id,
  u.symbol,
  u.market,
  ut.last as underlayingLast,
  o.strike,
  o.expiration,
  ot.last * ot.`OPEN_INTREST` * o.`CONTRACT_SIZE` * r.value as marketSize
  from stock_option as o
  join security as s on o.id = s.id
  join security as u on s.UNDERLAYING_FK = u.id
  join tick as ot on ot.`SECURITY_FK` = o.`ID`
  join tick as ut on ut.`SECURITY_FK` = u.`ID`
  join rate as r on r.`CURRENCY` = s.CURRENCY
  where o.strike <= ut.last
  and o.expiration >= '2010-04-01'
  order by u.symbol asc, o.strike desc, o.expiration asc, market desc
) as list
group by id
order by marketSize desc

