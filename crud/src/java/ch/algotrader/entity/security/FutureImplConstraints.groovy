constraints= {
    symbol(nullable : true)
    isin(nullable : true)
    bbgid(nullable : true)
    ric(nullable : true)
    conid(nullable : true)
    lmaxid(nullable : true)

    expiration(format : 'yyyy-MM-dd')
    firstNotice(nullable : true, format : 'yyyy-MM-dd')
    lastTrading(nullable : true, format : 'yyyy-MM-dd')

    underlying(nullable : true)
    securityFamily()

    subscriptions(display : false)
    positions(display : false)
}
