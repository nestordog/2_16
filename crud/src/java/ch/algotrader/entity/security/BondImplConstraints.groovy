constraints= {
    symbol(nullable : true)
    isin(nullable : true)
    bbgid(nullable : true)
    ric(nullable : true)
    conid(nullable : true)
    lmaxid(nullable : true)

    maturity(format : 'yyyy-MM-dd')
    coupon()

    underlying(nullable : true)
    securityFamily()

    subscriptions(display : false)
    positions(display : false)
}
