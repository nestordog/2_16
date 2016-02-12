<#-- // Property accessors -->
<#if pojo.hasSubclasses() && !pojo.isSubclass() && pojo.isAbstract()>
    public abstract String getObjectType();

</#if>
<#if (pojo.isSubclass() || pojo.hasSubclasses()) && !pojo.isAbstract()>
<#if pojo.isSubclass()>
    @Override
</#if>
    public String getObjectType() {
        return "${pojo.getDeclarationName()}";
    }

</#if>
<#foreach property in pojo.getAllPropertiesIterator()>
<#if pojo.getMetaAttribAsBool(property, "gen-property", true)>
<#if !c2h.isCollection(property) && !property.equals(pojo.getVersionProperty())>
<#if pojo.hasFieldJavaDoc(property)>    
    /**       
${pojo.getFieldJavaDoc(property, 4)}
     */
</#if>
    <#include "GetPropertyAnnotation.ftl"/>
    ${pojo.getPropertyGetModifiers(property)} <#if !c2h.isToOne(property)>${pojo.getJavaTypeName(property, jdk5)}<#else>long</#if> ${pojo.getGetterSignature(property)}<#if c2h.isToOne(property)>Id</#if>() {
        return this.${property.name}<#if c2h.isToOne(property)>Id</#if>;
    }

</#if>
</#if>
</#foreach>
