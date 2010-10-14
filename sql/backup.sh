#!/bin/sh
set -v off

cd /usr/local/AlgoTrader/sql

/usr/local/bin/mysqldump -u @localhost algotrader > backup.sql

/usr/local/bin/smbclient -U aflury -c "cd aflury/Algotrader/backup;put backup.sql" //bubba/home franklin91

cd /usr/local/AlgoTrader/results/tickdata

/usr/local/bin/smbclient -U aflury -c "cd aflury/Algotrader/backup/tickdata;recurse;prompt;mput *" //bubba/home franklin91
