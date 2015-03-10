<#if !pojo.isAbstract()>    
    /**
     * Constructs new instances of {@link ${pojo.getDeclarationName()}.
     */
    public static final class Factory {
    
        public static ${pojo.getDeclarationName()} newInstance() {
            return new ${pojo.getDeclarationName()}Impl();
        }

<#if pojo.needsMinimalConstructor()>    
        public static ${pojo.getDeclarationName()} newInstance(${c2j.asParameterList(pojo.getPropertyClosureForMinimalConstructor(), jdk5, pojo)}) {
            final  ${pojo.getDeclarationName()} entity = new  ${pojo.getDeclarationName()}Impl();
<#foreach field in pojo.getPropertyClosureForMinimalConstructor()>
            entity.set${field.name?cap_first}(${field.name});
</#foreach>
            return entity;
        }
</#if>    
<#if pojo.needsFullConstructor()>

        public static ${pojo.getDeclarationName()} newInstance(${c2j.asParameterList(pojo.getPropertyClosureForFullConstructor(), jdk5, pojo)}) {
            final  ${pojo.getDeclarationName()} entity = new  ${pojo.getDeclarationName()}Impl();
<#foreach field in pojo.getPropertiesForFullConstructor()> 
               entity.set${field.name?cap_first}(${field.name});
</#foreach>
            return entity;
        }
</#if>
    }    
</#if>