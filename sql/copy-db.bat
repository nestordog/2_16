%MySQL_HOME%\bin\mysqladmin drop algotrader1 -u root --password=password -f
%MySQL_HOME%\bin\mysqladmin create algotrader1 -u root --password=password
%MySQL_HOME%\bin\mysqldump.exe -u root --password=password  algotrader | %MySQL_HOME%\bin\mysql.exe -u root --password=password algotrader1

%MySQL_HOME%\bin\mysqladmin drop algotrader2 -u root --password=password -f
%MySQL_HOME%\bin\mysqladmin create algotrader2 -u root --password=password
%MySQL_HOME%\bin\mysqldump.exe -u root --password=password  algotrader | %MySQL_HOME%\bin\mysql.exe -u root --password=password algotrader2

%MySQL_HOME%\bin\mysqladmin drop algotrader3 -u root --password=password -f
%MySQL_HOME%\bin\mysqladmin create algotrader3 -u root --password=password
%MySQL_HOME%\bin\mysqldump.exe -u root --password=password  algotrader | %MySQL_HOME%\bin\mysql.exe -u root --password=password algotrader3

%MySQL_HOME%\bin\mysqladmin drop algotrader4 -u root --password=password -f
%MySQL_HOME%\bin\mysqladmin create algotrader4 -u root --password=password
%MySQL_HOME%\bin\mysqldump.exe -u root --password=password  algotrader | %MySQL_HOME%\bin\mysql.exe -u root --password=password algotrader4

%MySQL_HOME%\bin\mysqladmin drop algotrader5 -u root --password=password -f
%MySQL_HOME%\bin\mysqladmin create algotrader5 -u root --password=password
%MySQL_HOME%\bin\mysqldump.exe -u root --password=password  algotrader | %MySQL_HOME%\bin\mysql.exe -u root --password=password algotrader5
