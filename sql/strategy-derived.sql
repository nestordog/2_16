CREATE OR REPLACE VIEW strategy_derived AS
SELECT strategy.ID,
(SELECT IFNULL(SUM(p1.MAINTENANCE_MARGIN),0)
  FROM strategy as st1
  INNER JOIN position as p1 ON st1.id = p1.STRATEGY_FK
  WHERE st1.id = strategy.ID
) as MAINTENANCE_MARGIN,
(SELECT IFNULL(SUM(-t2.QUANTITY * f2.CONTRACT_SIZE * t2.PRICE - t2.COMMISSION),0) +
  (SELECT SUM(CASE WHEN (t3.type = 'CREDIT' OR t3.type = 'INTREST') THEN t3.PRICE WHEN (t3.type = 'DEBIT' OR t3.type = 'FEES') THEN -t3.PRICE END) FROM transaction as t3)
  * st2.ALLOCATION
  FROM strategy  as st2
  LEFT JOIN transaction as t2 ON st2.ID = t2.STRATEGY_FK and (t2.type = 'BUY' OR t2.type = 'SELL' OR t2.type = 'EXPIRATION')
  LEFT JOIN security as s2 ON t2.SECURITY_FK = s2.ID
  LEFT JOIN security_family as f2 ON s2.SECURITY_FAMILY_FK = f2.ID
  WHERE st2.ID = strategy.ID
) as CASH_BALANCE,
(SELECT IFNULL(SUM(-p3.QUANTITY * f3.CONTRACT_SIZE * p3.EXIT_VALUE),0)
  FROM strategy  as st3
  LEFT JOIN position as p3 ON st3.ID = p3.STRATEGY_FK
  LEFT JOIN security as s3 ON p3.ID = s3.POSITION_FK
  LEFT JOIN security_family as f3 ON s3.SECURITY_FAMILY_FK = f3.ID
  WHERE st3.ID = strategy.ID
) AS REDEMPTION_VALUE
FROM STRATEGY
WHERE strategy.ID > 0
GROUP BY strategy.ID
