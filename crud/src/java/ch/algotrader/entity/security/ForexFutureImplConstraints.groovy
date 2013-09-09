constraints= {
    symbol(nullable : true)
    isin(nullable : true)
    bbgid(nullable : true)
    ric(nullable : true)
    conid(nullable : true)

    expiration(format : 'yyyy-MM-dd')
    baseCurrency()

    underlying(nullable : true)
    securityFamily()

    subscriptions(display : false)
    positions(display : false)
}
