update security set on_watchlist = false
where symbol like 'osmi%'
and on_watchlist = true;

delete from stock_option
where id in (
  select id
  from security
  where isin is null);

delete from security
where isin is null;

delete from transaction;

delete from position;

INSERT INTO `transaction` (`id`, `NUMBER`, `DATE_TIME`, `QUANTITY`, `PRICE`, `COMMISSION`, `TYPE`, `SECURITY_FK`, `ACCOUNT_FK`, `POSITION_FK`) VALUES
  (1,1111,'2010-01-14 18:04:31',1,10000,0,'CREDIT',NULL,1,NULL);
