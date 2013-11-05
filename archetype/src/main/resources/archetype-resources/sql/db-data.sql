

LOCK TABLES `strategy` WRITE;
/*!40000 ALTER TABLE `strategy` DISABLE KEYS */;
INSERT INTO `strategy` (`id`, `NAME`, `AUTO_ACTIVATE`, `ALLOCATION`, `INIT_MODULES`, `RUN_MODULES`, `VERSION`) VALUES (27,'${artifactId.toUpperCase()}','',1,'${artifactId}-init','${artifactId}-run',0);
/*!40000 ALTER TABLE `strategy` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `subscription`
--

LOCK TABLES `subscription` WRITE;
/*!40000 ALTER TABLE `subscription` DISABLE KEYS */;
INSERT INTO `subscription` (`id`, `PERSISTENT`, `SECURITY_FK`, `STRATEGY_FK`, `VERSION`) VALUES (138,'',10,27,0);
/*!40000 ALTER TABLE `subscription` ENABLE KEYS */;
UNLOCK TABLES;
