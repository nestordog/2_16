"D:\MySQL Server 5.1\bin\mysqldump.exe" -d -u @localhost -r D:\AlgoTrader\sql\db-structure.sql algotrader
"D:\MySQL Server 5.1\bin\mysqldump.exe" -c -t -u @localhost -r D:\AlgoTrader\sql\db-data.sql algotrader

"D:\AlgoTrader\sql\unix2dos.exe" D:\AlgoTrader\sql\db-structure.sql
"D:\AlgoTrader\sql\unix2dos.exe" D:\AlgoTrader\sql\db-data.sql
