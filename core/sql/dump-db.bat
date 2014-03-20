set db=algotrader

mysqldump.exe --skip-triggers --no-data -u root -ppassword -r ^
db-structure.sql ^
%db%

mysqldump.exe ^
--skip-extended-insert  --skip-triggers ^
--ignore-table=%db%.bar ^
--ignore-table=%db%.cash_balance ^
--ignore-table=%db%.combination ^
--ignore-table=%db%.component ^
--ignore-table=%db%.forex_future ^
--ignore-table=%db%.future ^
--ignore-table=%db%.measurement ^
--ignore-table=%db%.portfolio_value ^
--ignore-table=%db%.position ^
--ignore-table=%db%.stock_option ^
--ignore-table=%db%.synthetic_index ^
--ignore-table=%db%.tick ^
--where="id<1000" ^
--complete-insert --no-create-info -u root -ppassword ^
-r db-data.sql ^
%db%

"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-structure.sql
"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-data.sql
