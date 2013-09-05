
dataSource {
    pooled = true
    driverClassName = "org.gjt.mm.mysql.Driver"
    username = "root"
    password = "password"
}
hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
}
// environment specific settings
environments {
    development {
        dataSource { //            dbCreate = "validate" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:mysql://localhost:3306/algoTradertest" }
    }
}
