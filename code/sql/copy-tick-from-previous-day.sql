set @to = '2011-05-30 00:00:00';
set @from = '2011-05-27 00:00:00';

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
) select
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
where date_time = @from;
