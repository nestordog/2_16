<#assign minimalParamList = c2j.asVOParameterList(pojo.getPropertyClosureForMinimalConstructor(true), jdk5, pojo)>
<#assign fullParamList = c2j.asVOParameterList(pojo.getPropertyClosureForFullConstructor(true), jdk5, pojo)>  
<#if pojo.needsMinimalConstructor(true)>    
    public ${pojo.getDeclarationName()}VO(${minimalParamList}) {
        super(${c2j.asVOArgumentList(pojo.getPropertyClosureForSuperclassMinimalConstructor(true))}); 
<#foreach field in pojo.getPropertiesForFullConstructor(true)>
<#if !c2h.isCollection(field)>
<#if pojo.isRequiredInConstructor(field)>
        this.${field.name}<#if c2h.isToOne(field)>Id</#if> = ${field.name}<#if c2h.isToOne(field)>Id</#if>;
<#else>
        this.${field.name}<#if c2h.isToOne(field)>Id</#if> = <#if c2h.isToOne(field) || c2j.isPrimitive(field, jdk5)>0<#else>null</#if>;
</#if>
</#if>
</#foreach>
    }
</#if>  
<#if pojo.needsFullConstructor(true) && (!pojo.needsMinimalConstructor(true) || !minimalParamList.equals(fullParamList))>

    public ${pojo.getDeclarationName()}VO(${fullParamList}) {
        super(${c2j.asVOArgumentList(pojo.getPropertyClosureForSuperclassFullConstructor(true))});
<#foreach field in pojo.getPropertiesForFullConstructor(true)> 
<#if !c2h.isCollection(field)>
        this.${field.name}<#if c2h.isToOne(field)>Id</#if> = ${field.name}<#if c2h.isToOne(field)>Id</#if>;
</#if>
</#foreach>
    }
</#if>
