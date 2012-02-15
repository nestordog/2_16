SELECT
  sum(abs(transaction.QUANTITY) * (tick.ASK - tick.BID) * 100 / 2 * 0.3 / 10) AS sum_of_spread_per_year,
  avg(tick.ASK - tick.BID) AS avg_spread,
  sum(abs(transaction.QUANTITY) * 0.3 / 10) AS contracts_per_year
FROM
  transaction
  INNER JOIN tick ON (transaction.SECURITY_FK = tick.SECURITY_FK)
  AND (transaction.DATE_TIME = tick.DATE_TIME)
GROUP BY
  avg(tick.ASK - tick.BID)
