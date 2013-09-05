constraints= {
    symbol(nullable : true)
    isin(nullable : true)
    bbgid(nullable : true)
    ric(nullable : true)
    conid(nullable : true)

    maturity(format : 'yyyy-MM-dd')
    coupon()

    underlying(nullable : true)
    securityFamily()

    derivedSecurityFamilies(display : false)
    subscriptions(display : false)
    positions(display : false)
}
