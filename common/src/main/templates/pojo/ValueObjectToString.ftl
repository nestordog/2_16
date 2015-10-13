<#macro appendName prop idx indent>
${indent}buffer.append("<#if idx != 0>,</#if>${prop.getName()}=");
</#macro>
<#macro appendValue prop indent>
<#assign sqlType = c2h.getFirstColumnSqlType(prop)>
<#if sqlType == "TIMESTAMP">
${indent}DateTimeUtil.formatLocalZone(${pojo.getGetterSignature(prop)}().toInstant(), buffer);
<#elseif sqlType == "TIME"> 
${indent}DateTimeUtil.formatLocalTime(DateTimeLegacy.toLocalTime(${pojo.getGetterSignature(prop)}()), buffer);
<#elseif sqlType == "DATE"> 
${indent}DateTimeUtil.formatLocalDate(DateTimeLegacy.toLocalDate(${pojo.getGetterSignature(prop)}()), buffer);
<#else>
${indent}buffer.append(${pojo.getGetterSignature(prop)}());
</#if>
</#macro>
<#if pojo.needsToString()>
    public String toString() {
        StringBuffer buffer = new StringBuffer();
<#assign toStringProperties = pojo.getToStringPropertiesIterator()>
<#list toStringProperties as property>
<#if !c2h.isCollection(property) && !property.equals(pojo.getVersionProperty())>
<#if c2h.isToOne(property)>
<#if property.isOptional()>
        if (${pojo.getGetterSignature(property)}Id() != 0) {
            buffer.append("<#if property_index != 0>,</#if>${property.getName()}Id=");
            buffer.append(${pojo.getGetterSignature(property)}Id());
        }
<#else>
        buffer.append("<#if property_index != 0>,</#if>${property.getName()}Id=");
        buffer.append(${pojo.getGetterSignature(property)}Id());
</#if>
<#elseif !c2j.isPrimitive(property, jdk5) && property.isOptional()>
        if (${pojo.getGetterSignature(property)}() != null) {
            <@appendName prop=property idx=property_index indent="            " />
            <@appendValue prop=property indent="            " />
        }
        
<#else>
        <@appendName prop=property idx=property_index indent="        " />
        <@appendValue prop=property indent="        " />
</#if>
</#if>
</#list>      
        return buffer.toString();
    }
</#if>