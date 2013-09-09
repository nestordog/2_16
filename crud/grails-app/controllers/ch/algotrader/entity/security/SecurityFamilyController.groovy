package ch.algotrader.entity.security

import grails.util.GrailsNameUtils

class SecurityFamilyController {

    def scaffold = SecurityFamilyImpl

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [securityFamilyImplInstanceList: SecurityFamilyImpl.list(params), securityFamilyImplInstanceTotal: SecurityFamilyImpl.count()]
    }

    def show(Long id) {
        def securityFamilyImplInstance = SecurityFamilyImpl.get(id)
        if (!securityFamilyImplInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                message(code: 'securityFamilyImpl.label', default: 'SecurityFamilyImpl'),
                id
            ])
            redirect(action: "list")
            return
        }

        def shortName = GrailsNameUtils.getShortName(securityFamilyImplInstance.class)
        if (shortName != "SecurityFamilyImpl")
            redirect(controller: shortName[0..-5], action: "show", params: params)

        [securityFamilyImplInstance: securityFamilyImplInstance]
    }

    def edit(Long id) {
        def securityFamilyImplInstance = SecurityFamilyImpl.get(id)
        if (!securityFamilyImplInstance) {
            flash.message = message(code: 'default.not.found.message', args: [
                message(code: 'securityFamilyImpl.label', default: 'SecurityFamilyImpl'),
                id
            ])
            redirect(action: "list")
            return
        }

        def shortName = GrailsNameUtils.getShortName(securityFamilyImplInstance.class)
        if (shortName != "SecurityFamilyImpl")
            redirect(controller: shortName[0..-5], action: "edit", params: params)

        [securityFamilyImplInstance: securityFamilyImplInstance]
    }
}
