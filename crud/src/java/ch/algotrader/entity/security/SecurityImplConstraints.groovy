constraints= {
    symbol(nullable : true, validator: { val, obj ->
        if(val && val.blank) {
            obj.sym == val
        }
    })
    isin(nullable : true)
    bbgid(nullable : true)
    ric(nullable : true)
    conid(nullable : true)
    lmaxid(nullable : true)

    underlying(nullable : true)
    securityFamily()

    subscriptions(display : false)
    positions(display : false)
}
