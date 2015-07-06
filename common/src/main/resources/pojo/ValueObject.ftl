${pojo.getPackageDeclaration()}
// Generated ${date} by Hibernate Tools ${version} for AlgoTrader

<#assign classbody>
<#include "ValueObjectTypeDeclaration.ftl"/>

<#include "ValueObjectFields.ftl"/>

<#include "ValueObjectConstructors.ftl"/>
       
<#include "ValueObjectPropertyAccessors.ftl"/>

<#include "ValueObjectToString.ftl"/>

<#include "ValueObjectEqualsHashcode.ftl"/>
}
</#assign>

${pojo.generateVOImports()}
${classbody}

