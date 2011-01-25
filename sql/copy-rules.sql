SET FOREIGN_KEY_CHECKS = 0;

-- 1
DROP TABLE IF EXISTS algotrader1.rule;
CREATE TABLE algotrader1.rule LIKE algotrader.rule;
INSERT INTO algotrader1.rule SELECT * FROM algotrader.rule;

DROP TABLE IF EXISTS algotrader1.rules2strategies;
CREATE TABLE algotrader1.rules2strategies LIKE algotrader.rules2strategies;
INSERT INTO algotrader1.rules2strategies SELECT * FROM algotrader.rules2strategies;

UPDATE algotrader1.strategy, algotrader.strategy
SET algotrader1.strategy.allocation = algotrader.strategy.allocation,
algotrader1.strategy.auto_activate = algotrader.strategy.auto_activate
WHERE algotrader1.strategy.id = algotrader.strategy.id;

-- 2
DROP TABLE IF EXISTS algotrader2.rule;
CREATE TABLE algotrader2.rule LIKE algotrader.rule;
INSERT INTO algotrader2.rule SELECT * FROM algotrader.rule;

DROP TABLE IF EXISTS algotrader2.rules2strategies;
CREATE TABLE algotrader2.rules2strategies LIKE algotrader.rules2strategies;
INSERT INTO algotrader2.rules2strategies SELECT * FROM algotrader.rules2strategies;

UPDATE algotrader2.strategy, algotrader.strategy
SET algotrader2.strategy.allocation = algotrader.strategy.allocation,
algotrader2.strategy.auto_activate = algotrader.strategy.auto_activate
WHERE algotrader2.strategy.id = algotrader.strategy.id;

-- 3
DROP TABLE IF EXISTS algotrader3.rule;
CREATE TABLE algotrader3.rule LIKE algotrader.rule;
INSERT INTO algotrader3.rule SELECT * FROM algotrader.rule;

DROP TABLE IF EXISTS algotrader3.rules2strategies;
CREATE TABLE algotrader3.rules2strategies LIKE algotrader.rules2strategies;
INSERT INTO algotrader3.rules2strategies SELECT * FROM algotrader.rules2strategies;

UPDATE algotrader3.strategy, algotrader.strategy
SET algotrader3.strategy.allocation = algotrader.strategy.allocation,
algotrader3.strategy.auto_activate = algotrader.strategy.auto_activate
WHERE algotrader3.strategy.id = algotrader.strategy.id;

-- 4
DROP TABLE IF EXISTS algotrader4.rule;
CREATE TABLE algotrader4.rule LIKE algotrader.rule;
INSERT INTO algotrader4.rule SELECT * FROM algotrader.rule;

DROP TABLE IF EXISTS algotrader4.rules2strategies;
CREATE TABLE algotrader4.rules2strategies LIKE algotrader.rules2strategies;
INSERT INTO algotrader4.rules2strategies SELECT * FROM algotrader.rules2strategies;

UPDATE algotrader4.strategy, algotrader.strategy
SET algotrader4.strategy.allocation = algotrader.strategy.allocation,
algotrader4.strategy.auto_activate = algotrader.strategy.auto_activate
WHERE algotrader4.strategy.id = algotrader.strategy.id;

-- 5
DROP TABLE IF EXISTS algotrader5.rule;
CREATE TABLE algotrader5.rule LIKE algotrader.rule;
INSERT INTO algotrader5.rule SELECT * FROM algotrader.rule;

DROP TABLE IF EXISTS algotrader5.rules2strategies;
CREATE TABLE algotrader5.rules2strategies LIKE algotrader.rules2strategies;
INSERT INTO algotrader5.rules2strategies SELECT * FROM algotrader.rules2strategies;

UPDATE algotrader5.strategy, algotrader.strategy
SET algotrader5.strategy.allocation = algotrader.strategy.allocation,
algotrader5.strategy.auto_activate = algotrader.strategy.auto_activate
WHERE algotrader5.strategy.id = algotrader.strategy.id;
