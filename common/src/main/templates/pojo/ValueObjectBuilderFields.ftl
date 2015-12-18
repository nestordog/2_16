<#foreach field in pojo.getPropertyClosureForFullConstructor(true)>
<#if pojo.getMetaAttribAsBool(field, "gen-property", true)><#if !c2h.isCollection(field) && !field.equals(pojo.getVersionProperty())><#if pojo.hasMetaAttribute(field, "field-description")>    /**
${pojo.getFieldJavaDoc(field, 4)}
     */
</#if>    ${pojo.getFieldModifiers(field)} <#if !c2h.isToOne(field)>${pojo.getJavaTypeName(field, jdk5)}<#else>long</#if> ${field.name}<#if c2h.isToOne(field)>Id</#if><#if pojo.hasFieldInitializor(field, jdk5)> = ${pojo.getFieldInitialization(field, jdk5)}</#if>;

</#if>
</#if>
</#foreach>
