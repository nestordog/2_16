set @indexId = 29;
set @expiration = '2009-02-18 13:00:00';
set @type = 'CALL';
set @spot = -48.01;
set @date = '2008-12-24 00:00:00';

select s.*, so.*
from stock_option so
inner join security s on s.id = so.id
join tick as t on s.id = t.security_fk
where s.underlaying_fk = @indexId
and so.expiration >= @expiration
and if(so.type = 'PUT', so.strike, -so.strike)  <= if(so.type = 'PUT', @spot, -@spot)
and so.type = @type
and t.date_time = @date
order by so.expiration asc, if(so.type = 'PUT', -so.strike, so.strike) asc
limit 1

