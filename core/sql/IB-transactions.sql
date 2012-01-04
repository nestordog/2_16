SELECT
  t.DATE_TIME,
  t.QUANTITY,
  t.PRICE,
  -t.QUANTITY * t.PRICE * security_family.CONTRACT_SIZE as proceeds,
  t.COMMISSION,
  t.CURRENCY,
  t.TYPE,
  s.SYMBOL,
  so.STRIKE,
  so.EXPIRATION,
  so.TYPE
FROM
  transaction as t
  INNER JOIN security as s ON (t.SECURITY_FK = s.id)
  LEFT OUTER JOIN stock_option as so ON (s.id = so.ID)
  INNER JOIN security_family ON (s.SECURITY_FAMILY_FK = security_family.id)
WHERE
  t.date_time < '2011-08-01 00:00:00'
ORDER BY
  t.CURRENCY,
  so.`TYPE`,
  so.EXPIRATION DESC,
  so.STRIKE,
  t.DATE_TIME
