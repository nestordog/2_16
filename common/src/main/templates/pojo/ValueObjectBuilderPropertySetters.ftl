<#-- // Property accessors -->
<#foreach property in pojo.getPropertyClosureForFullConstructor(true)>
<#if pojo.getMetaAttribAsBool(property, "gen-property", true)>
<#if !c2h.isCollection(property) && !property.equals(pojo.getVersionProperty())>
<#if pojo.hasFieldJavaDoc(property)>    
    /**       
${pojo.getFieldJavaDoc(property, 4)}
     */
</#if>
    ${pojo.getPropertyGetModifiers(property)} ${pojo.getDeclarationName()}VOBuilder set${pojo.getPropertyName(property)}<#if c2h.isToOne(property)>Id</#if>(final <#if !c2h.isToOne(property)>${pojo.getJavaTypeName(property, jdk5)}<#else>long</#if> ${property.name}<#if c2h.isToOne(property)>Id</#if>) {
        this.${property.name}<#if c2h.isToOne(property)>Id</#if> = ${property.name}<#if c2h.isToOne(property)>Id</#if>;
        return this;
    }

</#if>
</#if>
</#foreach>
