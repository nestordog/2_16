update security
set on_watchlist = false
where symbol like 'osmi%'
and on_watchlist = true;

delete from stock_option
where id in (
  select id
  from security
  where isin is null);

delete from transaction;

delete from position;

update SECURITY
set position_fk = null
where not position_fk is null;

delete from security
where isin is null;

INSERT INTO `transaction` (`id`, `DATE_TIME`, `QUANTITY`, `PRICE`, `COMMISSION`, `TYPE`, `SECURITY_FK`, `ACCOUNT_FK`, `POSITION_FK`) VALUES
  (-1,'1999-01-01 00:00:00',-1,10000,0,'CREDIT',NULL,1,NULL);

UPDATE rule
set target_fk = NULL;

UPDATE rule
set activatable = false
where prepared = true;
