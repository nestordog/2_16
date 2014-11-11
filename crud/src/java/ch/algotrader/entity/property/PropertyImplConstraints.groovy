constraints = {
    name(blank: false)
    persistent()
    intValue(nullable: true)
    doubleValue(nullable: true)
    moneyValue(nullable: true)
    textValue(nullable: true)
    dateTimeValue(nullable: true, attributes: [precision : 'minute'])
    booleanValue(nullable: true)
}
