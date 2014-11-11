constraints = {
    date(blank: false, format : 'yyyy-MM-dd')
    lateOpen(format : 'HH:mm', attributes: [precision : 'minute'])
    earlyClose(format : 'HH:mm', attributes: [precision : 'minute'])

    exchange()
}
