set db=algotrader

SET mypath=%~dp0
cd %mypath:~0,-1%

mysqldump.exe ^
--skip-triggers ^
--skip-set-charset ^
--no-data ^
-u root ^
-ppassword -r ^
db-structure.sql ^
%db%

mysqldump.exe ^
--complete-insert ^
--no-create-info ^
--skip-add-locks ^
--skip-disable-keys ^
--skip-extended-insert ^
--skip-triggers ^
--ignore-table=%db%.allocation ^
--ignore-table=%db%.bar ^
--ignore-table=%db%.bond ^
--ignore-table=%db%.bond_family ^
--ignore-table=%db%.cash_balance ^
--ignore-table=%db%.combination ^
--ignore-table=%db%.commodity ^
--ignore-table=%db%.component ^
--ignore-table=%db%.easy_to_borrow ^
--ignore-table=%db%.forex_future ^
--ignore-table=%db%.fund ^
--ignore-table=%db%.future ^
--ignore-table=%db%.generic_tick ^
--ignore-table=%db%.intrest_rate ^
--ignore-table=%db%.option ^
--ignore-table=%db%.order ^
--ignore-table=%db%.order_status ^
--ignore-table=%db%.order_property ^
--ignore-table=%db%.measurement ^
--ignore-table=%db%.portfolio_value ^
--ignore-table=%db%.position ^
--ignore-table=%db%.stock_option ^
--ignore-table=%db%.synthetic_index ^
--ignore-table=%db%.tick ^
--where="id<1000" ^
-u root ^
-ppassword ^
-r db-data.sql ^
%db%

"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-structure.sql
"D:\AlgoTrader\core\sql\unix2dos.exe" D:\AlgoTrader\core\sql\db-data.sql
