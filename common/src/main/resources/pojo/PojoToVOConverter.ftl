	/**
	 * converts this entity to its corresponding value object
	 */
<#if pojo.isAbstract()>    
    public abstract ${pojo.getDeclarationName()}VO convertToVO();
<#else>
    public ${pojo.getDeclarationName()}VO convertToVO() {
        return Converter.INSTANCE.convert(this);
    }
    
    /**
     * Converts instances of  of {@link ${pojo.getDeclarationName()}} to corresponding value objects.
     */
    public static final class Converter implements ch.algotrader.entity.Converter<${pojo.getDeclarationName()}, ${pojo.getDeclarationName()}VO> {

    	public static final Converter INSTANCE = new Converter();

        @Override
        public ${pojo.getDeclarationName()}VO convert(final ${pojo.getDeclarationName()} entity) {

            if (entity == null) {
                return null;
            }

            return new ${pojo.getDeclarationName()}VO(
<#list pojo.getPropertyClosureForFullConstructor(true) as field><#if !c2h.isCollection(field)><#if field_index != 0>,</#if>
                     entity.${pojo.getGetterSignature(field)}()<#if c2h.isToOne(field)>.getId()</#if></#if></#list>
            );
        }
    }  
</#if>