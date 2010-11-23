"D:\MySQL Server 5.1\bin\mysqldump.exe" -X --hex-blob -t -u @localhost -r D:\AlgoTrader\sql\rules.xml algotrader rule
"D:\MySQL Server 5.1\bin\mysqldump.exe" -t -u @localhost -r D:\AlgoTrader\sql\rules.sql algotrader rule
"D:\AlgoTrader\sql\unix2dos.exe" D:\AlgoTrader\sql\rules.xml

"D:\MySQL Server 5.1\bin\mysqldump.exe" -d -u @localhost -r D:\AlgoTrader\sql\db-structure.sql algotraderlive
"D:\AlgoTrader\sql\unix2dos.exe" D:\AlgoTrader\sql\db-structure.sql
