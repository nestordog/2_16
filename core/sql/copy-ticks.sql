set @from = '2010-09-14 00:00:00';
set @to = '2010-09-15 00:00:00';
set @expiration = '2010-09-15 13:00:00';

insert into tick (
    date_time,
    last,
    last_date_time,
    vol,
    vol_bid,
    vol_ask,
    bid,
    ask,
    open_intrest,
    settlement,
    security_fk
)
select
  @to,
  last,
  last_date_time,
  vol,
  vol_bid,
  vol_ask,
  bid,
  ask,
  open_intrest,
  settlement,
  security_fk
  from tick
  join stock_option as s on security_fk = s.id
  where date_time = @from
  and s.expiration = @expiration;
