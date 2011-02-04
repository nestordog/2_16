#!/bin/sh
set -v off

cd /usr/local/AlgoTrader/backup

/usr/local/bin/mysqldump -u @localhost algotrader > backup.sql

/usr/local/bin/ftpsync.pl /usr/local/AlgoTrader/backup/ ftp://aflury:franklin91@bubba/AlgoTrader/backup/sql/

/usr/local/bin/ftpsync.pl /usr/local/AlgoTrader/results/tickdata/ ftp://aflury:franklin91@bubba/AlgoTrader/backup/tickdata/
