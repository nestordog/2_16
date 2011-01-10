CREATE OR REPLACE VIEW saldo AS
SELECT
  t1.DATE_TIME AS date_time,
  t1.TYPE AS type,
  s1.SYMBOL AS symbol,
  s1.ISIN AS isin,
  t1.POSITION_FK AS position_fk,
  o1.STRIKE AS STRIKE,
  o1.EXPIRATION AS expiration,
  t1.QUANTITY AS quantity,
  t1.PRICE AS price,
  t1.COMMISSION AS commission,
  (SELECT SUM(CASE t2.TYPE WHEN 'CREDIT' THEN t2.PRICE WHEN 'DEBIT' THEN t2.PRICE ELSE (-t2.QUANTITY * t2.PRICE - t2.COMMISSION) END)
    FROM transaction AS t2
    WHERE t2.id <= t1.id
  ) AS saldo
FROM transaction t1
LEFT JOIN security s1 ON t1.SECURITY_FK = s1.id
LEFT JOIN stock_option o1 ON o1.ID = s1.id
ORDER BY t1.id;
