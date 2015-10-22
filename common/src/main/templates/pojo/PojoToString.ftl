<#if pojo.needsToString()>
    public String toString() {
        StringBuffer buffer = new StringBuffer();
<#assign toStringProperties = pojo.getToStringPropertiesIterator()>
<#list toStringProperties as property>
<#if property_index != 0 || pojo.getMetaAttribAsBool(property, "use-in-tostring-long", false)>
        buffer.append("<#if property_index != 0>,</#if><#if pojo.getMetaAttribAsBool(property, "use-in-tostring-long", false)>${property.getName()}=</#if>");
</#if>
        buffer.append(${pojo.getGetterSignature(property)}());
</#list>      
        return buffer.toString();
    }
</#if>