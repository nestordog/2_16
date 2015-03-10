${pojo.getPackageDeclaration()}
// Generated ${date} by Hibernate Tools ${version} for AlgoTrader

<#assign classbody>
${pojo.getClassModifiers()} interface ${pojo.getDeclarationName()}I {

<#include "PojoInterfacePropertyAccessors.ftl"/>

}
</#assign>

${pojo.generateImports()}
${classbody}

