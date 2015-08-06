<#if !pojo.isAbstract()>    
    /**
     * Constructs new instances of {@link ${pojo.getDeclarationName()}}.
     */
    public static final class Factory {
    
        public static ${pojo.getDeclarationName()} newInstance() {
            return new ${pojo.getDeclarationName()}Impl();
        }

<#if pojo.needsMinimalConstructor(false)>    
        public static ${pojo.getDeclarationName()} newInstance(${c2j.asParameterList(pojo.getPropertyClosureForMinimalConstructor(false), jdk5, pojo)}) {
            final  ${pojo.getDeclarationName()} entity = new  ${pojo.getDeclarationName()}Impl();
<#foreach field in pojo.getPropertyClosureForMinimalConstructor(false)>
            entity.set${field.name?cap_first}(${field.name});
</#foreach>
            return entity;
        }
</#if>    
<#if pojo.needsFullConstructor(false)>

        public static ${pojo.getDeclarationName()} newInstance(${c2j.asParameterList(pojo.getPropertyClosureForFullConstructor(false), jdk5, pojo)}) {
            final  ${pojo.getDeclarationName()} entity = new  ${pojo.getDeclarationName()}Impl();
<#foreach field in pojo.getPropertyClosureForFullConstructor(false)> 
            entity.set${field.name?cap_first}(${field.name});
</#foreach>
            return entity;
        }
</#if>
    }    
</#if>