constraints = {

    name(blank : false)
    symbolRoot(nullable : true)
    isinRoot(nullable : true)
    ricRoot(nullable : true)
    tradingClass(nullable : true)
    currency()
    contractSize()
    scale()
    tickSizePattern()
    executionCommission(nullable : true)
    clearingCommission(nullable : true)
    fee(nullable : true)
    tradeable()
    synthetic()
    periodicity(nullable : true)
    maxGap(nullable : true)

    maturityDistance()
    length()
    quotationStyle()

    underlying(nullable : true)
    brokerParameters()
    exchange()

    securities(display : false)
}
