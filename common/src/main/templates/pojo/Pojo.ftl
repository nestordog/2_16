${pojo.getPackageDeclaration()}
// Generated ${date} by Hibernate Tools ${version} for AlgoTrader

<#assign classbody>
<#include "PojoTypeDeclaration.ftl"/>

<#include "PojoFields.ftl"/>

<#include "PojoHeader.ftl"/>
       
<#include "PojoPropertyAccessors.ftl"/>

<#include "PojoEqualsHashcode.ftl"/>

<#include "PojoExtraClassCode.ftl"/>

<#include "PojoToVOConverter.ftl"/>

<#include "PojoFactory.ftl"/>
}
</#assign>

${pojo.generateImports()}
${classbody}

