CREATE VIEW rules_by_strategy AS
SELECT
  `strategy`.NAME AS STRATEGY_NAME,
  `rule`.NAME AS RULE_NAME,
  `rule`.PRIORITY,
  `rule`.DEFINITION,
  `rule`.SUBSCRIBER,
  `rule`.LISTENERS,
  `rule`.AUTO_ACTIVATE,
  `rule`.INIT,
  `rule`.id
FROM
  `rules2strategies`
  INNER JOIN `strategy` ON (`rules2strategies`.STRATEGIES_FK = `strategy`.id)
  INNER JOIN `rule` ON (`rules2strategies`.RULES_FK = `rule`.id)
ORDER BY
  STRATEGY_NAME,
  `rule`.id
