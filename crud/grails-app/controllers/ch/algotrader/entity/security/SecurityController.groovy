package ch.algotrader.entity.security

import grails.util.GrailsNameUtils

class SecurityController {

    def scaffold = SecurityImpl

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [securityImplInstanceList: SecurityImpl.list(params), securityImplInstanceTotal: SecurityImpl.count()]
    }

    def create() {
        flash.message = 'create not allowed'
        redirect(action: "list")
    }

    def save() {
        flash.message = 'save not allowed'
        redirect(action: "list")
    }

    def show(Long id) {
        def securityImplInstance = SecurityImpl.get(id)
        if (!securityImplInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                message(code: 'securityImpl.label', default: 'SecurityImpl'),
                id
            ])
            redirect(action: "list")
            return
        }

        def shortName = GrailsNameUtils.getShortName(securityImplInstance.class)

        redirect(controller: shortName[0..-5], action: "show", params: params)
    }

    def edit(Long id) {
        def securityImplInstance = SecurityImpl.get(id)
        if (!securityImplInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                message(code: 'securityImpl.label', default: 'SecurityImpl'),
                id
            ])
            redirect(action: "list")
            return
        }

        def shortName = GrailsNameUtils.getShortName(securityImplInstance.class)
        redirect(controller: shortName[0..-5], action: "edit", params: params)
    }
}
