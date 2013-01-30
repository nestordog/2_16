mysqldump.exe --skip-triggers --no-data -u @localhost -r ^
D:\AlgoTrader\core\sql\db-structure.sql ^
algotrader

mysqldump.exe ^
--skip-extended-insert  --skip-triggers ^
--ignore-table=algotrader.bar ^
--ignore-table=algotrader.cash_balance ^
--ignore-table=algotrader.combination ^
--ignore-table=algotrader.component ^
--ignore-table=algotrader.forex_future ^
--ignore-table=algotrader.future ^
--ignore-table=algotrader.measurement ^
--ignore-table=algotrader.portfolio_value ^
--ignore-table=algotrader.position ^
--ignore-table=algotrader.stock_option ^
--ignore-table=algotrader.synthetic_index ^
--ignore-table=algotrader.tick ^
--where="id<1000" ^
--complete-insert --no-create-info -u @localhost ^
-r D:\AlgoTrader\core\sql\db-data.sql ^
algotrader

"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-structure.sql
"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-data.sql
