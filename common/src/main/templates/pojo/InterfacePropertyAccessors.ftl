<#-- Property accessors for interface -->
<#foreach field in pojo.getAllPropertiesIterator()>
<#if pojo.getMetaAttribAsBool(field, "gen-property", true)>
<#if !c2h.isCollection(field) && !c2h.isToOne(field) && !field.equals(pojo.getVersionProperty())>
<#if pojo.hasMetaAttribute(field, "field-description")>    /**
${pojo.getFieldJavaDoc(field, 4)}
     */
</#if>    ${pojo.getPropertyGetModifiers(field)} ${pojo.getJavaTypeName(field, true)} ${pojo.getGetterSignature(field)}();

</#if>
</#if>
</#foreach>