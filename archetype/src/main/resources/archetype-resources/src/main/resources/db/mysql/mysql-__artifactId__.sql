INSERT INTO `strategy` (`NAME`, `AUTO_ACTIVATE`, `ALLOCATION`, `INIT_MODULES`, `VERSION`) VALUES ('${serviceName.toUpperCase()}',1,1,'${artifactId}',0);

INSERT INTO `subscription` (`PERSISTENT`, `SECURITY_FK`, `STRATEGY_FK`, `FEED_TYPE`, `VERSION`)
SELECT 1, se.ID, st.ID, 'IB', 0
FROM `security` as se, `strategy` as st
WHERE
st.`NAME` = '${serviceName.toUpperCase()}' AND (
se.`SYMBOL` IN ($instruments) OR 
se.`BBGID` IN ($instruments) OR
se.`RIC` IN ($instruments) OR
se.`CONID` IN ($instruments) OR
se.`LMAXID` IN ($instruments)
);