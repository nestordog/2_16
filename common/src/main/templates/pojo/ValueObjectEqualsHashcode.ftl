<#if pojo.needsEqualsHashCode()>
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) { 
		    return false;
		} else if (!(obj instanceof ${pojo.getDeclarationName()}VO)) {
		    return false;
		} else {
		    ${pojo.getDeclarationName()}VO that = (${pojo.getDeclarationName()}VO) obj;
<#assign equalsHashCodePropertiesIterator = pojo.getEqualsHashCodePropertiesIterator()>
<#list equalsHashCodePropertiesIterator as property>
<#if c2h.isToOne(property)>
		    <#if property_index == 0>return <#else>       </#if>this.${pojo.getGetterSignature(property)}Id() == that.${pojo.getGetterSignature(property)}Id()<#if property_has_next> &&<#else>;</#if>
<#else>
<#if c2j.isPrimitive(property, jdk5)>
		    <#if property_index == 0>return <#else>       </#if>this.${pojo.getGetterSignature(property)}() == that.${pojo.getGetterSignature(property)}()<#if property_has_next> &&<#else>;</#if>
<#else>
		    <#if property_index == 0>return <#else>       </#if>Objects.equals(this.${pojo.getGetterSignature(property)}(), that.${pojo.getGetterSignature(property)}())<#if property_has_next> &&<#else>;</#if>
</#if>
</#if>
</#list>
        }
    }
   
    public int hashCode() {
        int hash  = 17;
<#assign equalsHashCodePropertiesIterator = pojo.getEqualsHashCodePropertiesIterator()>
<#list equalsHashCodePropertiesIterator as property>
<#if c2h.isToOne(property)>
		hash = hash * 37 + Long.hashCode(${pojo.getGetterSignature(property)}Id());
<#else>
<#if c2j.isPrimitive(property, jdk5)>
        hash = hash * 37 + ${c2j.getBoxedPrimitive(property, jdk5)}.hashCode(${pojo.getGetterSignature(property)}());
<#else>
        hash = hash * 37 + Objects.hashCode(${pojo.getGetterSignature(property)}());
</#if>
</#if>
</#list>
        return hash;
   }   
</#if>