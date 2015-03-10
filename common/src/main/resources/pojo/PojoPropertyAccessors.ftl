<#-- // Property accessors -->
<#foreach property in pojo.getAllPropertiesIterator()>
<#if pojo.getMetaAttribAsBool(property, "gen-property", true)>
 <#if pojo.hasFieldJavaDoc(property)>    
    /**       
     * ${pojo.getFieldJavaDoc(property, 4)}
     */
</#if>
    <#include "GetPropertyAnnotation.ftl"/>
    ${pojo.getPropertyGetModifiers(property)} ${pojo.getJavaTypeName(property, jdk5)} ${pojo.getGetterSignature(property)}() {
        return this.${property.name};
    }
    
    ${pojo.getPropertySetModifiers(property)} void set${pojo.getPropertyName(property)}(${pojo.getJavaTypeName(property, jdk5)} ${property.name}) {
        this.${property.name} = ${property.name};
    }
    
<#if c2h.isCollection(property)>  	
<#if !property.getValue().isIndexed()>        
    ${pojo.getPropertySetModifiers(property)} boolean add${pojo.getPropertyName(property)}(${c2j.getGenericCollectionElementDeclaration(property.getValue(), true, pojo)}) {
        element.set${pojo.getDeclarationName()}(this);
		return this.${property.name}.add(element);
    }		
		
    ${pojo.getPropertySetModifiers(property)} boolean remove${pojo.getPropertyName(property)}(${c2j.getGenericCollectionElementDeclaration(property.getValue(), true, pojo)}) {
        element.set${pojo.getDeclarationName()}(this);
		return this.${property.name}.remove(element);
    } 
    				
<#else>
    ${pojo.getPropertySetModifiers(property)} ${c2j.getGenericCollectionElementType(property.getValue(), true, pojo)} add${pojo.getPropertyName(property)}(${c2j.getGenericCollectionElementDeclaration(property.getValue(), true, pojo)}) {
        element.set${pojo.getDeclarationName()}(this);
		return this.${property.name}.put(key, element);
    } 
    
    ${pojo.getPropertySetModifiers(property)} ${c2j.getGenericCollectionElementType(property.getValue(), true, pojo)} remove${pojo.getPropertyName(property)}(${c2j.getIndexType(property.getValue(), true, pojo)} key) {
		return this.${property.name}.remove(key);
    } 
    
</#if>		       
</#if>
</#if>
</#foreach>
