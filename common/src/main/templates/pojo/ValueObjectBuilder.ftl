<#if !pojo.isAbstract()>
${pojo.getPackageDeclaration()}
// Generated ${date} by Hibernate Tools ${version} for AlgoTrader

<#assign classbody>
<#include "ValueObjectBuilderTypeDeclaration.ftl"/>

<#include "ValueObjectBuilderFields.ftl"/>
       
<#include "ValueObjectBuilderPropertySetters.ftl"/>

<#include "ValueObjectBuilderBuildMethod.ftl"/>

}
</#assign>

${pojo.generateVOImports()}
${classbody}

</#if>