    @Override
    public ${pojo.getDeclarationName()}VO convert(final ${pojo.getDeclarationName()} entity) {

<#if pojo.isAbstract()>
        throw new java.lang.UnsupportedOperationException("convert not allowed for abstract entities");
<#else>
        if (entity == null) {
            return null;
        }

        return new ${pojo.getDeclarationName()}VO(
<#list pojo.getPropertyClosureForFullConstructor(true) as field><#if !c2h.isCollection(field)><#if field_index != 0>,</#if>
                 entity.${pojo.getGetterSignature(field)}()<#if c2h.isToOne(field)>.getId()</#if></#if></#list>
        );
</#if>
    }